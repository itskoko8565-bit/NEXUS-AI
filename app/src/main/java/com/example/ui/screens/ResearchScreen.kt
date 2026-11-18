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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ResearchBrief
import com.example.data.model.ResearchGenerationOutput
import com.example.data.model.UiState
import com.example.ui.components.AiErrorBanner
import com.example.ui.components.AiLoadingCard
import com.example.ui.components.BackendStatusBadge
import com.example.ui.components.EmptyStudioState
import com.example.ui.components.NexusCard
import com.example.ui.components.NexusSectionHeader
import com.example.ui.components.ScoreRing
import com.example.ui.components.StagePill
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResearchScreen(
    defaultPlatform: String,
    researchState: UiState<ResearchGenerationOutput>,
    savedBriefsState: UiState<List<ResearchBrief>>,
    onRunResearch: (topic: String, audience: String, platform: String) -> Unit,
    onSaveBrief: (ResearchGenerationOutput) -> Unit,
    onDeleteBrief: (String) -> Unit,
    onSendAngleToScript: (topic: String, angle: String) -> Unit
) {
    var topic by rememberSaveable { mutableStateOf("") }
    var audience by rememberSaveable { mutableStateOf("") }
    var platform by rememberSaveable(defaultPlatform) { mutableStateOf(defaultPlatform) }
    val platforms = listOf("YouTube", "YouTube Shorts", "TikTok", "Instagram Reels", "Podcast")

    val savedBriefs = (savedBriefsState as? UiState.Success)?.data.orEmpty()

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
                        text = "AI Content Research",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    BackendStatusBadge(label = "LIVE GEMINI", isLive = true)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic or Niche") },
                    placeholder = { Text("e.g., On-device AI agents for Android developers") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("research_topic_input")
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = audience,
                    onValueChange = { audience = it },
                    label = { Text("Target Audience (Optional)") },
                    placeholder = { Text("e.g., Indie creators & mobile engineers") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("research_audience_input")
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Target Platform",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    platforms.forEach { item ->
                        FilterChip(
                            selected = platform == item,
                            onClick = { platform = item },
                            label = { Text(item) }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { onRunResearch(topic, audience, platform) },
                    enabled = researchState !is UiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("run_research_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color(0xFF002026)
                    )
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Generate Research Brief")
                }
            }
        }

        when (researchState) {
            is UiState.Loading -> {
                item { AiLoadingCard(label = "Synthesizing market demand & high-CTR angles...") }
            }
            is UiState.Error -> {
                item { AiErrorBanner(message = researchState.message) }
            }
            is UiState.Success -> {
                val result = researchState.data
                item {
                    NexusCard(
                        modifier = Modifier.fillMaxWidth(),
                        highlightAccent = CyanAccent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                StagePill(text = "RESEARCH SYNTHESIS", accent = CyanAccent)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = result.topic,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Audience: ${result.audience}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            ScoreRing(
                                score = result.viralScore,
                                label = "VIRAL INDEX",
                                accent = EmeraldSuccess,
                                size = 76.dp
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = result.summary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "HIGH-CTR CONTENT ANGLES",
                            style = MaterialTheme.typography.labelMedium,
                            color = VioletAccent
                        )
                        Spacer(Modifier.height(8.dp))
                        result.angles.forEachIndexed { idx, angle ->
                            NexusCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = { onSendAngleToScript(result.topic, angle) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${idx + 1}. $angle",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Description,
                                        contentDescription = "Send angle to Script Generator",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "KEYWORDS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            result.keywords.forEach { kw ->
                                StagePill(text = "#$kw", accent = VioletAccent)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { onSaveBrief(result) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("save_research_brief_button")
                        ) {
                            Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("Save Brief to Workspace")
                        }
                    }
                }
            }
            else -> Unit
        }

        item {
            NexusSectionHeader(
                title = "Saved Research Briefs",
                subtitle = "${savedBriefs.size} briefs persisted in Firestore"
            )
        }

        if (savedBriefs.isEmpty()) {
            item {
                EmptyStudioState(
                    icon = Icons.Filled.Search,
                    title = "No saved briefs yet",
                    description = "Generate a research brief above and tap Save Brief to keep it synced across devices."
                )
            }
        } else {
            items(savedBriefs, key = { it.id }) { brief ->
                NexusCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = brief.topic,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Viral Index ${brief.viralScore}/100 • ${brief.angles.size} angles",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanAccent
                            )
                        }
                        IconButton(onClick = { onDeleteBrief(brief.id) }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete research brief",
                                tint = TextMuted
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = brief.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    if (brief.angles.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onSendAngleToScript(brief.topic, brief.angles.first()) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Draft Script from Top Angle")
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}
