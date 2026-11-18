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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BookmarkAdd
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
import com.example.data.model.ShortsProductionPlan
import com.example.data.model.UiState
import com.example.ui.components.AiErrorBanner
import com.example.ui.components.AiLoadingCard
import com.example.ui.components.BackendStatusBadge
import com.example.ui.components.NexusCard
import com.example.ui.components.StagePill
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShortsScreen(
    defaultShortDurationSec: Int,
    shortsState: UiState<ShortsProductionPlan>,
    onGenerateShort: (topic: String, hookStyle: String, durationSec: Int, platform: String) -> Unit,
    onSaveShortPackage: (ShortsProductionPlan) -> Unit
) {
    var topic by rememberSaveable { mutableStateOf("") }
    var hookStyle by rememberSaveable { mutableStateOf("Contrarian Claim") }
    var durationSec by rememberSaveable(defaultShortDurationSec) { mutableIntStateOf(defaultShortDurationSec) }
    var platform by rememberSaveable { mutableStateOf("YouTube Shorts") }

    val hookStyles = listOf("Contrarian Claim", "Result First", "Fast Mistake Fix", "Curiosity Gap")
    val durations = listOf(15, 30, 45, 60)
    val platforms = listOf("YouTube Shorts", "TikTok", "Instagram Reels")

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
                        text = "Shorts Production",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    BackendStatusBadge(label = "9:16 ENGINE", isLive = true)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Short Concept / Hook Topic") },
                    placeholder = { Text("e.g., 3 Compose performance traps killing 60fps scroll") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shorts_topic_input")
                )
                Spacer(Modifier.height(10.dp))
                Text("Hook Archetype", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hookStyles.forEach { style ->
                        FilterChip(
                            selected = hookStyle == style,
                            onClick = { hookStyle = style },
                            label = { Text(style) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Target Runtime", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    durations.forEach { sec ->
                        FilterChip(
                            selected = durationSec == sec,
                            onClick = { durationSec = sec },
                            label = { Text("${sec}s") }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Vertical Platform", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    platforms.forEach { plat ->
                        FilterChip(
                            selected = platform == plat,
                            onClick = { platform = plat },
                            label = { Text(plat) }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { onGenerateShort(topic, hookStyle, durationSec, platform) },
                    enabled = shortsState !is UiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("generate_shorts_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberWarning,
                        contentColor = Color(0xFF1F1400)
                    )
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Produce Vertical Beat Sheet")
                }
            }
        }

        when (shortsState) {
            is UiState.Loading -> {
                item { AiLoadingCard(label = "Building timecoded hooks, captions & beats...") }
            }
            is UiState.Error -> {
                item { AiErrorBanner(message = shortsState.message) }
            }
            is UiState.Success -> {
                val plan = shortsState.data
                item {
                    NexusCard(
                        modifier = Modifier.fillMaxWidth(),
                        highlightAccent = AmberWarning
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StagePill(text = "${plan.durationSeconds}S VERTICAL PLAN", accent = AmberWarning)
                            StagePill(text = "${plan.beats.size} BEATS", accent = CyanAccent)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = plan.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(10.dp))
                        Text("3 OPENING HOOK VARIANTS", style = MaterialTheme.typography.labelMedium, color = CyanAccent)
                        Spacer(Modifier.height(6.dp))
                        plan.hooks.forEachIndexed { idx, hook ->
                            Text(
                                text = "H${idx + 1}: $hook",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("AUDIO / SFX DIRECTION", style = MaterialTheme.typography.labelMedium, color = VioletAccent)
                        Text(
                            text = plan.audioCue,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            plan.hashtags.forEach { tag ->
                                StagePill(text = tag, accent = EmeraldSuccess)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { onSaveShortPackage(plan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("save_short_package_button")
                        ) {
                            Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("Save Beat Sheet to Scripts & Projects")
                        }
                    }
                }

                items(plan.beats) { beat ->
                    NexusCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StagePill(text = beat.timecode, accent = CyanAccent)
                            StagePill(text = beat.captionOverlay, accent = VioletAccent)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "\"${beat.spokenLine}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Visual: ${beat.visualDirection}",
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
