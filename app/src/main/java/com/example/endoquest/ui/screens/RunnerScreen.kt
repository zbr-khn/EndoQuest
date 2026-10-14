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
    LaunchedEffect(viewModel.jumpTrigger) {
        if (viewModel.jumpTrigger > 0) {
            jumpAnim.snapTo(0f)
            jumpAnim.animateTo(1f, animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
            jumpAnim.animateTo(0f, animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
            viewModel.resetJump()
        }
    }

    // Athletic slide-under-obstacle animation (850ms duration with low body drop and full obstacle clearance)
    val slideAnim = remember { Animatable(0f) }
    LaunchedEffect(viewModel.slideTrigger) {
        if (viewModel.slideTrigger > 0) {
            slideAnim.snapTo(0f)
            slideAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
            )
            viewModel.resetSlide()
        }
    }

    // Mid-air slide cancels jump and dives directly into a ground slide
    LaunchedEffect(viewModel.isSliding) {
        if (viewModel.isSliding) {
            jumpAnim.snapTo(0f)
        }
    }

    val isDucking = viewModel.isSliding || slideAnim.isRunning || slideAnim.value in 0.001f..0.999f

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
        jumpAnim.snapTo(0f)
        slideAnim.snapTo(0f)
    }

    // Main 3D perspective game loop
    LaunchedEffect(viewModel.isPoliceEncounterActive, viewModel.runnerResetTrigger) {
        var lastTime = System.currentTimeMillis()
        while (!viewModel.isRound1Complete) {
            val now = System.currentTimeMillis()
            val dt = ((now - lastTime).toFloat() / 1000f).coerceIn(0.001f, 0.05f)
            lastTime = now

            if (!viewModel.isPoliceEncounterActive) {
                obstacleSpawnTimer -= dt
                coinSpawnTimer -= dt

                // 1. LANE-BASED PLAYABLE OBSTACLE GENERATOR (Fair reaction windows: ~3.0s - 3.4s cadence)
                if (obstacleSpawnTimer <= 0f) {
                    obstacleSpawnTimer = 3.0f + Random.nextFloat() * 0.4f

                    // Guarantees all 3 obstacles appear first; subsequent waves use playable single or double patterns
                    val baseDepth = 0.05f
                    val primaryType = if (guaranteedObstacleQueue.isNotEmpty()) {
                        guaranteedObstacleQueue.removeAt(0)
                    } else {
                        listOf(
                            ObstacleType.DECAYED_TOOTH,
                            ObstacleType.GUM_SOCKET,
                            ObstacleType.ROTATING_BUR
                        ).random()
                    }

                    val primaryLane = Random.nextInt(3)
                    val newItems = mutableListOf<RunnerItem>()

                    // 40% chance of double pattern only after guaranteed wave has been served
                    val useDoublePattern = guaranteedObstacleQueue.isEmpty() && Random.nextFloat() < 0.40f

                    if (!useDoublePattern) {
                        when (primaryType) {
                            ObstacleType.DECAYED_TOOTH -> {
                                newItems.add(
                                    RunnerItem(
                                        id = nextItemId++,
                                        lane = primaryLane,
                                        depth = baseDepth,
                                        isCoin = false,
                                        obstacleType = ObstacleType.DECAYED_TOOTH
                                    )
                                )
                                // Slide Guide Coins: Straight line directly underneath the tooth arch tunnel
                                newItems.add(RunnerItem(id = nextItemId++, lane = primaryLane, depth = baseDepth + 0.08f, isCoin = true, elevation = 0f))
                                newItems.add(RunnerItem(id = nextItemId++, lane = primaryLane, depth = baseDepth, isCoin = true, elevation = 0f))
                                newItems.add(RunnerItem(id = nextItemId++, lane = primaryLane, depth = baseDepth - 0.08f, isCoin = true, elevation = 0f))
                            }

                            ObstacleType.GUM_SOCKET -> {
                                newItems.add(
                                    RunnerItem(
                                        id = nextItemId++,
                                        lane = primaryLane,
                                        depth = baseDepth,
                                        isCoin = false,
                                        obstacleType = ObstacleType.GUM_SOCKET
                                    )
                                )
                                // Jump Arc Coins: Parabolic arc floating directly over the gum socket
                                newItems.add(RunnerItem(id = nextItemId++, lane = primaryLane, depth = baseDepth + 0.08f, isCoin = true, elevation = 0.40f))
                                newItems.add(RunnerItem(id = nextItemId++, lane = primaryLane, depth = baseDepth + 0.04f, isCoin = true, elevation = 0.75f))
                                newItems.add(RunnerItem(id = nextItemId++, lane = primaryLane, depth = baseDepth, isCoin = true, elevation = 1.0f))
                                newItems.add(RunnerItem(id = nextItemId++, lane = primaryLane, depth = baseDepth - 0.04f, isCoin = true, elevation = 0.75f))
                                newItems.add(RunnerItem(id = nextItemId++, lane = primaryLane, depth = baseDepth - 0.08f, isCoin = true, elevation = 0.40f))
                            }

                            ObstacleType.ROTATING_BUR -> {
                                val safeLane = when (primaryLane) {
                                    0 -> 1
                                    2 -> 1
                                    else -> if (Random.nextBoolean()) 0 else 2
                                }
                                newItems.add(
                                    RunnerItem(
                                        id = nextItemId++,
                                        lane = primaryLane,
                                        depth = baseDepth,
                                        isCoin = false,
                                        obstacleType = ObstacleType.ROTATING_BUR
                                    )
                                )
                                // Lane Dodge Guide Coins: leading into the open safe adjacent lane
                                newItems.add(RunnerItem(id = nextItemId++, lane = primaryLane, depth = baseDepth + 0.12f, isCoin = true, elevation = 0f))
                                newItems.add(RunnerItem(id = nextItemId++, lane = safeLane, depth = baseDepth + 0.06f, isCoin = true, elevation = 0f))
                                newItems.add(RunnerItem(id = nextItemId++, lane = safeLane, depth = baseDepth, isCoin = true, elevation = 0f))
                                newItems.add(RunnerItem(id = nextItemId++, lane = safeLane, depth = baseDepth - 0.06f, isCoin = true, elevation = 0f))
                            }

                            ObstacleType.NONE -> {}
                        }
                    } else {
                        // Double Blocker Pattern: 2 lanes blocked, 1 lane guaranteed safe and open
                        val openLane = Random.nextInt(3)
                        val blockedLanes = (0..2).filter { it != openLane }

                        for (lane in blockedLanes) {
                            val type = if (Random.nextBoolean()) ObstacleType.ROTATING_BUR else ObstacleType.GUM_SOCKET
                            newItems.add(RunnerItem(id = nextItemId++, lane = lane, depth = baseDepth, isCoin = false, obstacleType = type))
                        }

                        // Open Lane Guide Coins
                        newItems.add(RunnerItem(id = nextItemId++, lane = openLane, depth = baseDepth + 0.08f, isCoin = true, elevation = 0f))
                        newItems.add(RunnerItem(id = nextItemId++, lane = openLane, depth = baseDepth, isCoin = true, elevation = 0f))
                        newItems.add(RunnerItem(id = nextItemId++, lane = openLane, depth = baseDepth - 0.08f, isCoin = true, elevation = 0f))
                    }

                    items = items + newItems
                }

                // 2. IN-BETWEEN COIN FORMATIONS
                if (coinSpawnTimer <= 0f) {
                    coinSpawnTimer = 1.3f + Random.nextFloat() * 0.4f

                    // Avoid clutter atop horizon obstacles
                    val canSpawnCoins = items.none { it.depth in -0.05f..0.15f }
                    if (canSpawnCoins) {
                        val cLane = Random.nextInt(3)
                        val pattern = Random.nextInt(3)
                        val coinBurst = mutableListOf<RunnerItem>()

                        when (pattern) {
                            0 -> {
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.10f, isCoin = true, elevation = 0f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.05f, isCoin = true, elevation = 0f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.00f, isCoin = true, elevation = 0f))
                            }
                            1 -> {
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.08f, isCoin = true, elevation = 0.45f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.04f, isCoin = true, elevation = 0.85f))
                                coinBurst.add(RunnerItem(id = nextItemId++, lane = cLane, depth = 0.00f, isCoin = true, elevation = 0.45f))
                            }
                            2 -> {
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

            val isAirborneNow = viewModel.isJumping || jumpAnim.isRunning || jumpAnim.value > 0.15f
            val isDuckingNow = viewModel.isSliding || slideAnim.isRunning || slideAnim.value in 0.001f..0.999f

            val updated = mutableListOf<RunnerItem>()

            for (item in items) {
                if (viewModel.isPoliceEncounterActive) break

                item.depth += baseSpeed * (dt * 60f).coerceIn(0.5f, 2.5f)
                if (item.depth < 1.08f) {
                    val itemLaneX = (item.lane - 1).toFloat()
                    val sameLane = abs(itemLaneX - animatedLaneX) < 0.42f

                    if (!viewModel.isPoliceEncounterActive) {
                        if (item.isCoin) {
                            val inCoinRange = item.depth in 0.84f..0.98f && sameLane
                            if (inCoinRange && !item.cleared) {
                                val isHighCoin = item.elevation > 0.30f
                                val canCollect = if (isHighCoin) isAirborneNow else true

                                if (canCollect) {
                                    item.cleared = true
                                    viewModel.soundManager.playCoin()
                                    viewModel.addMoney(item.value)
                                } else {
                                    updated.add(item)
                                }
                            } else {
                                updated.add(item)
                            }
                        } else {
                            if (!item.cleared) {
                                if (sameLane) {
                                    when (item.obstacleType) {
                                        ObstacleType.DECAYED_TOOTH -> {
                                            // Player slides under the hollow U-shaped root archway!
                                            // If the player is sliding anytime while approaching or passing under (0.80f..0.98f),
                                            // the tooth is successfully cleared!
                                            if (isDuckingNow && item.depth in 0.80f..0.98f) {
                                                item.cleared = true
                                                updated.add(item)
                                            } else if (item.depth >= 0.90f && !isDuckingNow) {
                                                // Reached the tooth upright or jumped into the crown: COLLISION!
                                                if (!isGameOverTriggered) {
                                                    isGameOverTriggered = true
                                                    viewModel.soundManager.playCollision()
                                                    items = emptyList()
                                                    onGameOver()
                                                    break
                                                }
                                            } else {
                                                // Approaching (< 0.90f); player can still slide on time
                                                updated.add(item)
                                            }
                                        }

                                        ObstacleType.GUM_SOCKET -> {
                                            // Player jumps over the deep socket hole!
                                            // If the player is airborne while passing over (0.80f..0.98f),
                                            // the socket is successfully cleared!
                                            if (isAirborneNow && item.depth in 0.80f..0.98f) {
                                                item.cleared = true
                                                updated.add(item)
                                            } else if (item.depth >= 0.90f && !isAirborneNow) {
                                                // Reached socket hole on the ground without jumping: COLLISION!
                                                if (!isGameOverTriggered) {
                                                    isGameOverTriggered = true
                                                    viewModel.soundManager.playCollision()
                                                    items = emptyList()
                                                    onGameOver()
                                                    break
                                                }
                                            } else {
                                                // Approaching (< 0.90f); player can still jump on time
                                                updated.add(item)
                                            }
                                        }

                                        ObstacleType.ROTATING_BUR -> {
                                            // Solid bur drill blocking the lane! Must change lane!
                                            if (item.depth >= 0.90f) {
                                                // Reached the rotating bur in the same lane: COLLISION!
                                                if (!isGameOverTriggered) {
                                                    isGameOverTriggered = true
                                                    viewModel.soundManager.playCollision()
                                                    items = emptyList()
                                                    onGameOver()
                                                    break
                                                }
                                            } else {
                                                // Approaching in same lane (< 0.90f); player can still switch lanes
                                                updated.add(item)
                                            }
                                        }

                                        ObstacleType.NONE -> {
                                            updated.add(item)
                                        }
                                    }
                                } else {
                                    // Player dodged to an adjacent safe lane!
                                    if (item.depth >= 0.92f) {
                                        item.cleared = true
                                    }
                                    updated.add(item)
                                }
                            } else {
                                // Already cleared, continues moving towards camera into foreground
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
                            val threshold = 22f
                            val absX = abs(accumulatedDragX)
                            val absY = abs(accumulatedDragY)

                            if (absX >= threshold || absY >= threshold) {
                                if (absY >= absX) {
                                    if (accumulatedDragY > 0) {
                                        viewModel.slide()
                                    } else {
                                        viewModel.jump()
                                    }
                                } else {
                                    if (accumulatedDragX > 0) {
                                        viewModel.moveRight()
                                    } else {
                                        viewModel.moveLeft()
                                    }
                                }
                                dragTriggered = true
                                change.consume()
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
                // Top Bar: Money Counter & Inspections
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

                // 3D-PERSPECTIVE RUNNER ARENA CANVAS
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

                        // Helper for drawing a single item
                        fun drawRunnerItemInstance(item: RunnerItem) {
                            if (item.depth < 0f) return
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

                        // Player constants
                        val playerDepth = 0.92f
                        val jumpYOffset = jumpAnim.value * (110f * s)
                        val playerY = horizonY + (bottomY - horizonY) * playerDepth - jumpYOffset
                        val roadWAtPlayer = roadWidthTop + (roadWidthBottom - roadWidthTop) * playerDepth
                        val laneWAtPlayer = roadWAtPlayer / 3f
                        val playerX = horizonX + (animatedLaneX * laneWAtPlayer)

                        // 6a. DISTANT ITEMS (depth <= playerDepth)
                        val distantItems = items.filter { it.depth <= playerDepth }.sortedBy { it.depth }
                        for (item in distantItems) {
                            drawRunnerItemInstance(item)
                        }

                        // 7. DENTAL POLICE OFFICER (Approaching in distance or intercepted)
                        if (viewModel.isPoliceEncounterActive) {
                            val policeDepth = 0.3f + 0.58f * viewModel.policeSprintProgress
                            val pCenter = horizonX
                            val pY = horizonY + (bottomY - horizonY) * policeDepth
                            val pScale = (0.35f + 0.95f * policeDepth) * s
                            drawDentalPoliceOfficer(Offset(pCenter, pY), pScale, viewModel.policeSaluting)
                        }

                        // 8. PROMINENT HUMAN PLAYER CHARACTER: ALEX (Drawn at depth 0.92f)
                        val shadowScale = (1f - jumpAnim.value * 0.45f) * s
                        val shadowY = horizonY + (bottomY - horizonY) * playerDepth + (if (isDucking) 24f * s else 28f * s)
                        drawOval(
                            Color.Black.copy(alpha = 0.42f),
                            Offset(playerX - (if (isDucking) 26f else 30f) * shadowScale, shadowY - 8f * shadowScale),
                            Size((if (isDucking) 52f else 60f) * shadowScale, 16f * shadowScale)
                        )

                        drawAlexPlayerCharacter(
                            center = Offset(playerX, playerY),
                            frame = runFrame,
                            isJumping = viewModel.isJumping || jumpAnim.value > 0.05f,
                            isSliding = isDucking,
                            slideProgress = slideAnim.value,
                            scale = s * 1.12f
                        )

                        // 6b. FOREGROUND ITEMS (depth > playerDepth: passing OVER/PAST player towards camera)
                        // When Decayed Tooth is in foreground, its transparent archway allows Alex underneath
                        // to remain visible while the tooth crown and roots correctly occlude background
                        val foregroundItems = items.filter { it.depth > playerDepth }.sortedBy { it.depth }
                        for (item in foregroundItems) {
                            drawRunnerItemInstance(item)
                        }
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

            // ROUND 1 COMPLETE MODAL
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
                                        Text("Police Inspections Passed:", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("${viewModel.progress.round1QuestionsCorrect}/3 (+$${viewModel.progress.round1QuestionsCorrect * 500})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF00E5FF))
                                    }
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Round 1 Total Money:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Text("$${viewModel.progress.round1TotalMoney}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color(0xFFFFD700))
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
                                Text("Proceed to Round 2: Three-Door Maze 🧩➡️", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }
                    }
                }
            }

            // DENTAL POLICE INSPECTION MODAL (at 15s, 30s, 45s)
            if (viewModel.isPoliceEncounterActive && viewModel.policeSprintProgress >= 0.95f) {
                val currentQ = viewModel.currentRunQuestion
                val shuffledOptions = viewModel.currentRunShuffledOptions
                val passed = viewModel.runCheckpointPassed

                if (currentQ != null) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).padding(18.dp),
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "👮‍♂️", fontSize = 28.sp)
                                        Column {
                                            Text(
                                                text = "OFFICER FLOSS INSPECTION ${viewModel.round1QuestionCount + 1}/3",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = Color(0xFF00E5FF)
                                            )
                                            Text("Clinical Highway Checkpoint (+ $500)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFD700))
                                        }
                                    }
                                }

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
                                        val isSelected = viewModel.selectedRunOptionIndex == idx
                                        val btnColor = when {
                                            passed == true && isSelected -> Color(0xFF2E7D32)
                                            passed == false && isSelected -> Color(0xFFC62828)
                                            isSelected -> Color(0xFF0288D1)
                                            else -> Color(0xFF243044)
                                        }

                                        Button(
                                            onClick = {
                                                if (passed == null) {
                                                    viewModel.submitRunQuizAnswer(idx, onRoundOver = onRoundOver)
                                                }
                                            },
                                            enabled = passed == null,
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = btnColor,
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

                                if (passed == true) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "✅ Inspection Passed! +$500 Added!",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFFFD700)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = currentQ.explanation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = { viewModel.resumeRunnerFromPolice() },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                                    ) {
                                        Text("Resume Highway Run 🏃‍♂️💨", fontWeight = FontWeight.ExtraBold, color = Color.Black)
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
 * Draws 3D Metallic Gold Coins with edge bevels and dollar embossing.
 */
fun DrawScope.draw3DMetallicGoldCoin(
    center: Offset,
    scale: Float,
    spinAngle: Float
) {
    val r = 13f * scale
    val normAngle = (spinAngle % 180f) / 180f
    val cosVal = abs(cos(normAngle * PI.toFloat()))
    val coinW = (r * 2f * (0.15f + 0.85f * cosVal)).coerceAtLeast(2f)

    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFE65100), Color(0xFFFF8F00), Color(0xFFFFB300)),
            startX = center.x - coinW / 2f - 2f * scale,
            endX = center.x + coinW / 2f + 2f * scale
        ),
        topLeft = Offset(center.x - coinW / 2f - 1.5f * scale, center.y - r),
        size = Size(coinW + 3f * scale, r * 2f)
    )

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFF8F00)),
            center = Offset(center.x - coinW * 0.2f, center.y - r * 0.2f),
            radius = r * 1.2f
        ),
        topLeft = Offset(center.x - coinW / 2f, center.y - r),
        size = Size(coinW, r * 2f)
    )

    if (coinW > r * 0.8f) {
        drawOval(
            color = Color(0xFFFFF176).copy(alpha = 0.7f),
            topLeft = Offset(center.x - coinW * 0.35f, center.y - r * 0.7f),
            size = Size(coinW * 0.7f, r * 1.4f),
            style = Stroke(width = 1.5f * scale)
        )
    }

    if (coinW > r * 1.1f) {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = r * 1.2f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                alpha = 230
            }
            canvas.nativeCanvas.drawText("$", center.x, center.y + r * 0.42f, paint)
        }
    }
}

/**
 * Draws the 3 cartoon obstacles with ground alignment and drop shadows.
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
            val obsW = (74f * scale).toInt().coerceAtLeast(1)
            val obsH = (obsW * 346 / 548).coerceAtLeast(1)
            val groundY = center.y + 54f * scale
            val left = (center.x - obsW / 2f).toInt()
            val top = (groundY - obsH).toInt()

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
            // Massive cartoon-style decayed tooth with towering crown and cavernous U-shaped root arch
            // Root archway clearance is 84f * scale above the asphalt contact line
            val obsW = (165f * scale).toInt().coerceAtLeast(1)
            val obsH = (obsW * 600 / 492).coerceAtLeast(1)
            val groundY = center.y + 54f * scale
            val left = (center.x - obsW / 2f).toInt()
            val top = (groundY - obsH).toInt()

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
            val obsW = (76f * scale).toInt().coerceAtLeast(1)
            val obsH = (obsW * 600 / 388).coerceAtLeast(1)
            val groundY = center.y + 54f * scale
            val left = (center.x - obsW / 2f).toInt()
            val top = (groundY - obsH).toInt()

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

            val burHeadX = center.x - obsW * 0.25f
            val burHeadY = top + obsH * 0.16f
            val sparkRadius = 14f * scale
            val rad = (spinAngle * PI / 180.0).toFloat()

            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                radius = sparkRadius * 1.3f,
                center = Offset(burHeadX, burHeadY)
            )

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
 * Renders protagonist Alex.
 * When sliding (isSliding = true), Alex executes an athletic runner slide dropping their entire body flat
 * onto the road surface, ensuring head, hair, torso, arms, and legs pass completely clear under obstacle archways.
 */
fun DrawScope.drawAlexPlayerCharacter(
    center: Offset,
    frame: Int,
    isJumping: Boolean,
    isSliding: Boolean,
    slideProgress: Float = 0f,
    scale: Float = 1f
) {
    val skinColor = Color(0xFFFFCC80)
    val hairColor = Color(0xFF3E2723)
    val scrubTeal = Color(0xFF00838F)
    val scrubPant = Color(0xFF00695C)
    val shoeWhite = Color(0xFFFAFAFA)
    val shoeSole = Color(0xFF0288D1)

    val asphaltY = center.y + 54f * scale

    if (isSliding) {
        // =========================================================================
        // ATHLETIC LOW GROUND SLIDE (Passing completely under archway without clipping)
        // Highest point is only ~26f * scale above asphalt (clearance is 84f * scale)
        // =========================================================================
        val slideCenter = Offset(center.x, asphaltY - 14f * scale)

        // Trailing speed blur streaks along the asphalt
        for (i in -1..1) {
            val streakY = slideCenter.y + (i * 6f * scale)
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(slideCenter.x - 38f * scale, streakY),
                end = Offset(slideCenter.x - 16f * scale, streakY),
                strokeWidth = 2.5f * scale,
                cap = StrokeCap.Round
            )
        }

        // Road friction dust puffs kicking behind
        drawCircle(
            color = Color(0xFFB0BEC5).copy(alpha = 0.5f),
            radius = 5f * scale,
            center = Offset(slideCenter.x - 22f * scale, asphaltY - 4f * scale)
        )
        drawCircle(
            color = Color(0xFFCFD8DC).copy(alpha = 0.4f),
            radius = 3.5f * scale,
            center = Offset(slideCenter.x - 32f * scale, asphaltY - 6f * scale)
        )

        // 1. Sliding Legs & Sneakers (Extended flat along asphalt)
        val legLeftX = slideCenter.x - 24f * scale
        val legRightX = slideCenter.x + 2f * scale
        val legY = asphaltY - 6f * scale

        drawLine(
            color = scrubPant,
            start = Offset(slideCenter.x - 6f * scale, slideCenter.y + 2f * scale),
            end = Offset(legLeftX, legY),
            strokeWidth = 8f * scale,
            cap = StrokeCap.Round
        )
        drawRoundRect(
            color = shoeWhite,
            topLeft = Offset(legLeftX - 14f * scale, legY - 4f * scale),
            size = Size(16f * scale, 8f * scale),
            cornerRadius = CornerRadius(3f * scale, 3f * scale)
        )
        drawLine(
            color = shoeSole,
            start = Offset(legLeftX - 14f * scale, legY + 4f * scale),
            end = Offset(legLeftX + 2f * scale, legY + 4f * scale),
            strokeWidth = 2.5f * scale
        )

        // 2. Horizontal Sliding Torso (Teal scrubs flat above road)
        drawRoundRect(
            color = scrubTeal,
            topLeft = Offset(slideCenter.x - 12f * scale, slideCenter.y - 10f * scale),
            size = Size(26f * scale, 18f * scale),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )

        // Stethoscope tube visible on chest
        drawLine(
            color = Color(0xFFB0BEC5),
            start = Offset(slideCenter.x - 4f * scale, slideCenter.y - 6f * scale),
            end = Offset(slideCenter.x + 8f * scale, slideCenter.y - 4f * scale),
            strokeWidth = 2.5f * scale,
            cap = StrokeCap.Round
        )

        // 3. Lowered Head & Hair (Facing forward, tucked close to asphalt)
        val headCenter = Offset(slideCenter.x + 18f * scale, slideCenter.y - 4f * scale)
        val headRadius = 11f * scale
        // Hair
        drawCircle(hairColor, radius = headRadius * 1.15f, center = Offset(headCenter.x - 2f * scale, headCenter.y - 2f * scale))
        // Face
        drawCircle(skinColor, radius = headRadius, center = headCenter)

        // Focused racing eyes (> <)
        drawLine(
            Color.Black,
            Offset(headCenter.x + 3f * scale, headCenter.y - 3f * scale),
            Offset(headCenter.x + 7f * scale, headCenter.y - 1f * scale),
            strokeWidth = 2f * scale
        )
        drawLine(
            Color.Black,
            Offset(headCenter.x + 7f * scale, headCenter.y - 1f * scale),
            Offset(headCenter.x + 3f * scale, headCenter.y + 1f * scale),
            strokeWidth = 2f * scale
        )

        // 4. Arms Reaching Forward Low (Sliding posture)
        drawLine(
            color = scrubTeal,
            start = Offset(slideCenter.x + 4f * scale, slideCenter.y - 2f * scale),
            end = Offset(slideCenter.x + 28f * scale, slideCenter.y + 6f * scale),
            strokeWidth = 6.5f * scale,
            cap = StrokeCap.Round
        )
        drawCircle(skinColor, radius = 4f * scale, center = Offset(slideCenter.x + 28f * scale, slideCenter.y + 6f * scale))
    } else {
        // =========================================================================
        // UPRIGHT RUNNING / JUMPING
        // =========================================================================
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

        // 3. ARMS & HANDS
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

        // 4. LEGS & SNEAKERS
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
