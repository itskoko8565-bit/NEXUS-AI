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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.StudioPreferences
import com.example.ui.components.BackendStatusBadge
import com.example.ui.components.NexusCard
import com.example.ui.components.NexusSectionHeader
import com.example.ui.theme.CoralError
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    userDisplayName: String,
    userEmail: String,
    preferences: StudioPreferences,
    onPlatformChange: (String) -> Unit,
    onToneChange: (String) -> Unit,
    onShortDurationChange: (Int) -> Unit,
    onCompactDensityChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val dbId = runCatching { context.getString(R.string.firestore_database_id) }.getOrDefault("configured")
    val platforms = listOf("YouTube", "YouTube Shorts", "TikTok", "Instagram Reels")
    val tones = listOf("Crisp & Analytical", "High-Energy Tech", "Storytelling", "Direct & Tactical")
    val durations = listOf(15, 30, 45, 60)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NexusSectionHeader(
                title = stringResource(R.string.settings_title),
                subtitle = "Account session, DataStore studio defaults, and backend status"
            )
        }

        item {
            NexusCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Google account avatar",
                        tint = CyanAccent,
                        modifier = Modifier.size(44.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userDisplayName.ifBlank { "NEXUS Creator" },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = userEmail.ifBlank { "Signed in with Google" },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    BackendStatusBadge(label = "AUTHENTICATED", isLive = true)
                }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("sign_out_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("Sign Out")
                }
            }
        }

        item {
            NexusCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Studio Defaults (DataStore)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                Text("Default Target Platform", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    platforms.forEach { plat ->
                        FilterChip(
                            selected = preferences.defaultPlatform == plat,
                            onClick = { onPlatformChange(plat) },
                            label = { Text(plat) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Default Script Tone", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tones.forEach { tone ->
                        FilterChip(
                            selected = preferences.defaultTone == tone,
                            onClick = { onToneChange(tone) },
                            label = { Text(tone) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Default Short Runtime", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    durations.forEach { sec ->
                        FilterChip(
                            selected = preferences.defaultShortDurationSec == sec,
                            onClick = { onShortDurationChange(sec) },
                            label = { Text("${sec}s") }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Compact Dashboard Density",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tighter vertical padding in workspace lists",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = preferences.compactDensity,
                        onCheckedChange = onCompactDensityChange
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Material You Dynamic Accent",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Use system wallpaper dark palette on Android 12+",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = preferences.dynamicColor,
                        onCheckedChange = onDynamicColorChange
                    )
                }
            }
        }

        item {
            NexusCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Backend & Integration Registry",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                IntegrationRow("Firebase AI Logic (gemini-3.8-flash)", true)
                IntegrationRow("Firebase App Check Attestation", true)
                IntegrationRow("Cloud Firestore ($dbId)", true)
                IntegrationRow("Google Sign-In (Credential Manager)", true)
                IntegrationRow("YouTube / TikTok Channel OAuth Sync", false)
                IntegrationRow("Cloud Video MP4 Render Engine", false)
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun IntegrationRow(name: String, isLive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 42.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        BackendStatusBadge(
            label = if (isLive) "LIVE" else "NOT CONNECTED",
            isLive = isLive
        )
    }
}
