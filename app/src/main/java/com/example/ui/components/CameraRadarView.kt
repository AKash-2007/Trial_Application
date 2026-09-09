package com.example.ui.components

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.detection.PrivacyDetector
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ThreatCrimson
import com.example.ui.theme.WarningAmber
import java.util.concurrent.Executors

@Composable
fun CameraRadarView(
    detector: PrivacyDetector?,
    hasCameraPermission: Boolean,
    isProtectionActive: Boolean,
    isPeekerDetected: Boolean,
    facesCount: Int,
    ambientLightLux: Float,
    isLowLight: Boolean,
    isEnvironmentalFiltered: Boolean,
    fps: Int,
    cpuEstimatePercent: Int,
    isBatterySaverActive: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPeekerDetected) ThreatCrimson.copy(alpha = 0.6f)
            else if (isEnvironmentalFiltered) WarningAmber.copy(alpha = 0.6f)
            else CyberCyanLight.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("camera_radar_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Live Telemetry Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (!isProtectionActive) Color.Gray
                                else if (isPeekerDetected) ThreatCrimson
                                else if (isEnvironmentalFiltered) WarningAmber
                                else SafeEmerald
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!isProtectionActive) "PROTECTION PAUSED"
                        else if (isPeekerDetected) "THREAT DETECTED"
                        else if (isEnvironmentalFiltered) "ENV. MOTION FILTERED"
                        else "SCANNING LOCAL FRONT CAM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (!isProtectionActive) Color(0xFF94A3B8)
                        else if (isPeekerDetected) ThreatCrimson
                        else if (isEnvironmentalFiltered) WarningAmber
                        else CyberCyanLight
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraFront,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasCameraPermission) "Cam Active" else "Sensors Ready",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Radar & Camera Preview Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF090D16)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission && detector != null && isProtectionActive) {
                    // Real CameraX preview integration with analyzer
                    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
                    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val provider = cameraProviderFuture.get()
                                    cameraProvider = provider
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }
                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also {
                                            it.setAnalyzer(cameraExecutor, detector)
                                        }

                                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                                    provider.unbindAll()
                                    provider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )

                    DisposableEffect(Unit) {
                        onDispose {
                            try {
                                cameraProvider?.unbindAll()
                                cameraExecutor.shutdown()
                            } catch (_: Exception) {}
                        }
                    }
                }

                // Semi-transparent radar reticle overlay on top of camera or standalone
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x660B0F19))
                )

                // Concentric circles
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .border(1.dp, CyberCyanLight.copy(alpha = 0.2f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(1.dp, CyberCyanLight.copy(alpha = 0.35f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.dp, CyberCyanLight.copy(alpha = 0.5f), CircleShape)
                )

                // Radar scanning sweep line
                if (isProtectionActive) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .rotate(sweepAngle)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(75.dp)
                                .height(2.dp)
                                .align(Alignment.CenterStart)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(CyberCyanLight, Color.Transparent)
                                    )
                                )
                        )
                    }
                }

                // Primary user head blip (Center)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SafeEmerald.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, SafeEmerald),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Primary Owner",
                                tint = SafeEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "Owner",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = SafeEmerald
                    )
                }

                // Secondary Peeker Blip (Appears if peeker detected)
                if (isPeekerDetected || facesCount > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 40.dp, top = 25.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = ThreatCrimson.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(2.dp, ThreatCrimson),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Peeker Threat",
                                        tint = ThreatCrimson,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "⚠️ Peeker",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = ThreatCrimson
                            )
                        }
                    }
                }

                // Environmental Motion indicator badge inside radar
                if (isEnvironmentalFiltered) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xDD1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterDrama,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Environmental motion ignored (No face)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color(0xFFFDE68A)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lower Telemetry Pills Row: FPS, CPU, Light Level, Battery Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // FPS Telemetry
                TelemetryPill(
                    icon = Icons.Default.Speed,
                    label = "Rate",
                    value = "${fps} FPS",
                    tint = CyberCyanLight
                )

                // CPU Telemetry
                TelemetryPill(
                    icon = Icons.Default.BatteryChargingFull,
                    label = "CPU",
                    value = "~${cpuEstimatePercent}%",
                    tint = if (isBatterySaverActive) SafeEmerald else CyberCyanLight
                )

                // Light Level Telemetry
                TelemetryPill(
                    icon = if (isLowLight) Icons.Default.Nightlight else Icons.Default.LightMode,
                    label = if (isLowLight) "Low Light" else "Ambient",
                    value = "${ambientLightLux.toInt()} Lux",
                    tint = if (isLowLight) WarningAmber else CyberCyanLight
                )
            }
        }
    }
}

@Composable
private fun TelemetryPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
