package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClipIntelligenceOutput
import com.example.data.model.ResearchGenerationOutput
import com.example.data.model.ScriptFormat
import com.example.data.model.ScriptGenerationOutput
import com.example.data.model.ShortsProductionPlan
import com.example.data.model.UiState
import com.example.data.model.VideoStoryboardPlan
import com.example.data.repository.NexusAiRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NexusAiViewModel(
    private val aiRepository: NexusAiRepository = NexusAiRepository()
) : ViewModel() {

    private val _researchState = MutableStateFlow<UiState<ResearchGenerationOutput>>(UiState.Idle)
    val researchState: StateFlow<UiState<ResearchGenerationOutput>> = _researchState.asStateFlow()

    private val _scriptStreamState = MutableStateFlow<UiState<ScriptGenerationOutput>>(UiState.Idle)
    val scriptStreamState: StateFlow<UiState<ScriptGenerationOutput>> = _scriptStreamState.asStateFlow()

    private val _shortsState = MutableStateFlow<UiState<ShortsProductionPlan>>(UiState.Idle)
    val shortsState: StateFlow<UiState<ShortsProductionPlan>> = _shortsState.asStateFlow()

    private val _storyboardState = MutableStateFlow<UiState<VideoStoryboardPlan>>(UiState.Idle)
    val storyboardState: StateFlow<UiState<VideoStoryboardPlan>> = _storyboardState.asStateFlow()

    private val _clipIntelState = MutableStateFlow<UiState<ClipIntelligenceOutput>>(UiState.Idle)
    val clipIntelState: StateFlow<UiState<ClipIntelligenceOutput>> = _clipIntelState.asStateFlow()

    // Cross-screen handoff prefill for Script Generator
    private val _prefilledScriptTopic = MutableStateFlow("")
    val prefilledScriptTopic: StateFlow<String> = _prefilledScriptTopic.asStateFlow()

    private val _prefilledScriptAngle = MutableStateFlow("")
    val prefilledScriptAngle: StateFlow<String> = _prefilledScriptAngle.asStateFlow()

    fun primeScriptFromResearch(topic: String, angle: String) {
        _prefilledScriptTopic.value = topic
        _prefilledScriptAngle.value = angle
    }

    fun clearScriptPrefill() {
        _prefilledScriptTopic.value = ""
        _prefilledScriptAngle.value = ""
    }

    fun runResearch(topic: String, audience: String, platform: String) {
        if (topic.isBlank()) {
            _researchState.value = UiState.Error("Enter a topic or niche to research.")
            return
        }
        viewModelScope.launch {
            _researchState.value = UiState.Loading
            try {
                val result = aiRepository.generateResearchBrief(topic.trim(), audience.trim(), platform)
                _researchState.value = UiState.Success(result)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Research timed out", e)
                _researchState.value = UiState.Error("Research timed out after 60 seconds. Please retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Research failed", e)
                _researchState.value = UiState.Error(e.localizedMessage ?: "AI research failed.")
            }
        }
    }

    fun generateScript(
        title: String,
        format: ScriptFormat,
        tone: String,
        durationSec: Int,
        angleNotes: String
    ) {
        if (title.isBlank()) {
            _scriptStreamState.value = UiState.Error("Enter a working video title or prompt.")
            return
        }
        viewModelScope.launch {
            _scriptStreamState.value = UiState.Loading
            var latestDraft = ""
            try {
                aiRepository.streamScriptDraft(
                    title = title.trim(),
                    format = format,
                    tone = tone,
                    durationSec = durationSec,
                    angleNotes = angleNotes.trim()
                ).collect { accumulated ->
                    latestDraft = accumulated
                    _scriptStreamState.value = UiState.Streaming(accumulated)
                }
                val structured = aiRepository.structureScriptDraft(
                    title = title.trim(),
                    format = format,
                    tone = tone,
                    durationSec = durationSec,
                    rawDraft = latestDraft
                )
                _scriptStreamState.value = UiState.Success(structured)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Script generation timed out", e)
                _scriptStreamState.value = UiState.Error("Script generation timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Script generation failed", e)
                _scriptStreamState.value = UiState.Error(e.localizedMessage ?: "Script generation failed.")
            }
        }
    }

    fun generateShortsProduction(
        topic: String,
        hookStyle: String,
        durationSeconds: Int,
        platform: String
    ) {
        if (topic.isBlank()) {
            _shortsState.value = UiState.Error("Describe your Short topic first.")
            return
        }
        viewModelScope.launch {
            _shortsState.value = UiState.Loading
            try {
                val plan = aiRepository.generateShortsPlan(
                    topic = topic.trim(),
                    hookStyle = hookStyle,
                    durationSeconds = durationSeconds,
                    platform = platform
                )
                _shortsState.value = UiState.Success(plan)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Shorts generation timed out", e)
                _shortsState.value = UiState.Error("Shorts production timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Shorts generation failed", e)
                _shortsState.value = UiState.Error(e.localizedMessage ?: "Shorts generation failed.")
            }
        }
    }

    fun generateVideoStoryboard(
        concept: String,
        visualStyle: String,
        aspectRatio: String,
        sceneCount: Int
    ) {
        if (concept.isBlank()) {
            _storyboardState.value = UiState.Error("Enter a video concept to storyboard.")
            return
        }
        viewModelScope.launch {
            _storyboardState.value = UiState.Loading
            try {
                val plan = aiRepository.generateVideoStoryboard(
                    concept = concept.trim(),
                    visualStyle = visualStyle,
                    aspectRatio = aspectRatio,
                    sceneCount = sceneCount
                )
                _storyboardState.value = UiState.Success(plan)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Storyboard timed out", e)
                _storyboardState.value = UiState.Error("Storyboard generation timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Storyboard generation failed", e)
                _storyboardState.value = UiState.Error(e.localizedMessage ?: "Storyboard generation failed.")
            }
        }
    }

    fun analyzeClip(
        clipTitle: String,
        transcriptOrNotes: String,
        frameBitmap: Bitmap?
    ) {
        if (clipTitle.isBlank() || transcriptOrNotes.isBlank()) {
            _clipIntelState.value = UiState.Error("Provide both a clip title and transcript or segment notes.")
            return
        }
        viewModelScope.launch {
            _clipIntelState.value = UiState.Loading
            try {
                val result = aiRepository.analyzeClipIntelligence(
                    clipTitle = clipTitle.trim(),
                    transcriptOrNotes = transcriptOrNotes.trim(),
                    frameBitmap = frameBitmap
                )
                _clipIntelState.value = UiState.Success(result)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Clip analysis timed out", e)
                _clipIntelState.value = UiState.Error("Clip analysis timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Clip analysis failed", e)
                _clipIntelState.value = UiState.Error(e.localizedMessage ?: "Clip Intelligence failed.")
            }
        }
    }

    private companion object {
        const val TAG = "NexusAiVM"
    }
}
