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
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.UiState
import com.example.data.model.VideoStoryboardPlan
import com.example.ui.components.AiErrorBanner
import com.example.ui.components.AiLoadingCard
import com.example.ui.components.BackendStatusBadge
import com.example.ui.components.NexusCard
import com.example.ui.components.StagePill
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoStudioScreen(
    storyboardState: UiState<VideoStoryboardPlan>,
    onGenerateStoryboard: (concept: String, visualStyle: String, aspectRatio: String, sceneCount: Int) -> Unit,
    onSaveStoryboardAsProject: (VideoStoryboardPlan) -> Unit
) {
    var concept by rememberSaveable { mutableStateOf("") }
    var visualStyle by rememberSaveable { mutableStateOf("Obsidian Cyber Documentary") }
    var aspectRatio by rememberSaveable { mutableStateOf("16:9") }
    var sceneCount by rememberSaveable { mutableIntStateOf(4) }

    val styles = listOf(
        "Obsidian Cyber Documentary",
        "Clean Apple-Grade Studio",
        "High-Contrast Editorial",
        "Minimal Kinetic Motion"
    )
    val ratios = listOf("16:9", "9:16", "1:1")
    val counts = listOf(3, 4, 6)

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
                        text = "Video Studio",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    BackendStatusBadge(label = "STORYBOARD AI", isLive = true)
                }
                Spacer(Modifier.height(8.dp))
                BackendStatusBadge(label = "MP4 Cloud Render: Not Connected", isLive = false)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "AI Storyboard & Shot Direction runs live via Gemini 3.8 Flash and saves directly to Firestore Projects. Cloud MP4 video rendering/encoding is not connected in this foundation build.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text("Video Concept / Narrative Brief") },
                    placeholder = { Text("e.g., Cinematic walkthrough of an autonomous mobile workflow") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_studio_concept_input")
                )
                Spacer(Modifier.height(10.dp))
                Text("Visual Aesthetic", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    styles.forEach { style ->
                        FilterChip(
                            selected = visualStyle == style,
                            onClick = { visualStyle = style },
                            label = { Text(style) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Aspect Ratio", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ratios.forEach { r ->
                                FilterChip(
                                    selected = aspectRatio == r,
                                    onClick = { aspectRatio = r },
                                    label = { Text(r) }
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Scenes", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            counts.forEach { count ->
                                FilterChip(
                                    selected = sceneCount == count,
                                    onClick = { sceneCount = count },
                                    label = { Text("$count") }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { onGenerateStoryboard(concept, visualStyle, aspectRatio, sceneCount) },
                    enabled = storyboardState !is UiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("generate_storyboard_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color(0xFF002026)
                    )
                ) {
                    Icon(Icons.Filled.MovieCreation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Direct AI Storyboard")
                }
            }
        }

        when (storyboardState) {
            is UiState.Loading -> {
                item { AiLoadingCard(label = "Composing shot list, lighting & visual prompts...") }
            }
            is UiState.Error -> {
                item { AiErrorBanner(message = storyboardState.message) }
            }
            is UiState.Success -> {
                val plan = storyboardState.data
                item {
                    NexusCard(
                        modifier = Modifier.fillMaxWidth(),
                        highlightAccent = CyanAccent
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StagePill(text = plan.aspectRatio, accent = CyanAccent)
                            StagePill(text = "${plan.scenes.size} SCENES", accent = VioletAccent)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = plan.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = plan.logline,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Style Guide: ${plan.visualStyleGuide}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { onSaveStoryboardAsProject(plan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("save_storyboard_project_button")
                        ) {
                            Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("Save Storyboard to Projects")
                        }
                    }
                }

                items(plan.scenes) { scene ->
                    NexusCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StagePill(text = "SCENE ${scene.sceneNumber} • ${scene.durationLabel}", accent = CyanAccent)
                            StagePill(text = scene.shotType, accent = EmeraldSuccess)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("VOICEOVER", style = MaterialTheme.typography.labelMedium, color = VioletAccent)
                        Text(
                            text = scene.voiceover,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("VISUAL DIRECTION PROMPT", style = MaterialTheme.typography.labelMedium, color = CyanAccent)
                        Text(
                            text = scene.visualPrompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Lighting: ${scene.lightingGrade}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            else -> Unit
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}
