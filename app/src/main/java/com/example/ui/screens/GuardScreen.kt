package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.detection.PrivacyDetector
import com.example.ui.PrivacyUiState
import com.example.ui.components.CameraRadarView
import com.example.ui.components.ProtectedContentCard
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ThreatCrimson
import com.example.ui.theme.WarningAmber

@Composable
fun GuardScreen(
    uiState: PrivacyUiState,
    detector: PrivacyDetector?,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onToggleProtection: () -> Unit,
    onTriggerSimulatedPeeker: (String, Boolean) -> Unit,
    onTriggerSimulatedEnvMotion: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Camera Permission Banner if not yet granted
        if (!hasCameraPermission) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = WarningAmber.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Front Camera Access Required",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "All image processing stays 100% on-device. No data leaves your phone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("enable_camera_button")
                    ) {
                        Text("Enable", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Master Protection Control Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF131A29),
            border = BorderStroke(
                1.dp,
                if (uiState.isProtectionActive) CyberCyanLight.copy(alpha = 0.4f)
                else Color(0xFF334155)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("master_guard_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = if (uiState.isProtectionActive) {
                                listOf(CyberCyanLight.copy(alpha = 0.10f), Color.Transparent)
                            } else {
                                listOf(Color(0xFF1E293B).copy(alpha = 0.1f), Color.Transparent)
                            },
                            radius = 600f
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (uiState.isProtectionActive) SafeEmerald.copy(alpha = 0.2f) else Color(0xFF334155),
                            border = BorderStroke(
                                2.dp,
                                if (uiState.isProtectionActive) SafeEmerald else Color(0xFF64748B)
                            ),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "Protection State",
                                    tint = if (uiState.isProtectionActive) SafeEmerald else Color(0xFF94A3B8),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (uiState.isProtectionActive) "SHIELD ACTIVE" else "SHIELD PAUSED",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = if (uiState.isProtectionActive) Color.White else Color(0xFF94A3B8)
                            )
                            Text(
                                text = if (uiState.isProtectionActive) "Peeker & gaze protection on" else "Screen unguarded",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.isProtectionActive) SafeEmerald else Color(0xFF64748B)
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isProtectionActive,
                        onCheckedChange = { onToggleProtection() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SafeEmerald,
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("protection_toggle_switch")
                    )
                }
            }
        }

        // Live Camera & Visual Radar HUD Card
        CameraRadarView(
            detector = detector,
            hasCameraPermission = hasCameraPermission,
            isProtectionActive = uiState.isProtectionActive,
            isPeekerDetected = uiState.isPrivacyBlurTriggered,
            facesCount = uiState.facesCount,
            ambientLightLux = uiState.ambientLightLux,
            isLowLight = uiState.isLowLight,
            isEnvironmentalFiltered = uiState.isEnvironmentalMotionFiltered,
            fps = uiState.currentFps,
            cpuEstimatePercent = uiState.cpuEstimatePercent,
            isBatterySaverActive = uiState.isBatterySaverEnabled
        )

        // Status & Mode Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sensitivity Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = CyberCyanLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Sensitivity",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = uiState.sensitivity.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Battery-Saving Status Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = if (uiState.isBatterySaverEnabled) SafeEmerald else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Battery Saver",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = if (uiState.isBatterySaverEnabled) "Active (~4% CPU)" else "Off (12 FPS)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (uiState.isBatterySaverEnabled) SafeEmerald else Color.White
                        )
                    }
                }
            }
        }

        // Low-Light & Environmental Filter Notice Badges
        if (uiState.isLowLight) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WarningAmber.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Low-Light Enhancement Active",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFDE68A)
                        )
                        Text(
                            text = "Adaptive contrast boost is detecting shadows and silhouettes at ${uiState.ambientLightLux.toInt()} Lux.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // Interactive Testing Sandbox Controls
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = CyberCyanLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REAL-TIME SIMULATION & TESTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = CyberCyanLight
                        )
                    }
                    Text(
                        text = "Instant Demo",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Test how the app triggers privacy blur on shoulder surfers while ignoring background movements.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Simulation Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onTriggerSimulatedPeeker("over right shoulder", false) },
                        colors = ButtonDefaults.buttonColors(containerColor = ThreatCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_peeker_button")
                    ) {
                        Text(
                            text = "Test Peeker",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = { onTriggerSimulatedEnvMotion() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFBBF24)),
                        border = BorderStroke(1.dp, Color(0xFFFBBF24)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_env_motion_button")
                    ) {
                        Text(
                            text = "Test Env Motion",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onTriggerSimulatedPeeker("in dim lighting", true) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyanLight),
                    border = BorderStroke(1.dp, CyberCyanLight.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_low_light_peeker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test Low-Light Peeker Detection",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Protected Content Card (Sample Screen Content)
        ProtectedContentCard(
            isBlurred = uiState.isPrivacyBlurTriggered
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
