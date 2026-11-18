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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
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
import com.example.data.model.CalendarEntry
import com.example.data.model.CalendarStatus
import com.example.data.model.UiState
import com.example.ui.components.AiErrorBanner
import com.example.ui.components.EmptyStudioState
import com.example.ui.components.NexusCard
import com.example.ui.components.NexusSectionHeader
import com.example.ui.components.StagePill
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    defaultPlatform: String,
    calendarState: UiState<List<CalendarEntry>>,
    onCreateEntry: (
        title: String,
        platform: String,
        date: String,
        time: String,
        status: CalendarStatus,
        notes: String
    ) -> Unit,
    onCycleStatus: (CalendarEntry) -> Unit,
    onDeleteEntry: (String) -> Unit
) {
    var statusFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    val allEntries = (calendarState as? UiState.Success)?.data.orEmpty()
    val filteredEntries = if (statusFilter == null) {
        allEntries
    } else {
        allEntries.filter { it.status == statusFilter }
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
                        title = "Content Calendar",
                        subtitle = "${allEntries.size} scheduled releases"
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("add_calendar_entry_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color(0xFF002026)
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Schedule")
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { statusFilter = null },
                        label = { Text("All (${allEntries.size})") }
                    )
                }
                items(CalendarStatus.entries) { status ->
                    val count = allEntries.count { it.status == status.name }
                    FilterChip(
                        selected = statusFilter == status.name,
                        onClick = {
                            statusFilter = if (statusFilter == status.name) null else status.name
                        },
                        label = { Text("${status.label} ($count)") }
                    )
                }
            }
        }

        when (calendarState) {
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
                item { AiErrorBanner(message = calendarState.message) }
            }
            is UiState.Success -> {
                if (filteredEntries.isEmpty()) {
                    item {
                        EmptyStudioState(
                            icon = Icons.Filled.CalendarMonth,
                            title = "No scheduled content",
                            description = "Plan your next release slot with date, platform, and production status.",
                            actionLabel = "Add Release Slot",
                            onAction = { showAddDialog = true }
                        )
                    }
                } else {
                    items(filteredEntries, key = { it.id }) { entry ->
                        val statusAccent = when (entry.parsedStatus) {
                            CalendarStatus.PLANNED -> TextSecondary
                            CalendarStatus.FILMING -> AmberWarning
                            CalendarStatus.READY -> CyanAccent
                            CalendarStatus.PUBLISHED -> EmeraldSuccess
                        }
                        NexusCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        StagePill(text = entry.scheduledDate, accent = CyanAccent)
                                        StagePill(text = entry.scheduledTime, accent = VioletAccent)
                                        StagePill(text = entry.parsedStatus.label, accent = statusAccent)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = entry.platform,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                IconButton(onClick = { onDeleteEntry(entry.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete calendar entry",
                                        tint = TextMuted
                                    )
                                }
                            }
                            if (entry.notes.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = entry.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onCycleStatus(entry) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text("Set Status: ${entry.parsedStatus.nextStatus().label}")
                            }
                        }
                    }
                }
            }
            else -> Unit
        }

        item { Spacer(Modifier.height(12.dp)) }
    }

    if (showAddDialog) {
        val defaultDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }.time
        )
        var title by rememberSaveable { mutableStateOf("") }
        var platform by rememberSaveable(defaultPlatform) { mutableStateOf(defaultPlatform) }
        var date by rememberSaveable { mutableStateOf(defaultDate) }
        var time by rememberSaveable { mutableStateOf("18:00") }
        var notes by rememberSaveable { mutableStateOf("") }
        var status by rememberSaveable { mutableStateOf(CalendarStatus.PLANNED) }

        val dateValid = date.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Schedule Content Drop") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Release Title *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_calendar_title")
                    )
                    OutlinedTextField(
                        value = platform,
                        onValueChange = { platform = it },
                        label = { Text("Platform") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("YYYY-MM-DD") },
                            isError = !dateValid,
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Time") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CalendarStatus.entries.forEach { st ->
                            FilterChip(
                                selected = status == st,
                                onClick = { status = st },
                                label = { Text(st.label) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Release Notes") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && dateValid) {
                            onCreateEntry(title, platform, date, time, status, notes)
                            showAddDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && dateValid,
                    modifier = Modifier.testTag("dialog_calendar_confirm")
                ) {
                    Text("Save Slot")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
