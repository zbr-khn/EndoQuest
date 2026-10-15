package com.example.endoquest.ui.screens

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endoquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Round 2: Dental Enamel Maze Screen.
 * Features:
 * - 3 Visually Distinct Doors (Gate 1: Enamel, Gate 2: Dentin, Gate 3: Pulp) + Exit Portal.
 * - Smooth Touch Virtual Joystick at the bottom of the screen (replaces all bottom buttons).
 * - Pac-Man-inspired Dental Bacterium Pursuer with BFS corridor pathfinding and proximity warning.
 */
@Composable
fun MazeScreen(
    viewModel: GameViewModel,
    onProceedToZombie: () -> Unit,
    onRoundOver: () -> Unit
) {
    // 9x9 Dental Enamel Maze Grid with 3 Doors:
    // 0 = Open Corridor, 1 = Enamel Wall
    // 2 = Door 1 (1, 3), 3 = Door 2 (7, 3), 4 = Door 3 (3, 7), 9 = Exit Portal (7, 7)
    val mazeGrid = remember {
        arrayOf(
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 1), // Start at (1, 1)
            intArrayOf(1, 0, 1, 0, 1, 0, 1, 0, 1),
            intArrayOf(1, 2, 1, 0, 0, 0, 1, 3, 1), // Door 1 at (1, 3), Door 2 at (7, 3)
            intArrayOf(1, 0, 1, 0, 1, 0, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 1), // Center junction (5, 5)
            intArrayOf(1, 0, 1, 0, 1, 0, 1, 0, 1),
            intArrayOf(1, 0, 1, 4, 1, 0, 0, 9, 1), // Door 3 at (3, 7), Exit Portal at (7, 7)
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1)
        )
    }

    // Activate bacterium after initial 2-second grace period
    LaunchedEffect(Unit) {
        delay(2000)
        viewModel.activateBacterium()
    }

    // Pac-Man Bacterium pursuit loop (steps every 550ms along corridors)
    LaunchedEffect(viewModel.isBacteriumActive, viewModel.isMazeCheckpointActive, viewModel.isRound2Complete) {
        while (viewModel.isBacteriumActive && !viewModel.isMazeCheckpointActive && !viewModel.isRound2Complete) {
            delay(550)
            viewModel.stepBacteriumTowardsPlayer(mazeGrid, onCatchPlayer = onRoundOver)
        }
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

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "mazeAnim")
    val portalPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portalScale"
    )

    val bacteriumWobble by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bacteriumWobble"
    )

    // Virtual Joystick States
    var joystickKnobOffset by remember { mutableStateOf(Offset.Zero) }
    var activeJoystickDirection by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val maxJoystickRadius = 110f // pixels

    // Continuous movement loop while joystick is held in a direction
    LaunchedEffect(activeJoystickDirection, viewModel.isMazeCheckpointActive, viewModel.isRound2Complete) {
        while (activeJoystickDirection != null && !viewModel.isMazeCheckpointActive && !viewModel.isRound2Complete) {
            val (dx, dy) = activeJoystickDirection!!
            viewModel.moveMaze(dx, dy, mazeGrid, onTriggerCheckpoint = {}, onReachExit = {})
            delay(190) // Continuous smooth step rate
        }
    }

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
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP BAR: Dynamic Reward & Three-Door Gate Counter
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Round 2: Three-Door Enamel Maze",
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
                                    text = "Clinic Target",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF80DEEA)
                                )
                                Text(
                                    text = "$3,500 EXACT",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF00E5FF)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    viewModel.skipMazeRound()
                                    onProceedToZombie()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD54F).copy(alpha = 0.2f),
                                    contentColor = Color(0xFFFFD54F)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Skip ⏭️", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { viewModel.toggleAudioMute() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Text(
                                    text = if (viewModel.isMuted) "🔇" else "🔊",
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Dynamic Reward Live Math Display
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E2A40),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Dynamic Reward: +$${viewModel.requiredRound2Money}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFD54F)
                                )
                                Text(
                                    text = "Doors Unlocked: ${viewModel.mazeCheckpointsSolved.size}/3",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (viewModel.mazeCheckpointsSolved.size >= 3) Color(0xFF76FF03) else Color(0xFFFF8A80)
                                )
                            }
                        }
                    }
                }

                // PROXIMITY WARNING BANNER
                if (viewModel.bacteriumWarningActive && !viewModel.isRound2Complete) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFB71C1C).copy(alpha = 0.9f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️ BIOFILM BACTERIUM PURSUING! EVADE TO DOORS! ⚠️",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                        }
                    }
                }

                // CENTER: DENTAL MAZE CANVAS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0D1424))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val minDim = minOf(canvasW, canvasH) * 0.96f
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
                                        // Enamel Wall
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
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.5f),
                                            start = Offset(x + 3f, y + 3f),
                                            end = Offset(x + cellSize - 3f, y + 3f),
                                            strokeWidth = 2f
                                        )
                                    }
                                    0 -> {
                                        // Open Corridor
                                        drawRect(
                                            color = Color(0xFF10192A),
                                            topLeft = Offset(x, y),
                                            size = Size(cellSize, cellSize)
                                        )
                                        drawCircle(
                                            color = Color(0xFF1E2D4A),
                                            radius = 2f,
                                            center = Offset(x + cellSize / 2f, y + cellSize / 2f)
                                        )
                                    }
                                    2 -> {
                                        // Door 1: Enamel Gate (Yellow)
                                        val isSolved = 0 in viewModel.mazeCheckpointsSolved
                                        val gateColor = if (isSolved) Color(0xFF00E676) else Color(0xFFFFD54F)
                                        drawRect(
                                            color = if (isSolved) Color(0xFF102A1C) else Color(0xFF2A2210),
                                            topLeft = Offset(x, y),
                                            size = Size(cellSize, cellSize)
                                        )
                                        drawRoundRect(
                                            color = gateColor,
                                            topLeft = Offset(x + 3f, y + 3f),
                                            size = Size(cellSize - 6f, cellSize - 6f),
                                            cornerRadius = CornerRadius(7f, 7f),
                                            style = Stroke(width = 3f)
                                        )
                                        drawIntoCanvas { canvas ->
                                            val paint = Paint().apply {
                                                color = if (isSolved) android.graphics.Color.GREEN else android.graphics.Color.YELLOW
                                                textSize = cellSize * 0.40f
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
                                        // Door 2: Dentin Gate (Cyan)
                                        val isSolved = 1 in viewModel.mazeCheckpointsSolved
                                        val gateColor = if (isSolved) Color(0xFF00E676) else Color(0xFF00E5FF)
                                        drawRect(
                                            color = if (isSolved) Color(0xFF102A1C) else Color(0xFF10242A),
                                            topLeft = Offset(x, y),
                                            size = Size(cellSize, cellSize)
                                        )
                                        drawRoundRect(
                                            color = gateColor,
                                            topLeft = Offset(x + 3f, y + 3f),
                                            size = Size(cellSize - 6f, cellSize - 6f),
                                            cornerRadius = CornerRadius(7f, 7f),
                                            style = Stroke(width = 3f)
                                        )
                                        drawIntoCanvas { canvas ->
                                            val paint = Paint().apply {
                                                color = if (isSolved) android.graphics.Color.GREEN else android.graphics.Color.CYAN
                                                textSize = cellSize * 0.40f
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
                                    4 -> {
                                        // Door 3: Pulp Gate (Magenta / Violet)
                                        val isSolved = 2 in viewModel.mazeCheckpointsSolved
                                        val gateColor = if (isSolved) Color(0xFF00E676) else Color(0xFFE040FB)
                                        drawRect(
                                            color = if (isSolved) Color(0xFF102A1C) else Color(0xFF28102B),
                                            topLeft = Offset(x, y),
                                            size = Size(cellSize, cellSize)
                                        )
                                        drawRoundRect(
                                            color = gateColor,
                                            topLeft = Offset(x + 3f, y + 3f),
                                            size = Size(cellSize - 6f, cellSize - 6f),
                                            cornerRadius = CornerRadius(7f, 7f),
                                            style = Stroke(width = 3f)
                                        )
                                        drawIntoCanvas { canvas ->
                                            val paint = Paint().apply {
                                                color = if (isSolved) android.graphics.Color.GREEN else android.graphics.Color.MAGENTA
                                                textSize = cellSize * 0.40f
                                                textAlign = Paint.Align.CENTER
                                                isFakeBoldText = true
                                            }
                                            canvas.nativeCanvas.drawText(
                                                if (isSolved) "🔓" else "🔒3",
                                                x + cellSize / 2f,
                                                y + cellSize * 0.65f,
                                                paint
                                            )
                                        }
                                    }
                                    9 -> {
                                        // Exit Portal (Clinic Gate)
                                        val allSolved = viewModel.mazeCheckpointsSolved.size >= 3
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

                        // RENDER DENTAL BACTERIUM PURSUER
                        val bactX = startX + viewModel.bacteriumX * cellSize + cellSize / 2f
                        val bactY = startY + viewModel.bacteriumY * cellSize + cellSize / 2f
                        drawIllustratedBacterium(
                            center = Offset(bactX, bactY),
                            cellSize = cellSize,
                            wobble = bacteriumWobble
                        )

                        // RENDER PROTAGONIST ALEX
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

                // BOTTOM: VIRTUAL JOYSTICK (Smooth Touch Control)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF16233B), Color(0xFF0E1726))
                                )
                            )
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        // Initial touch point inside joystick
                                    },
                                    onDragEnd = {
                                        joystickKnobOffset = Offset.Zero
                                        activeJoystickDirection = null
                                    },
                                    onDragCancel = {
                                        joystickKnobOffset = Offset.Zero
                                        activeJoystickDirection = null
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val newOffset = joystickKnobOffset + dragAmount
                                        val distance = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                                        val clampedDistance = distance.coerceAtMost(maxJoystickRadius)
                                        val angle = atan2(newOffset.y, newOffset.x)

                                        joystickKnobOffset = Offset(
                                            x = cos(angle) * clampedDistance,
                                            y = sin(angle) * clampedDistance
                                        )

                                        // Map angle to primary 4-way direction if deflected beyond deadzone (16px)
                                        if (clampedDistance > 16f) {
                                            val (dx, dy) = when {
                                                abs(newOffset.x) > abs(newOffset.y) -> {
                                                    if (newOffset.x > 0) Pair(1, 0) else Pair(-1, 0)
                                                }
                                                else -> {
                                                    if (newOffset.y > 0) Pair(0, 1) else Pair(0, -1)
                                                }
                                            }
                                            activeJoystickDirection = Pair(dx, dy)
                                        } else {
                                            activeJoystickDirection = null
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Guide Arrows on Base
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension / 2f
                            val center = Offset(r, r)
                            drawCircle(
                                color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                                radius = r - 2f,
                                style = Stroke(width = 2.5f)
                            )
                            // Outer ring accent
                            drawCircle(
                                color = Color.White.copy(alpha = 0.08f),
                                radius = r * 0.65f,
                                style = Stroke(width = 1.5f)
                            )
                        }

                        // Direction Indicator Icons
                        Text("▲", fontSize = 12.sp, color = Color(0xFF80DEEA).copy(alpha = 0.5f), modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp))
                        Text("▼", fontSize = 12.sp, color = Color(0xFF80DEEA).copy(alpha = 0.5f), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
                        Text("◀", fontSize = 12.sp, color = Color(0xFF80DEEA).copy(alpha = 0.5f), modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp))
                        Text("▶", fontSize = 12.sp, color = Color(0xFF80DEEA).copy(alpha = 0.5f), modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp))

                        // Draggable Knob
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = joystickKnobOffset.x.toInt(),
                                        y = joystickKnobOffset.y.toInt()
                                    )
                                }
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF00E5FF), Color(0xFF0097A7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧭", fontSize = 18.sp)
                        }
                    }
                }
            }

            // CHECKPOINT QUESTION MODAL (Gates 1, 2, and 3)
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
                                val gateNames = listOf("Enamel Gate", "Dentin Gate", "Pulp Gate")
                                val currentGateName = gateNames.getOrElse(viewModel.currentMazeCheckpointIndex) { "Canal Gate" }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "🔒", fontSize = 26.sp)
                                        Column {
                                            Text(
                                                text = "DOOR ${viewModel.currentMazeCheckpointIndex + 1}: $currentGateName",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = Color(0xFF00E5FF)
                                            )
                                            Text("Answer correctly to unlock!", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                        }
                                    }

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

                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2838)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Category: ${currentQ.category}",
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

            // ROUND 2 COMPLETE MODAL
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
                            Text(text = "🏆🚪✨", fontSize = 56.sp)
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
                                text = "All 3 Doors Unlocked & Biofilm Evaded!\nDynamic Clinical Grant Awarded!",
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

/**
 * Renders an illustrated cartoon dental bacterium character (Pac-Man style pursuer).
 * Complete vector canvas illustration with purple slimy body, flagella tentacles,
 * menacing cartoon eyes, sharp teeth, and toxic green biofilm glow.
 */
fun DrawScope.drawIllustratedBacterium(
    center: Offset,
    cellSize: Float,
    wobble: Float
) {
    val radius = cellSize * 0.38f

    // 1. Toxic Biofilm Halo / Aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF76FF03).copy(alpha = 0.35f), Color(0xFF9C27B0).copy(alpha = 0.2f), Color.Transparent),
            center = center,
            radius = radius * 1.6f
        ),
        radius = radius * 1.6f,
        center = center
    )

    // 2. Animated Flagella / Pili Tentacles
    val flagellaColor = Color(0xFFBA68C8)
    for (i in 0 until 6) {
        val baseAngle = (i * 60f + wobble * 2f) * (Math.PI / 180f).toFloat()
        val startX = center.x + cos(baseAngle) * (radius * 0.85f)
        val startY = center.y + sin(baseAngle) * (radius * 0.85f)
        val endX = center.x + cos(baseAngle) * (radius * 1.45f)
        val endY = center.y + sin(baseAngle) * (radius * 1.45f) + sin(wobble + i) * 3f

        drawLine(
            color = flagellaColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFFE040FB),
            radius = 2f,
            center = Offset(endX, endY)
        )
    }

    // 3. Main Microbial Body (Vibrant Magenta/Purple gradient)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE040FB), Color(0xFF8E24AA), Color(0xFF4A148C)),
            center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // 4. Menacing Cartoon Eyes
    val eyeOffsetX = radius * 0.30f
    val eyeOffsetY = -radius * 0.22f
    val eyeRadius = radius * 0.26f

    // Left Eye (Yellow sclera with red pupil)
    drawCircle(Color(0xFFFFEE58), radius = eyeRadius, center = Offset(center.x - eyeOffsetX, center.y + eyeOffsetY))
    drawCircle(Color(0xFFD50000), radius = eyeRadius * 0.5f, center = Offset(center.x - eyeOffsetX + 1f, center.y + eyeOffsetY))
    drawCircle(Color.White, radius = eyeRadius * 0.2f, center = Offset(center.x - eyeOffsetX - 1f, center.y + eyeOffsetY - 1f))

    // Right Eye
    drawCircle(Color(0xFFFFEE58), radius = eyeRadius, center = Offset(center.x + eyeOffsetX, center.y + eyeOffsetY))
    drawCircle(Color(0xFFD50000), radius = eyeRadius * 0.5f, center = Offset(center.x + eyeOffsetX + 1f, center.y + eyeOffsetY))
    drawCircle(Color.White, radius = eyeRadius * 0.2f, center = Offset(center.x + eyeOffsetX - 1f, center.y + eyeOffsetY - 1f))

    // 5. Grinning Serrated Teeth (Mouth)
    val mouthY = center.y + radius * 0.35f
    val mouthW = radius * 0.9f
    drawRoundRect(
        color = Color(0xFF1A0A2A),
        topLeft = Offset(center.x - mouthW / 2f, mouthY - 3f),
        size = Size(mouthW, radius * 0.36f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // 4 Pointy Teeth
    val numTeeth = 4
    val toothSpacing = mouthW / (numTeeth + 1)
    for (t in 1..numTeeth) {
        val tx = center.x - mouthW / 2f + t * toothSpacing
        val toothPath = Path().apply {
            moveTo(tx - 2f, mouthY - 2f)
            lineTo(tx + 2f, mouthY - 2f)
            lineTo(tx, mouthY + 5f)
            close()
        }
        drawPath(toothPath, color = Color.White)
    }
}
