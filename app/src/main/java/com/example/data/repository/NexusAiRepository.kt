package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.model.ClipIntelligenceOutput
import com.example.data.model.ResearchGenerationOutput
import com.example.data.model.ScriptFormat
import com.example.data.model.ScriptGenerationOutput
import com.example.data.model.ShortBeat
import com.example.data.model.ShortsProductionPlan
import com.example.data.model.StoryboardScene
import com.example.data.model.VideoStoryboardPlan
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class NexusAiRepository {

    companion object {
        private const val MODEL_NAME = "gemini-3.8-flash"
        private const val TIMEOUT_MS = 60_000L
    }

    private val researchModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "topic" to Schema.string("Refined topic headline"),
                "audience" to Schema.string("Target viewer persona"),
                "summary" to Schema.string("Concise 3-4 sentence opportunity & trend synthesis"),
                "viralScore" to Schema.integer("Estimated viral potential score from 1 to 100"),
                "angles" to Schema.array(
                    items = Schema.string("Unique content angle or title hook"),
                    description = "4 to 5 high-performing content angles"
                ),
                "keywords" to Schema.array(
                    items = Schema.string("Search keyword or tag"),
                    description = "6 to 10 high-intent keywords"
                )
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val scriptStructuredModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string("Compelling video title"),
                "hook" to Schema.string("Opening 0-5 second pattern-interrupt hook"),
                "body" to Schema.string("Full structured script body with pacing markers"),
                "cta" to Schema.string("Closing call to action"),
                "visualCues" to Schema.array(
                    items = Schema.string("Timestamped B-roll or on-screen graphic cue"),
                    description = "4 to 8 visual production cues"
                )
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val streamingTextModel by lazy {
        Firebase.ai.generativeModel(modelName = MODEL_NAME)
    }

    private val shortsModel by lazy {
        val beatSchema = Schema.obj(
            properties = mapOf(
                "timecode" to Schema.string("Time range e.g. 00:00-00:04"),
                "spokenLine" to Schema.string("Exact spoken narration line"),
                "visualDirection" to Schema.string("Camera framing, cut, or motion action"),
                "captionOverlay" to Schema.string("Punchy 2-5 word on-screen text overlay")
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string("Short video working title"),
                "durationSeconds" to Schema.integer("Total target runtime in seconds"),
                "hooks" to Schema.array(
                    items = Schema.string("Alternative 2-second opening hook"),
                    description = "3 distinct opening hook options"
                ),
                "beats" to Schema.array(
                    items = beatSchema,
                    description = "4 to 6 sequential production beats"
                ),
                "audioCue" to Schema.string("Music tempo, SFX, and pacing note"),
                "hashtags" to Schema.array(
                    items = Schema.string("Relevant hashtag starting with #"),
                    description = "5 to 8 hashtags"
                )
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val storyboardModel by lazy {
        val sceneSchema = Schema.obj(
            properties = mapOf(
                "sceneNumber" to Schema.integer("1-based scene number"),
                "durationLabel" to Schema.string("Scene duration e.g. 6s"),
                "shotType" to Schema.string("Camera shot & movement e.g. Low-angle push-in"),
                "voiceover" to Schema.string("Voiceover or dialogue line"),
                "visualPrompt" to Schema.string("Detailed prompt for visual look / B-roll generation"),
                "lightingGrade" to Schema.string("Lighting and color palette direction")
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string("Production title"),
                "logline" to Schema.string("One-sentence creative logline"),
                "aspectRatio" to Schema.string("Aspect ratio string e.g. 16:9 or 9:16"),
                "visualStyleGuide" to Schema.string("Consistent art direction and color grade"),
                "scenes" to Schema.array(
                    items = sceneSchema,
                    description = "Sequential storyboard scenes"
                )
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val clipIntelModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "clipTitle" to Schema.string("Analyzed clip title"),
                "retentionScore" to Schema.integer("Estimated retention score 0-100"),
                "hookStrength" to Schema.integer("Opening hook strength score 0-100"),
                "pacingVerdict" to Schema.string("Concise editorial pacing diagnosis"),
                "highlights" to Schema.array(
                    items = Schema.string("Timecoded viral moment or strong segment"),
                    description = "3 to 6 highlights"
                ),
                "improvements" to Schema.array(
                    items = Schema.string("Concrete edit or re-cut recommendation"),
                    description = "3 to 5 actionable improvements"
                )
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    suspend fun generateResearchBrief(
        topic: String,
        audience: String,
        platform: String
    ): ResearchGenerationOutput = withTimeout(TIMEOUT_MS) {
        val prompt = """
            You are NEXUS AI's Senior Content Strategist.
            Conduct focused editorial & creator research for:
            - Topic: $topic
            - Target Audience: ${audience.ifBlank { "Tech-savvy creators & builders" }}
            - Platform: $platform

            Provide a sharp, realistic synthesis of why this topic resonates, a calibrated viralScore (0-100),
            4-5 high-CTR angles, and 6-10 high-intent keywords.
        """.trimIndent()

        val response = researchModel.generateContent(prompt)
        val jsonStr = response.text ?: throw IllegalStateException("Empty response from Gemini.")
        val json = JSONObject(jsonStr)

        val anglesArr = json.optJSONArray("angles")
        val angles = if (anglesArr != null) {
            (0 until anglesArr.length()).map { anglesArr.getString(it) }
        } else emptyList()

        val kwArr = json.optJSONArray("keywords")
        val keywords = if (kwArr != null) {
            (0 until kwArr.length()).map { kwArr.getString(it) }
        } else emptyList()

        ResearchGenerationOutput(
            topic = json.optString("topic", topic),
            audience = json.optString("audience", audience),
            summary = json.optString("summary", ""),
            viralScore = json.optInt("viralScore", 75).coerceIn(0, 100),
            angles = angles,
            keywords = keywords
        )
    }

    fun streamScriptDraft(
        title: String,
        format: ScriptFormat,
        tone: String,
        durationSec: Int,
        angleNotes: String
    ): Flow<String> = flow {
        val prompt = """
            You are NEXUS AI's Lead Scriptwriter.
            Write a production-ready ${format.label} script.
            - Title / Working Idea: $title
            - Tone: $tone
            - Target Runtime: ${durationSec}s
            - Creative Angle / Context: ${angleNotes.ifBlank { "Maximize retention with crisp storytelling" }}

            Structure cleanly with:
            [HOOK - 0:00]
            [BODY - Beat by Beat with visual cues in brackets]
            [CTA - Closing]
        """.trimIndent()

        val accumulated = StringBuilder()
        withTimeout(TIMEOUT_MS) {
            streamingTextModel.generateContentStream(prompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    accumulated.append(text)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("Gemini returned an empty script stream.")
        }
    }

    suspend fun structureScriptDraft(
        title: String,
        format: ScriptFormat,
        tone: String,
        durationSec: Int,
        rawDraft: String
    ): ScriptGenerationOutput = withTimeout(TIMEOUT_MS) {
        val prompt = """
            Convert this script draft into clean structured production sections for ${format.label} ($tone, ${durationSec}s).
            Title: $title
            Draft:
            $rawDraft
        """.trimIndent()

        val response = scriptStructuredModel.generateContent(prompt)
        val jsonStr = response.text ?: throw IllegalStateException("Empty structured script response.")
        val json = JSONObject(jsonStr)
        val cuesArr = json.optJSONArray("visualCues")
        val cues = if (cuesArr != null) {
            (0 until cuesArr.length()).map { cuesArr.getString(it) }
        } else emptyList()

        ScriptGenerationOutput(
            title = json.optString("title", title),
            hook = json.optString("hook", ""),
            body = json.optString("body", rawDraft),
            cta = json.optString("cta", ""),
            visualCues = cues
        )
    }

    suspend fun generateShortsPlan(
        topic: String,
        hookStyle: String,
        durationSeconds: Int,
        platform: String
    ): ShortsProductionPlan = withTimeout(TIMEOUT_MS) {
        val prompt = """
            You are NEXUS AI's Short-Form Viral Producer.
            Design a $durationSeconds-second vertical short for $platform.
            - Topic: $topic
            - Hook Style: $hookStyle

            Provide 3 distinct 2-second hook options, 4-6 timecoded beats covering the full $durationSeconds seconds,
            concise on-screen caption overlays, audio/SFX direction, and hashtags.
        """.trimIndent()

        val response = shortsModel.generateContent(prompt)
        val jsonStr = response.text ?: throw IllegalStateException("Empty response from Gemini.")
        val json = JSONObject(jsonStr)

        val hooksArr = json.optJSONArray("hooks")
        val hooks = if (hooksArr != null) {
            (0 until hooksArr.length()).map { hooksArr.getString(it) }
        } else emptyList()

        val beatsArr = json.optJSONArray("beats")
        val beats = if (beatsArr != null) {
            (0 until beatsArr.length()).map { idx ->
                val item = beatsArr.getJSONObject(idx)
                ShortBeat(
                    timecode = item.optString("timecode", "00:00"),
                    spokenLine = item.optString("spokenLine", ""),
                    visualDirection = item.optString("visualDirection", ""),
                    captionOverlay = item.optString("captionOverlay", "")
                )
            }
        } else emptyList()

        val tagsArr = json.optJSONArray("hashtags")
        val hashtags = if (tagsArr != null) {
            (0 until tagsArr.length()).map { tagsArr.getString(it) }
        } else emptyList()

        ShortsProductionPlan(
            title = json.optString("title", topic),
            durationSeconds = json.optInt("durationSeconds", durationSeconds),
            hooks = hooks,
            beats = beats,
            audioCue = json.optString("audioCue", "Crisp riser into punchy synth beat"),
            hashtags = hashtags
        )
    }

    suspend fun generateVideoStoryboard(
        concept: String,
        visualStyle: String,
        aspectRatio: String,
        sceneCount: Int
    ): VideoStoryboardPlan = withTimeout(TIMEOUT_MS) {
        val prompt = """
            You are NEXUS AI's Video Studio Director.
            Build a $sceneCount-scene production storyboard for:
            - Concept: $concept
            - Visual Aesthetic: $visualStyle
            - Aspect Ratio: $aspectRatio

            For each scene, include shotType, durationLabel, voiceover, detailed visualPrompt, and lightingGrade.
        """.trimIndent()

        val response = storyboardModel.generateContent(prompt)
        val jsonStr = response.text ?: throw IllegalStateException("Empty storyboard response.")
        val json = JSONObject(jsonStr)

        val scenesArr = json.optJSONArray("scenes")
        val scenes = if (scenesArr != null) {
            (0 until scenesArr.length()).map { idx ->
                val obj = scenesArr.getJSONObject(idx)
                StoryboardScene(
                    sceneNumber = obj.optInt("sceneNumber", idx + 1),
                    durationLabel = obj.optString("durationLabel", "6s"),
                    shotType = obj.optString("shotType", "Medium Shot"),
                    voiceover = obj.optString("voiceover", ""),
                    visualPrompt = obj.optString("visualPrompt", ""),
                    lightingGrade = obj.optString("lightingGrade", "Cyber obsidian rim light")
                )
            }
        } else emptyList()

        VideoStoryboardPlan(
            title = json.optString("title", concept),
            logline = json.optString("logline", ""),
            aspectRatio = json.optString("aspectRatio", aspectRatio),
            visualStyleGuide = json.optString("visualStyleGuide", visualStyle),
            scenes = scenes
        )
    }

    suspend fun analyzeClipIntelligence(
        clipTitle: String,
        transcriptOrNotes: String,
        frameBitmap: Bitmap? = null
    ): ClipIntelligenceOutput = withTimeout(TIMEOUT_MS) {
        val instruction = """
            You are NEXUS AI's Clip Intelligence Editor.
            Audit this video clip / transcript${if (frameBitmap != null) " and attached visual frame" else ""} for viewer retention,
            hook strength, pacing, viral clip highlights, and concrete editorial improvements.
            Clip Title: $clipTitle
            Transcript / Segment Notes:
            $transcriptOrNotes
        """.trimIndent()

        val response = if (frameBitmap != null) {
            clipIntelModel.generateContent(
                content {
                    image(frameBitmap)
                    text(instruction)
                }
            )
        } else {
            clipIntelModel.generateContent(instruction)
        }

        val jsonStr = response.text ?: throw IllegalStateException("Empty Clip Intelligence response.")
        val json = JSONObject(jsonStr)

        val highlightsArr = json.optJSONArray("highlights")
        val highlights = if (highlightsArr != null) {
            (0 until highlightsArr.length()).map { highlightsArr.getString(it) }
        } else emptyList()

        val improvementsArr = json.optJSONArray("improvements")
        val improvements = if (improvementsArr != null) {
            (0 until improvementsArr.length()).map { improvementsArr.getString(it) }
        } else emptyList()

        ClipIntelligenceOutput(
            clipTitle = json.optString("clipTitle", clipTitle),
            retentionScore = json.optInt("retentionScore", 78).coerceIn(0, 100),
            hookStrength = json.optInt("hookStrength", 80).coerceIn(0, 100),
            pacingVerdict = json.optString("pacingVerdict", "Strong opening energy"),
            highlights = highlights,
            improvements = improvements
        )
    }
}
