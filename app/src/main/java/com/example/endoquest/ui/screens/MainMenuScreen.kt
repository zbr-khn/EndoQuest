package com.example.endoquest.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endoquest.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    onInstructions: () -> Unit,
    onAbout: () -> Unit
) {
    // Infinite transition for continuous lively animations
    val infiniteTransition = rememberInfiniteTransition(label = "mainMenuAnimations")

    // Pulsing hero button scale
    val playPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playPulse"
    )

    // Floating animation ticker (0..1f)
    val floatTicker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floatTicker"
    )

    // Glow aura pulse for badge
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A111E)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dynamic animated Canvas background with floating dental sparkles & tooth motifs
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Deep arcade cyber-teal gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF04131F),
                            Color(0xFF0A2234),
                            Color(0xFF0D2D42),
                            Color(0xFF061826)
                        )
                    ),
                    size = size
                )

                // Ambient glowing radial aura behind hero
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.18f * auraAlpha),
                            Color(0xFF00838F).copy(alpha = 0.08f * auraAlpha),
                            Color.Transparent
                        ),
                        center = Offset(w / 2f, h * 0.30f),
                        radius = w * 0.70f
                    ),
                    center = Offset(w / 2f, h * 0.30f),
                    radius = w * 0.70f
                )

                // 12 Floating dental sparkle & cross particles
                val particles = listOf(
                    Triple(0.12f, 0.18f, 14f),
                    Triple(0.85f, 0.22f, 18f),
                    Triple(0.25f, 0.42f, 10f),
                    Triple(0.88f, 0.50f, 12f),
                    Triple(0.15f, 0.70f, 16f),
                    Triple(0.82f, 0.78f, 14f),
                    Triple(0.08f, 0.90f, 12f),
                    Triple(0.92f, 0.92f, 15f),
                    Triple(0.50f, 0.10f, 11f),
                    Triple(0.35f, 0.85f, 13f),
                    Triple(0.68f, 0.88f, 10f),
                    Triple(0.40f, 0.32f, 9f)
                )

                for ((idx, p) in particles.withIndex()) {
                    val (baseX, baseY, pSize) = p
                    val cycleOffset = (idx * 0.15f) % 1f
                    val currentProgress = (floatTicker + cycleOffset) % 1f

                    val px = w * (baseX + 0.04f * sin((currentProgress + idx) * 6.28f))
                    val py = (h * (baseY - currentProgress * 0.4f)).let { if (it < 0) it + h else it }

                    val sparkleAlpha = (0.25f + 0.65f * sin((currentProgress + idx) * 3.14f)).coerceIn(0.1f, 0.9f)
                    val sparkleColor = if (idx % 2 == 0) Color(0xFF00E5FF).copy(alpha = sparkleAlpha)
                    else Color(0xFFFFD54F).copy(alpha = sparkleAlpha)

                    // Draw 4-point sparkling star ✦
                    val arm = pSize * (0.8f + 0.3f * sin(currentProgress * 6.28f))
                    val path = Path().apply {
                        moveTo(px, py - arm)
                        lineTo(px + arm * 0.25f, py - arm * 0.25f)
                        lineTo(px + arm, py)
                        lineTo(px + arm * 0.25f, py + arm * 0.25f)
                        lineTo(px, py + arm)
                        lineTo(px - arm * 0.25f, py + arm * 0.25f)
                        lineTo(px - arm, py)
                        lineTo(px - arm * 0.25f, py - arm * 0.25f)
                        close()
                    }
                    drawPath(path, sparkleColor)
                }
            }

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Status Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF132236).copy(alpha = 0.90f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🏥 CLINICAL EDITION", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00E5FF))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF132236).copy(alpha = 0.90f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "⭐ $5,000 GOAL", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD54F))
                        }
                    }
                }

                // Center Hero: Animated Mascot Icon & Layered Arcade Title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    // Pulsing Hero Icon Frame
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(126.dp)
                    ) {
                        // Outer pulsating aura ring
                        Box(
                            modifier = Modifier
                                .size(124.dp)
                                .clip(RoundedCornerShape(40.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF).copy(alpha = auraAlpha * 0.7f),
                                            Color(0xFFFFD54F).copy(alpha = auraAlpha * 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Main badge surface
                        Surface(
                            shape = RoundedCornerShape(32.dp),
                            color = Color(0xFF132438),
                            shadowElevation = 18.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                3.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF00E5FF),
                                        Color(0xFFFFD54F),
                                        Color(0xFF00838F),
                                        Color(0xFF00E5FF)
                                    )
                                )
                            ),
                            modifier = Modifier.size(105.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                                    contentDescription = "App Icon",
                                    modifier = Modifier
                                        .size(92.dp)
                                        .clip(RoundedCornerShape(26.dp))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Title: "EndoQuest" with glowing arcade flair
                    Text(
                        text = "ENDOQUEST",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.5.sp
                        ),
                        color = Color(0xFF00E5FF)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Gold Arcade Subtitle Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFA000),
                        shadowElevation = 8.dp
                    ) {
                        Text(
                            text = "★ THE ROOT CANAL RUN ★",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = Color(0xFF1A1100),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-Round Adventure Flow Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            "🏃 R1: Runner",
                            "🧩 R2: Maze",
                            "🧟 R3: Brawl",
                            "🦷 R4: RCT"
                        ).forEach { stage ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF18283E).copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF37474F))
                            ) {
                                Text(
                                    text = stage,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB0BEC5),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lore Hook Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF142234).copy(alpha = 0.85f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263238)),
                        modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                        Text(
                            text = "⚡ Irreversible Pulpitis strikes! Sprint the 3D highway, ace Dental Police inspections, dodge rotten roots, and save the molar!",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center,
                            color = Color(0xFFCFD8DC),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                // Bottom Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pulsing Giant Play CTA Button
                    Button(
                        onClick = onStartGame,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(64.dp)
                            .scale(playPulseScale),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 12.dp,
                            pressedElevation = 6.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "PLAY STORY & RUNNER",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color(0xFF00363A)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "🏃‍♂️💨", fontSize = 22.sp)
                        }
                    }

                    // Secondary Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onInstructions,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF101C2C).copy(alpha = 0.85f),
                                contentColor = Color(0xFF81D4FA)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0288D1).copy(alpha = 0.6f))
                        ) {
                            Text(text = "🎮 How to Play", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onAbout,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF101C2C).copy(alpha = 0.85f),
                                contentColor = Color(0xFFA5D6A7)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF43A047).copy(alpha = 0.6f))
                        ) {
                            Text(text = "📚 Clinical Guide", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
