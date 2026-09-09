package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SensitivityLevel
import com.example.ui.PrivacyUiState
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ThreatCrimson
import com.example.ui.theme.WarningAmber

@Composable
fun SettingsScreen(
    uiState: PrivacyUiState,
    onSetSensitivity: (SensitivityLevel) -> Unit,
    onToggleBatterySaver: () -> Unit,
    onToggleLowLightBoost: () -> Unit,
    onToggleVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section: Customizable Sensitivity Levels
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = CyberCyanLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CUSTOMIZABLE SENSITIVITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = CyberCyanLight
                )
            }
            Text(
                text = "Adjust gaze angle tolerance, face confidence thresholds, and reaction debounce timing.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            SensitivityLevel.values().forEach { level ->
                val isSelected = uiState.sensitivity == level
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF131A29),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) CyberCyanLight else Color(0xFF1F2937)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetSensitivity(level) }
                        .testTag("sensitivity_option_${level.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSetSensitivity(level) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = CyberCyanLight,
                                unselectedColor = Color(0xFF64748B)
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = level.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                if (level == SensitivityLevel.MEDIUM) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = CyberCyanLight.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Recommended",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = CyberCyanLight,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (level) {
                                    SensitivityLevel.LOW -> "Requires >80% confidence & 500ms confirmation. Best for busy trains or crowded cafes to avoid false blurs."
                                    SensitivityLevel.MEDIUM -> "Balanced >65% confidence & 300ms confirmation. Reliable detection with natural grace period."
                                    SensitivityLevel.HIGH -> "Fast >45% confidence & 150ms instant response. Detects distant peeks and sharp sideways glances."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // Section: Battery-Saving Mode & CPU Efficiency
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = SafeEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CPU & BATTERY OPTIMIZATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = SafeEmerald
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF131A29),
                border = BorderStroke(1.dp, Color(0xFF1F2937)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Battery-Saving Mode",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Minimizes CPU consumption while camera is active",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Switch(
                            checked = uiState.isBatterySaverEnabled,
                            onCheckedChange = { onToggleBatterySaver() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SafeEmerald
                            ),
                            modifier = Modifier.testTag("battery_saver_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Frame Analysis Frequency", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text(if (uiState.isBatterySaverEnabled) "3 FPS (~330ms)" else "12 FPS (~85ms)", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated CPU Load", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text(if (uiState.isBatterySaverEnabled) "~3% to 4%" else "~12% to 15%", style = MaterialTheme.typography.labelSmall, color = if (uiState.isBatterySaverEnabled) SafeEmerald else CyberCyanLight)
                            }
                        }
                    }
                }
            }
        }

        // Section: Low-Light Conditions Mode
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Nightlight,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LOW-LIGHT EFFICIENCY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = WarningAmber
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF131A29),
                border = BorderStroke(1.dp, Color(0xFF1F2937)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adaptive Low-Light Boost",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Dynamically boosts contrast and sensitivity in dim rooms (<40 Lux) to spot shadows and glances.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Switch(
                        checked = uiState.isLowLightBoostEnabled,
                        onCheckedChange = { onToggleLowLightBoost() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = WarningAmber
                        ),
                        modifier = Modifier.testTag("low_light_boost_switch")
                    )
                }
            }
        }

        // Section: Alerts & Feedback
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = CyberCyanLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HAPTIC ALERTS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = CyberCyanLight
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF131A29),
                border = BorderStroke(1.dp, Color(0xFF1F2937)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tactile Vibration Alert",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Subtly vibrates phone when an intruder looking at your screen is confirmed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Switch(
                        checked = uiState.isVibrationEnabled,
                        onCheckedChange = { onToggleVibration() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberCyanLight
                        ),
                        modifier = Modifier.testTag("vibration_alert_switch")
                    )
                }
            }
        }

        // Section: 100% On-Device Privacy Architecture Guarantee
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SafeEmerald.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.OfflinePin,
                                contentDescription = null,
                                tint = SafeEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "100% LOCAL ON-DEVICE PRIVACY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = SafeEmerald
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "• All camera frames are analyzed in-memory via on-device machine vision and discarded immediately.\n" +
                            "• Zero frames, photos, or biometric data are stored or sent anywhere.\n" +
                            "• Requires no internet access (complete offline functionality).\n" +
                            "• Motion and threat logs are stored exclusively in your local on-device SQLite database.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
