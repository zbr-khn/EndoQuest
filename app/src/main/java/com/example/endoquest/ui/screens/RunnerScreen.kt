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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endoquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

data class RunnerItem(
    val id: Int,
    val lane: Int, // 0: Left, 1: Center, 2: Right
    var depth: Float, // 0.0 (horizon) to 1.0 (foreground/player)
    val isCoin: Boolean,
    val value: Int = 25,
    val obstacleType: ObstacleType = ObstacleType.NONE
)

enum class ObstacleType {
    NONE, LOW_TRAY, HIGH_LAMP, MOBILE_CART, TOOTH_BARRIER
}

@Composable
fun RunnerScreen(
    viewModel: GameViewModel,
    onTriggerQuiz: () -> Unit,
    onReturnToClinic: () -> Unit,
    onGameOver: () -> Unit
) {
    var items by remember { mutableStateOf(listOf<RunnerItem>()) }
    var nextItemId by remember { mutableStateOf(0) }
    var baseSpeed by remember { mutableStateOf(0.008f) }
    var runFrame by remember { mutableStateOf(0) }
    var coinSpinAngle by remember { mutableStateOf(0f) }
    var roadOffset by remember { mutableStateOf(0f) }

    // Jump state animation
    val jumpProgress by animateFloatAsState(
        targetValue = if (viewModel.isJumping) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        finishedListener = { viewModel.resetJump() },
        label = "jumpAnim"
    )

    // Slide auto-reset timer (crouch gets back up after 750ms automatically)
    LaunchedEffect(viewModel.isSliding) {
        if (viewModel.isSliding) {
            delay(750)
            viewModel.resetActionState()
        }
    }

    // Automatically transition to clinic when $5,000 target is reached (Dr. Smile popup & auto-transition)
    LaunchedEffect(viewModel.showGoalReachedPopup) {
        if (viewModel.showGoalReachedPopup) {
            viewModel.soundManager.playQuizSuccess()
            delay(2800)
            onReturnToClinic()
        }
    }

    // Police sprint approach animation
    LaunchedEffect(viewModel.isPoliceEncounterActive) {
        if (viewModel.isPoliceEncounterActive) {
            val startTime = System.currentTimeMillis()
            val duration = 800f
            while (viewModel.isPoliceEncounterActive) {
                val elapsed = System.currentTimeMillis() - startTime
                val p = (elapsed / duration).coerceIn(0f, 1f)
                viewModel.updatePoliceSprint(p)
                if (p >= 1f) break
                delay(16)
            }
        }
    }

    // Running animation & coin spin ticker
    LaunchedEffect(true) {
        while (true) {
            delay(110)
            runFrame = (runFrame + 1) % 4
            coinSpinAngle = (coinSpinAngle + 10f) % 360f
            roadOffset = (roadOffset + 0.03f) % 1f
        }
    }

    // Smooth horizontal lane interpolation
    val targetLaneX = when (viewModel.playerLane) {
        0 -> -1f
        1 -> 0f
        else -> 1f
    }
    val animatedLaneX by animateFloatAsState(
        targetValue = targetLaneX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "laneSlide"
    )

    var isGameOverTriggered by remember { mutableStateOf(false) }
    var dragTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.runnerResetTrigger) {
        isGameOverTriggered = false
        items = emptyList()
    }

    // Main 3D perspective game loop (Strictly gameplay driven, ZERO bottom buttons, 100% active collisions)
    LaunchedEffect(viewModel.isPoliceEncounterActive, viewModel.runnerResetTrigger) {
        var lastTime = System.currentTimeMillis()
        while (viewModel.progress.moneyCollected < viewModel.progress.targetMoney && !viewModel.showGoalReachedPopup) {
            val now = System.currentTimeMillis()
            val dt = (now - lastTime).toFloat() / 1000f
            lastTime = now

            if (!viewModel.isPoliceEncounterActive) {
                if (Random.nextFloat() < 0.018f) {
                    val lane = Random.nextInt(3)
                    val isCoin = Random.nextFloat() > 0.35f
                    val obsType = if (!isCoin) {
                        when (Random.nextInt(4)) {
                            0 -> ObstacleType.LOW_TRAY
                            1 -> ObstacleType.HIGH_LAMP
                            2 -> ObstacleType.MOBILE_CART
                            else -> ObstacleType.TOOTH_BARRIER
                        }
                    } else ObstacleType.NONE

                    val newItem = RunnerItem(
                        id = nextItemId++,
                        lane = lane,
                        depth = 0f,
                        isCoin = isCoin,
                        value = 25,
                        obstacleType = obsType
                    )
                    items = items + newItem
                }
            }

            val updated = mutableListOf<RunnerItem>()

            for (item in items) {
                if (viewModel.isPoliceEncounterActive) break

                item.depth += baseSpeed * (dt * 60f).coerceIn(0.5f, 2.5f)
                if (item.depth < 1.08f) {
                    val itemLaneX = (item.lane - 1).toFloat()
                    // 100% active collision check (zero invulnerability bugs)
                    if (!viewModel.isPoliceEncounterActive && item.depth in 0.88f..0.98f && abs(itemLaneX - animatedLaneX) < 0.42f) {
                        if (item.isCoin) {
                            viewModel.soundManager.playCoin()
                            viewModel.addMoney(item.value)
                            if (viewModel.isPoliceEncounterActive) {
                                items = items.filter { it.isCoin || it.depth < 0.50f }
                                break
                            }
                        } else {
                            val hit = when (item.obstacleType) {
                                ObstacleType.LOW_TRAY -> !viewModel.isSliding
                                ObstacleType.HIGH_LAMP -> !viewModel.isJumping
                                ObstacleType.MOBILE_CART, ObstacleType.TOOTH_BARRIER -> true
                                ObstacleType.NONE -> false
                            }

                            if (hit && !isGameOverTriggered) {
                                isGameOverTriggered = true
                                viewModel.soundManager.playCollision()
                                items = emptyList()
                                onGameOver()
                                break
                            } else if (!hit) {
                                updated.add(item)
                            }
                        }
                    } else {
                        updated.add(item)
                    }
                }
            }
            if (!viewModel.isPoliceEncounterActive) {
                items = updated
            }

            delay(16)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragTriggered = false },
                    onDragEnd = { dragTriggered = false },
                    onDragCancel = { dragTriggered = false },
                    onDrag = { change, dragAmount ->
                        if (!dragTriggered) {
                            change.consume()
                            val (dx, dy) = dragAmount
                            if (abs(dx) > abs(dy)) {
                                if (dx > 25) {
                                    viewModel.moveRight()
                                    dragTriggered = true
                                } else if (dx < -25) {
                                    viewModel.moveLeft()
                                    dragTriggered = true
                                }
                            } else {
                                if (dy < -25) {
                                    viewModel.jump()
                                    dragTriggered = true
                                } else if (dy > 25) {
                                    viewModel.slide()
                                    dragTriggered = true
                                }
                            }
                        }
                    }
                )
            },
        color = Color(0xFF0A0E21)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar: Money Counter & Sound Toggle ONLY (Zero bottom buttons)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Collected Funds ($25 Coins)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF90CAF9)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🪙 $${viewModel.progress.moneyCollected}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${viewModel.progress.moneyCollected / 25} coins)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "RCT Cost Target",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF90CAF9)
                                )
                                Text(
                                    text = "$5,000",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF00E5FF)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { viewModel.toggleAudioMute() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Text(
                                    text = if (viewModel.isMuted) "🔇" else "🔊",
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // 3D-PERSPECTIVE RUNNER ARENA CANVAS (Takes full remaining screen space)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val s = (w / 380f).coerceIn(1.2f, 3.2f)

                        val horizonX = w / 2f
                        val horizonY = h * 0.32f
                        val bottomY = h * 0.98f

                        // 1. SKY GRADIENT & SUN
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF0288D1), Color(0xFF4FC3F7), Color(0xFFE1F5FE)),
                                startY = 0f,
                                endY = horizonY
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(w, horizonY)
                        )

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color.Transparent),
                                center = Offset(w * 0.82f, horizonY * 0.45f),
                                radius = 50f * s
                            ),
                            radius = 50f * s,
                            center = Offset(w * 0.82f, horizonY * 0.45f)
                        )

                        // 2. HORIZON SCENERY
                        drawOval(Color(0xFF43A047), Offset(-w * 0.1f, horizonY - 20f * s), Size(w * 0.7f, 40f * s))
                        drawOval(Color(0xFF2E7D32), Offset(w * 0.4f, horizonY - 25f * s), Size(w * 0.8f, 50f * s))

                        // 3. GROUND LAWN
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF4CAF50), Color(0xFF388E3C), Color(0xFF1B5E20)),
                                startY = horizonY,
                                endY = h
                            ),
                            topLeft = Offset(0f, horizonY),
                            size = Size(w, h - horizonY)
                        )

                        // 4. 3D ROAD PERSPECTIVE
                        val roadWidthTop = w * 0.08f
                        val roadWidthBottom = w * 0.88f
                        val leftTopX = horizonX - roadWidthTop / 2f
                        val rightTopX = horizonX + roadWidthTop / 2f
                        val leftBottomX = (w - roadWidthBottom) / 2f
                        val rightBottomX = leftBottomX + roadWidthBottom

                        val roadPath = Path().apply {
                            moveTo(leftTopX, horizonY)
                            lineTo(rightTopX, horizonY)
                            lineTo(rightBottomX, bottomY)
                            lineTo(leftBottomX, bottomY)
                            close()
                        }
                        drawPath(roadPath, color = Color(0xFF2C3E50))

                        // Curbs
                        drawPath(Path().apply {
                            moveTo(leftTopX - 4f * s, horizonY); lineTo(leftTopX, horizonY); lineTo(leftBottomX, bottomY); lineTo(leftBottomX - 14f * s, bottomY); close()
                        }, Color(0xFFE53935))
                        drawPath(Path().apply {
                            moveTo(rightTopX, horizonY); lineTo(rightTopX + 4f * s, horizonY); lineTo(rightBottomX + 14f * s, bottomY); lineTo(rightBottomX, bottomY); close()
                        }, Color(0xFFE53935))

                        // 5. MOVING LANE DIVIDERS
                        val numDashes = 7
                        for (d in 0 until numDashes) {
                            val rawD = (d.toFloat() / numDashes + roadOffset) % 1f
                            val depthVal = rawD * rawD
                            val dashY = horizonY + (bottomY - horizonY) * depthVal
                            val dashHeight = (10f * s) * (0.3f + 1.8f * depthVal)
                            val currentRoadW = roadWidthTop + (roadWidthBottom - roadWidthTop) * depthVal
                            val currentLeft = horizonX - currentRoadW / 2f
                            val laneW = currentRoadW / 3f

                            drawLine(Color(0xFFFFD54F), Offset(currentLeft + laneW, dashY), Offset(currentLeft + laneW, dashY + dashHeight), (2f * s) * (0.5f + 1.2f * depthVal))
                            drawLine(Color(0xFFFFD54F), Offset(currentLeft + laneW * 2f, dashY), Offset(currentLeft + laneW * 2f, dashY + dashHeight), (2f * s) * (0.5f + 1.2f * depthVal))
                        }

                        // 6. DRAW ITEMS
                        val sortedItems = items.sortedBy { it.depth }
                        for (item in sortedItems) {
                            val depthSquared = item.depth
                            val currentY = horizonY + (bottomY - horizonY) * depthSquared
                            val currentRoadW = roadWidthTop + (roadWidthBottom - roadWidthTop) * depthSquared
                            val laneW = currentRoadW / 3f
                            val currentLeft = horizonX - currentRoadW / 2f
                            val itemX = currentLeft + laneW * (item.lane + 0.5f)
                            val scale = 0.25f + 0.95f * depthSquared

                            if (item.isCoin) {
                                val bobY = currentY + sin(coinSpinAngle * (PI / 180.0).toFloat() * 3f + item.id) * (4f * scale * s)
                                draw3DMetallicGoldCoin(Offset(itemX, bobY), scale * s, coinSpinAngle + item.id * 45f)
                            } else {
                                drawDentalObstacle(Offset(itemX, currentY), item.obstacleType, scale * s)
                            }
                        }

                        // 7. DENTAL POLICE OFFICER
                        if (viewModel.isPoliceEncounterActive) {
                            val policeDepth = 0.3f + 0.58f * viewModel.policeSprintProgress
                            val pCenter = horizonX
                            val pY = horizonY + (bottomY - horizonY) * policeDepth
                            val pScale = (0.35f + 0.95f * policeDepth) * s
                            drawDentalPoliceOfficer(Offset(pCenter, pY), pScale, viewModel.policeSaluting)
                        }

                        // 8. PROMINENT HUMAN PLAYER CHARACTER: ALEX
                        val playerDepth = 0.92f
                        val jumpYOffset = jumpProgress * (90f * s)
                        val playerY = horizonY + (bottomY - horizonY) * playerDepth - jumpYOffset
                        val roadWAtPlayer = roadWidthTop + (roadWidthBottom - roadWidthTop) * playerDepth
                        val laneWAtPlayer = roadWAtPlayer / 3f
                        val playerX = horizonX + (animatedLaneX * laneWAtPlayer)

                        // Ground shadow
                        val shadowScale = (1f - jumpProgress * 0.45f) * s
                        val shadowY = horizonY + (bottomY - horizonY) * playerDepth + (if (viewModel.isSliding) 5f * s else 18f * s)
                        drawOval(Color.Black.copy(alpha = 0.4f), Offset(playerX - 32f * shadowScale, shadowY - 9f * shadowScale), Size(64f * shadowScale, 18f * shadowScale))

                        drawAlexPlayerCharacter(
                            center = Offset(playerX, playerY),
                            frame = runFrame,
                            isJumping = viewModel.isJumping,
                            isSliding = viewModel.isSliding,
                            scale = s * 1.4f
                        )
                    }

                    // Milestone Toast
                    if (viewModel.progress.moneyCollected >= 500 && viewModel.progress.moneyCollected < 600) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
                            color = Color(0xFFFFD700),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 6.dp
                        ) {
                            Text(
                                text = "⭐ $500 Milestone Reached! Dental Police Patrol Activated! ⭐",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // DR. SMILE CONGRATULATORY CELEBRATION POPUP AT $5,000
            if (viewModel.showGoalReachedPopup) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).padding(24.dp),
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
                            Text(text = "👨‍⚕️🎉", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "DR. SMILE CONGRATULATES YOU!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFFFD700),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "\"Magnificent work, Alex! You successfully completed the journey and collected all $5,000 for your treatment.\"\n\nNow entering the dental clinic to begin the Root Canal Treatment procedure!",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onReturnToClinic,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text("Enter Dental Clinic 🏥", fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // DENTAL POLICE PHYSICAL ENCOUNTER & INSPECTION DIALOGUE OVERLAY
            if (viewModel.isPoliceEncounterActive && viewModel.policeSprintProgress >= 0.80f) {
                val currentQ = viewModel.currentRunQuestion
                val shuffledOptions = viewModel.currentRunShuffledOptions

                if (currentQ != null) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.82f)).padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        ) {
                            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "🚨👮‍♂️", fontSize = 28.sp)
                                    Column {
                                        Text("DENTAL HIGHWAY PATROL", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color(0xFFFF5252))
                                        Text("Officer Floss: \"Halt! Endodontic Road Inspection!\"", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFD54F))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2838)), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Category: ${currentQ.category} (${currentQ.difficulty})", style = MaterialTheme.typography.labelSmall, color = Color(0xFF80DEEA))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(currentQ.questionText, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val prefixes = listOf("A. ", "B. ", "C. ", "D. ")
                                val isAnswered = viewModel.runCheckpointPassed != null
                                val isPassed = viewModel.runCheckpointPassed == true
                                val selectedIndex = viewModel.selectedRunOptionIndex

                                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    shuffledOptions.forEachIndexed { idx, optText ->
                                        val isThisSelected = selectedIndex == idx
                                        val isThisCorrect = optText == currentQ.correctAnswerText

                                        val btnColor = when {
                                            isAnswered && isThisCorrect -> Color(0xFF2E7D32)
                                            isAnswered && isThisSelected && !isThisCorrect -> Color(0xFFC62828)
                                            else -> Color(0xFF243044)
                                        }

                                        val letterPrefix = prefixes.getOrElse(idx) { "" }

                                        Button(
                                            onClick = {
                                                if (!isPassed) {
                                                    viewModel.submitRunQuizAnswer(idx)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().height(46.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = btnColor, contentColor = Color.White)
                                        ) {
                                            Text(
                                                text = letterPrefix + optText,
                                                style = MaterialTheme.typography.bodySmall,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                if (isAnswered) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isPassed) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = if (isPassed) "✅ INSPECTION PASSED! Officer Salutes: +$${currentQ.coinReward} Reward!"
                                                       else "❌ INCORRECT ANSWER! Clinical Review:",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(currentQ.explanation, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (isPassed) {
                                        Button(
                                            onClick = { viewModel.resumeRunnerFromPolice() },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                                        ) {
                                            Text("Officer Salutes: Resume 3D Runner! 🏃‍♂️💨", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.retryRunQuestion() },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                                        ) {
                                            Text("🔄 Try Question Again", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders a recognized metallic golden coin with spinning 3D perspective and "$25" denomination.
 */
fun DrawScope.draw3DMetallicGoldCoin(
    center: Offset,
    scale: Float,
    spinAngle: Float
) {
    val radius = 24f * scale
    val rad = Math.toRadians(spinAngle.toDouble())
    val cosAngle = abs(cos(rad)).toFloat().coerceIn(0.12f, 1f)

    val coinW = radius * cosAngle
    val coinH = radius

    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFA000)),
            startX = center.x - coinW,
            endX = center.x + coinW
        ),
        topLeft = Offset(center.x - coinW, center.y - coinH),
        size = Size(coinW * 2f, coinH * 2f)
    )

    if (cosAngle > 0.35f) {
        val innerW = coinW * 0.8f
        val innerH = coinH * 0.8f
        drawOval(
            color = Color(0xFFFFE082),
            topLeft = Offset(center.x - innerW, center.y - innerH),
            size = Size(innerW * 2f, innerH * 2f)
        )
        drawOval(
            color = Color(0xFFFFC107),
            topLeft = Offset(center.x - innerW * 0.9f, center.y - innerH * 0.9f),
            size = Size(innerW * 1.8f, innerH * 1.8f)
        )

        if (cosAngle > 0.45f) {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = android.graphics.Color.rgb(100, 55, 0)
                    textSize = 15f * scale
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(
                    "$25",
                    center.x,
                    center.y + (5f * scale),
                    paint
                )
            }
        }
    }

    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = 3.5f * scale,
        center = Offset(center.x - coinW * 0.5f, center.y - coinH * 0.5f)
    )
}

/**
 * Renders dental-themed obstacles in 3D perspective.
 */
fun DrawScope.drawDentalObstacle(
    center: Offset,
    type: ObstacleType,
    scale: Float
) {
    val r = 24f * scale

    when (type) {
        ObstacleType.LOW_TRAY -> {
            val tw = r * 2.6f
            val th = r * 1.0f
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(center.x - tw / 2f + 4f, center.y + 4f),
                size = Size(tw, th),
                cornerRadius = CornerRadius(6f * scale, 6f * scale)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFCFD8DC), Color(0xFF90A4AE), Color(0xFF607D8B))
                ),
                topLeft = Offset(center.x - tw / 2f, center.y - th / 2f),
                size = Size(tw, th),
                cornerRadius = CornerRadius(6f * scale, 6f * scale)
            )
            drawLine(
                color = Color(0xFF37474F),
                start = Offset(center.x - tw * 0.35f, center.y),
                end = Offset(center.x + tw * 0.35f, center.y),
                strokeWidth = 3f * scale
            )
            drawCircle(Color(0xFF4CAF50), radius = 6f * scale, center = Offset(center.x, center.y - th * 0.8f))

            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12f * scale
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText("⬇️ SLIDE", center.x, center.y - th * 1.2f, paint)
            }
        }

        ObstacleType.HIGH_LAMP -> {
            val lampW = r * 2.4f
            val lampH = r * 0.9f
            val lampY = center.y - r * 1.8f

            drawLine(
                color = Color(0xFF78909C),
                start = Offset(center.x - r * 1.5f, 0f),
                end = Offset(center.x, lampY),
                strokeWidth = 4f * scale
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFECEFF1), Color(0xFFB0BEC5))
                ),
                topLeft = Offset(center.x - lampW / 2f, lampY - lampH / 2f),
                size = Size(lampW, lampH),
                cornerRadius = CornerRadius(8f * scale, 8f * scale)
            )
            drawCircle(Color(0xFFFFEE58), radius = 8f * scale, center = Offset(center.x, lampY))

            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = android.graphics.Color.YELLOW
                    textSize = 12f * scale
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText("⬆️ JUMP", center.x, lampY - lampH * 0.8f, paint)
            }
        }

        ObstacleType.MOBILE_CART -> {
            val cartW = r * 2.0f
            val cartH = r * 2.4f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0288D1), Color(0xFF01579B))
                ),
                topLeft = Offset(center.x - cartW / 2f, center.y - cartH / 2f),
                size = Size(cartW, cartH),
                cornerRadius = CornerRadius(8f * scale, 8f * scale)
            )
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12f * scale
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText("⬇️ SLIDE", center.x, center.y - cartH * 0.6f, paint)
            }
        }

        ObstacleType.TOOTH_BARRIER -> {
            val toothW = r * 2.2f
            val toothH = r * 2.0f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFF59D), Color(0xFFEEEEEE))
                ),
                topLeft = Offset(center.x - toothW / 2f, center.y - toothH / 2f),
                size = Size(toothW, toothH),
                cornerRadius = CornerRadius(14f * scale, 14f * scale)
            )
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = android.graphics.Color.YELLOW
                    textSize = 12f * scale
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText("⬆️ JUMP", center.x, center.y - toothH * 0.6f, paint)
            }
        }

        ObstacleType.NONE -> {}
    }
}

/**
 * Draws the Dental Police Officer standing on the highway holding an illuminated red stop sign.
 */
fun DrawScope.drawDentalPoliceOfficer(
    center: Offset,
    scale: Float,
    isSaluting: Boolean
) {
    val skinColor = Color(0xFFFFCC80)
    val uniformBlue = Color(0xFF0D1B2A)
    val vestNeon = Color(0xFF76FF03)

    val headCenter = Offset(center.x, center.y - 70f * scale)

    drawRoundRect(
        color = uniformBlue,
        topLeft = Offset(headCenter.x - 24f * scale, headCenter.y - 32f * scale),
        size = Size(48f * scale, 18f * scale),
        cornerRadius = CornerRadius(4f * scale, 4f * scale)
    )
    drawCircle(Color(0xFFFFD700), radius = 5f * scale, center = Offset(headCenter.x, headCenter.y - 24f * scale))
    drawRect(
        color = Color.Black,
        topLeft = Offset(headCenter.x - 22f * scale, headCenter.y - 14f * scale),
        size = Size(44f * scale, 5f * scale)
    )

    drawCircle(skinColor, radius = 18f * scale, center = headCenter)
    drawCircle(Color.Black, radius = 2.5f * scale, center = Offset(headCenter.x - 6f * scale, headCenter.y - 2f * scale))
    drawCircle(Color.Black, radius = 2.5f * scale, center = Offset(headCenter.x + 6f * scale, headCenter.y - 2f * scale))
    drawLine(
        color = Color(0xFF3E2723),
        start = Offset(headCenter.x - 8f * scale, headCenter.y + 6f * scale),
        end = Offset(headCenter.x + 8f * scale, headCenter.y + 6f * scale),
        strokeWidth = 3f * scale
    )

    val torsoTop = center.y - 50f * scale
    val torsoH = 50f * scale
    drawRoundRect(
        color = uniformBlue,
        topLeft = Offset(center.x - 22f * scale, torsoTop),
        size = Size(44f * scale, torsoH),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )
    drawRect(
        color = vestNeon,
        topLeft = Offset(center.x - 18f * scale, torsoTop + 6f * scale),
        size = Size(36f * scale, torsoH - 12f * scale)
    )
    drawLine(
        color = Color.White,
        start = Offset(center.x - 18f * scale, torsoTop + 24f * scale),
        end = Offset(center.x + 18f * scale, torsoTop + 24f * scale),
        strokeWidth = 4f * scale
    )

    if (isSaluting) {
        drawLine(
            color = uniformBlue,
            start = Offset(center.x + 22f * scale, torsoTop + 10f * scale),
            end = Offset(center.x + 35f * scale, torsoTop - 5f * scale),
            strokeWidth = 7f * scale,
            cap = StrokeCap.Round
        )
        drawLine(
            color = skinColor,
            start = Offset(center.x + 35f * scale, torsoTop - 5f * scale),
            end = Offset(headCenter.x + 18f * scale, headCenter.y - 10f * scale),
            strokeWidth = 7f * scale,
            cap = StrokeCap.Round
        )
    } else {
        drawLine(
            color = uniformBlue,
            start = Offset(center.x + 22f * scale, torsoTop + 10f * scale),
            end = Offset(center.x + 36f * scale, torsoTop + 20f * scale),
            strokeWidth = 7f * scale,
            cap = StrokeCap.Round
        )
        drawRect(
            color = Color(0xFFFF1744),
            topLeft = Offset(center.x + 34f * scale, torsoTop - 35f * scale),
            size = Size(10f * scale, 55f * scale)
        )
        drawCircle(
            color = Color(0xFFFF5252).copy(alpha = 0.5f),
            radius = 16f * scale,
            center = Offset(center.x + 39f * scale, torsoTop - 25f * scale)
        )
    }

    val legY = torsoTop + torsoH
    drawLine(
        color = uniformBlue,
        start = Offset(center.x - 10f * scale, legY),
        end = Offset(center.x - 10f * scale, legY + 38f * scale),
        strokeWidth = 9f * scale,
        cap = StrokeCap.Round
    )
    drawLine(
        color = uniformBlue,
        start = Offset(center.x + 10f * scale, legY),
        end = Offset(center.x + 10f * scale, legY + 38f * scale),
        strokeWidth = 9f * scale,
        cap = StrokeCap.Round
    )
    drawRect(Color.Black, Offset(center.x - 18f * scale, legY + 34f * scale), Size(16f * scale, 12f * scale))
    drawRect(Color.Black, Offset(center.x + 2f * scale, legY + 34f * scale), Size(16f * scale, 12f * scale))
}

/**
 * Renders prominent protagonist Alex with dynamic running animation frames, teal scrubs, hair, and stethoscope.
 */
fun DrawScope.drawAlexPlayerCharacter(
    center: Offset,
    frame: Int,
    isJumping: Boolean,
    isSliding: Boolean,
    scale: Float = 1f
) {
    val skinColor = Color(0xFFFFCC80)
    val hairColor = Color(0xFF3E2723)
    val scrubTeal = Color(0xFF00838F)
    val scrubPant = Color(0xFF00695C)
    val shoeWhite = Color(0xFFFAFAFA)
    val shoeSole = Color(0xFF0288D1)

    val ySlideOffset = if (isSliding) 25f * scale else 0f
    val headCenter = Offset(center.x, center.y - 78f * scale + ySlideOffset)
    val torsoTop = center.y - 48f * scale + ySlideOffset

    // 1. HEAD & FACE
    drawRoundRect(
        color = hairColor,
        topLeft = Offset(headCenter.x - 22f * scale, headCenter.y - 28f * scale),
        size = Size(44f * scale, 32f * scale),
        cornerRadius = CornerRadius(14f * scale, 14f * scale)
    )
    drawCircle(color = skinColor, radius = 20f * scale, center = headCenter)
    drawCircle(color = Color.Black, radius = 2.8f * scale, center = Offset(headCenter.x - 6f * scale, headCenter.y - 2f * scale))
    drawCircle(color = Color.Black, radius = 2.8f * scale, center = Offset(headCenter.x + 6f * scale, headCenter.y - 2f * scale))
    drawLine(Color(0xFF3E2723), Offset(headCenter.x - 9f * scale, headCenter.y - 8f * scale), Offset(headCenter.x - 3f * scale, headCenter.y - 6f * scale), strokeWidth = 2f * scale)
    drawLine(Color(0xFF3E2723), Offset(headCenter.x + 3f * scale, headCenter.y - 6f * scale), Offset(headCenter.x + 9f * scale, headCenter.y - 8f * scale), strokeWidth = 2f * scale)
    drawLine(Color(0xFFB71C1C), Offset(headCenter.x - 4f * scale, headCenter.y + 8f * scale), Offset(headCenter.x + 4f * scale, headCenter.y + 8f * scale), strokeWidth = 2.5f * scale)

    // 2. TORSO (Teal scrubs with stethoscope & hospital badge)
    val torsoHeight = if (isSliding) 32f * scale else 62f * scale
    drawRoundRect(
        color = scrubTeal,
        topLeft = Offset(center.x - 20f * scale, torsoTop),
        size = Size(40f * scale, torsoHeight),
        cornerRadius = CornerRadius(8f * scale, 8f * scale)
    )
    val vNeck = Path().apply {
        moveTo(center.x - 8f * scale, torsoTop)
        lineTo(center.x, torsoTop + 14f * scale)
        lineTo(center.x + 8f * scale, torsoTop)
    }
    drawPath(vNeck, color = skinColor)

    // Stethoscope tubing around neck
    drawPath(Path().apply {
        moveTo(headCenter.x - 10f * scale, headCenter.y + 16f * scale)
        quadraticTo(center.x, torsoTop + 10f * scale, center.x + 10f * scale, headCenter.y + 16f * scale)
    }, color = Color(0xFFB0BEC5), style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round))

    // Hospital ID Badge
    drawRect(Color.White, Offset(center.x - 14f * scale, torsoTop + 16f * scale), Size(9f * scale, 12f * scale))
    drawCircle(Color(0xFF0288D1), radius = 2f * scale, center = Offset(center.x - 9.5f * scale, torsoTop + 20f * scale))

    // 3. ARMS & HANDS (Swinging animation frames 0..3)
    val armSwing = (when {
        isJumping -> -24f
        isSliding -> 28f
        else -> when (frame) {
            0 -> 18f
            1 -> 6f
            2 -> -18f
            else -> -6f
        }
    }) * scale

    // Left Arm
    drawLine(
        color = scrubTeal,
        start = Offset(center.x - 20f * scale, torsoTop + 6f * scale),
        end = Offset(center.x - 32f * scale + armSwing, torsoTop + 32f * scale),
        strokeWidth = 8f * scale,
        cap = StrokeCap.Round
    )
    drawCircle(skinColor, radius = 5f * scale, center = Offset(center.x - 32f * scale + armSwing, torsoTop + 32f * scale))

    // Right Arm
    drawLine(
        color = scrubTeal,
        start = Offset(center.x + 20f * scale, torsoTop + 6f * scale),
        end = Offset(center.x + 32f * scale - armSwing, torsoTop + 32f * scale),
        strokeWidth = 8f * scale,
        cap = StrokeCap.Round
    )
    drawCircle(skinColor, radius = 5f * scale, center = Offset(center.x + 32f * scale - armSwing, torsoTop + 32f * scale))

    // 4. LEGS & SNEAKERS (Striding animation frames 0..3)
    val torsoBottom = torsoTop + torsoHeight

    if (isSliding) {
        drawLine(
            color = scrubPant,
            start = Offset(center.x - 10f * scale, torsoBottom),
            end = Offset(center.x + 28f * scale, torsoBottom + 12f * scale),
            strokeWidth = 9f * scale,
            cap = StrokeCap.Round
        )
        drawRoundRect(Color(0xFF212121), Offset(center.x + 26f * scale, torsoBottom + 8f * scale), Size(18f * scale, 10f * scale), CornerRadius(4f * scale, 4f * scale))
    } else {
        val legSpread = (when {
            isJumping -> -14f
            else -> when (frame) {
                0 -> 22f
                1 -> 8f
                2 -> -22f
                else -> -8f
            }
        }) * scale

        val legLength = (if (isJumping) 30f else 46f) * scale

        // Left Leg
        drawLine(
            color = scrubPant,
            start = Offset(center.x - 10f * scale, torsoBottom),
            end = Offset(center.x - 12f * scale + legSpread, torsoBottom + legLength),
            strokeWidth = 9f * scale,
            cap = StrokeCap.Round
        )
        drawRoundRect(shoeWhite, Offset(center.x - 24f * scale + legSpread, torsoBottom + legLength - 4f * scale), Size(20f * scale, 10f * scale), CornerRadius(3f * scale, 3f * scale))
        drawLine(shoeSole, Offset(center.x - 24f * scale + legSpread, torsoBottom + legLength + 6f * scale), Offset(center.x - 4f * scale + legSpread, torsoBottom + legLength + 6f * scale), strokeWidth = 3f * scale)

        // Right Leg
        drawLine(
            color = scrubPant,
            start = Offset(center.x + 10f * scale, torsoBottom),
            end = Offset(center.x + 12f * scale - legSpread, torsoBottom + legLength),
            strokeWidth = 9f * scale,
            cap = StrokeCap.Round
        )
        drawRoundRect(shoeWhite, Offset(center.x + 4f * scale - legSpread, torsoBottom + legLength - 4f * scale), Size(20f * scale, 10f * scale), CornerRadius(3f * scale, 3f * scale))
        drawLine(shoeSole, Offset(center.x + 4f * scale - legSpread, torsoBottom + legLength + 6f * scale), Offset(center.x + 24f * scale - legSpread, torsoBottom + legLength + 6f * scale), strokeWidth = 3f * scale)
    }
}
