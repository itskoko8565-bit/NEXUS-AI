package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CalendarEntry
import com.example.data.model.CalendarStatus
import com.example.data.model.ClipAnalysis
import com.example.data.model.CreatorProject
import com.example.data.model.GeneratedScript
import com.example.data.model.ProjectFormat
import com.example.data.model.ProjectStage
import com.example.data.model.ResearchBrief
import com.example.data.model.ScriptFormat
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NexusFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        check(!uid.isNullOrBlank()) { "User must be signed in with Google before accessing Firestore." }
        return uid
    }

    private fun <T> observeUserSubcollection(
        userId: String,
        collectionName: String,
        mapper: (List<T>) -> List<T>,
        clazz: Class<T>
    ): Flow<List<T>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch." }
        val path = "users/$userId/$collectionName"
        emitAll(
            db.collection("users").document(userId).collection(collectionName)
                .snapshots()
                .map { snapshot ->
                    val items = snapshot.toObjects(
                        clazz,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    mapper(items)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeProjects(userId: String): Flow<List<CreatorProject>> =
        observeUserSubcollection(
            userId = userId,
            collectionName = "projects",
            clazz = CreatorProject::class.java,
            mapper = { list -> list.sortedByDescending { it.updatedAt ?: it.createdAt } }
        )

    fun observeResearchBriefs(userId: String): Flow<List<ResearchBrief>> =
        observeUserSubcollection(
            userId = userId,
            collectionName = "researchBriefs",
            clazz = ResearchBrief::class.java,
            mapper = { list -> list.sortedByDescending { it.updatedAt ?: it.createdAt } }
        )

    fun observeScripts(userId: String): Flow<List<GeneratedScript>> =
        observeUserSubcollection(
            userId = userId,
            collectionName = "scripts",
            clazz = GeneratedScript::class.java,
            mapper = { list -> list.sortedByDescending { it.updatedAt ?: it.createdAt } }
        )

    fun observeCalendarItems(userId: String): Flow<List<CalendarEntry>> =
        observeUserSubcollection(
            userId = userId,
            collectionName = "calendarItems",
            clazz = CalendarEntry::class.java,
            mapper = { list -> list.sortedWith(compareBy({ it.scheduledDate }, { it.scheduledTime })) }
        )

    fun observeClipAnalyses(userId: String): Flow<List<ClipAnalysis>> =
        observeUserSubcollection(
            userId = userId,
            collectionName = "clipAnalyses",
            clazz = ClipAnalysis::class.java,
            mapper = { list -> list.sortedByDescending { it.updatedAt ?: it.createdAt } }
        )

    suspend fun createProject(
        userId: String,
        title: String,
        niche: String,
        format: ProjectFormat,
        stage: ProjectStage,
        targetPlatform: String,
        hook: String,
        notes: String,
        clipScore: Int = 0,
        scheduledDate: String? = null
    ): Result<String> {
        var docPath = "users/$userId/projects"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            val cleanTitle = title.trim().take(120)
            require(cleanTitle.isNotEmpty()) { "Project title is required." }
            val validDate = scheduledDate?.trim()?.takeIf { it.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$")) }

            val docRef = db.collection("users").document(uid).collection("projects").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "niche" to niche.trim().take(80),
                "format" to format.name,
                "stage" to stage.name,
                "targetPlatform" to targetPlatform.trim().ifEmpty { "YouTube" }.take(40),
                "hook" to hook.trim().take(400),
                "notes" to notes.trim().take(4000),
                "clipScore" to clipScore.coerceIn(0, 100),
                "scheduledDate" to validDate,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateProjectStage(userId: String, projectId: String, newStage: ProjectStage): Result<Unit> {
        val docPath = "users/$userId/projects/$projectId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed." }
            require(projectId.isNotBlank()) { "Project ID must not be blank." }
            db.collection("users").document(uid).collection("projects").document(projectId)
                .update(
                    mapOf(
                        "stage" to newStage.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteProject(userId: String, projectId: String): Result<Unit> =
        deleteDocument(userId, "projects", projectId)

    suspend fun saveResearchBrief(
        userId: String,
        topic: String,
        audience: String,
        summary: String,
        viralScore: Int,
        angles: List<String>,
        keywords: List<String>
    ): Result<String> {
        var docPath = "users/$userId/researchBriefs"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed." }
            val cleanTopic = topic.trim().take(160)
            val cleanSummary = summary.trim().take(4000)
            require(cleanTopic.isNotEmpty() && cleanSummary.isNotEmpty()) {
                "Topic and summary are required."
            }
            val docRef = db.collection("users").document(uid).collection("researchBriefs").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "topic" to cleanTopic,
                "audience" to audience.trim().take(120),
                "summary" to cleanSummary,
                "viralScore" to viralScore.coerceIn(0, 100),
                "angles" to angles.map { it.trim() }.filter { it.isNotEmpty() }.take(10),
                "keywords" to keywords.map { it.trim() }.filter { it.isNotEmpty() }.take(15),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteResearchBrief(userId: String, briefId: String): Result<Unit> =
        deleteDocument(userId, "researchBriefs", briefId)

    suspend fun saveScript(
        userId: String,
        title: String,
        format: ScriptFormat,
        tone: String,
        targetDurationSec: Int,
        hook: String,
        body: String,
        cta: String,
        visualCues: List<String>,
        projectId: String? = null
    ): Result<String> {
        var docPath = "users/$userId/scripts"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed." }
            val cleanTitle = title.trim().take(160)
            val cleanBody = body.trim().take(12000)
            require(cleanTitle.isNotEmpty() && cleanBody.isNotEmpty()) {
                "Script title and body are required."
            }
            val validProjectId = projectId?.trim()?.takeIf { it.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$")) }
            val docRef = db.collection("users").document(uid).collection("scripts").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "format" to format.name,
                "tone" to tone.trim().ifEmpty { "Crisp & Analytical" }.take(60),
                "targetDurationSec" to targetDurationSec.coerceIn(10, 7200),
                "hook" to hook.trim().take(600),
                "body" to cleanBody,
                "cta" to cta.trim().take(600),
                "visualCues" to visualCues.map { it.trim() }.filter { it.isNotEmpty() }.take(25),
                "projectId" to validProjectId,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteScript(userId: String, scriptId: String): Result<Unit> =
        deleteDocument(userId, "scripts", scriptId)

    suspend fun createCalendarEntry(
        userId: String,
        title: String,
        platform: String,
        scheduledDate: String,
        scheduledTime: String,
        status: CalendarStatus,
        notes: String,
        projectId: String? = null
    ): Result<String> {
        var docPath = "users/$userId/calendarItems"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed." }
            val cleanTitle = title.trim().take(140)
            val cleanDate = scheduledDate.trim()
            require(cleanTitle.isNotEmpty()) { "Title is required." }
            require(cleanDate.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) {
                "Date must be YYYY-MM-DD."
            }
            val cleanTime = scheduledTime.trim().ifEmpty { "18:00" }.take(10)
            val validProjectId = projectId?.trim()?.takeIf { it.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$")) }

            val docRef = db.collection("users").document(uid).collection("calendarItems").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "platform" to platform.trim().ifEmpty { "YouTube" }.take(40),
                "scheduledDate" to cleanDate,
                "scheduledTime" to cleanTime,
                "status" to status.name,
                "projectId" to validProjectId,
                "notes" to notes.trim().take(1000),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateCalendarStatus(
        userId: String,
        entryId: String,
        newStatus: CalendarStatus
    ): Result<Unit> {
        val docPath = "users/$userId/calendarItems/$entryId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed." }
            require(entryId.isNotBlank()) { "Calendar entry ID must not be blank." }
            db.collection("users").document(uid).collection("calendarItems").document(entryId)
                .update(
                    mapOf(
                        "status" to newStatus.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCalendarEntry(userId: String, entryId: String): Result<Unit> =
        deleteDocument(userId, "calendarItems", entryId)

    suspend fun saveClipAnalysis(
        userId: String,
        clipTitle: String,
        sourceSummary: String,
        retentionScore: Int,
        hookStrength: Int,
        pacingVerdict: String,
        highlights: List<String>,
        improvements: List<String>
    ): Result<String> {
        var docPath = "users/$userId/clipAnalyses"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed." }
            val cleanTitle = clipTitle.trim().take(160)
            val cleanSummary = sourceSummary.trim().take(4000)
            require(cleanTitle.isNotEmpty() && cleanSummary.isNotEmpty()) {
                "Clip title and summary are required."
            }
            val docRef = db.collection("users").document(uid).collection("clipAnalyses").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "clipTitle" to cleanTitle,
                "sourceSummary" to cleanSummary,
                "retentionScore" to retentionScore.coerceIn(0, 100),
                "hookStrength" to hookStrength.coerceIn(0, 100),
                "pacingVerdict" to pacingVerdict.trim().ifEmpty { "Balanced pacing" }.take(200),
                "highlights" to highlights.map { it.trim() }.filter { it.isNotEmpty() }.take(12),
                "improvements" to improvements.map { it.trim() }.filter { it.isNotEmpty() }.take(10),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteClipAnalysis(userId: String, analysisId: String): Result<Unit> =
        deleteDocument(userId, "clipAnalyses", analysisId)

    private suspend fun deleteDocument(
        userId: String,
        collectionName: String,
        docId: String
    ): Result<Unit> {
        val docPath = "users/$userId/$collectionName/$docId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed." }
            require(docId.isNotBlank()) { "Document ID must not be blank." }
            db.collection("users").document(uid).collection(collectionName).document(docId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
