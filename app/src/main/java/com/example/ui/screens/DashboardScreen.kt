package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MotionEventType
import com.example.data.MotionLog
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ThreatCrimson
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    logs: List<MotionLog>,
    threatCount: Int,
    ignoredCount: Int,
    searchQuery: String,
    selectedFilterType: MotionEventType?,
    onSearchChange: (String) -> Unit,
    onFilterChange: (MotionEventType?) -> Unit,
    onDeleteLog: (Long) -> Unit,
    onClearAllLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Analytics Stats
            item {
                Text(
                    text = "MOTION & PRIVACY ANALYTICS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = CyberCyanLight
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Threats Blocked Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF131A29),
                        border = BorderStroke(1.dp, ThreatCrimson.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = ThreatCrimson,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Threats Blocked",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = threatCount.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Text(
                                text = "Peekers blurred",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = ThreatCrimson
                            )
                        }
                    }

                    // Background Ignored Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF131A29),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterDrama,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Env. Movements",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ignoredCount.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Text(
                                text = "Filtered without blur",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = WarningAmber
                            )
                        }
                    }
                }
            }

            // Search Bar & Filter Row
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = CyberCyanLight
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    },
                    placeholder = { Text("Search logs by angle, sensitivity, or details...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyanLight,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_logs_input")
                )
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilterType == null,
                            onClick = { onFilterChange(null) },
                            label = { Text("All (${logs.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyanLight.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyanLight
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilterType == MotionEventType.PEEKER_DETECTED,
                            onClick = { onFilterChange(MotionEventType.PEEKER_DETECTED) },
                            label = { Text("Peeker Detected") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ThreatCrimson.copy(alpha = 0.2f),
                                selectedLabelColor = ThreatCrimson
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilterType == MotionEventType.BACKGROUND_IGNORED,
                            onClick = { onFilterChange(MotionEventType.BACKGROUND_IGNORED) },
                            label = { Text("Env Motion Ignored") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarningAmber.copy(alpha = 0.2f),
                                selectedLabelColor = WarningAmber
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilterType == MotionEventType.LOW_LIGHT_WARNING,
                            onClick = { onFilterChange(MotionEventType.LOW_LIGHT_WARNING) },
                            label = { Text("Low-Light Alerts") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF818CF8).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF818CF8)
                            )
                        )
                    }
                }
            }

            // Action Row: Export Report & Clear All
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MOTION EVENT LOGS (${logs.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                val report = buildString {
                                    appendLine("=== PRIVACY GUARD AUDIT REPORT ===")
                                    appendLine("Generated: ${Date()}")
                                    appendLine("Threats Blocked: $threatCount")
                                    appendLine("Environmental Movements Filtered: $ignoredCount")
                                    appendLine("Total Events: ${logs.size}")
                                    appendLine("-----------------------------------")
                                    logs.forEach { log ->
                                        appendLine("[${Date(log.timestamp)}] ${log.eventType.label} (${(log.confidence * 100).toInt()}%) - ${log.details}")
                                    }
                                }
                                clipboardManager.setText(AnnotatedString(report))
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Audit report copied to clipboard!")
                                }
                            },
                            modifier = Modifier.testTag("export_logs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = CyberCyanLight
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", color = CyberCyanLight, style = MaterialTheme.typography.labelSmall)
                        }

                        if (logs.isNotEmpty()) {
                            TextButton(
                                onClick = { showClearConfirmDialog = true },
                                modifier = Modifier.testTag("clear_all_logs_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFF87171)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", color = Color(0xFFF87171), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Empty state if no logs
            if (logs.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF131A29),
                        border = BorderStroke(1.dp, Color(0xFF1F2937)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Motion Logs Recorded",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "When privacy guard detects another person or filters background movement, on-device logs will appear here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Motion Log Items
            items(logs, key = { it.id }) { log ->
                MotionLogItemCard(
                    log = log,
                    onDelete = { onDeleteLog(log.id) }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        // Clear Confirmation Dialog
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                title = { Text("Clear All Motion Logs?", color = Color.White) },
                text = {
                    Text(
                        "This will permanently erase all motion and threat records stored locally on your device.",
                        color = Color(0xFFCBD5E1)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearAllLogs()
                            showClearConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThreatCrimson)
                    ) {
                        Text("Delete All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun MotionLogItemCard(
    log: MotionLog,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isThreat = log.eventType == MotionEventType.PEEKER_DETECTED || log.eventType == MotionEventType.MULTIPLE_FACES
    val isEnvIgnored = log.eventType == MotionEventType.BACKGROUND_IGNORED
    val isLowLight = log.eventType == MotionEventType.LOW_LIGHT_WARNING

    val themeColor = if (isThreat) ThreatCrimson
    else if (isEnvIgnored) WarningAmber
    else if (isLowLight) Color(0xFF818CF8)
    else CyberCyanLight

    val timeFormatted = remember(log.timestamp) {
        val sdf = SimpleDateFormat("h:mm:ss a • MMM d", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF131A29),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("motion_log_item_${log.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Event Type Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isThreat) Icons.Default.VisibilityOff
                            else if (isEnvIgnored) Icons.Default.FilterDrama
                            else Icons.Default.Nightlight,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = log.eventType.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = themeColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete log",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details description
            Text(
                text = log.details,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Row: Confidence bar, Lux, Sensitivity, Battery Saver
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Confidence",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${(log.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = themeColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { log.confidence },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = themeColor,
                        trackColor = Color(0xFF1E293B)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${log.lightLevelLux.toInt()} Lux",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = log.sensitivity,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = CyberCyanLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (log.isBatterySaverActive) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SafeEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "⚡ Low CPU",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = SafeEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
