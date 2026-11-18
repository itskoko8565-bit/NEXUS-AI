package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.GeneratedScript
import com.example.data.model.ScriptFormat
import com.example.data.model.ScriptGenerationOutput
import com.example.data.model.UiState
import com.example.ui.components.AiErrorBanner
import com.example.ui.components.AiLoadingCard
import com.example.ui.components.BackendStatusBadge
import com.example.ui.components.EmptyStudioState
import com.example.ui.components.NexusCard
import com.example.ui.components.NexusSectionHeader
import com.example.ui.components.StagePill
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScriptGenScreen(
    defaultTone: String,
    prefilledTopic: String,
    prefilledAngle: String,
    onConsumePrefill: () -> Unit,
    scriptState: UiState<ScriptGenerationOutput>,
    savedScriptsState: UiState<List<GeneratedScript>>,
    onGenerateScript: (title: String, format: ScriptFormat, tone: String, durationSec: Int, angle: String) -> Unit,
    onSaveScript: (ScriptGenerationOutput, ScriptFormat, String, Int) -> Unit,
    onDeleteScript: (String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var angleNotes by rememberSaveable { mutableStateOf("") }
    var format by rememberSaveable { mutableStateOf(ScriptFormat.LONG_FORM) }
    var tone by rememberSaveable(defaultTone) { mutableStateOf(defaultTone) }
    var durationSec by rememberSaveable { mutableFloatStateOf(180f) }

    LaunchedEffect(prefilledTopic, prefilledAngle) {
        if (prefilledTopic.isNotBlank() || prefilledAngle.isNotBlank()) {
            if (prefilledTopic.isNotBlank()) title = prefilledTopic
            if (prefilledAngle.isNotBlank()) angleNotes = prefilledAngle
            onConsumePrefill()
        }
    }

    val tones = listOf("Crisp & Analytical", "High-Energy Tech", "Storytelling", "Direct & Tactical")
    val savedScripts = (savedScriptsState as? UiState.Success)?.data.orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NexusCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Script Generation",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    BackendStatusBadge(label = "STREAMING GEMINI", isLive = true)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Working Title / Concept") },
                    placeholder = { Text("e.g., Why Local AI Changed Mobile Architecture") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("script_title_input")
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = angleNotes,
                    onValueChange = { angleNotes = it },
                    label = { Text("Creative Angle & Key Points") },
                    placeholder = { Text("Optional context, proof points, or research angle") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("script_angle_input")
                )
                Spacer(Modifier.height(10.dp))
                Text("Format", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScriptFormat.entries.forEach { item ->
                        FilterChip(
                            selected = format == item,
                            onClick = { format = item },
                            label = { Text(item.label) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Voice Tone", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tones.forEach { item ->
                        FilterChip(
                            selected = tone == item,
                            onClick = { tone = item },
                            label = { Text(item) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Target Duration", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(
                        text = "${durationSec.roundToInt()}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanAccent
                    )
                }
                Slider(
                    value = durationSec,
                    onValueChange = { durationSec = it },
                    valueRange = 30f..600f
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onGenerateScript(title, format, tone, durationSec.roundToInt(), angleNotes)
                    },
                    enabled = scriptState !is UiState.Loading && scriptState !is UiState.Streaming,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("generate_script_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color(0xFF002026)
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Stream Production Script")
                }
            }
        }

        when (scriptState) {
            is UiState.Loading -> {
                item { AiLoadingCard(label = "Initializing Gemini 3.8 Flash script stream...") }
            }
            is UiState.Streaming -> {
                item {
                    NexusCard(
                        modifier = Modifier.fillMaxWidth(),
                        highlightAccent = VioletAccent
                    ) {
                        StagePill(text = "STREAMING DRAFT...", accent = VioletAccent)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = scriptState.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            is UiState.Error -> {
                item { AiErrorBanner(message = scriptState.message) }
            }
            is UiState.Success -> {
                val output = scriptState.data
                item {
                    NexusCard(
                        modifier = Modifier.fillMaxWidth(),
                        highlightAccent = EmeraldSuccess
                    ) {
                        StagePill(text = "PRODUCTION READY", accent = EmeraldSuccess)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = output.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("OPENING HOOK", style = MaterialTheme.typography.labelMedium, color = CyanAccent)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = output.hook,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("SCRIPT BODY", style = MaterialTheme.typography.labelMedium, color = VioletAccent)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = output.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (output.cta.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text("CLOSING CTA", style = MaterialTheme.typography.labelMedium, color = EmeraldSuccess)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = output.cta,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        if (output.visualCues.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text("VISUAL CUES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            output.visualCues.forEach { cue ->
                                Text(
                                    text = "• $cue",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { onSaveScript(output, format, tone, durationSec.roundToInt()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("save_script_button")
                        ) {
                            Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("Save Script to Library")
                        }
                    }
                }
            }
            else -> Unit
        }

        item {
            NexusSectionHeader(
                title = "Script Library",
                subtitle = "${savedScripts.size} scripts stored in Firestore"
            )
        }

        if (savedScripts.isEmpty()) {
            item {
                EmptyStudioState(
                    icon = Icons.Filled.Description,
                    title = "No saved scripts yet",
                    description = "Generate a streaming script above and save it to access across your devices."
                )
            }
        } else {
            items(savedScripts, key = { it.id }) { script ->
                NexusCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = script.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${script.parsedFormat.label} • ${script.tone} • ${script.targetDurationSec}s",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanAccent
                            )
                        }
                        IconButton(onClick = { onDeleteScript(script.id) }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete script",
                                tint = TextMuted
                            )
                        }
                    }
                    if (script.hook.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Hook: ${script.hook}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}
