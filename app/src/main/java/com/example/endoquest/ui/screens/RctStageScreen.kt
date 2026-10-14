package com.example.endoquest.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endoquest.ui.viewmodel.GameViewModel

/**
 * Clinical Root Canal Treatment (RCT) Simulation Screen.
 * Strictly ZERO quiz questions: focuses 100% on hands-on clinical instrumentation
 * and chemical debridement simulation via interactive slider controls.
 */
@Composable
fun RctStageScreen(
    viewModel: GameViewModel,
    onCompleteAllStages: () -> Unit
) {
    val stages = viewModel.rctStages
    val currentIndex = viewModel.currentRctStageIndex
    val stage = stages.getOrElse(currentIndex) { stages.last() }

    // Interactive procedure simulation slider (0.0 to 1.0)
    var procedureSliderProgress by remember(currentIndex) { mutableStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color(0xFF0D131F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Stage Number & Clinical Progress
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLINICAL RCT STAGE ${stage.stageNumber} OF ${stages.size}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color(0xFF00E5FF)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        stages.forEachIndexed { idx, st ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            idx == currentIndex -> Color(0xFF00E5FF)
                                            idx < currentIndex -> Color(0xFF2E7D32)
                                            else -> Color.White.copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Stage ${st.stageNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (idx == currentIndex) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stage.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clinical Instructions Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🩺", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clinical Procedure Protocol",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF80DEEA)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stage.educationalDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // INTERACTIVE SLIDER-DRIVEN PROCEDURE SIMULATION
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚙️ Interactive Clinical Simulation",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stage.procedurePlaceholderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Canvas Animation Synchronized with Slider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0A0F1D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRctProcedureSimulation(
                                stageNumber = stage.stageNumber,
                                progress = procedureSliderProgress,
                                size = size
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Smooth Progress Slider (0% to 100%)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Procedure Progress", style = MaterialTheme.typography.labelMedium, color = Color.White)
                            Text(
                                "${(procedureSliderProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (procedureSliderProgress >= 0.99f) Color(0xFF00E676) else Color(0xFFFFD54F)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = procedureSliderProgress,
                            onValueChange = { procedureSliderProgress = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00E5FF),
                                activeTrackColor = Color(0xFF00E5FF),
                                inactiveTrackColor = Color(0xFF1E2838)
                            )
                        )

                        Text(
                            text = if (procedureSliderProgress >= 0.99f)
                                "✨ Step Completed 100%! Ready to advance."
                            else
                                "👉 Slide to 100% to perform the clinical step",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (procedureSliderProgress >= 0.99f) Color(0xFF00E676) else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Advance Button
            Button(
                onClick = {
                    val hasMore = viewModel.advanceRctStage()
                    if (!hasMore) {
                        onCompleteAllStages()
                    }
                },
                enabled = procedureSliderProgress >= 0.99f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentIndex < stages.size - 1) Color(0xFF00E5FF) else Color(0xFF43A047),
                    disabledContainerColor = Color(0xFF1E2838)
                )
            ) {
                Text(
                    text = if (currentIndex < stages.size - 1)
                        "Advance to Stage ${stage.stageNumber + 1} ➡️"
                    else
                        "Complete Clinical Procedure & View Results 🎉",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (procedureSliderProgress >= 0.99f) Color.Black else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Draws custom interactive Canvas simulations synchronized with the slider progress for each clinical RCT stage.
 */
fun DrawScope.drawRctProcedureSimulation(
    stageNumber: Int,
    progress: Float,
    size: Size
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f

    when (stageNumber) {
        1 -> {
            // Stage 1: The Pulp Chamber (Tissue Dissolution & Smear Layer Removal with NaOCl and 17% EDTA)
            val toothW = 160f
            val toothH = 180f
            val toothTop = cy - 70f

            // Dentin crown
            drawRoundRect(
                color = Color(0xFFFFF9C4),
                topLeft = Offset(cx - toothW / 2f, toothTop),
                size = Size(toothW, toothH),
                cornerRadius = CornerRadius(24f, 24f)
            )
            // Enamel cap
            drawRoundRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(cx - toothW / 2f - 8f, toothTop - 10f),
                size = Size(toothW + 16f, 50f),
                cornerRadius = CornerRadius(20f, 20f)
            )

            // Necrotic pulp tissue being dissolved by NaOCl
            val pulpW = 70f
            val pulpH = 60f
            val dissolvedAlpha = (1f - progress * 0.85f).coerceIn(0.15f, 1f)
            drawRoundRect(
                color = Color(0xFFD32F2F).copy(alpha = dissolvedAlpha),
                topLeft = Offset(cx - pulpW / 2f, toothTop + 40f),
                size = Size(pulpW, pulpH),
                cornerRadius = CornerRadius(16f, 16f)
            )

            // High-speed bur access & irrigation cannula
            val burY = toothTop - 50f + progress * 75f
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFCFD8DC), Color(0xFF90A4AE))
                ),
                topLeft = Offset(cx - 24f, burY - 45f),
                size = Size(48f, 35f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRect(Color(0xFF78909C), Offset(cx - 3f, burY - 12f), Size(6f, 26f))
            drawCircle(Color(0xFFFFD54F), radius = 6f, center = Offset(cx, burY + 14f))

            // Effervescent NaOCl saponification & bubbling
            if (progress > 0.15f) {
                drawCircle(Color(0xFF80DEEA).copy(alpha = 0.8f), radius = 4f, center = Offset(cx - 16f, burY + 8f))
                drawCircle(Color(0xFF80DEEA).copy(alpha = 0.7f), radius = 3.5f, center = Offset(cx + 18f, burY + 10f))
                drawCircle(Color(0xFFB2EBF2).copy(alpha = 0.9f), radius = 5f, center = Offset(cx + 4f, burY + 22f))
            }

            // Access cavity chamber
            val cutDepth = progress * 35f
            drawRect(
                color = Color(0xFF0A0F1D),
                topLeft = Offset(cx - 16f, toothTop - 12f),
                size = Size(32f, cutDepth + 12f)
            )
        }

        2 -> {
            // Stage 2: The Apical Danger Zone (Side-Vented Needle & Working Length Apex Locator)
            val ealW = 90f
            val ealH = 130f
            val ealLeft = 30f
            val ealTop = cy - 65f

            // Apex Locator Housing
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(ealLeft, ealTop),
                size = Size(ealW, ealH),
                cornerRadius = CornerRadius(12f, 12f)
            )
            // Display screen
            drawRoundRect(
                color = Color(0xFF004D40),
                topLeft = Offset(ealLeft + 10f, ealTop + 12f),
                size = Size(ealW - 20f, 65f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            val isAtApex = progress > 0.82f
            val barCount = 5
            for (b in 0 until barCount) {
                val active = (progress * barCount).toInt() >= b
                val barColor = if (isAtApex) Color(0xFF00E676) else if (active) Color(0xFFFFD600) else Color(0xFF00251A)
                drawRect(
                    color = barColor,
                    topLeft = Offset(ealLeft + 16f + b * 12f, ealTop + 45f),
                    size = Size(8f, 18f)
                )
            }

            val canalX = cx + 55f
            val canalTop = cy - 80f
            val canalH = 160f

            val rootPath = Path().apply {
                moveTo(canalX - 35f, canalTop)
                lineTo(canalX + 35f, canalTop)
                lineTo(canalX + 8f, canalTop + canalH)
                lineTo(canalX - 8f, canalTop + canalH)
                close()
            }
            drawPath(rootPath, color = Color(0xFFFFF9C4))

            val lumenPath = Path().apply {
                moveTo(canalX - 10f, canalTop)
                lineTo(canalX + 10f, canalTop)
                lineTo(canalX + 3f, canalTop + canalH - 10f)
                lineTo(canalX - 3f, canalTop + canalH - 10f)
                close()
            }
            drawPath(lumenPath, color = Color(0xFFD32F2F))

            // Side-vented needle placed 2-3mm short of apex
            val fileTipY = canalTop + (canalH - 12f) * progress
            drawRoundRect(
                color = Color(0xFFFFD600),
                topLeft = Offset(canalX - 10f, fileTipY - 70f),
                size = Size(20f, 28f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawCircle(Color(0xFF00E5FF), radius = 6f, center = Offset(canalX, fileTipY - 42f))
            drawLine(
                color = Color(0xFFECEFF1),
                start = Offset(canalX, fileTipY - 42f),
                end = Offset(canalX, fileTipY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Side vent reflux jets
            if (progress > 0.4f) {
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(canalX - 2f, fileTipY - 6f),
                    end = Offset(canalX - 10f, fileTipY - 14f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(canalX + 2f, fileTipY - 6f),
                    end = Offset(canalX + 10f, fileTipY - 14f),
                    strokeWidth = 2f
                )
            }

            if (isAtApex) {
                drawCircle(Color(0xFF00E676), radius = 8f, center = Offset(canalX, fileTipY))
            }
        }

        3 -> {
            // Stage 3: The Inter-Appointment Vault (Calcium Hydroxide Intracanal Medicament Placement)
            val canalX = cx
            val canalTop = cy - 80f
            val canalH = 160f

            val rootPath = Path().apply {
                moveTo(canalX - 50f, canalTop)
                lineTo(canalX + 50f, canalTop)
                lineTo(canalX + 12f, canalTop + canalH)
                lineTo(canalX - 12f, canalTop + canalH)
                close()
            }
            drawPath(rootPath, color = Color(0xFFFFF9C4))

            // Clean, shaped canal lumen
            val canalWidth = 14f + progress * 6f
            val lumenPath = Path().apply {
                moveTo(canalX - canalWidth, canalTop)
                lineTo(canalX + canalWidth, canalTop)
                lineTo(canalX + 4f, canalTop + canalH - 12f)
                lineTo(canalX - 4f, canalTop + canalH - 12f)
                close()
            }
            drawPath(lumenPath, color = Color(0xFF10192A))

            // Calcium Hydroxide paste dense filling from apex upward
            val fillHeight = (canalH - 12f) * progress
            val fillTopY = canalTop + canalH - 12f - fillHeight
            if (progress > 0.05f) {
                val pastePath = Path().apply {
                    moveTo(canalX - (canalWidth * progress), fillTopY)
                    lineTo(canalX + (canalWidth * progress), fillTopY)
                    lineTo(canalX + 4f, canalTop + canalH - 12f)
                    lineTo(canalX - 4f, canalTop + canalH - 12f)
                    close()
                }
                drawPath(pastePath, color = Color(0xFFFAFAFA))
            }

            // Lentulo spiral / delivery syringe tip
            val syringeTipY = (fillTopY - 10f).coerceAtLeast(canalTop)
            drawLine(
                color = Color(0xFFFFD54F),
                start = Offset(canalX, canalTop - 30f),
                end = Offset(canalX, syringeTipY),
                strokeWidth = 4f
            )
            drawCircle(Color(0xFF00E5FF), radius = 5f, center = Offset(canalX, syringeTipY))
        }
    }
}
