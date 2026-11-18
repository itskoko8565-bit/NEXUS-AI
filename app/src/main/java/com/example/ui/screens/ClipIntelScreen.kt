package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ClipAnalysis
import com.example.data.model.ClipIntelligenceOutput
import com.example.data.model.UiState
import com.example.ui.components.AiErrorBanner
import com.example.ui.components.AiLoadingCard
import com.example.ui.components.BackendStatusBadge
import com.example.ui.components.EmptyStudioState
import com.example.ui.components.NexusCard
import com.example.ui.components.NexusSectionHeader
import com.example.ui.components.ScoreRing
import com.example.ui.components.StagePill
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun ClipIntelScreen(
    clipIntelState: UiState<ClipIntelligenceOutput>,
    savedAnalysesState: UiState<List<ClipAnalysis>>,
    onAnalyzeClip: (title: String, notesOrTranscript: String, frameBitmap: Bitmap?) -> Unit,
    onSaveAnalysis: (ClipIntelligenceOutput, String) -> Unit,
    onDeleteAnalysis: (String) -> Unit
) {
    val context = LocalContext.current
    var clipTitle by rememberSaveable { mutableStateOf("") }
    var transcriptOrNotes by rememberSaveable { mutableStateOf("") }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedBitmap = decodeScaledBitmap(context, uri)
        }
    }

    val savedReports = (savedAnalysesState as? UiState.Success)?.data.orEmpty()

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
                        text = "Clip Intelligence",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    BackendStatusBadge(label = "MULTIMODAL AI", isLive = true)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = clipTitle,
                    onValueChange = { clipTitle = it },
                    label = { Text("Clip or Video Title") },
                    placeholder = { Text("e.g., App Launch Teaser Cut v2") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clip_title_input")
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = transcriptOrNotes,
                    onValueChange = { transcriptOrNotes = it },
                    label = { Text("Transcript, Hook Script, or Segment Breakdown") },
                    placeholder = { Text("Paste spoken transcript or describe key timestamps to audit...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clip_notes_input")
                )
                Spacer(Modifier.height(10.dp))
                if (selectedBitmap != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected video frame or thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(MaterialTheme.shapes.small)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Frame / Thumbnail Attached",
                                style = MaterialTheme.typography.titleSmall,
                                color = EmeraldSuccess
                            )
                            Text(
                                text = "Gemini Vision will inspect visual composition alongside your script.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = { selectedBitmap = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove frame", tint = TextMuted)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("pick_clip_frame_button")
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Attach Frame / Thumbnail (Optional)")
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Button(
                    onClick = { onAnalyzeClip(clipTitle, transcriptOrNotes, selectedBitmap) },
                    enabled = clipIntelState !is UiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("analyze_clip_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess,
                        contentColor = Color(0xFF002016)
                    )
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Audit Retention & Hook")
                }
            }
        }

        when (clipIntelState) {
            is UiState.Loading -> {
                item { AiLoadingCard(label = "Scoring hook strength, pacing & retention curve...") }
            }
            is UiState.Error -> {
                item { AiErrorBanner(message = clipIntelState.message) }
            }
            is UiState.Success -> {
                val audit = clipIntelState.data
                item {
                    NexusCard(
                        modifier = Modifier.fillMaxWidth(),
                        highlightAccent = EmeraldSuccess
                    ) {
                        StagePill(text = "CLIP INTELLIGENCE REPORT", accent = EmeraldSuccess)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = audit.clipTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ScoreRing(
                                score = audit.retentionScore,
                                label = "RETENTION",
                                accent = EmeraldSuccess
                            )
                            ScoreRing(
                                score = audit.hookStrength,
                                label = "HOOK POWER",
                                accent = CyanAccent
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("PACING VERDICT", style = MaterialTheme.typography.labelMedium, color = VioletAccent)
                        Text(
                            text = audit.pacingVerdict,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("VIRAL CLIP WINDOWS", style = MaterialTheme.typography.labelMedium, color = CyanAccent)
                        Spacer(Modifier.height(4.dp))
                        audit.highlights.forEach { hl ->
                            Text(
                                text = "• $hl",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("RECOMMENDED EDITS", style = MaterialTheme.typography.labelMedium, color = AmberWarning)
                        Spacer(Modifier.height(4.dp))
                        audit.improvements.forEach { imp ->
                            Text(
                                text = "→ $imp",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { onSaveAnalysis(audit, transcriptOrNotes) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("save_clip_audit_button")
                        ) {
                            Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("Save Audit to Firestore")
                        }
                    }
                }
            }
            else -> Unit
        }

        item {
            NexusSectionHeader(
                title = "Saved Clip Audits",
                subtitle = "${savedReports.size} reports stored in Firestore"
            )
        }

        if (savedReports.isEmpty()) {
            item {
                EmptyStudioState(
                    icon = Icons.Filled.AutoAwesome,
                    title = "No saved clip audits yet",
                    description = "Run Clip Intelligence on a transcript or thumbnail to benchmark your retention and hook scores."
                )
            }
        } else {
            items(savedReports, key = { it.id }) { report ->
                NexusCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.clipTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Retention ${report.retentionScore}% • Hook ${report.hookStrength}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldSuccess
                            )
                        }
                        IconButton(onClick = { onDeleteAnalysis(report.id) }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete clip audit",
                                tint = TextMuted
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = report.pacingVerdict,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

private fun decodeScaledBitmap(context: android.content.Context, uri: Uri): Bitmap? = try {
    val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            val maxDim = 1024
            val w = info.size.width
            val h = info.size.height
            if (w > maxDim || h > maxDim) {
                val ratio = minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
                decoder.setTargetSize((w * ratio).toInt().coerceAtLeast(1), (h * ratio).toInt().coerceAtLeast(1))
            }
        }
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
    raw
} catch (_: Exception) {
    null
}
