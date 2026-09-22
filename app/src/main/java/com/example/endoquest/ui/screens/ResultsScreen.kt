package com.example.endoquest.ui.screens

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endoquest.ui.viewmodel.GameViewModel
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ResultsScreen(
    viewModel: GameViewModel,
    onPlayAgain: () -> Unit
) {
    val progress = viewModel.progress
    val accuracyPercent = (progress.rctQuizAccuracy * 100).toInt()

    val masteryTier = when {
        accuracyPercent >= 85 -> "🏆 Board Certified Endodontist"
        accuracyPercent >= 65 -> "🥈 Senior Clinical Resident"
        else -> "🥉 Junior Dental Practitioner"
    }

    val tierColor = when {
        accuracyPercent >= 85 -> Color(0xFFFFD700)
        accuracyPercent >= 65 -> Color(0xFF00E5FF)
        else -> Color(0xFF81C784)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confettiAnim")
    val confettiProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiFloat"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color(0xFF0A0F1D)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Celebratory Confetti background
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCelebratoryConfetti(confettiProgress, size)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header & Trophy
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "🎉🦷🏆", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Treatment Completed!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Alex's tooth is fully saved and restored!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF80DEEA),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Clinical Mastery Tier Badge Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ENDODONTIC CLINICAL RATING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = masteryTier,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = tierColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"Outstanding clinical execution! You successfully completed all ${viewModel.rctStages.size} biological stages of modern root canal therapy!\" — Dr. Smile",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detailed Results Metrics Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ResultMetricRow("Total Money Collected", "$${progress.moneyCollected} (${progress.moneyCollected / 25} gold coins)", Color(0xFFFFD700))
                        ResultMetricRow("RCT Stages Mastered", "${progress.rctStagesCompleted} / ${viewModel.rctStages.size} Stages Completed", Color(0xFF00E5FF))
                        ResultMetricRow("Clinical Quiz Accuracy", "$accuracyPercent% (${progress.correctQuizAnswers} of ${progress.totalQuizAttempts.coerceAtLeast(1)} correct)", tierColor)
                        ResultMetricRow("Tooth Prognosis", "Excellent (Hermetic Apical Seal)", Color(0xFF4CAF50))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Play Again Button
                Button(
                    onClick = {
                        viewModel.resetGame()
                        onPlayAgain()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text(
                        text = "Play Again 🔄",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ResultMetricRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

/**
 * Draws floating celebratory confetti and sparkles on the Results Screen.
 */
fun DrawScope.drawCelebratoryConfetti(progress: Float, size: Size) {
    val confettiColors = listOf(
        Color(0xFFFFD700),
        Color(0xFF00E5FF),
        Color(0xFFFF4081),
        Color(0xFF76FF03),
        Color(0xFFE040FB)
    )

    val random = Random(42)
    for (i in 0 until 40) {
        val startX = random.nextFloat() * size.width
        val speed = 0.6f + random.nextFloat() * 0.8f
        val y = ((progress * speed + random.nextFloat()) % 1f) * size.height
        val color = confettiColors[i % confettiColors.size]
        val cSize = 6f + random.nextFloat() * 6f

        drawCircle(
            color = color.copy(alpha = 0.75f),
            radius = cSize / 2f,
            center = Offset(startX + sin(y * 0.05f) * 15f, y)
        )
    }
}
