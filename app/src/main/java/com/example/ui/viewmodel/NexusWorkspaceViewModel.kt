package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CalendarEntry
import com.example.data.model.CalendarStatus
import com.example.data.model.ClipAnalysis
import com.example.data.model.CreatorProject
import com.example.data.model.GeneratedScript
import com.example.data.model.ProjectFormat
import com.example.data.model.ProjectStage
import com.example.data.model.ResearchBrief
import com.example.data.model.ScriptFormat
import com.example.data.model.UiState
import com.example.data.repository.NexusFirestoreRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NexusWorkspaceViewModel(
    private val repository: NexusFirestoreRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    val projectsState: StateFlow<UiState<List<CreatorProject>>> =
        observeAsUiState(repository.observeProjects(currentUserId), "projects")

    val researchBriefsState: StateFlow<UiState<List<ResearchBrief>>> =
        observeAsUiState(repository.observeResearchBriefs(currentUserId), "research briefs")

    val scriptsState: StateFlow<UiState<List<GeneratedScript>>> =
        observeAsUiState(repository.observeScripts(currentUserId), "scripts")

    val calendarState: StateFlow<UiState<List<CalendarEntry>>> =
        observeAsUiState(repository.observeCalendarItems(currentUserId), "calendar items")

    val clipAnalysesState: StateFlow<UiState<List<ClipAnalysis>>> =
        observeAsUiState(repository.observeClipAnalyses(currentUserId), "clip analyses")

    private fun <T> observeAsUiState(
        source: Flow<List<T>>,
        label: String
    ): StateFlow<UiState<List<T>>> = source
        .map<List<T>, UiState<List<T>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing $label", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load $label"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    fun createProject(
        title: String,
        niche: String,
        format: ProjectFormat,
        stage: ProjectStage,
        targetPlatform: String,
        hook: String,
        notes: String,
        clipScore: Int = 0,
        scheduledDate: String? = null
    ) {
        viewModelScope.launch {
            val result = repository.createProject(
                userId = currentUserId,
                title = title,
                niche = niche,
                format = format,
                stage = stage,
                targetPlatform = targetPlatform,
                hook = hook,
                notes = notes,
                clipScore = clipScore,
                scheduledDate = scheduledDate
            )
            result.fold(
                onSuccess = { _events.tryEmit("Project saved to Firestore.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Could not save project.") }
            )
        }
    }

    fun advanceProjectStage(project: CreatorProject) {
        val next = project.parsedStage.nextStage()
        if (next == project.parsedStage) return
        viewModelScope.launch {
            repository.updateProjectStage(currentUserId, project.id, next).fold(
                onSuccess = { _events.tryEmit("Moved \"${project.title}\" to ${next.label}.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to update stage.") }
            )
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(currentUserId, projectId).fold(
                onSuccess = { _events.tryEmit("Project deleted.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to delete project.") }
            )
        }
    }

    fun saveResearchBrief(
        topic: String,
        audience: String,
        summary: String,
        viralScore: Int,
        angles: List<String>,
        keywords: List<String>
    ) {
        viewModelScope.launch {
            repository.saveResearchBrief(
                userId = currentUserId,
                topic = topic,
                audience = audience,
                summary = summary,
                viralScore = viralScore,
                angles = angles,
                keywords = keywords
            ).fold(
                onSuccess = { _events.tryEmit("Research brief saved to workspace.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to save brief.") }
            )
        }
    }

    fun deleteResearchBrief(briefId: String) {
        viewModelScope.launch {
            repository.deleteResearchBrief(currentUserId, briefId).fold(
                onSuccess = { _events.tryEmit("Research brief deleted.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to delete brief.") }
            )
        }
    }

    fun saveScript(
        title: String,
        format: ScriptFormat,
        tone: String,
        targetDurationSec: Int,
        hook: String,
        body: String,
        cta: String,
        visualCues: List<String>,
        projectId: String? = null
    ) {
        viewModelScope.launch {
            repository.saveScript(
                userId = currentUserId,
                title = title,
                format = format,
                tone = tone,
                targetDurationSec = targetDurationSec,
                hook = hook,
                body = body,
                cta = cta,
                visualCues = visualCues,
                projectId = projectId
            ).fold(
                onSuccess = { _events.tryEmit("Script saved to library.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to save script.") }
            )
        }
    }

    fun deleteScript(scriptId: String) {
        viewModelScope.launch {
            repository.deleteScript(currentUserId, scriptId).fold(
                onSuccess = { _events.tryEmit("Script removed.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to delete script.") }
            )
        }
    }

    fun createCalendarEntry(
        title: String,
        platform: String,
        scheduledDate: String,
        scheduledTime: String,
        status: CalendarStatus,
        notes: String,
        projectId: String? = null
    ) {
        viewModelScope.launch {
            repository.createCalendarEntry(
                userId = currentUserId,
                title = title,
                platform = platform,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                status = status,
                notes = notes,
                projectId = projectId
            ).fold(
                onSuccess = { _events.tryEmit("Scheduled on Content Calendar.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to schedule item.") }
            )
        }
    }

    fun cycleCalendarStatus(entry: CalendarEntry) {
        val next = entry.parsedStatus.nextStatus()
        viewModelScope.launch {
            repository.updateCalendarStatus(currentUserId, entry.id, next).fold(
                onSuccess = { _events.tryEmit("Updated status to ${next.label}.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to update status.") }
            )
        }
    }

    fun deleteCalendarEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteCalendarEntry(currentUserId, entryId).fold(
                onSuccess = { _events.tryEmit("Calendar item removed.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to remove item.") }
            )
        }
    }

    fun saveClipAnalysis(
        clipTitle: String,
        sourceSummary: String,
        retentionScore: Int,
        hookStrength: Int,
        pacingVerdict: String,
        highlights: List<String>,
        improvements: List<String>
    ) {
        viewModelScope.launch {
            repository.saveClipAnalysis(
                userId = currentUserId,
                clipTitle = clipTitle,
                sourceSummary = sourceSummary,
                retentionScore = retentionScore,
                hookStrength = hookStrength,
                pacingVerdict = pacingVerdict,
                highlights = highlights,
                improvements = improvements
            ).fold(
                onSuccess = { _events.tryEmit("Clip Intelligence report saved.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to save report.") }
            )
        }
    }

    fun deleteClipAnalysis(analysisId: String) {
        viewModelScope.launch {
            repository.deleteClipAnalysis(currentUserId, analysisId).fold(
                onSuccess = { _events.tryEmit("Clip report deleted.") },
                onFailure = { _events.tryEmit(it.localizedMessage ?: "Failed to delete report.") }
            )
        }
    }

    private companion object {
        const val TAG = "NexusWorkspaceVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
