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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.endoquest.R
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
    val obstacleType: ObstacleType = ObstacleType.NONE,
    var cleared: Boolean = false,
    val elevation: Float = 0f // 0f = ground level, up to 1f for apex jump height
)

enum class ObstacleType {
    NONE,
    GUM_SOCKET,      // Jump over (Compact Broken Gum with deep empty socket hole in center)
    DECAYED_TOOTH,   // Slide under (Angry Decayed Tooth with large U-shaped arch between roots)
    ROTATING_BUR     // Dodge (Giant Rotating Dental Bur Handpiece blocking the entire lane)
}

@Composable
fun RunnerScreen(
    viewModel: GameViewModel,
    onProceedToMaze: () -> Unit,
    onRoundOver: () -> Unit,
    onGameOver: () -> Unit
) {
    val toothBitmap = ImageBitmap.imageResource(id = R.drawable.obs_decayed_tooth)
    val gumBitmap = ImageBitmap.imageResource(id = R.drawable.obs_gum_socket)
    val burBitmap = ImageBitmap.imageResource(id = R.drawable.obs_dental_bur)

    var items by remember { mutableStateOf(listOf<RunnerItem>()) }
    var nextItemId by remember { mutableStateOf(0) }
    var baseSpeed by remember { mutableStateOf(0.008f) }
    var runFrame by remember { mutableStateOf(0) }
    var coinSpinAngle by remember { mutableStateOf(0f) }
    var roadOffset by remember { mutableStateOf(0f) }

    // Jump state animation (Subway Surfers style full rise and fall)
    val jumpAnim = remember { Animatable(0f) }
    LaunchedEffect(viewModel.isJumping) {
        if (viewModel.isJumping) {
            jumpAnim.animateTo(1f, animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
            jumpAnim.animateTo(0f, animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
            viewModel.resetJump()
        } else {
            jumpAnim.snapTo(0f)
        }
    }

    // Acrobatic rolling animation state (720-degree forward somersault tumble over 750ms)
    val rollAnim = remember { Animatable(0f) }
    LaunchedEffect(viewModel.isSliding) {
        if (viewModel.isSliding) {
            rollAnim.snapTo(0f)
            rollAnim.animateTo(
                targetValue = 720f,
                animationSpec = tween(durationMillis = 750, easing = LinearEasing)
            )
            viewModel.resetActionState()
        } else {
            rollAnim.snapTo(0f)
        }
    }

    // Dental Police inspection timer: exactly every 15 seconds (15s, 30s, 45s; max 3 encounters)
    LaunchedEffect(viewModel.isPoliceEncounterActive, viewModel.isRound1Complete, viewModel.runnerResetTrigger) {
        if (!viewModel.isPoliceEncounterActive && !viewModel.isRound1Complete && viewModel.round1QuestionCount < 3) {
            val waitMillis = 15000L
            delay(waitMillis)
            if (!viewModel.isPoliceEncounterActive && !viewModel.isRound1Complete && viewModel.round1QuestionCount < 3) {
                viewModel.triggerPoliceEncounter()
            }
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
    var accumulatedDragX by remember { mutableStateOf(0f) }
    var accumulatedDragY by remember { mutableStateOf(0f) }

    // Guaranteed obstacle queue ensuring all 3 types appear in every run with natural order variation
    val guaranteedObstacleQueue = remember { mutableListOf<ObstacleType>() }
    var obstacleSpawnTimer by remember { mutableStateOf(1.2f) }
    var coinSpawnTimer by remember { mutableStateOf(0.3f) }

    fun refillGuaranteedQueue() {
        guaranteedObstacleQueue.clear()
        // Guarantees DECAYED_TOOTH, GUM_SOCKET, and ROTATING_BUR all appear in each run
        val mandatoryThree = listOf(
            ObstacleType.DECAYED_TOOTH,
            ObstacleType.GUM_SOCKET,
            ObstacleType.ROTATING_BUR
        ).shuffled()
        guaranteedObstacleQueue.addAll(mandatoryThree)
    }

    LaunchedEffect(Unit) {
        if (guaranteedObstacleQueue.isEmpty()) {
            refillGuaranteedQueue()
        }
    }

    LaunchedEffect(viewModel.runnerResetTrigger) {
        isGameOverTriggered = false
        items = emptyList()
        obstacleSpawnTimer = 1.2f
        coinSpawnTimer = 0.3f
        refillGuaranteedQueue()
    }

    // Main 3D perspective game loop (Strictly gameplay driven, ZERO bottom buttons, 100% active collisions)
    LaunchedEffect(viewModel.isPoliceEncounterActive, viewModel.runnerResetTrigger) {
        var lastTime = System.currentTimeMillis()
        while (!viewModel.isRound1Complete) {
            val now = System.currentTimeMillis()
            val dt = ((now - lastTime).toFloat() / 1000f).coerceIn(0.001f, 0.05f)
            lastTime = now

            if (!viewModel.isPoliceEncounterActive) {
                obstacleSpawnTimer -= dt
                coinSpawnTimer -= dt

                // 1. CONTROLLED OBSTACLE SPAWNER (Frequency: ~3.0s - 3.4s cadence for rhythmic, fair reaction windows)
                if (obstacleSpawnTimer <= 0f) {
                    obstacleSpawnTimer = 3.0f + Random.nextFloat() * 0.4f

                    // Guaranteed queue ensures all 3 appear first; subsequent obstacles are weighted/random
                    val obsType = if (guaranteedObstacleQueue.isNotEmpty()) {
                        guaranteedObstacleQueue.removeAt(0)
                    } else {
                        listOf(
                            ObstacleType.DECAYED_TOOTH,
                            ObstacleType.GUM_SOCKET,
                            ObstacleType.ROTATING_BUR
                        ).random()
                    }

                    val obsLane = Random.nextInt(3)
                    val newItems = mutableListOf<RunnerItem>()

                    when (obsType) {
                        ObstacleType.DECAYED_TOOTH -> {
                            // Decayed Tooth (Player must SLIDE under)
                            val baseDepth = 0.06f
                            newItems.add(
                                RunnerItem(
                                    id = nextItemId++,
                                    lane = obsLane,
                                    depth = baseDepth,
                                    isCoin = false,
                                    obstacleType = ObstacleType.DECAYED_TOOTH
                                )
                            )
                            // Slide Guide Coins: Straight line directly underneath the tooth arch tunnel
                            newItems.add(RunnerItem(id = nextItemId++, lane = obsLane, depth = baseDepth + 0.08f, isCoin = true, elevation = 0f))
                            newItems.add(RunnerItem(id = nextItemId++, lane = obsLane, depth = baseDepth, isCoin = true, elevation = 0f))
                            newItems.add(RunnerItem(id = nextItemId++, lane = obsLane, depth = baseDepth - 0.08f, isCoin = true, elevation = 0f))
                        }

                        ObstacleType.GUM_SOCKET -> {
                            // Broken Gum / Socket (Player must JUMP over)
                            val baseDepth = 0.06f
                            newItems.add(
                                RunnerItem(
                                    id = nextItemId++,
                                    lane = obsLane,
                                    depth = baseDepth,
                                    isCoin = false,
                                    obstacleType = ObstacleType.GUM_SOCKET
                                )
                            )
                            // Jump Arc Coins: Parabolic arc floating directly over the gum socket
                            newItems.add(RunnerItem(id = nextItemId++, lane = obsLane, depth = baseDepth + 0.08f, isCoin = true, elevation = 0.40f))
                            newItems.add(RunnerItem(id = nextItemId++, lane = obsLane, depth = baseDepth + 0.04f, isCoin = true, elevation = 0.75f))
                            newItems.add(RunnerItem(id = nextItemId++, lane = obsLane, depth = baseDepth, isCoin = true, elevation = 1.0f))
                            newItems.add(RunnerItem(id = nextItemId++, lane = obsLane, depth = baseDepth - 0.04f, isCoin = true, elevation = 0.75f))
                            newItems.add(RunnerItem(id = nextItemId++, lane = obsLane, depth = baseDepth - 0.08f, isCoin = true, elevation = 0.40f))
                        }

                        ObstacleType.ROTATING_BUR -> {
                            // Giant Rotating Bur (Player must CHANGE LANE / DODGE)
                            val baseDepth = 0.06f
                            val safeLane = when (obsLane) {
                                0 -> 1
                                2 -> 1
                                else -> if (Random.nextBoolean()) 0 else 2
                            }
                            newItems.add(
                                RunnerItem(
                                    id = nextItemId++,
                                    lane = obsLane,
                                    depth = baseDepth,
                                    isCoin = false,
                                    obstacleType = ObstacleType.ROTATING_BUR
                                )
                            )
                            // Lane Dodge Guide Coins: leading into the open safe adjacent lane
                            newItems.add(RunnerItem(id = nextItemId++, lane = obsLane, depth = baseDepth + 0.12f, isCoin = true, elevation = 0f))
                            newItems.add(RunnerItem(id = nextItemId++, lane = safeLane, depth = baseDepth + 0.06f, isCoin = true, elevation = 0f))
                            newItems.add(RunnerItem(id = nextItemId++, lane = safeLane, depth = baseDepth, isCoin = true, elevation = 0f))
                            newItems.add(RunnerItem(id = nextItemId++, lane = safeLane, depth = baseDepth - 0.06f, isCoin = true, elevation = 0f))
                        }

                        ObstacleType.NONE -> {}
                    }

                    items = items + newItems
                }

                // 2. IN-BETWEEN COIN FORMATIONS (Steady coin rhythm between obstacles)
                if (coinSpawnTimer <= 0f) {
                    coinSpawnTimer = 1.3f + Random.nextFloat() * 0.4f

                    // Avoid visual clutter directly atop horizon obstacles
                    val canSpawnCoins = items.none { it.depth in -0.05f..0.15f }
                    if (canSpawnCoins) {
                        val cLane = Random.nextInt(3)
                        val pattern = Random.nextInt(3)
                        val coinBurst = mutableListOf<RunnerItem>()

                        when (pattern) {
                            0 -> {
                                // 3 straight ground coins
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.10f, isCoin = true, elevation = 0f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.05f, isCoin = true, elevation = 0f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.00f, isCoin = true, elevation = 0f))
                            }
                            1 -> {
                                // 3 airborne jump coins
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.08f, isCoin = true, elevation = 0.45f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.04f, isCoin = true, elevation = 0.85f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.00f, isCoin = true, elevation = 0.45f))
                            }
                            2 -> {
                                // Gentle diagonal lane switch
                                val nextLane = if (cLane == 0) 1 else if (cLane == 2) 1 else (if (Random.nextBoolean()) 0 else 2)
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.10f, isCoin = true, elevation = 0f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.05f, isCoin = true, elevation = 0f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = nextLane, depth = 0.00f, isCoin = true, elevation = 0f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = nextLane, depth = -0.05f, isCoin = true, elevation = 0f))
                            }
                        }
                        items = items + coinBurst
                    }
                }
            }

            val updated = mutableListOf<RunnerItem>()

            for (item in items) {
                if (viewModel.isPoliceEncounterActive) break

                item.depth += baseSpeed * (dt * 60f).coerceIn(0.5f, 2.5f)
                if (item.depth < 1.08f) {
                    val itemLaneX = (item.lane - 1).toFloat()
                    val inRange = item.depth in 0.86f..0.98f && abs(itemLaneX - animatedLaneX) < 0.42f

                    if (!viewModel.isPoliceEncounterActive && inRange) {
                        if (item.isCoin) {
                            val isAirborne = viewModel.isJumping || jumpAnim.value > 0.15f
                            val isHighCoin = item.elevation > 0.30f
                            val canCollect = if (isHighCoin) isAirborne else true

                            if (canCollect && !item.cleared) {
                                item.cleared = true
                                viewModel.soundManager.playCoin()
                                viewModel.addMoney(item.value)
                                // Coin is collected, omit from updated list to remove from view
                            } else {
                                updated.add(item)
                            }
                        } else {
                            if (!item.cleared) {
                                val isAirborne = viewModel.isJumping || jumpAnim.value > 0.15f
                                val isDucking = viewModel.isSliding
                                val hit = when (item.obstacleType) {
                                    ObstacleType.GUM_SOCKET -> !isAirborne   // Jump over it!
                                    ObstacleType.DECAYED_TOOTH -> !isDucking // Slide under it!
                                    ObstacleType.ROTATING_BUR -> true        // Solid block! Must dodge lane!
                                    ObstacleType.NONE -> false
                                }

                                if (hit && !isGameOverTriggered) {
                                    isGameOverTriggered = true
                                    viewModel.soundManager.playCollision()
                                    items = emptyList()
                                    onGameOver()
                                    break
                                } else if (!hit) {
                                    // Cleared! Mark as cleared so subsequent frames don't re-trigger collision
                                    item.cleared = true
                                    updated.add(item)
                                }
                            } else {
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
            } else {
                // Clear immediate foreground obstacles while police inspection is active
                items = items.filter { it.isCoin || it.depth < 0.40f }
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
                    onDragStart = {
                        dragTriggered = false
                        accumulatedDragX = 0f
                        accumulatedDragY = 0f
                    },
                    onDragEnd = { dragTriggered = false },
                    onDragCancel = { dragTriggered = false },
                    onDrag = { change, dragAmount ->
                        if (!dragTriggered) {
                            accumulatedDragX += dragAmount.x
                            accumulatedDragY += dragAmount.y
                            val threshold = 30f
                            if (abs(accumulatedDragX) > abs(accumulatedDragY)) {
                                if (accumulatedDragX > threshold) {
                                    viewModel.moveRight()
                                    dragTriggered = true
                                    change.consume()
                                } else if (accumulatedDragX < -threshold) {
                                    viewModel.moveLeft()
                                    dragTriggered = true
                                    change.consume()
                                }
                            } else {
                                if (accumulatedDragY < -threshold) {
                                    viewModel.jump()
                                    dragTriggered = true
                                    change.consume()
                                } else if (accumulatedDragY > threshold) {
                                    viewModel.slide()
                                    dragTriggered = true
                                    change.consume()
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
                                    text = "Police Inspections",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF90CAF9)
                                )
                                Text(
                                    text = "${viewModel.round1QuestionCount}/3 (+$500)",
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
                            if (item.depth < 0f) continue // Not yet visible over the horizon

                            val depthSquared = item.depth
                            val currentY = horizonY + (bottomY - horizonY) * depthSquared
                            val currentRoadW = roadWidthTop + (roadWidthBottom - roadWidthTop) * depthSquared
                            val laneW = currentRoadW / 3f
                            val currentLeft = horizonX - currentRoadW / 2f
                            val itemX = currentLeft + laneW * (item.lane + 0.5f)
                            val scale = 0.25f + 0.95f * depthSquared

                            if (item.isCoin) {
                                val bobY = currentY + sin(coinSpinAngle * (PI / 180.0).toFloat() * 3f + item.id) * (4f * scale * s)
                                val elevationOffset = item.elevation * (100f * scale * s)
                                val coinY = bobY - elevationOffset

                                // For airborne coins (e.g. jump arcs), draw ground shadow on asphalt
                                if (item.elevation > 0.15f) {
                                    val shadowW = (13f * scale * s) * (1f - item.elevation * 0.30f)
                                    drawOval(
                                        color = Color.Black.copy(alpha = 0.28f * (1f - item.elevation * 0.40f)),
                                        topLeft = Offset(itemX - shadowW, currentY - 3f * scale * s),
                                        size = Size(shadowW * 2f, 6f * scale * s)
                                    )
                                }

                                draw3DMetallicGoldCoin(
                                    center = Offset(itemX, coinY),
                                    scale = scale * s * (1f + item.elevation * 0.12f),
                                    spinAngle = coinSpinAngle + item.id * 45f
                                )
                            } else {
                                drawDentalObstacle(
                                    center = Offset(itemX, currentY),
                                    type = item.obstacleType,
                                    scale = scale * s,
                                    toothBitmap = toothBitmap,
                                    gumBitmap = gumBitmap,
                                    burBitmap = burBitmap,
                                    spinAngle = coinSpinAngle
                                )
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
                        val jumpYOffset = jumpAnim.value * (110f * s)
                        val playerY = horizonY + (bottomY - horizonY) * playerDepth - jumpYOffset
                        val roadWAtPlayer = roadWidthTop + (roadWidthBottom - roadWidthTop) * playerDepth
                        val laneWAtPlayer = roadWAtPlayer / 3f
                        val playerX = horizonX + (animatedLaneX * laneWAtPlayer)

                        // Ground shadow
                        val shadowScale = (1f - jumpAnim.value * 0.45f) * s
                        val shadowY = horizonY + (bottomY - horizonY) * playerDepth + (if (viewModel.isSliding) 12f * s else 18f * s)
                        drawOval(
                            Color.Black.copy(alpha = 0.42f),
                            Offset(playerX - (if (viewModel.isSliding) 24f else 30f) * shadowScale, shadowY - 8f * shadowScale),
                            Size((if (viewModel.isSliding) 48f else 60f) * shadowScale, 16f * shadowScale)
                        )

                        drawAlexPlayerCharacter(
                            center = Offset(playerX, playerY),
                            frame = runFrame,
                            isJumping = viewModel.isJumping || jumpAnim.value > 0.05f,
                            isSliding = viewModel.isSliding,
                            rollAngle = rollAnim.value,
                            scale = s * 1.12f
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

            // ROUND 1 COMPLETE CELEBRATION MODAL
            if (viewModel.isRound1Complete) {
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
                            Text(text = "🏁👮‍♂️✨", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "ROUND 1 COMPLETE!",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFFFD700),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Dental Highway Cleared!\nOfficer Floss grants full clinical passage.",
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
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Coins Collected:", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("$${viewModel.progress.round1CoinEarnings}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFFD700))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Police Inspections (3/3):", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("+$1,500", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF00E5FF))
                                    }
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total Round 1 Funds:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Text("$${viewModel.progress.moneyCollected}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color(0xFFFFD700))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onProceedToMaze,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                            ) {
                                Text("Proceed to Round 2: Dental Maze ➡️", fontWeight = FontWeight.ExtraBold, color = Color.Black)
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
                                        Text("DENTAL HIGHWAY PATROL (INSPECTION ${viewModel.round1QuestionCount + 1} OF 3)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color(0xFFFF5252))
                                        Text("Officer Floss: \"Halt! Endodontic Inspection! (+$500 on pass)\"", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFD54F))
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
                                                    viewModel.submitRunQuizAnswer(idx, onRoundOver = onRoundOver)
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
                                                text = if (isPassed) "✅ INSPECTION PASSED! Officer Salutes: +$500 Reward!"
                                                       else "❌ INCORRECT ANSWER! Round Over!",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(currentQ.explanation, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (isPassed) {
                                        if (viewModel.round1QuestionCount >= 3) {
                                            Button(
                                                onClick = onProceedToMaze,
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                                            ) {
                                                Text("All 3 Passed! Proceed to Maze ➡️", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    items = items.filter { it.isCoin || it.depth < 0.40f }
                                                    viewModel.resumeRunnerFromPolice()
                                                },
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                                            ) {
                                                Text("Officer Salutes: Resume 3D Runner! 🏃‍♂️💨", fontWeight = FontWeight.ExtraBold, color = Color.Black)
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
 * Renders the 3 custom cartoon dental obstacles:
 * 1. GUM_SOCKET: Broken Gum / Empty Socket sitting directly on asphalt (Must JUMP over).
 * 2. DECAYED_TOOTH: Angry Decayed Tooth with large U-shaped arch between downward roots (Must SLIDE under).
 * 3. ROTATING_BUR: Giant Rotating Dental Bur Handpiece blocking the entire lane from ground up (Must CHANGE LANE / DODGE).
 */
fun DrawScope.drawDentalObstacle(
    center: Offset,
    type: ObstacleType,
    scale: Float,
    toothBitmap: ImageBitmap,
    gumBitmap: ImageBitmap,
    burBitmap: ImageBitmap,
    spinAngle: Float = 0f
) {
    when (type) {
        ObstacleType.GUM_SOCKET -> {
            // Compact cartoon-style Broken Gum with deep empty socket hole in center (Must JUMP over)
            val obsW = (70f * scale).toInt().coerceAtLeast(1)
            val obsH = (obsW * 346 / 548).coerceAtLeast(1)
            val groundY = center.y + 10f * scale
            val left = (center.x - obsW / 2f).toInt()
            val top = (groundY - obsH).toInt()

            // Ground contact drop shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.35f),
                topLeft = Offset(center.x - obsW * 0.52f, groundY - 4f * scale),
                size = Size(obsW * 1.04f, 8f * scale)
            )

            drawImage(
                image = gumBitmap,
                dstOffset = IntOffset(left, top),
                dstSize = IntSize(obsW, obsH),
                filterQuality = FilterQuality.High
            )
        }

        ObstacleType.DECAYED_TOOTH -> {
            // Massive cartoon-style decayed tooth with towering crown and cavernous U-shaped root arch for clear somersault rolling
            val obsW = (165f * scale).toInt().coerceAtLeast(1)
            val obsH = (obsW * 600 / 492).coerceAtLeast(1)
            val groundY = center.y + 16f * scale
            val left = (center.x - obsW / 2f).toInt()
            val top = (groundY - obsH).toInt()

            // Dual root tip drop shadows on asphalt
            drawOval(
                color = Color.Black.copy(alpha = 0.40f),
                topLeft = Offset(center.x - obsW * 0.46f, groundY - 4f * scale),
                size = Size(obsW * 0.34f, 8f * scale)
            )
            drawOval(
                color = Color.Black.copy(alpha = 0.40f),
                topLeft = Offset(center.x + obsW * 0.12f, groundY - 4f * scale),
                size = Size(obsW * 0.34f, 8f * scale)
            )

            drawImage(
                image = toothBitmap,
                dstOffset = IntOffset(left, top),
                dstSize = IntSize(obsW, obsH),
                filterQuality = FilterQuality.High
            )
        }

        ObstacleType.ROTATING_BUR -> {
            // Giant rotating dental bur handpiece blocking the player's lane from the ground up (Must CHANGE LANE)
            val obsW = (76f * scale).toInt().coerceAtLeast(1)
            val obsH = (obsW * 600 / 388).coerceAtLeast(1)
            val groundY = center.y + 12f * scale
            val left = (center.x - obsW / 2f).toInt()
            val top = (groundY - obsH).toInt()

            // Heavy base drop shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.42f),
                topLeft = Offset(center.x - obsW * 0.42f, groundY - 4f * scale),
                size = Size(obsW * 0.84f, 8f * scale)
            )

            drawImage(
                image = burBitmap,
                dstOffset = IntOffset(left, top),
                dstSize = IntSize(obsW, obsH),
                filterQuality = FilterQuality.High
            )

            // Dynamic spinning sparks & glowing rotation arcs near the bur head
            val burHeadX = center.x - obsW * 0.25f
            val burHeadY = top + obsH * 0.16f
            val sparkRadius = 14f * scale
            val rad = (spinAngle * PI / 180.0).toFloat()

            // Subtle electric blue bur aura
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                radius = sparkRadius * 1.3f,
                center = Offset(burHeadX, burHeadY)
            )

            // Rotating sparks
            for (k in 0..2) {
                val angleK = rad * 2f + k * (2f * PI.toFloat() / 3f)
                val sx = burHeadX + cos(angleK) * sparkRadius
                val sy = burHeadY + sin(angleK) * sparkRadius
                drawCircle(
                    color = Color(0xFFFF9100),
                    radius = 2.5f * scale,
                    center = Offset(sx, sy)
                )
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
 * When sliding/rolling (isSliding = true), Alex executes an athletic forward somersault roll directly underneath obstacles.
 */
fun DrawScope.drawAlexPlayerCharacter(
    center: Offset,
    frame: Int,
    isJumping: Boolean,
    isSliding: Boolean,
    rollAngle: Float = 0f,
    scale: Float = 1f
) {
    val skinColor = Color(0xFFFFCC80)
    val hairColor = Color(0xFF3E2723)
    val scrubTeal = Color(0xFF00838F)
    val scrubPant = Color(0xFF00695C)
    val shoeWhite = Color(0xFFFAFAFA)
    val shoeSole = Color(0xFF0288D1)

    if (isSliding) {
        // ==========================================
        // ACROBATIC SOMERSAULT FORWARD ROLL
        // ==========================================
        // Alex curls into an agile tumbling ball tucked right against the road surface
        val rollCenter = Offset(center.x, center.y + 4f * scale)
        val ballRadius = 22f * scale

        // Trailing speed blur streaks behind the tumbling character
        for (i in -1..1) {
            val streakY = rollCenter.y + (i * 9f * scale)
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(rollCenter.x - 34f * scale, streakY),
                end = Offset(rollCenter.x - 14f * scale, streakY),
                strokeWidth = 2.5f * scale,
                cap = StrokeCap.Round
            )
        }

        // Road friction dust puffs
        drawCircle(
            color = Color(0xFFB0BEC5).copy(alpha = 0.5f),
            radius = 6f * scale,
            center = Offset(rollCenter.x - 18f * scale, rollCenter.y + 16f * scale)
        )
        drawCircle(
            color = Color(0xFFCFD8DC).copy(alpha = 0.4f),
            radius = 4f * scale,
            center = Offset(rollCenter.x - 26f * scale, rollCenter.y + 14f * scale)
        )

        // Rotate the tucked tumbling character around its center
        withTransform({
            rotate(degrees = rollAngle, pivot = rollCenter)
        }) {
            // Outer motion spin blur ring
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.22f),
                radius = ballRadius * 1.15f,
                center = rollCenter
            )

            // 1. Tucked Torso (Curled scrub sphere)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00ACC1), scrubTeal, Color(0xFF006064)),
                    center = rollCenter,
                    radius = ballRadius
                ),
                radius = ballRadius,
                center = rollCenter
            )

            // Stethoscope tubing curled inside tuck
            drawCircle(
                color = Color(0xFFB0BEC5),
                radius = ballRadius * 0.72f,
                center = rollCenter,
                style = Stroke(width = 3f * scale)
            )

            // 2. Tucked Head (Positioned forward/down in tuck)
            val headCenter = Offset(rollCenter.x + 8f * scale, rollCenter.y - 6f * scale)
            // Hair
            drawCircle(hairColor, radius = 13f * scale, center = headCenter)
            // Face
            drawCircle(skinColor, radius = 10f * scale, center = Offset(headCenter.x + 2f * scale, headCenter.y))
            // Shut determined racing eye (> <)
            drawLine(
                Color.Black,
                Offset(headCenter.x + 5f * scale, headCenter.y - 2f * scale),
                Offset(headCenter.x + 9f * scale, headCenter.y),
                strokeWidth = 2f * scale
            )
            drawLine(
                Color.Black,
                Offset(headCenter.x + 9f * scale, headCenter.y),
                Offset(headCenter.x + 5f * scale, headCenter.y + 2f * scale),
                strokeWidth = 2f * scale
            )

            // 3. Tucked Arms (Hugging knees tightly)
            drawLine(
                color = scrubTeal,
                start = Offset(rollCenter.x - 4f * scale, rollCenter.y - 12f * scale),
                end = Offset(rollCenter.x + 4f * scale, rollCenter.y + 10f * scale),
                strokeWidth = 7f * scale,
                cap = StrokeCap.Round
            )
            drawCircle(skinColor, radius = 4f * scale, center = Offset(rollCenter.x + 4f * scale, rollCenter.y + 10f * scale))

            // 4. Tucked Legs & White Sneakers
            drawLine(
                color = scrubPant,
                start = Offset(rollCenter.x - 8f * scale, rollCenter.y + 4f * scale),
                end = Offset(rollCenter.x - 2f * scale, rollCenter.y + 14f * scale),
                strokeWidth = 8f * scale,
                cap = StrokeCap.Round
            )
            // Curled Sneaker
            drawRoundRect(
                color = shoeWhite,
                topLeft = Offset(rollCenter.x - 8f * scale, rollCenter.y + 10f * scale),
                size = Size(14f * scale, 8f * scale),
                cornerRadius = CornerRadius(3f * scale, 3f * scale)
            )
            drawLine(
                shoeSole,
                Offset(rollCenter.x - 8f * scale, rollCenter.y + 18f * scale),
                Offset(rollCenter.x + 4f * scale, rollCenter.y + 18f * scale),
                strokeWidth = 2.5f * scale
            )
        }
    } else {
        // ==========================================
        // UPRIGHT RUNNING / JUMPING
        // ==========================================
        val headCenter = Offset(center.x, center.y - 78f * scale)
        val torsoTop = center.y - 48f * scale

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
        val torsoHeight = 62f * scale
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
