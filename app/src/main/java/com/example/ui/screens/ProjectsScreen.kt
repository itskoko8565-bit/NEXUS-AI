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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.CreatorProject
import com.example.data.model.ProjectFormat
import com.example.data.model.ProjectStage
import com.example.data.model.UiState
import com.example.ui.components.AiErrorBanner
import com.example.ui.components.EmptyStudioState
import com.example.ui.components.NexusCard
import com.example.ui.components.NexusSectionHeader
import com.example.ui.components.StagePill
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectsScreen(
    defaultPlatform: String,
    projectsState: UiState<List<CreatorProject>>,
    onCreateProject: (
        title: String,
        niche: String,
        format: ProjectFormat,
        stage: ProjectStage,
        platform: String,
        hook: String,
        notes: String
    ) -> Unit,
    onAdvanceStage: (CreatorProject) -> Unit,
    onDeleteProject: (String) -> Unit
) {
    var selectedStageFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    val allProjects = (projectsState as? UiState.Success)?.data.orEmpty()
    val filteredProjects = if (selectedStageFilter == null) {
        allProjects
    } else {
        allProjects.filter { it.stage == selectedStageFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    NexusSectionHeader(
                        title = "Project Pipeline",
                        subtitle = "${allProjects.size} total projects synced in Firestore"
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("new_project_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color(0xFF002026)
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("New")
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedStageFilter == null,
                        onClick = { selectedStageFilter = null },
                        label = { Text("All (${allProjects.size})") }
                    )
                }
                items(ProjectStage.entries) { stage ->
                    val count = allProjects.count { it.stage == stage.name }
                    FilterChip(
                        selected = selectedStageFilter == stage.name,
                        onClick = {
                            selectedStageFilter = if (selectedStageFilter == stage.name) null else stage.name
                        },
                        label = { Text("${stage.label} ($count)") }
                    )
                }
            }
        }

        when (projectsState) {
            is UiState.Loading -> {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = CyanAccent)
                    }
                }
            }
            is UiState.Error -> {
                item { AiErrorBanner(message = projectsState.message) }
            }
            is UiState.Success -> {
                if (filteredProjects.isEmpty()) {
                    item {
                        EmptyStudioState(
                            icon = Icons.Filled.FolderOpen,
                            title = "No projects in this stage",
                            description = "Create a new production project or switch your stage filter above.",
                            actionLabel = "Create Project",
                            onAction = { showCreateDialog = true }
                        )
                    }
                } else {
                    items(filteredProjects, key = { it.id }) { project ->
                        NexusCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        StagePill(
                                            text = project.parsedStage.label,
                                            accent = if (project.stage == ProjectStage.PUBLISHED.name) EmeraldSuccess else CyanAccent
                                        )
                                        StagePill(text = project.parsedFormat.label, accent = VioletAccent)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = project.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "${project.targetPlatform}${if (project.niche.isNotBlank()) " • ${project.niche}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                IconButton(onClick = { onDeleteProject(project.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete project",
                                        tint = TextMuted
                                    )
                                }
                            }
                            if (project.hook.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Hook: ${project.hook}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            if (project.notes.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = project.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            if (project.parsedStage != ProjectStage.PUBLISHED) {
                                Spacer(Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { onAdvanceStage(project) },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text("Move to ${project.parsedStage.nextStage().label}")
                                    Spacer(Modifier.size(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> Unit
        }

        item { Spacer(Modifier.height(12.dp)) }
    }

    if (showCreateDialog) {
        var title by rememberSaveable { mutableStateOf("") }
        var niche by rememberSaveable { mutableStateOf("") }
        var hook by rememberSaveable { mutableStateOf("") }
        var notes by rememberSaveable { mutableStateOf("") }
        var platform by rememberSaveable(defaultPlatform) { mutableStateOf(defaultPlatform) }
        var format by rememberSaveable { mutableStateOf(ProjectFormat.LONG_FORM) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Studio Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Project Title *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_project_title")
                    )
                    OutlinedTextField(
                        value = niche,
                        onValueChange = { niche = it },
                        label = { Text("Niche / Pillar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = platform,
                        onValueChange = { platform = it },
                        label = { Text("Target Platform") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProjectFormat.entries.forEach { fmt ->
                            FilterChip(
                                selected = format == fmt,
                                onClick = { format = fmt },
                                label = { Text(fmt.label) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = hook,
                        onValueChange = { hook = it },
                        label = { Text("Opening Hook") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Production Notes") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateProject(
                                title,
                                niche,
                                format,
                                ProjectStage.IDEA,
                                platform,
                                hook,
                                notes
                            )
                            showCreateDialog = false
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.testTag("dialog_create_project_confirm")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
