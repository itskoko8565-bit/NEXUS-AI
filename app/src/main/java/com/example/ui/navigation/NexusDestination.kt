package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavGroup(val title: String) {
    COMMAND("COMMAND"),
    AI_STUDIO("AI STUDIO"),
    PRODUCTION("PRODUCTION & GROWTH"),
    SYSTEM("SYSTEM")
}

enum class NexusDestination(
    val route: String,
    val title: String,
    val shortLabel: String,
    val subtitle: String,
    val group: NavGroup,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD(
        route = "dashboard",
        title = "NEXUS AI",
        shortLabel = "Dashboard",
        subtitle = "Creator Command Center",
        group = NavGroup.COMMAND,
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    ),
    RESEARCH(
        route = "research",
        title = "Content Research",
        shortLabel = "Research",
        subtitle = "Gemini Topic & Angle Intelligence",
        group = NavGroup.AI_STUDIO,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    SCRIPT_GEN(
        route = "script_gen",
        title = "Script Generator",
        shortLabel = "Scripts",
        subtitle = "Streaming Hooks, Beats & Visual Cues",
        group = NavGroup.AI_STUDIO,
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description
    ),
    SHORTS(
        route = "shorts",
        title = "Shorts Production",
        shortLabel = "Shorts",
        subtitle = "Vertical 15-60s Hook & Beat Engine",
        group = NavGroup.AI_STUDIO,
        selectedIcon = Icons.Filled.Bolt,
        unselectedIcon = Icons.Outlined.Bolt
    ),
    VIDEO_STUDIO(
        route = "video_studio",
        title = "Video Studio",
        shortLabel = "Studio",
        subtitle = "AI Storyboard & Scene Director",
        group = NavGroup.AI_STUDIO,
        selectedIcon = Icons.Filled.MovieCreation,
        unselectedIcon = Icons.Outlined.MovieCreation
    ),
    CLIP_INTEL(
        route = "clip_intel",
        title = "Clip Intelligence",
        shortLabel = "Clip Intel",
        subtitle = "Retention, Hook & Pacing Audit",
        group = NavGroup.AI_STUDIO,
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    ),
    PROJECTS(
        route = "projects",
        title = "Projects",
        shortLabel = "Projects",
        subtitle = "Production Pipeline & Assets",
        group = NavGroup.PRODUCTION,
        selectedIcon = Icons.Filled.FolderOpen,
        unselectedIcon = Icons.Outlined.FolderOpen
    ),
    CALENDAR(
        route = "calendar",
        title = "Content Calendar",
        shortLabel = "Calendar",
        subtitle = "Release Schedule & Cadence",
        group = NavGroup.PRODUCTION,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ),
    ANALYTICS(
        route = "analytics",
        title = "Analytics",
        shortLabel = "Analytics",
        subtitle = "Workspace Telemetry & Channel Sync",
        group = NavGroup.PRODUCTION,
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics
    ),
    SETTINGS(
        route = "settings",
        title = "Settings",
        shortLabel = "Settings",
        subtitle = "Account, Defaults & Integrations",
        group = NavGroup.SYSTEM,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    );

    companion object {
        val bottomBarPrimary = listOf(DASHBOARD, RESEARCH, SCRIPT_GEN, PROJECTS)

        fun fromRoute(route: String?): NexusDestination =
            entries.firstOrNull { it.route == route } ?: DASHBOARD
    }
}
