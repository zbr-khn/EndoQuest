package com.example.endoquest.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.sin

data class CinematicStoryScene(
    val sceneNumber: Int,
    val sceneTag: String,
    val comicBadge: String,
    val title: String,
    val speaker: String,
    val dialogue: String,
    val description: String
)

@Composable
fun StoryScreen(
    onProceedToRunner: () -> Unit
) {
    var sceneIndex by remember { mutableStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "storyPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePulse"
    )

    val painZapOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zapShake"
    )

    val comicBadgeBounce by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "comicBadgeBounce"
    )

    val scenes = listOf(
        CinematicStoryScene(
            sceneNumber = 1,
            sceneTag = "CHAPTER 1: DOWNTOWN",
            comicBadge = "☀️ SUNNY DOWNTOWN 🏙️",
            title = "Radiant Smile Dental Clinic",
            speaker = "Narrator",
            dialogue = "\"A sunny morning in the city, where state-of-the-art endodontic dental care awaits...\"",
            description = "The sparkling glass facade of Radiant Smile Dental Clinic gleams under the bright morning sun."
        ),
        CinematicStoryScene(
            sceneNumber = 2,
            sceneTag = "CHAPTER 2: 3:00 AM CRISIS",
            comicBadge = "⚡💥 THROB! OUCH! 💥⚡",
            title = "Midnight Agony",
            speaker = "Alex",
            dialogue = "\"OWWWWW! My molar feels like it's EXPLODING! No painkiller is touching this throbbing fire!\"",
            description = "At 3:00 AM, excruciating pain shocks Alex as acute irreversible pulpitis inflames the lower molar."
        ),
        CinematicStoryScene(
            sceneNumber = 3,
            sceneTag = "CHAPTER 3: CLINIC ARRIVAL",
            comicBadge = "🚨 EMERGENCY RUSH! 🏃💨",
            title = "Emergency Arrival",
            speaker = "Alex",
            dialogue = "\"Doctor, please help! The throbbing nerve pain is unbearable! I can't take this another second!\"",
            description = "Clutching the swollen jaw in sheer agony, Alex charges into the emergency clinic reception."
        ),
        CinematicStoryScene(
            sceneNumber = 4,
            sceneTag = "CHAPTER 4: DR. SMILE",
            comicBadge = "🩺 DR. SMILE READY ✨",
            title = "Dr. Smile's Operatory",
            speaker = "Dr. Smile",
            dialogue = "\"Take a deep breath and have a seat in the chair, Alex. Let's see what's happening to that molar.\"",
            description = "Professional endodontist Dr. Smile stands ready in pristine clinical scrubs with precision diagnostic tools."
        ),
        CinematicStoryScene(
            sceneNumber = 5,
            sceneTag = "CHAPTER 5: EXAMINATION",
            comicBadge = "🔍 OPERATORY CHECK 🩻",
            title = "Dental Operatory Check",
            speaker = "Dr. Smile",
            dialogue = "\"Open wide. Let me inspect that deep carious lesion with the dental mirror and explorer...\"",
            description = "Under the focused beam of the surgical lamp, Dr. Smile evaluates severe pulp sensitivity."
        ),
        CinematicStoryScene(
            sceneNumber = 6,
            sceneTag = "CHAPTER 6: DIAGNOSIS",
            comicBadge = "⚠️ IRREVERSIBLE PULPITIS 🦷",
            title = "Diagnosis: RCT Required",
            speaker = "Dr. Smile",
            dialogue = "\"The radiograph confirms Irreversible Pulpitis. You need an immediate Root Canal Treatment. Total cost: $5,000.\"",
            description = "Dr. Smile reviews the periapical radiograph and presents the itemized treatment plan: $5,000."
        ),
        CinematicStoryScene(
            sceneNumber = 7,
            sceneTag = "CHAPTER 7: STICKER SHOCK",
            comicBadge = "😱 $5,000?!?! 💸💸",
            title = "The $5,000 Sticker Shock",
            speaker = "Alex",
            dialogue = "\"WHAT?!?! FIVE THOUSAND DOLLARS?! For just ONE tooth?! I don't have that kind of money!\"",
            description = "Alex's jaw literally hits the floor in pure comic shock at the staggering $5,000 price tag!"
        ),
        CinematicStoryScene(
            sceneNumber = 8,
            sceneTag = "CHAPTER 8: THE RUNNER CALL",
            comicBadge = "🔥 HIGHWAY SPRINT COMMENCE! 🏃💨",
            title = "\"I'll Be Back With the Money!\"",
            speaker = "Alex",
            dialogue = "\"Hold my dental chair, Dr. Smile! I'll sprint across the city highway and collect every single coin!\"",
            description = "Determined to save the natural molar, Alex bolts out into the 3-lane 3D highway to collect gold coins!"
        )
    )

    val currentScene = scenes[sceneIndex]

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = Color(0xFF070B16)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF091423), Color(0xFF132238), Color(0xFF1C2C44))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP HEADER: Chapter Badge & Progress Bar & Skip
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF102135),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = currentScene.sceneTag,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                ),
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Button(
                            onClick = onProceedToRunner,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E2D44),
                                contentColor = Color(0xFFFFD54F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Skip ⏩", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in scenes.indices) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .height(5.dp)
                                    .width(if (i == sceneIndex) 26.dp else 10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (i == sceneIndex) Color(0xFF00E5FF)
                                        else if (i < sceneIndex) Color(0xFFFFD54F)
                                        else Color.White.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                }

                // CENTER COMIC CANVAS CARD
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2A)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1E3048)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Custom Canvas Artwork
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(22.dp))
                                .scale(if (sceneIndex == 1) 1f else pulseScale)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawStorySceneArt(sceneIndex, size, painZapOffset)
                            }
                        }

                        // Floating Comic Sound-Effect Action Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = when (sceneIndex) {
                                1 -> Color(0xFFFF1744)
                                6 -> Color(0xFFFF5252)
                                7 -> Color(0xFFFF9100)
                                else -> Color(0xFF00B0FF)
                            },
                            shadowElevation = 12.dp,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .scale(comicBadgeBounce)
                        ) {
                            Text(
                                text = currentScene.comicBadge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Title Bar Over Artwork
                        Surface(
                            shape = RoundedCornerShape(bottomEnd = 16.dp),
                            color = Color(0xFF0A1320).copy(alpha = 0.88f),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = currentScene.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color(0xFFFFD54F),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // COMIC DIALOGUE BALLOON CARD
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF152236)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF233B58)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Speaker Badge
                            Surface(
                                shape = CircleShape,
                                color = when (currentScene.speaker) {
                                    "Dr. Smile" -> Color(0xFF00B4D8)
                                    "Alex" -> Color(0xFFFF9F1C)
                                    else -> Color(0xFF9D4EDD)
                                },
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = when (currentScene.speaker) {
                                            "Dr. Smile" -> "👨‍⚕️"
                                            "Alex" -> "🏃"
                                            else -> "🎙️"
                                        },
                                        fontSize = 17.sp
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = currentScene.speaker.uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Comic Dialogue Text
                        Text(
                            text = currentScene.dialogue,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 20.sp
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Narrative Lore Subtext
                        Text(
                            text = currentScene.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF90A4AE)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // BOTTOM NAVIGATION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (sceneIndex > 0) {
                        OutlinedButton(
                            onClick = { sceneIndex-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF132034),
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF37474F))
                        ) {
                            Text("⬅️ Back", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (sceneIndex < scenes.size - 1) {
                                sceneIndex++
                            } else {
                                onProceedToRunner()
                            }
                        },
                        modifier = Modifier
                            .weight(if (sceneIndex > 0) 2f else 1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sceneIndex == scenes.size - 1) Color(0xFFFFD700) else Color(0xFF00E5FF)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (sceneIndex < scenes.size - 1) "Next Scene ➡️" else "START 3D RUNNER! 🏃💨",
                                color = Color(0xFF051722),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Renders custom Canvas artwork for all 8 narrative scenes with comic visual flourishes.
 */
fun DrawScope.drawStorySceneArt(sceneIndex: Int, size: Size, shakeOffset: Float) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val s = (min(w, h) / 300f).coerceIn(1f, 3.5f)

    when (sceneIndex) {
        0 -> {
            // SCENE 1: Modern dental clinic exterior with sunbeams
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0288D1), Color(0xFF4FC3F7), Color(0xFFE0F7FA))
                ),
                size = size
            )
            // Glowing Sun
            drawCircle(Color(0xFFFFF176), radius = 32f * s, center = Offset(w * 0.82f, h * 0.18f))
            drawCircle(Color(0xFFFFD54F), radius = 24f * s, center = Offset(w * 0.82f, h * 0.18f))

            // Fluffy Clouds
            drawCircle(Color.White.copy(alpha = 0.90f), radius = 24f * s, center = Offset(w * 0.22f, h * 0.16f))
            drawCircle(Color.White.copy(alpha = 0.90f), radius = 32f * s, center = Offset(w * 0.32f, h * 0.14f))

            // Ground & Lawn
            val groundY = h * 0.68f
            drawRect(Color(0xFF388E3C), topLeft = Offset(0f, groundY), size = Size(w, h - groundY))
            drawRect(Color(0xFF78909C), topLeft = Offset(0f, groundY + 28f * s), size = Size(w, h - groundY - 28f * s))

            // Clinic Building
            val bWidth = (210f * s).coerceAtMost(w * 0.88f)
            val bHeight = 155f * s
            val bLeft = cx - bWidth / 2f
            val bTop = groundY - bHeight + 10f * s

            drawRoundRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(bLeft, bTop),
                size = Size(bWidth, bHeight),
                cornerRadius = CornerRadius(16f * s, 16f * s)
            )

            // Clinic Sign Marquee
            drawRoundRect(
                color = Color(0xFF00838F),
                topLeft = Offset(bLeft + 12f * s, bTop + 8f * s),
                size = Size(bWidth - 24f * s, 28f * s),
                cornerRadius = CornerRadius(8f * s, 8f * s)
            )
            // Dental Tooth/Cross Emblem
            drawRect(Color.White, topLeft = Offset(cx - 3.5f * s, bTop + 12f * s), size = Size(7f * s, 18f * s))
            drawRect(Color.White, topLeft = Offset(cx - 9f * s, bTop + 17f * s), size = Size(18f * s, 7f * s))

            // Windows
            val winW = (bWidth - 42f * s) / 2f
            val winH = 26f * s
            drawRoundRect(Color(0xFF80DEEA), Offset(bLeft + 12f * s, bTop + 44f * s), Size(winW, winH), CornerRadius(6f * s, 6f * s))
            drawRoundRect(Color(0xFF80DEEA), Offset(cx + 9f * s, bTop + 44f * s), Size(winW, winH), CornerRadius(6f * s, 6f * s))
            drawRoundRect(Color(0xFF80DEEA), Offset(bLeft + 12f * s, bTop + 78f * s), Size(winW, winH), CornerRadius(6f * s, 6f * s))
            drawRoundRect(Color(0xFF80DEEA), Offset(cx + 9f * s, bTop + 78f * s), Size(winW, winH), CornerRadius(6f * s, 6f * s))

            // Glass Doors
            val doorW = 46f * s
            val doorH = 46f * s
            drawRoundRect(
                color = Color(0xFF4DD0E1),
                topLeft = Offset(cx - doorW / 2f, bTop + bHeight - doorH),
                size = Size(doorW, doorH),
                cornerRadius = CornerRadius(4f * s, 4f * s)
            )
            drawLine(Color(0xFF00838F), Offset(cx, bTop + bHeight - doorH), Offset(cx, bTop + bHeight), strokeWidth = 3f * s)

            // Landscaping
            drawCircle(Color(0xFF2E7D32), radius = 24f * s, center = Offset(bLeft - 10f * s, groundY + 6f * s))
            drawCircle(Color(0xFF43A047), radius = 26f * s, center = Offset(bLeft + bWidth + 10f * s, groundY + 6f * s))
        }

        1 -> {
            // SCENE 2: Alex experiencing excruciating tooth pain at 3 AM
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF070E18), Color(0xFF152238))
                ),
                size = size
            )
            // Crescent Moon & Window
            drawRoundRect(
                color = Color(0xFF1E3048),
                topLeft = Offset(24f * s, 24f * s),
                size = Size(58f * s, 58f * s),
                cornerRadius = CornerRadius(12f * s, 12f * s)
            )
            drawCircle(Color(0xFFFFEE58), radius = 17f * s, center = Offset(53f * s, 53f * s))
            drawCircle(Color(0xFF1E3048), radius = 13f * s, center = Offset(58f * s, 49f * s))

            // Glowing 03:00 AM Digital Alarm Clock
            drawRect(Color(0xFF3E2723), topLeft = Offset(w - 78f * s, cy + 12f * s), size = Size(68f * s, 85f * s))
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(w - 72f * s, cy - 12f * s),
                size = Size(56f * s, 24f * s),
                cornerRadius = CornerRadius(6f * s, 6f * s)
            )
            drawLine(Color(0xFFFF1744), Offset(w - 66f * s, cy), Offset(w - 24f * s, cy), strokeWidth = 5f * s)

            // Alex clutching swollen cheek in pain with shake
            val headX = cx - 12f * s + (shakeOffset * s * 0.6f)
            val headY = cy - 8f * s

            // Pulsating Red Shockwaves from Jaw
            drawCircle(Color(0xFFFF1744).copy(alpha = 0.25f), radius = 80f * s, center = Offset(headX + 26f * s, headY + 14f * s))
            drawCircle(Color(0xFFFF5252).copy(alpha = 0.45f), radius = 55f * s, center = Offset(headX + 26f * s, headY + 14f * s), style = Stroke(width = 3f * s))
            drawCircle(Color(0xFFFF8A80).copy(alpha = 0.65f), radius = 35f * s, center = Offset(headX + 26f * s, headY + 14f * s), style = Stroke(width = 2.5f * s))

            // Hair
            drawCircle(Color(0xFF3E2723), radius = 44f * s, center = Offset(headX, headY - 10f * s))
            // Face
            drawCircle(Color(0xFFFFCC80), radius = 40f * s, center = Offset(headX, headY))
            // Swollen inflamed cheek
            drawCircle(Color(0xFFFF1744).copy(alpha = 0.75f), radius = 19f * s, center = Offset(headX + 22f * s, headY + 12f * s))

            // Distressed comic eyes
            drawLine(Color.Black, Offset(headX - 18f * s, headY - 6f * s), Offset(headX - 6f * s, headY - 2f * s), strokeWidth = 3.5f * s)
            drawLine(Color.Black, Offset(headX + 6f * s, headY - 2f * s), Offset(headX + 18f * s, headY - 6f * s), strokeWidth = 3.5f * s)

            // Grimacing mouth
            drawLine(Color(0xFFB71C1C), Offset(headX - 12f * s, headY + 18f * s), Offset(headX + 14f * s, headY + 14f * s), strokeWidth = 4f * s)

            // Clutching Hand
            drawCircle(Color(0xFFFFB74D), radius = 17f * s, center = Offset(headX + 26f * s, headY + 14f * s))

            // Lightning pain zaps
            val pathZap = Path().apply {
                moveTo(headX + 38f * s, headY - 6f * s)
                lineTo(headX + 60f * s, headY - 28f * s)
                lineTo(headX + 50f * s, headY - 20f * s)
                lineTo(headX + 76f * s, headY - 42f * s)
            }
            drawPath(pathZap, color = Color(0xFFFFEB3B), style = Stroke(width = 4.5f * s))
        }

        2 -> {
            // SCENE 3: Alex rushing into clinic clutching jaw
            drawRect(Color(0xFFECEFF1), size = size)
            drawRect(Color(0xFFCFD8DC), topLeft = Offset(0f, cy + 20f * s), size = Size(w, h - cy - 20f * s))

            // Reception Desk
            drawRoundRect(
                color = Color(0xFF00838F),
                topLeft = Offset(20f * s, cy - 22f * s),
                size = Size(88f * s, 85f * s),
                cornerRadius = CornerRadius(12f * s, 12f * s)
            )
            drawRect(Color(0xFF00ACC1), topLeft = Offset(14f * s, cy - 28f * s), size = Size(100f * s, 12f * s))

            // Dental tooth poster on wall
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(30f * s, 28f * s),
                size = Size(70f * s, 54f * s),
                cornerRadius = CornerRadius(8f * s, 8f * s)
            )
            drawCircle(Color(0xFF00BCD4), radius = 16f * s, center = Offset(65f * s, 55f * s))

            // Speed motion streaks behind Alex
            for (lineY in listOf(-30f, 0f, 30f)) {
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                    start = Offset(cx - 30f * s, cy + lineY * s),
                    end = Offset(cx + 10f * s, cy + lineY * s),
                    strokeWidth = 3f * s,
                    cap = StrokeCap.Round
                )
            }

            // Alex running in clutching jaw
            val px = cx + 45f * s
            val py = cy + 15f * s
            drawCircle(Color(0xFF3E2723), radius = 25f * s, center = Offset(px, py - 52f * s))
            drawCircle(Color(0xFFFFCC80), radius = 21f * s, center = Offset(px, py - 46f * s))
            drawCircle(Color(0xFFFF1744).copy(alpha = 0.7f), radius = 9f * s, center = Offset(px + 12f * s, py - 40f * s))
            drawCircle(Color(0xFFFFB74D), radius = 8f * s, center = Offset(px + 14f * s, py - 38f * s))
            drawRoundRect(Color(0xFF00838F), Offset(px - 15f * s, py - 24f * s), Size(30f * s, 44f * s), CornerRadius(6f * s, 6f * s))
            drawLine(Color(0xFF00695C), Offset(px - 6f * s, py + 20f * s), Offset(px - 20f * s, py + 58f * s), strokeWidth = 9f * s, cap = StrokeCap.Round)
            drawLine(Color(0xFF00695C), Offset(px + 6f * s, py + 20f * s), Offset(px + 16f * s, py + 58f * s), strokeWidth = 9f * s, cap = StrokeCap.Round)
        }

        3 -> {
            // SCENE 4: Professional dentist Dr. Smile operatory
            drawRect(Color(0xFF263238), size = size)
            drawRect(Color(0xFF37474F), topLeft = Offset(0f, 0f), size = Size(w, cy + 30f * s))
            drawRect(Color(0xFF455A64), topLeft = Offset(0f, cy + 30f * s), size = Size(w, h - cy - 30f * s))

            // Diagnostic X-ray monitor
            drawRoundRect(
                color = Color(0xFF1E2838),
                topLeft = Offset(24f * s, 32f * s),
                size = Size(90f * s, 70f * s),
                cornerRadius = CornerRadius(10f * s, 10f * s)
            )
            drawRect(Color(0xFF0D47A1), topLeft = Offset(30f * s, 38f * s), size = Size(78f * s, 58f * s))
            drawCircle(Color.White.copy(alpha = 0.85f), radius = 14f * s, center = Offset(69f * s, 67f * s))
            drawCircle(Color(0xFFFF1744), radius = 5f * s, center = Offset(69f * s, 67f * s))

            // Operatory Dental Chair
            val chairX = cx + 45f * s
            val chairY = cy + 20f * s
            drawRoundRect(Color(0xFF0097A7), Offset(chairX - 35f * s, chairY - 10f * s), Size(70f * s, 32f * s), CornerRadius(10f * s, 10f * s))
            drawRoundRect(Color(0xFF00838F), Offset(chairX + 15f * s, chairY - 42f * s), Size(24f * s, 42f * s), CornerRadius(8f * s, 8f * s))
            drawRect(Color(0xFF78909C), Offset(chairX - 8f * s, chairY + 22f * s), Size(16f * s, 32f * s))

            // Dr. Smile in white coat holding mirror
            val dx = cx - 35f * s
            val dy = cy + 10f * s
            drawCircle(Color(0xFF5D4037), radius = 23f * s, center = Offset(dx, dy - 55f * s))
            drawCircle(Color(0xFFFFCC80), radius = 19f * s, center = Offset(dx, dy - 50f * s))
            drawRoundRect(Color.White, Offset(dx - 12f * s, dy - 48f * s), Size(24f * s, 14f * s), CornerRadius(4f * s, 4f * s))
            drawRoundRect(Color.White, Offset(dx - 18f * s, dy - 30f * s), Size(36f * s, 56f * s), CornerRadius(6f * s, 6f * s))
            drawLine(Color(0xFF9E9E9E), Offset(dx + 16f * s, dy - 20f * s), Offset(dx + 30f * s, dy - 35f * s), strokeWidth = 3.5f * s)
            drawCircle(Color(0xFFE0E0E0), radius = 6.5f * s, center = Offset(dx + 32f * s, dy - 38f * s))
        }

        4 -> {
            // SCENE 5: Clinical examination
            drawRect(Color(0xFF1E293B), size = size)
            // Focused lamp beam
            drawCircle(Color(0xFFFFEE58).copy(alpha = 0.28f), radius = 95f * s, center = Offset(cx, cy - 20f * s))
            drawRoundRect(Color(0xFFCFD8DC), Offset(cx - 26f * s, 16f * s), Size(52f * s, 20f * s), CornerRadius(8f * s, 8f * s))
            drawCircle(Color(0xFFFFEE58), radius = 13f * s, center = Offset(cx, 26f * s))

            // Reclined chair with Alex
            val rx = cx - 12f * s
            val ry = cy + 24f * s
            drawRoundRect(Color(0xFF0284C7), Offset(rx - 52f * s, ry - 14f * s), Size(104f * s, 38f * s), CornerRadius(12f * s, 12f * s))
            drawCircle(Color(0xFFFFCC80), radius = 17f * s, center = Offset(rx - 36f * s, ry - 22f * s))
            drawRoundRect(Color(0xFFE0F2FE), Offset(rx - 22f * s, ry - 16f * s), Size(28f * s, 22f * s), CornerRadius(4f * s, 4f * s))

            // Dr. Smile examining
            val sx = cx + 45f * s
            val sy = cy
            drawCircle(Color(0xFFFFCC80), radius = 17f * s, center = Offset(sx - 10f * s, sy - 32f * s))
            drawRoundRect(Color.White, Offset(sx - 16f * s, sy - 30f * s), Size(16f * s, 10f * s), CornerRadius(3f * s, 3f * s))
            drawRoundRect(Color.White, Offset(sx - 18f * s, sy - 14f * s), Size(36f * s, 50f * s), CornerRadius(6f * s, 6f * s))
            drawLine(Color.White, Offset(sx - 14f * s, sy - 5f * s), Offset(rx - 22f * s, ry - 20f * s), strokeWidth = 5.5f * s, cap = StrokeCap.Round)
            drawLine(Color(0xFFE2E8F0), Offset(rx - 22f * s, ry - 20f * s), Offset(rx - 28f * s, ry - 22f * s), strokeWidth = 3f * s)
        }

        5 -> {
            // SCENE 6: Dr. Smile reveals RCT estimate & radiograph
            drawRect(Color(0xFF0F172A), size = size)

            val cardW = (235f * s).coerceAtMost(w * 0.90f)
            val cardH = 165f * s
            val cardLeft = cx - cardW / 2f
            val cardTop = cy - cardH / 2f - 10f * s

            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(18f * s, 18f * s)
            )
            // Golden Header Bar
            drawRoundRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardW, 38f * s),
                cornerRadius = CornerRadius(18f * s, 18f * s)
            )
            drawRect(Color(0xFFFFD700), Offset(cardLeft, cardTop + 18f * s), Size(cardW, 20f * s))

            // Tooth graphic in invoice
            drawCircle(Color.White, radius = 24f * s, center = Offset(cardLeft + 48f * s, cardTop + 82f * s))
            drawCircle(Color(0xFFEF4444), radius = 10f * s, center = Offset(cardLeft + 48f * s, cardTop + 82f * s))

            // Radiograph frame
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(cardLeft + 90f * s, cardTop + 54f * s),
                size = Size(cardW - 105f * s, 52f * s),
                cornerRadius = CornerRadius(6f * s, 6f * s)
            )
            drawLine(Color(0xFF38BDF8), Offset(cardLeft + 100f * s, cardTop + 70f * s), Offset(cardLeft + cardW - 25f * s, cardTop + 70f * s), strokeWidth = 3.5f * s)
            drawLine(Color(0xFF38BDF8), Offset(cardLeft + 100f * s, cardTop + 88f * s), Offset(cardLeft + cardW - 45f * s, cardTop + 88f * s), strokeWidth = 3.5f * s)

            // Price Tag Box ($5,000)
            drawRoundRect(
                color = Color(0xFF0284C7),
                topLeft = Offset(cardLeft + 16f * s, cardTop + cardH - 58f * s),
                size = Size(cardW - 32f * s, 46f * s),
                cornerRadius = CornerRadius(12f * s, 12f * s)
            )
            drawCircle(Color(0xFFFFD700), radius = 15f * s, center = Offset(cardLeft + 45f * s, cardTop + cardH - 35f * s))
            drawCircle(Color(0xFFFFA000), radius = 11f * s, center = Offset(cardLeft + 45f * s, cardTop + cardH - 35f * s))
            drawLine(Color(0xFFFFD700), Offset(cardLeft + 72f * s, cardTop + cardH - 35f * s), Offset(cardLeft + cardW - 30f * s, cardTop + cardH - 35f * s), strokeWidth = 7f * s, cap = StrokeCap.Round)
        }

        6 -> {
            // SCENE 7: Total Shock: $5,000 sticker shock!
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFEF4444), Color(0xFF7F1D1D), Color(0xFF450A0A)),
                    center = Offset(cx, cy),
                    radius = w * 0.75f
                ),
                size = size
            )

            // Dynamic radiating anime shock lines
            for (angle in 0..360 step 24) {
                val rad = Math.toRadians(angle.toDouble())
                val x1 = cx + (Math.cos(rad) * 70f * s).toFloat()
                val y1 = cy + (Math.sin(rad) * 70f * s).toFloat()
                val x2 = cx + (Math.cos(rad) * 150f * s).toFloat()
                val y2 = cy + (Math.sin(rad) * 150f * s).toFloat()
                drawLine(Color(0xFFFFCDD2).copy(alpha = 0.65f), Offset(x1, y1), Offset(x2, y2), strokeWidth = 3.5f * s)
            }

            // Comical floating dollar bills flying away
            for (offset in listOf(Pair(-80f, -60f), Pair(80f, -50f), Pair(-90f, 60f), Pair(85f, 70f))) {
                val (dx, dy) = offset
                drawRoundRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(cx + dx * s, cy + dy * s),
                    size = Size(36f * s, 20f * s),
                    cornerRadius = CornerRadius(4f * s, 4f * s)
                )
                drawCircle(Color(0xFFC8E6C9), radius = 5f * s, center = Offset(cx + dx * s + 18f * s, cy + dy * s + 10f * s))
            }

            // Alex's ultra-shocked face
            drawCircle(Color(0xFF3E2723), radius = 50f * s, center = Offset(cx, cy - 20f * s))
            drawCircle(Color(0xFFFFCC80), radius = 44f * s, center = Offset(cx, cy - 12f * s))

            // Comical bulging wide bug-eyes
            drawCircle(Color.White, radius = 17f * s, center = Offset(cx - 18f * s, cy - 20f * s))
            drawCircle(Color.White, radius = 17f * s, center = Offset(cx + 18f * s, cy - 20f * s))
            drawCircle(Color.Black, radius = 5f * s, center = Offset(cx - 18f * s, cy - 20f * s))
            drawCircle(Color.Black, radius = 5f * s, center = Offset(cx + 18f * s, cy - 20f * s))

            // Giant dropping jaw (O-shaped shock cavity)
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(cx - 18f * s, cy + 2f * s),
                size = Size(36f * s, 48f * s),
                cornerRadius = CornerRadius(18f * s, 18f * s)
            )
            drawCircle(Color(0xFFF43F5E), radius = 11f * s, center = Offset(cx, cy + 32f * s))

            // Dramatic blue anime sweat drops
            drawCircle(Color(0xFF00E5FF), radius = 7f * s, center = Offset(cx - 52f * s, cy - 32f * s))
            drawCircle(Color(0xFF00E5FF), radius = 6f * s, center = Offset(cx + 52f * s, cy - 28f * s))
        }

        7 -> {
            // SCENE 8: Alex charges heroically toward the 3D highway
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0288D1), Color(0xFF81D4FA), Color(0xFF4CAF50))
                ),
                size = size
            )

            // Dynamic 3D highway stretching toward horizon
            val roadPath = Path().apply {
                moveTo(cx, cy - 24f * s)
                lineTo(w * 0.94f, h)
                lineTo(w * 0.06f, h)
                close()
            }
            drawPath(roadPath, color = Color(0xFF263238))
            drawLine(Color(0xFFFFD700), Offset(cx, cy - 24f * s), Offset(cx, h), strokeWidth = 5f * s)

            // Speed lines & sparks
            for (sx in listOf(-50f, 50f)) {
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(cx + sx * s, cy + 20f * s),
                    end = Offset(cx + sx * 1.4f * s, cy + 70f * s),
                    strokeWidth = 3f * s,
                    cap = StrokeCap.Round
                )
            }

            // Alex running heroically
            val ax = cx
            val ay = cy + 42f * s
            drawCircle(Color(0xFF3E2723), radius = 24f * s, center = Offset(ax, ay - 44f * s))
            drawCircle(Color(0xFFFFCC80), radius = 20f * s, center = Offset(ax, ay - 38f * s))
            drawRoundRect(Color(0xFF00838F), Offset(ax - 16f * s, ay - 20f * s), Size(32f * s, 38f * s), CornerRadius(7f * s, 7f * s))
            drawLine(Color(0xFF00695C), Offset(ax - 7f * s, ay + 18f * s), Offset(ax - 24f * s, ay + 52f * s), strokeWidth = 8f * s, cap = StrokeCap.Round)
            drawLine(Color(0xFF00695C), Offset(ax + 7f * s, ay + 18f * s), Offset(ax + 22f * s, ay + 46f * s), strokeWidth = 8f * s, cap = StrokeCap.Round)

            // Golden Coins waiting on the highway
            drawCircle(Color(0xFFFFD700), radius = 20f * s, center = Offset(cx - 60f * s, cy + 12f * s))
            drawCircle(Color(0xFFFFA000), radius = 15f * s, center = Offset(cx - 60f * s, cy + 12f * s))
            drawCircle(Color(0xFFFFD700), radius = 24f * s, center = Offset(cx + 65f * s, cy - 6f * s))
            drawCircle(Color(0xFFFFA000), radius = 19f * s, center = Offset(cx + 65f * s, cy - 6f * s))
        }
    }
}
