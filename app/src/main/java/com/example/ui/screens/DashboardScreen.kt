package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CalendarEntry
import com.example.data.model.ClipAnalysis
import com.example.data.model.CreatorProject
import com.example.data.model.GeneratedScript
import com.example.data.model.ResearchBrief
import com.example.data.model.UiState
import com.example.ui.components.BackendStatusBadge
import com.example.ui.components.EmptyStudioState
import com.example.ui.components.MetricTile
import com.example.ui.components.NexusCard
import com.example.ui.components.NexusSectionHeader
import com.example.ui.components.StagePill
import com.example.ui.navigation.NexusDestination
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun DashboardScreen(
    userName: String,
    projectsState: UiState<List<CreatorProject>>,
    briefsState: UiState<List<ResearchBrief>>,
    scriptsState: UiState<List<GeneratedScript>>,
    calendarState: UiState<List<CalendarEntry>>,
    clipState: UiState<List<ClipAnalysis>>,
    onNavigate: (NexusDestination) -> Unit
) {
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()
    val briefs = (briefsState as? UiState.Success)?.data.orEmpty()
    val scripts = (scriptsState as? UiState.Success)?.data.orEmpty()
    val calendar = (calendarState as? UiState.Success)?.data.orEmpty()
    val clips = (clipState as? UiState.Success)?.data.orEmpty()

    val avgClipScore = if (clips.isNotEmpty()) {
        clips.map { it.retentionScore }.average().toInt()
    } else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            // Hero Studio Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(MaterialTheme.shapes.large)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nexus_studio_banner_1794992476886),
                    contentDescription = "NEXUS AI Command Center banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ObsidianBg.copy(alpha = 0.94f),
                                    ObsidianBg.copy(alpha = 0.65f),
                                    ObsidianBg.copy(alpha = 0.25f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BackendStatusBadge(label = "GEMINI 3.8 FLASH", isLive = true)
                        BackendStatusBadge(label = "FIRESTORE SYNC", isLive = true)
                    }
                    Column {
                        Text(
                            text = "Welcome back, ${userName.ifBlank { "Creator" }}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${projects.size} active projects • ${scripts.size} scripts • ${calendar.size} scheduled slots",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 600.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricTile(
                            label = "PROJECTS",
                            value = "${projects.size}",
                            icon = Icons.Filled.FolderOpen,
                            accent = CyanAccent,
                            caption = "${projects.count { it.stage == "PUBLISHED" }} published",
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "SCRIPTS",
                            value = "${scripts.size}",
                            icon = Icons.Filled.Description,
                            accent = VioletAccent,
                            caption = "${briefs.size} research briefs",
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "CALENDAR",
                            value = "${calendar.size}",
                            icon = Icons.Filled.CalendarMonth,
                            accent = EmeraldSuccess,
                            caption = "Scheduled slots",
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "AVG CLIP SCORE",
                            value = if (clips.isNotEmpty()) "$avgClipScore%" else "--",
                            icon = Icons.Filled.AutoAwesome,
                            accent = AmberWarning,
                            caption = "${clips.size} audited clips",
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricTile(
                                label = "PROJECTS",
                                value = "${projects.size}",
                                icon = Icons.Filled.FolderOpen,
                                accent = CyanAccent,
                                caption = "${projects.count { it.stage == "PUBLISHED" }} published",
                                modifier = Modifier.weight(1f)
                            )
                            MetricTile(
                                label = "SCRIPTS",
                                value = "${scripts.size}",
                                icon = Icons.Filled.Description,
                                accent = VioletAccent,
                                caption = "${briefs.size} briefs",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricTile(
                                label = "CALENDAR",
                                value = "${calendar.size}",
                                icon = Icons.Filled.CalendarMonth,
                                accent = EmeraldSuccess,
                                caption = "Scheduled releases",
                                modifier = Modifier.weight(1f)
                            )
                            MetricTile(
                                label = "AVG CLIP SCORE",
                                value = if (clips.isNotEmpty()) "$avgClipScore%" else "--",
                                icon = Icons.Filled.AutoAwesome,
                                accent = AmberWarning,
                                caption = "${clips.size} audited",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            NexusSectionHeader(
                title = "Studio Modules",
                subtitle = "Jump directly into any NEXUS AI production workflow"
            )
            Spacer(Modifier.height(10.dp))
            val studioTools = listOf(
                Triple(NexusDestination.RESEARCH, CyanAccent, Icons.Filled.Search),
                Triple(NexusDestination.SCRIPT_GEN, VioletAccent, Icons.Filled.Description),
                Triple(NexusDestination.SHORTS, AmberWarning, Icons.Filled.Bolt),
                Triple(NexusDestination.VIDEO_STUDIO, CyanAccent, Icons.Filled.MovieCreation),
                Triple(NexusDestination.CLIP_INTEL, EmeraldSuccess, Icons.Filled.AutoAwesome),
                Triple(NexusDestination.PROJECTS, VioletAccent, Icons.Filled.FolderOpen),
                Triple(NexusDestination.CALENDAR, EmeraldSuccess, Icons.Filled.CalendarMonth),
                Triple(NexusDestination.ANALYTICS, CyanAccent, Icons.Filled.Analytics)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(studioTools) { (dest, accent, icon) ->
                    NexusCard(
                        onClick = { onNavigate(dest) },
                        highlightAccent = accent,
                        modifier = Modifier
                            .width(172.dp)
                            .testTag("dash_launch_${dest.route}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accent.copy(alpha = 0.14f)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = dest.title,
                                    tint = accent,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open ${dest.title}",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = dest.shortLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = dest.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item {
            NexusSectionHeader(
                title = "Active Pipeline",
                subtitle = "Recent projects synced from Firestore",
                actionLabel = "All Projects",
                onActionClick = { onNavigate(NexusDestination.PROJECTS) }
            )
        }

        if (projects.isEmpty()) {
            item {
                EmptyStudioState(
                    icon = Icons.Filled.FolderOpen,
                    title = "No active projects yet",
                    description = "Start by running AI Content Research or create your first production project.",
                    actionLabel = "Run AI Research",
                    onAction = { onNavigate(NexusDestination.RESEARCH) }
                )
            }
        } else {
            items(projects.take(3), key = { it.id }) { project ->
                NexusCard(
                    onClick = { onNavigate(NexusDestination.PROJECTS) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${project.targetPlatform} • ${project.parsedFormat.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        StagePill(
                            text = project.parsedStage.label,
                            accent = if (project.stage == "PUBLISHED") EmeraldSuccess else CyanAccent
                        )
                    }
                }
            }
        }

        item {
            NexusSectionHeader(
                title = "Upcoming Releases",
                subtitle = "Next scheduled drops on your Content Calendar",
                actionLabel = "Open Calendar",
                onActionClick = { onNavigate(NexusDestination.CALENDAR) }
            )
        }

        if (calendar.isEmpty()) {
            item {
                EmptyStudioState(
                    icon = Icons.Filled.CalendarMonth,
                    title = "Calendar is clear",
                    description = "Schedule your upcoming Shorts or long-form drops to keep a steady cadence.",
                    actionLabel = "Schedule Drop",
                    onAction = { onNavigate(NexusDestination.CALENDAR) }
                )
            }
        } else {
            items(calendar.take(3), key = { it.id }) { entry ->
                NexusCard(
                    onClick = { onNavigate(NexusDestination.CALENDAR) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${entry.scheduledDate} at ${entry.scheduledTime} • ${entry.platform}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                        StagePill(
                            text = entry.parsedStatus.label,
                            accent = VioletAccent
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}
