package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Streaming(val text: String) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class ProjectFormat(val label: String) {
    LONG_FORM("Long-Form"),
    SHORT("Short"),
    REEL("Reel"),
    PODCAST("Podcast");

    companion object {
        fun fromRaw(raw: String): ProjectFormat =
            entries.firstOrNull { it.name == raw } ?: LONG_FORM
    }
}

enum class ProjectStage(val label: String, val stepIndex: Int) {
    IDEA("Idea", 0),
    RESEARCH("Research", 1),
    SCRIPTED("Scripted", 2),
    IN_PRODUCTION("In Production", 3),
    SCHEDULED("Scheduled", 4),
    PUBLISHED("Published", 5);

    fun nextStage(): ProjectStage =
        entries.getOrNull(stepIndex + 1) ?: PUBLISHED

    companion object {
        fun fromRaw(raw: String): ProjectStage =
            entries.firstOrNull { it.name == raw } ?: IDEA
    }
}

enum class ScriptFormat(val label: String) {
    LONG_FORM("Long-Form"),
    SHORT("YouTube Short"),
    REEL("Vertical Reel");

    companion object {
        fun fromRaw(raw: String): ScriptFormat =
            entries.firstOrNull { it.name == raw } ?: LONG_FORM
    }
}

enum class CalendarStatus(val label: String) {
    PLANNED("Planned"),
    FILMING("Filming"),
    READY("Ready"),
    PUBLISHED("Published");

    fun nextStatus(): CalendarStatus = when (this) {
        PLANNED -> FILMING
        FILMING -> READY
        READY -> PUBLISHED
        PUBLISHED -> PLANNED
    }

    companion object {
        fun fromRaw(raw: String): CalendarStatus =
            entries.firstOrNull { it.name == raw } ?: PLANNED
    }
}

@Keep
@IgnoreExtraProperties
data class CreatorProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val niche: String = "",
    val format: String = ProjectFormat.LONG_FORM.name,
    val stage: String = ProjectStage.IDEA.name,
    val targetPlatform: String = "YouTube",
    val hook: String = "",
    val notes: String = "",
    val clipScore: Long = 0L,
    val scheduledDate: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedFormat: ProjectFormat get() = ProjectFormat.fromRaw(format)
    val parsedStage: ProjectStage get() = ProjectStage.fromRaw(stage)
}

@Keep
@IgnoreExtraProperties
data class ResearchBrief(
    @DocumentId val id: String = "",
    val userId: String = "",
    val topic: String = "",
    val audience: String = "",
    val summary: String = "",
    val viralScore: Long = 0L,
    val angles: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GeneratedScript(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val format: String = ScriptFormat.LONG_FORM.name,
    val tone: String = "Authoritative & Crisp",
    val targetDurationSec: Long = 60L,
    val hook: String = "",
    val body: String = "",
    val cta: String = "",
    val visualCues: List<String> = emptyList(),
    val projectId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedFormat: ScriptFormat get() = ScriptFormat.fromRaw(format)
}

@Keep
@IgnoreExtraProperties
data class CalendarEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val platform: String = "YouTube",
    val scheduledDate: String = "",
    val scheduledTime: String = "18:00",
    val status: String = CalendarStatus.PLANNED.name,
    val projectId: String? = null,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedStatus: CalendarStatus get() = CalendarStatus.fromRaw(status)
}

@Keep
@IgnoreExtraProperties
data class ClipAnalysis(
    @DocumentId val id: String = "",
    val userId: String = "",
    val clipTitle: String = "",
    val sourceSummary: String = "",
    val retentionScore: Long = 0L,
    val hookStrength: Long = 0L,
    val pacingVerdict: String = "",
    val highlights: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

// AI Structured Generation Output Models

data class ResearchGenerationOutput(
    val topic: String,
    val audience: String,
    val summary: String,
    val viralScore: Int,
    val angles: List<String>,
    val keywords: List<String>
)

data class ScriptGenerationOutput(
    val title: String,
    val hook: String,
    val body: String,
    val cta: String,
    val visualCues: List<String>
)

data class ShortBeat(
    val timecode: String,
    val spokenLine: String,
    val visualDirection: String,
    val captionOverlay: String
)

data class ShortsProductionPlan(
    val title: String,
    val durationSeconds: Int,
    val hooks: List<String>,
    val beats: List<ShortBeat>,
    val audioCue: String,
    val hashtags: List<String>
)

data class StoryboardScene(
    val sceneNumber: Int,
    val durationLabel: String,
    val shotType: String,
    val voiceover: String,
    val visualPrompt: String,
    val lightingGrade: String
)

data class VideoStoryboardPlan(
    val title: String,
    val logline: String,
    val aspectRatio: String,
    val visualStyleGuide: String,
    val scenes: List<StoryboardScene>
)

data class ClipIntelligenceOutput(
    val clipTitle: String,
    val retentionScore: Int,
    val hookStrength: Int,
    val pacingVerdict: String,
    val highlights: List<String>,
    val improvements: List<String>
)

data class StudioPreferences(
    val defaultPlatform: String = "YouTube",
    val defaultTone: String = "Crisp & Analytical",
    val defaultShortDurationSec: Int = 45,
    val compactDensity: Boolean = false,
    val dynamicColor: Boolean = false
)
