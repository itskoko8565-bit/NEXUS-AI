package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.CalendarEntry
import com.example.data.model.ClipAnalysis
import com.example.data.model.CreatorProject
import com.example.data.model.GeneratedScript
import com.example.data.model.ProjectStage
import com.example.data.model.ResearchBrief
import com.example.data.model.UiState
import com.example.ui.components.BackendStatusBadge
import com.example.ui.components.MetricTile
import com.example.ui.components.NexusCard
import com.example.ui.components.NexusSectionHeader
import com.example.ui.components.PipelineBarChart
import com.example.ui.components.ScoreRing
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun AnalyticsScreen(
    projectsState: UiState<List<CreatorProject>>,
    briefsState: UiState<List<ResearchBrief>>,
    scriptsState: UiState<List<GeneratedScript>>,
    calendarState: UiState<List<CalendarEntry>>,
    clipState: UiState<List<ClipAnalysis>>
) {
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()
    val briefs = (briefsState as? UiState.Success)?.data.orEmpty()
    val scripts = (scriptsState as? UiState.Success)?.data.orEmpty()
    val calendar = (calendarState as? UiState.Success)?.data.orEmpty()
    val clips = (clipState as? UiState.Success)?.data.orEmpty()

    val avgRetention = if (clips.isNotEmpty()) clips.map { it.retentionScore }.average().toInt() else 0
    val avgHook = if (clips.isNotEmpty()) clips.map { it.hookStrength }.average().toInt() else 0
    val avgViral = if (briefs.isNotEmpty()) briefs.map { it.viralScore }.average().toInt() else 0

    val stageBars = ProjectStage.entries.map { stage ->
        val label = when (stage) {
            ProjectStage.IDEA -> "Idea"
            ProjectStage.RESEARCH -> "Res"
            ProjectStage.SCRIPTED -> "Script"
            ProjectStage.IN_PRODUCTION -> "Prod"
            ProjectStage.SCHEDULED -> "Sched"
            ProjectStage.PUBLISHED -> "Pub"
        }
        label to projects.count { it.stage == stage.name }
    }

    val platformCounts = (projects.map { it.targetPlatform } + calendar.map { it.platform })
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
    val totalPlatformItems = platformCounts.sumOf { it.value }.coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NexusSectionHeader(
                title = "Workspace Analytics",
                subtitle = "Live production metrics computed from your Firestore records"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = "TOTAL PROJECTS",
                    value = "${projects.size}",
                    icon = Icons.Filled.FolderOpen,
                    accent = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "SCRIPTS",
                    value = "${scripts.size}",
                    icon = Icons.Filled.Description,
                    accent = VioletAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = "RESEARCH BRIEFS",
                    value = "${briefs.size}",
                    icon = Icons.Filled.Search,
                    accent = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "CLIP AUDITS",
                    value = "${clips.size}",
                    icon = Icons.Filled.AutoAwesome,
                    accent = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            NexusCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pipeline Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    BackendStatusBadge(label = "LIVE DATA", isLive = true)
                }
                Spacer(Modifier.height(14.dp))
                PipelineBarChart(bars = stageBars)
            }
        }

        item {
            NexusCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "AI Quality Benchmarks",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Averages across your saved Clip Intelligence & Research Briefs",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScoreRing(score = avgRetention, label = "AVG RETENTION", accent = EmeraldSuccess)
                    ScoreRing(score = avgHook, label = "AVG HOOK", accent = CyanAccent)
                    ScoreRing(score = avgViral, label = "AVG VIRAL", accent = VioletAccent)
                }
            }
        }

        item {
            NexusCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Platform Mix",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                if (platformCounts.isEmpty()) {
                    Text(
                        text = "Create projects or schedule calendar releases to track platform mix.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    platformCounts.forEach { (platform, count) ->
                        val ratio = count.toFloat() / totalPlatformItems.toFloat()
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = platform,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "$count items",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CyanAccent
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp),
                                color = CyanAccent,
                                trackColor = ElevatedSurface
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Analytics,
                            contentDescription = null,
                            tint = AmberWarning
                        )
                        Text(
                            text = "External Channel Telemetry",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    BackendStatusBadge(label = "Not Connected", isLive = false)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "YouTube Studio, TikTok Analytics, and Instagram Insights OAuth integrations are not connected in this Android build. No simulated view counts or fake subscriber metrics are shown.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}
