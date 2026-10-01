package com.example.endoquest.ui.screens

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endoquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ZombieScreen(
    viewModel: GameViewModel,
    onProceedToClinic: () -> Unit,
    onRoundOver: () -> Unit
) {
    // Initialize zombie round on entry
    LaunchedEffect(Unit) {
        viewModel.initZombieRound()
    }

    // 15-second countdown timer for active zombie question
    LaunchedEffect(viewModel.currentZombieIndex, viewModel.isRound3Complete) {
        if (!viewModel.isRound3Complete && viewModel.currentZombieQuestion != null) {
            while (!viewModel.isRound3Complete && viewModel.zombieTimerSeconds > 0) {
                delay(1000)
                viewModel.tickZombieTimer(onRoundOver = onRoundOver)
            }
        }
    }

    // Zombie idle breathing and microbial aura animation
    val infiniteTransition = rememberInfiniteTransition(label = "zombieAnim")
    val idleBob by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zombieBob"
    )
    val auraSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auraSpin"
    )

    val zombieNames = listOf(
        "Dr. Decay (Plaque Enamel Biofilm)",
        "Tartar Titan (Calcified Canal Threat)",
        "E. faecalis Overlord (Persistent Periapical Boss)"
    )
    val currentZombieTitle = zombieNames.getOrElse(viewModel.currentZombieIndex) { "Microbial Mutant" }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color(0xFF090D1A)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP BAR: Round 3 Progress and Goal Header
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Round 3: Zombie Dental Challenge",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF81C784)
                                )
                                Text(
                                    text = "🪙 Funds: $${viewModel.progress.moneyCollected}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = Color(0xFFFFD700)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Final Clinic Goal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF81C784)
                                )
                                Text(
                                    text = "$5,000 EXACT",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF00E5FF)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleAudioMute() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Text(
                                    text = if (viewModel.isMuted) "🔇" else "🔊",
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Wave progress indicator
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1B2A1C),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Wave ${viewModel.currentZombieIndex + 1} / 3: +$500 per victory",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFD54F)
                                )
                                Text(
                                    text = "Remaining to Clinic: $${(5000 - viewModel.progress.moneyCollected).coerceAtLeast(0)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ZOMBIE CANVAS: Stylized Cartoon Dentist Zombie
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF101F18), Color(0xFF0B1410))
                            )
                        )
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawStylizedDentalZombie(
                            center = Offset(size.width / 2f, size.height * 0.58f + idleBob),
                            zombieIndex = viewModel.currentZombieIndex,
                            auraAngle = auraSpin
                        )
                    }

                    // Floating Zombie Name Tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "🧟‍♂️ $currentZombieTitle",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFA5D6A7),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // QUESTION & OPTIONS CARD WITH 15-SECOND COUNTDOWN TIMER
                val currentQ = viewModel.currentZombieQuestion
                val shuffledOptions = viewModel.currentZombieShuffledOptions
                val timerSec = viewModel.zombieTimerSeconds

                if (currentQ != null) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Header: Wave Number and Timer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "MICROBIAL COMBAT CHALLENGE",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color(0xFF69F0AE)
                                    )
                                    Text(
                                        text = "Neutralize with clinical accuracy! (+$500)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (timerSec <= 5) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                ) {
                                    Text(
                                        text = "⏱️ ${timerSec}s",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Timer Progress Bar
                            LinearProgressIndicator(
                                progress = { (timerSec.toFloat() / 15f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (timerSec <= 5) Color(0xFFFF1744) else Color(0xFF00E5FF),
                                trackColor = Color(0xFF1E2A38)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Question Card
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2838)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Pathology: ${currentQ.category} (${currentQ.difficulty})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF80DEEA)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentQ.questionText,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Option Buttons
                            val prefixes = listOf("A. ", "B. ", "C. ", "D. ")
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                shuffledOptions.forEachIndexed { idx, optText ->
                                    Button(
                                        onClick = {
                                            viewModel.submitZombieAnswer(
                                                optionIndex = idx,
                                                onRoundOver = onRoundOver,
                                                onNextZombie = {},
                                                onAllCleared = {}
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF243044),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = prefixes.getOrElse(idx) { "" } + optText,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ROUND 3 COMPLETE CELEBRATION MODAL (Target $5,000 Reached)
            if (viewModel.isRound3Complete) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.88f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    ) {
                        Column(
                            modifier = Modifier.padding(26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎉👨‍⚕️🦷", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "ALL MONSTERS DEFEATED!",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFFFD700),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Dental Biofilms Eradicated!\nFull Treatment Budget Secured!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF80DEEA),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2838)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Rounds 1 & 2 Carried Funds:", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("$3,500", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Round 3 Zombie Rewards (3x $500):", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("+$1,500", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF00E5FF))
                                    }
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total Clinical Budget:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Text("$5,000 EXACT", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color(0xFFFFD700))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "\"Magnificent work, Alex! You have conquered all three challenges and collected all \$5,000. Enter Dr. Smile's clinic to start your RCT!\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onProceedToClinic,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text("Enter Dental Clinic 🏥", fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws a stylized cartoon zombie dentist representing dental decay and biofilms.
 * Friendly, colorful, humorous character with green skin, comical eyes, and doctor attire.
 */
fun DrawScope.drawStylizedDentalZombie(
    center: Offset,
    zombieIndex: Int,
    auraAngle: Float
) {
    val zombieSkin = when (zombieIndex) {
        0 -> Color(0xFF81C784) // Dr. Decay: Minty Green
        1 -> Color(0xFFAED581) // Tartar Titan: Lime-ish Calculus Green
        else -> Color(0xFF4CAF50) // E. faecalis: Toxic Biofilm Green
    }
    val scrubColor = when (zombieIndex) {
        0 -> Color(0xFF5C6BC0) // Indigo scrubs
        1 -> Color(0xFF7E57C2) // Purple scrubs
        else -> Color(0xFF37474F) // Dark grey scrubs
    }
    val s = 1.1f

    // 1. FLOATING MICROBIAL AURA PARTICLES
    val rad = Math.toRadians(auraAngle.toDouble())
    for (i in 0 until 6) {
        val angleOffset = i * (Math.PI / 3.0) + rad
        val dist = 75f * s
        val px = center.x + (cos(angleOffset) * dist).toFloat()
        val py = (center.y - 30f * s) + (sin(angleOffset) * dist * 0.4f).toFloat()
        drawCircle(
            color = Color(0xFF76FF03).copy(alpha = 0.7f),
            radius = 4f * s,
            center = Offset(px, py)
        )
    }

    // 2. HEAD & FACE
    val headCenter = Offset(center.x, center.y - 45f * s)
    val headRadius = 32f * s

    // Messy Zombie Hair
    val hairPath = Path().apply {
        moveTo(headCenter.x - 32f * s, headCenter.y - 20f * s)
        lineTo(headCenter.x - 22f * s, headCenter.y - 42f * s)
        lineTo(headCenter.x - 10f * s, headCenter.y - 30f * s)
        lineTo(headCenter.x, headCenter.y - 46f * s)
        lineTo(headCenter.x + 12f * s, headCenter.y - 32f * s)
        lineTo(headCenter.x + 24f * s, headCenter.y - 44f * s)
        lineTo(headCenter.x + 34f * s, headCenter.y - 20f * s)
        close()
    }
    drawPath(hairPath, color = Color(0xFF424242))

    // Head Base
    drawCircle(color = zombieSkin, radius = headRadius, center = headCenter)

    // Goofy Asymmetrical Eyes
    // Left Eye (Big cartoon eye)
    drawCircle(color = Color.White, radius = 9f * s, center = Offset(headCenter.x - 11f * s, headCenter.y - 8f * s))
    drawCircle(color = Color(0xFFD32F2F), radius = 4f * s, center = Offset(headCenter.x - 10f * s, headCenter.y - 8f * s))
    drawCircle(color = Color.Black, radius = 2f * s, center = Offset(headCenter.x - 10f * s, headCenter.y - 8f * s))

    // Right Eye (Small spiral pupil)
    drawCircle(color = Color.White, radius = 7f * s, center = Offset(headCenter.x + 11f * s, headCenter.y - 7f * s))
    drawCircle(color = Color(0xFFFFEB3B), radius = 3.5f * s, center = Offset(headCenter.x + 12f * s, headCenter.y - 7f * s))
    drawCircle(color = Color.Black, radius = 1.8f * s, center = Offset(headCenter.x + 12f * s, headCenter.y - 7f * s))

    // Crazy Eyebrows
    drawLine(Color.Black, Offset(headCenter.x - 18f * s, headCenter.y - 18f * s), Offset(headCenter.x - 4f * s, headCenter.y - 15f * s), strokeWidth = 3f * s, cap = StrokeCap.Round)
    drawLine(Color.Black, Offset(headCenter.x + 4f * s, headCenter.y - 14f * s), Offset(headCenter.x + 18f * s, headCenter.y - 20f * s), strokeWidth = 3f * s, cap = StrokeCap.Round)

    // SILLY MOUTH WITH PROTRUDING CROOKED TEETH
    val mouthCenter = Offset(headCenter.x, headCenter.y + 14f * s)
    drawRoundRect(
        color = Color(0xFF263238),
        topLeft = Offset(mouthCenter.x - 16f * s, mouthCenter.y - 6f * s),
        size = Size(32f * s, 14f * s),
        cornerRadius = CornerRadius(6f * s, 6f * s)
    )
    // Left Tooth (White)
    drawRect(Color.White, Offset(mouthCenter.x - 10f * s, mouthCenter.y - 6f * s), Size(6f * s, 8f * s))
    // Center Tooth (Gold/Cavity)
    drawRect(Color(0xFFFFD700), Offset(mouthCenter.x - 2f * s, mouthCenter.y - 6f * s), Size(6f * s, 9f * s))
    // Bottom Protruding Fang
    drawRect(Color.White, Offset(mouthCenter.x + 4f * s, mouthCenter.y), Size(5f * s, 8f * s))

    // 3. TORSO (DOCTOR SCRUBS)
    val torsoTop = headCenter.y + headRadius - 4f * s
    drawRoundRect(
        color = scrubColor,
        topLeft = Offset(center.x - 26f * s, torsoTop),
        size = Size(52f * s, 48f * s),
        cornerRadius = CornerRadius(8f * s, 8f * s)
    )

    // Scrubs V-neck
    val vNeck = Path().apply {
        moveTo(center.x - 10f * s, torsoTop)
        lineTo(center.x, torsoTop + 14f * s)
        lineTo(center.x + 10f * s, torsoTop)
    }
    drawPath(vNeck, color = zombieSkin)

    // Dangling Stethoscope
    drawPath(
        Path().apply {
            moveTo(headCenter.x - 12f * s, torsoTop + 4f * s)
            quadraticTo(center.x, torsoTop + 24f * s, center.x + 12f * s, torsoTop + 4f * s)
        },
        color = Color(0xFFECEFF1),
        style = Stroke(width = 3f * s, cap = StrokeCap.Round)
    )
    drawCircle(Color(0xFFFFD700), radius = 5f * s, center = Offset(center.x, torsoTop + 24f * s))

    // Dr. Decay Name Badge
    drawRect(Color.White, Offset(center.x - 20f * s, torsoTop + 14f * s), Size(12f * s, 16f * s))
    drawRect(Color(0xFFD32F2F), Offset(center.x - 18f * s, torsoTop + 16f * s), Size(8f * s, 4f * s))

    // 4. OUTSTRETCHED CARTOON ZOMBIE ARMS
    // Left Arm
    drawLine(
        color = scrubColor,
        start = Offset(center.x - 26f * s, torsoTop + 10f * s),
        end = Offset(center.x - 48f * s, torsoTop + 24f * s),
        strokeWidth = 9f * s,
        cap = StrokeCap.Round
    )
    drawCircle(zombieSkin, radius = 6f * s, center = Offset(center.x - 48f * s, torsoTop + 24f * s))

    // Right Arm (Reaching forward)
    drawLine(
        color = scrubColor,
        start = Offset(center.x + 26f * s, torsoTop + 10f * s),
        end = Offset(center.x + 50f * s, torsoTop + 18f * s),
        strokeWidth = 9f * s,
        cap = StrokeCap.Round
    )
    drawCircle(zombieSkin, radius = 6f * s, center = Offset(center.x + 50f * s, torsoTop + 18f * s))
}
