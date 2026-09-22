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

@Composable
fun RctStageScreen(
    viewModel: GameViewModel,
    onCompleteAllStages: () -> Unit
) {
    val stages = viewModel.rctStages
    val currentIndex = viewModel.currentRctStageIndex
    val stage = stages.getOrElse(currentIndex) { stages.last() }

    // Sub-states within current stage: 
    // 0 = Step Question & Educational Description
    // 1 = Interactive Slider Procedure Simulation
    var stageStep by remember { mutableStateOf(0) }

    // Quiz interaction states
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var showTryAgainButton by remember { mutableStateOf(false) }

    // Interactive procedure simulation slider (0.0 to 1.0)
    var procedureSliderProgress by remember { mutableStateOf(0f) }

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
            // Header: Stage Number & Progress
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
                        text = "RCT STAGE ${stage.stageNumber} OF ${stages.size}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color(0xFF00E5FF)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("1. Step Question", "2. Procedure Slider").forEachIndexed { idx, name ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (stageStep == idx) Color(0xFF00E5FF)
                                        else if (stageStep > idx) Color(0xFF2E7D32)
                                        else Color.White.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stageStep == idx) Color.Black else Color.White
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

            when (stageStep) {
                0 -> {
                    // STEP 1: QUESTION RELATED TO WHAT IS TO BE DONE IN THIS STEP
                    val q = stage.quizQuestion
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🩺", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Clinical Procedure Task Question",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF80DEEA)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stage.educationalDescription,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = q.questionText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Options
                            q.options.forEachIndexed { index, option ->
                                val isSelected = selectedOptionIndex == index
                                val isCorrect = index == q.correctAnswerIndex

                                val btnColor = when {
                                    isAnswerCorrect == true && isCorrect -> Color(0xFF2E7D32)
                                    isAnswerCorrect == false && isSelected -> Color(0xFFC62828)
                                    isSelected -> Color(0xFF0288D1)
                                    else -> Color(0xFF1E2838)
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (isAnswerCorrect != true) {
                                            selectedOptionIndex = index
                                            val correct = index == q.correctAnswerIndex
                                            isAnswerCorrect = correct
                                            viewModel.recordQuizAnswer(correct)
                                            if (!correct) {
                                                showTryAgainButton = true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = btnColor,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Incorrect Try Again Flow
                            if (showTryAgainButton && isAnswerCorrect == false) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "❌ Incorrect Answer. Please Try Again!",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = q.explanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        selectedOptionIndex = null
                                        isAnswerCorrect = null
                                        showTryAgainButton = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                                ) {
                                    Text("🔄 Try Question Again", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }

                            // Correct Answer Feedback
                            if (isAnswerCorrect == true) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "✅ Correct! Task Understood.",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = q.explanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { stageStep = 1 },
                        enabled = isAnswerCorrect == true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text(
                            text = "Proceed to Procedure Slider ⚙️",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }

                1 -> {
                    // STEP 2: SLIDER SYNCHRONIZED PROCEDURE SIMULATION
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
                                text = "⚙️ Slide to Perform Clinical Procedure",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stage.procedurePlaceholderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
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
                                    Text("${(procedureSliderProgress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFFD54F))
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
                                    text = if (procedureSliderProgress >= 0.99f) "✨ Procedure Step Completed 100%!" else "👉 Slide the slider up to 100% to complete this step",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (procedureSliderProgress >= 0.99f) Color(0xFF00E676) else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val hasMore = viewModel.advanceRctStage()
                            if (hasMore) {
                                stageStep = 0
                                selectedOptionIndex = null
                                isAnswerCorrect = null
                                showTryAgainButton = false
                                procedureSliderProgress = 0f
                            } else {
                                onCompleteAllStages()
                            }
                        },
                        enabled = procedureSliderProgress >= 0.99f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentIndex < stages.size - 1) Color(0xFF00E5FF) else Color(0xFF43A047)
                        )
                    ) {
                        Text(
                            text = if (currentIndex < stages.size - 1) "Advance to Stage ${stage.stageNumber + 1} ➡️" else "Finish RCT & View Results 🎉",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }
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
            val toothW = 160f
            val toothH = 180f
            val toothTop = cy - 70f

            drawRoundRect(
                color = Color(0xFFFFF9C4),
                topLeft = Offset(cx - toothW / 2f, toothTop),
                size = Size(toothW, toothH),
                cornerRadius = CornerRadius(24f, 24f)
            )
            drawRoundRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(cx - toothW / 2f - 8f, toothTop - 10f),
                size = Size(toothW + 16f, 50f),
                cornerRadius = CornerRadius(20f, 20f)
            )

            val pulpW = 70f
            val pulpH = 60f
            drawRoundRect(
                color = Color(0xFFD32F2F),
                topLeft = Offset(cx - pulpW / 2f, toothTop + 40f),
                size = Size(pulpW, pulpH),
                cornerRadius = CornerRadius(16f, 16f)
            )

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

            if (progress > 0.15f) {
                drawCircle(Color(0xFF80DEEA).copy(alpha = 0.8f), radius = 4f, center = Offset(cx - 16f, burY + 8f))
                drawCircle(Color(0xFF80DEEA).copy(alpha = 0.7f), radius = 3.5f, center = Offset(cx + 18f, burY + 10f))
            }

            val cutDepth = progress * 35f
            drawRect(
                color = Color(0xFF0A0F1D),
                topLeft = Offset(cx - 16f, toothTop - 12f),
                size = Size(32f, cutDepth + 12f)
            )
        }

        2 -> {
            val ealW = 90f
            val ealH = 130f
            val ealLeft = 30f
            val ealTop = cy - 65f

            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(ealLeft, ealTop),
                size = Size(ealW, ealH),
                cornerRadius = CornerRadius(12f, 12f)
            )
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

            if (isAtApex) {
                drawCircle(Color(0xFF00E676), radius = 8f, center = Offset(canalX, fileTipY))
            }
        }

        3 -> {
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

            val canalWidth = 14f + progress * 10f
            val lumenPath = Path().apply {
                moveTo(canalX - canalWidth, canalTop)
                lineTo(canalX + canalWidth, canalTop)
                lineTo(canalX + 4f, canalTop + canalH - 12f)
                lineTo(canalX - 4f, canalTop + canalH - 12f)
                close()
            }
            drawPath(lumenPath, color = Color(0xFFE0F7FA))

            val fileTipY = canalTop + (canalH - 20f) * progress
            drawLine(
                color = Color(0xFFFFD54F),
                start = Offset(canalX - 4f, canalTop - 25f),
                end = Offset(canalX, fileTipY),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            val needleX = canalX + 8f
            val needleY = canalTop + (canalH - 45f) * progress
            drawLine(
                color = Color(0xFFCFD8DC),
                start = Offset(needleX + 18f, canalTop - 40f),
                end = Offset(needleX, needleY),
                strokeWidth = 3f
            )
            drawCircle(Color(0xFF4DD0E1).copy(alpha = 0.8f), radius = 5f, center = Offset(needleX - 6f, needleY + 6f))
        }

        4 -> {
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

            val coneTipY = canalTop + (canalH - 14f)
            val gpFillH = (canalH - 14f) * progress

            val gpPath = Path().apply {
                moveTo(canalX - 16f, canalTop + canalH - 14f - gpFillH)
                lineTo(canalX + 16f, canalTop + canalH - 14f - gpFillH)
                lineTo(canalX + 4f, coneTipY)
                lineTo(canalX - 4f, coneTipY)
                close()
            }
            drawPath(gpPath, color = Color(0xFFF48FB1))

            drawCircle(Color(0xFFFFB300), radius = 5f, center = Offset(canalX, coneTipY))

            val pluggerY = canalTop + canalH - 14f - gpFillH
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF90A4AE), Color(0xFF37474F))
                ),
                topLeft = Offset(canalX - 5f, pluggerY - 45f),
                size = Size(10f, 45f)
            )
            drawCircle(Color(0xFFFF5722), radius = 4f, center = Offset(canalX, pluggerY))
        }

        5 -> {
            val toothW = 150f
            val toothH = 150f
            val toothTop = cy - 40f

            drawRoundRect(
                color = Color(0xFFFFF59D),
                topLeft = Offset(cx - toothW / 2f, toothTop),
                size = Size(toothW, toothH),
                cornerRadius = CornerRadius(20f, 20f)
            )
            drawRoundRect(
                color = Color(0xFF00ACC1),
                topLeft = Offset(cx - 25f, toothTop - 15f),
                size = Size(50f, 40f),
                cornerRadius = CornerRadius(8f, 8f)
            )

            val crownY = toothTop - 80f + progress * 65f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFE0F7FA), Color(0xFFB2EBF2))
                ),
                topLeft = Offset(cx - toothW / 2f - 4f, crownY),
                size = Size(toothW + 8f, 60f),
                cornerRadius = CornerRadius(18f, 18f)
            )

            if (progress > 0.85f) {
                drawCircle(Color.White, radius = 6f, center = Offset(cx + 40f, crownY + 15f))
                drawLine(Color.White, Offset(cx + 30f, crownY + 15f), Offset(cx + 50f, crownY + 15f), strokeWidth = 2f)
                drawLine(Color.White, Offset(cx + 40f, crownY + 5f), Offset(cx + 40f, crownY + 25f), strokeWidth = 2f)
            }
        }
    }
}
