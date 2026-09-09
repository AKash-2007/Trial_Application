package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThreatCrimson

@Composable
fun PrivacyBlurOverlay(
    isTriggered: Boolean,
    reason: String,
    peekerAngle: String,
    peekerDistance: String,
    confidence: Float,
    isLowLight: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isTriggered,
        enter = fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        // Frosted Glass Layer with opaque privacy shield protection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFA0B0F19),
                            Color(0xF5111827),
                            Color(0xFA090D16)
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {} // Intercept taps
                .testTag("privacy_blur_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // Subtle decorative cyber grid rings
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .clip(CircleShape)
                    .border(1.dp, ThreatCrimson.copy(alpha = 0.25f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, ThreatCrimson.copy(alpha = 0.4f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Warning Beacon Badge
                Surface(
                    shape = CircleShape,
                    color = ThreatCrimson.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, ThreatCrimson.copy(alpha = pulseAlpha)),
                    modifier = Modifier
                        .size(88.dp)
                        .padding(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Screen Blurred",
                            tint = ThreatCrimson,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "PRIVACY SHIELD ACTIVE",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = ThreatCrimson,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Screen content obscured to protect your data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Threat Details Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DETECTION TELEMETRY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFFBBF24)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (reason.isNotBlank()) reason else "Secondary person looking over your shoulder",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White
                        )

                        if (peekerAngle.isNotBlank() || peekerDistance.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = listOf(peekerAngle, peekerDistance)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Confidence: ${(confidence * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF38BDF8)
                                )
                            }

                            if (isLowLight) {
                                Text(
                                    text = "🌙 Low-Light Enhanced",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFBBF24)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Dismiss / Snooze Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF334155),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_blur_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I'm Safe • Dismiss Blur (4s)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Blur lifts automatically when the peeker turns away",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
