package com.example.endoquest.ui.screens

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endoquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun MazeScreen(
    viewModel: GameViewModel,
    onProceedToZombie: () -> Unit,
    onRoundOver: () -> Unit
) {
    // 9x9 Dental Enamel Maze Grid
    // 0 = Open Corridor, 1 = Enamel Wall, 2 = Checkpoint 1, 3 = Checkpoint 2, 9 = Exit Portal
    val mazeGrid = remember {
        arrayOf(
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 1),
            intArrayOf(1, 1, 1, 0, 1, 0, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 1, 0, 1),
            intArrayOf(1, 0, 1, 1, 1, 0, 1, 0, 1),
            intArrayOf(1, 2, 0, 0, 1, 0, 0, 0, 1), // Gate 1 at (1, 5)
            intArrayOf(1, 1, 1, 0, 1, 1, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 0, 3, 0, 9, 1), // Gate 2 at (5, 7), Exit Portal at (7, 7)
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1)
        )
    }

    // 15-second Countdown Timer ticker during active checkpoint
    LaunchedEffect(viewModel.isMazeCheckpointActive) {
        if (viewModel.isMazeCheckpointActive) {
            while (viewModel.isMazeCheckpointActive && viewModel.mazeTimerSeconds > 0) {
                delay(1000)
                viewModel.tickMazeTimer(onRoundOver = onRoundOver)
            }
        }
    }

    // Portal pulsation animation
    val infiniteTransition = rememberInfiniteTransition(label = "portalPulse")
    val portalPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portalScale"
    )

    var dragProcessed by remember { mutableStateOf(false) }

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
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP BAR: Dynamic Reward Calculation Header
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
                                    text = "Round 2: Dental Enamel Maze",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF80DEEA)
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
                                    text = "Target Balance",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF80DEEA)
                                )
                                Text(
                                    text = "EXACTLY $3,500",
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

                        // Dynamic Reward Live Math Display
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E2A40),
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
                                    text = "Dynamic Maze Reward: +$${viewModel.requiredRound2Money}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFD54F)
                                )
                                Text(
                                    text = "Gates Unlocked: ${viewModel.mazeCheckpointsSolved.size}/2",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (viewModel.mazeCheckpointsSolved.size >= 2) Color(0xFF76FF03) else Color(0xFFFF8A80)
                                )
                            }
                        }
                    }
                }

                // CENTER: DENTAL MAZE CANVAS (Interactive with gestures)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0D1424))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { dragProcessed = false },
                                onDragEnd = { dragProcessed = false },
                                onDragCancel = { dragProcessed = false },
                                onDrag = { change, dragAmount ->
                                    if (!dragProcessed) {
                                        change.consume()
                                        val (dx, dy) = dragAmount
                                        if (abs(dx) > abs(dy)) {
                                            if (dx > 30) {
                                                viewModel.moveMaze(1, 0, mazeGrid, onTriggerCheckpoint = {}, onReachExit = {})
                                                dragProcessed = true
                                            } else if (dx < -30) {
                                                viewModel.moveMaze(-1, 0, mazeGrid, onTriggerCheckpoint = {}, onReachExit = {})
                                                dragProcessed = true
                                            }
                                        } else {
                                            if (dy > 30) {
                                                viewModel.moveMaze(0, 1, mazeGrid, onTriggerCheckpoint = {}, onReachExit = {})
                                                dragProcessed = true
                                            } else if (dy < -30) {
                                                viewModel.moveMaze(0, -1, mazeGrid, onTriggerCheckpoint = {}, onReachExit = {})
                                                dragProcessed = true
                                            }
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val minDim = minOf(canvasW, canvasH) * 0.95f
                        val cellSize = minDim / 9f
                        val startX = (canvasW - (cellSize * 9f)) / 2f
                        val startY = (canvasH - (cellSize * 9f)) / 2f

                        // Draw Grid Cells
                        for (r in 0 until 9) {
                            for (c in 0 until 9) {
                                val x = startX + c * cellSize
                                val y = startY + r * cellSize
                                val cellType = mazeGrid[r][c]

                                when (cellType) {
                                    1 -> {
                                        // Enamel Wall (Pearlescent Ivory with Dentin Bevel)
                                        drawRoundRect(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFFB0BEC5)),
                                                start = Offset(x, y),
                                                end = Offset(x + cellSize, y + cellSize)
                                            ),
                                            topLeft = Offset(x + 1f, y + 1f),
                                            size = Size(cellSize - 2f, cellSize - 2f),
                                            cornerRadius = CornerRadius(6f, 6f)
                                        )
                                        // Enamel Prism texture lines
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.5f),
                                            start = Offset(x + 3f, y + 3f),
                                            end = Offset(x + cellSize - 3f, y + 3f),
                                            strokeWidth = 2f
                                        )
                                    }
                                    0 -> {
                                        // Open Corridor (Deep canal blue)
                                        drawRect(
                                            color = Color(0xFF10192A),
                                            topLeft = Offset(x, y),
                                            size = Size(cellSize, cellSize)
                                        )
                                        // Tubule dots
                                        drawCircle(
                                            color = Color(0xFF1E2D4A),
                                            radius = 2f,
                                            center = Offset(x + cellSize / 2f, y + cellSize / 2f)
                                        )
                                    }
                                    2 -> {
                                        // Gate 1 (Checkpoint 1)
                                        val isSolved = 0 in viewModel.mazeCheckpointsSolved
                                        val gateColor = if (isSolved) Color(0xFF00E676) else Color(0xFFFFD54F)
                                        drawRect(
                                            color = if (isSolved) Color(0xFF102A1C) else Color(0xFF2A2210),
                                            topLeft = Offset(x, y),
                                            size = Size(cellSize, cellSize)
                                        )
                                        drawRoundRect(
                                            color = gateColor,
                                            topLeft = Offset(x + 4f, y + 4f),
                                            size = Size(cellSize - 8f, cellSize - 8f),
                                            cornerRadius = CornerRadius(8f, 8f),
                                            style = Stroke(width = 3f)
                                        )
                                        drawIntoCanvas { canvas ->
                                            val paint = Paint().apply {
                                                color = if (isSolved) android.graphics.Color.GREEN else android.graphics.Color.YELLOW
                                                textSize = cellSize * 0.45f
                                                textAlign = Paint.Align.CENTER
                                                isFakeBoldText = true
                                            }
                                            canvas.nativeCanvas.drawText(
                                                if (isSolved) "🔓" else "🔒1",
                                                x + cellSize / 2f,
                                                y + cellSize * 0.65f,
                                                paint
                                            )
                                        }
                                    }
                                    3 -> {
                                        // Gate 2 (Checkpoint 2)
                                        val isSolved = 1 in viewModel.mazeCheckpointsSolved
                                        val gateColor = if (isSolved) Color(0xFF00E676) else Color(0xFF00E5FF)
                                        drawRect(
                                            color = if (isSolved) Color(0xFF102A1C) else Color(0xFF10242A),
                                            topLeft = Offset(x, y),
                                            size = Size(cellSize, cellSize)
                                        )
                                        drawRoundRect(
                                            color = gateColor,
                                            topLeft = Offset(x + 4f, y + 4f),
                                            size = Size(cellSize - 8f, cellSize - 8f),
                                            cornerRadius = CornerRadius(8f, 8f),
                                            style = Stroke(width = 3f)
                                        )
                                        drawIntoCanvas { canvas ->
                                            val paint = Paint().apply {
                                                color = if (isSolved) android.graphics.Color.GREEN else android.graphics.Color.CYAN
                                                textSize = cellSize * 0.45f
                                                textAlign = Paint.Align.CENTER
                                                isFakeBoldText = true
                                            }
                                            canvas.nativeCanvas.drawText(
                                                if (isSolved) "🔓" else "🔒2",
                                                x + cellSize / 2f,
                                                y + cellSize * 0.65f,
                                                paint
                                            )
                                        }
                                    }
                                    9 -> {
                                        // Exit Portal (Clinic Gate)
                                        val allSolved = viewModel.mazeCheckpointsSolved.size >= 2
                                        val portalRadius = (cellSize * 0.42f) * if (allSolved) portalPulse else 0.85f
                                        val center = Offset(x + cellSize / 2f, y + cellSize / 2f)

                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = if (allSolved) listOf(Color(0xFF00E5FF), Color(0xFF0288D1), Color.Transparent)
                                                         else listOf(Color(0xFF78909C), Color.Transparent),
                                                center = center,
                                                radius = portalRadius * 1.3f
                                            ),
                                            radius = portalRadius * 1.3f,
                                            center = center
                                        )
                                        drawCircle(
                                            color = if (allSolved) Color(0xFF00E5FF) else Color(0xFF546E7A),
                                            radius = portalRadius,
                                            center = center,
                                            style = Stroke(width = 3.5f)
                                        )
                                        drawIntoCanvas { canvas ->
                                            val paint = Paint().apply {
                                                color = android.graphics.Color.WHITE
                                                textSize = cellSize * 0.45f
                                                textAlign = Paint.Align.CENTER
                                                isFakeBoldText = true
                                            }
                                            canvas.nativeCanvas.drawText("🚪", center.x, center.y + (cellSize * 0.16f), paint)
                                        }
                                    }
                                }
                            }
                        }

                        // Render Protagonist Alex in the Maze
                        val alexX = startX + viewModel.playerMazeX * cellSize + cellSize / 2f
                        val alexY = startY + viewModel.playerMazeY * cellSize + cellSize / 2f

                        // Shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.5f),
                            topLeft = Offset(alexX - cellSize * 0.35f, alexY + cellSize * 0.2f),
                            size = Size(cellSize * 0.7f, cellSize * 0.25f)
                        )

                        // Head & Hair
                        drawCircle(color = Color(0xFF3E2723), radius = cellSize * 0.30f, center = Offset(alexX, alexY - cellSize * 0.14f))
                        drawCircle(color = Color(0xFFFFCC80), radius = cellSize * 0.24f, center = Offset(alexX, alexY - cellSize * 0.12f))
                        // Eyes
                        drawCircle(color = Color.Black, radius = cellSize * 0.04f, center = Offset(alexX - cellSize * 0.08f, alexY - cellSize * 0.12f))
                        drawCircle(color = Color.Black, radius = cellSize * 0.04f, center = Offset(alexX + cellSize * 0.08f, alexY - cellSize * 0.12f))
                        // Torso (Teal scrubs)
                        drawRoundRect(
                            color = Color(0xFF00838F),
                            topLeft = Offset(alexX - cellSize * 0.24f, alexY + cellSize * 0.06f),
                            size = Size(cellSize * 0.48f, cellSize * 0.32f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }

                // BOTTOM: VIRTUAL D-PAD CONTROLS
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // UP BUTTON
                        Button(
                            onClick = {
                                viewModel.moveMaze(0, -1, mazeGrid, onTriggerCheckpoint = {}, onReachExit = {})
                            },
                            modifier = Modifier.size(width = 72.dp, height = 50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2F48))
                        ) {
                            Text("⬆️", fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // LEFT, CENTER COMPASS, RIGHT BUTTONS
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.moveMaze(-1, 0, mazeGrid, onTriggerCheckpoint = {}, onReachExit = {})
                                },
                                modifier = Modifier.size(width = 72.dp, height = 50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2F48))
                            ) {
                                Text("⬅️", fontSize = 20.sp)
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🧭", fontSize = 20.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.moveMaze(1, 0, mazeGrid, onTriggerCheckpoint = {}, onReachExit = {})
                                },
                                modifier = Modifier.size(width = 72.dp, height = 50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2F48))
                            ) {
                                Text("➡️", fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // DOWN BUTTON
                        Button(
                            onClick = {
                                viewModel.moveMaze(0, 1, mazeGrid, onTriggerCheckpoint = {}, onReachExit = {})
                            },
                            modifier = Modifier.size(width = 72.dp, height = 50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2F48))
                        ) {
                            Text("⬇️", fontSize = 20.sp)
                        }
                    }
                }
            }

            // CHECKPOINT QUESTION MODAL WITH 15-SECOND COUNTDOWN TIMER
            if (viewModel.isMazeCheckpointActive) {
                val currentQ = viewModel.currentMazeQuestion
                val shuffledOptions = viewModel.currentMazeShuffledOptions
                val timerSec = viewModel.mazeTimerSeconds

                if (currentQ != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.88f))
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Header: Gate number and Countdown Timer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "🔒", fontSize = 26.sp)
                                        Column {
                                            Text(
                                                text = "ENAMEL MAZE GATE ${viewModel.currentMazeCheckpointIndex + 1}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = Color(0xFF00E5FF)
                                            )
                                            Text("Answer correctly to unlock!", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                        }
                                    }

                                    // Timer Badge
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (timerSec <= 5) Color(0xFFD32F2F) else Color(0xFF00897B)
                                    ) {
                                        Text(
                                            text = "⏱️ ${timerSec}s",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

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

                                Spacer(modifier = Modifier.height(14.dp))

                                // Question Card
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2838)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Category: ${currentQ.category} (${currentQ.difficulty})",
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

                                Spacer(modifier = Modifier.height(14.dp))

                                // Options
                                val prefixes = listOf("A. ", "B. ", "C. ", "D. ")
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    shuffledOptions.forEachIndexed { idx, optText ->
                                        Button(
                                            onClick = {
                                                viewModel.submitMazeAnswer(
                                                    optionIndex = idx,
                                                    onRoundOver = onRoundOver,
                                                    onCorrect = {}
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
            }

            // ROUND 2 COMPLETE CELEBRATION MODAL
            if (viewModel.isRound2Complete) {
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
                            Text(text = "🏆🌀✨", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "ROUND 2 CLEARED!",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFFFD700),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enamel Maze Navigated!\nDynamic Clinical Grant Awarded!",
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
                                        Text("Round 1 Carryover:", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("$${viewModel.progress.round1TotalMoney}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Dynamic Maze Reward:", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("+$${viewModel.progress.round2DynamicReward}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF00E5FF))
                                    }
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Current Total Funds:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Text("$${viewModel.progress.moneyCollected}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color(0xFFFFD700))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Target balance of EXACTLY \$3,500 reached! Prepare for Round 3!",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF76FF03),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onProceedToZombie,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                            ) {
                                Text("Proceed to Round 3: Zombie Challenge 🧟‍♂️➡️", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
