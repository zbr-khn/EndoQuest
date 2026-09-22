package com.example.endoquest.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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

data class CinematicStoryScene(
    val sceneNumber: Int,
    val sceneTag: String,
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
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePulse"
    )

    val painZapOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zapShake"
    )

    val scenes = listOf(
        CinematicStoryScene(
            sceneNumber = 1,
            sceneTag = "SCENE 1: CLINIC EXTERIOR",
            title = "Radiant Smile Dental Clinic",
            speaker = "Narrator",
            dialogue = "\"A peaceful morning in the city, where state-of-the-art dental care awaits...\"",
            description = "The sparkling modern glass facade of Radiant Smile Dental Clinic gleams in the morning light."
        ),
        CinematicStoryScene(
            sceneNumber = 2,
            sceneTag = "SCENE 2: TOOTH PAIN",
            title = "Midnight Agony",
            speaker = "Alex",
            dialogue = "\"Owww! My molar feels like it's exploding! No painkiller is touching this!\"",
            description = "At 3:00 AM, Alex awakens in excruciating pain as throbbing pulpitis strikes the lower molar."
        ),
        CinematicStoryScene(
            sceneNumber = 3,
            sceneTag = "SCENE 3: CLINIC ARRIVAL",
            title = "Emergency Arrival",
            speaker = "Alex",
            dialogue = "\"Doctor, please help! I can't take this throbbing pain any longer!\"",
            description = "Clutching the aching jaw in desperation, Alex charges through the clinic's automatic glass doors."
        ),
        CinematicStoryScene(
            sceneNumber = 4,
            sceneTag = "SCENE 4: DR. SMILE",
            title = "Dr. Smile's Operatory",
            speaker = "Dr. Smile",
            dialogue = "\"Welcome to Radiant Smile, Alex. Take a seat in the chair; let's see what's happening.\"",
            description = "Professional dentist Dr. Smile stands ready in pristine clinical scrubs beside advanced diagnostic equipment."
        ),
        CinematicStoryScene(
            sceneNumber = 5,
            sceneTag = "SCENE 5: CLINICAL EXAMINATION",
            title = "Dental Operatory Check",
            speaker = "Dr. Smile",
            dialogue = "\"Open wide. Let me inspect that painful molar with the dental mirror and explorer...\"",
            description = "Under the focused beam of the surgical operatory lamp, Dr. Smile examines the inflamed tooth."
        ),
        CinematicStoryScene(
            sceneNumber = 6,
            sceneTag = "SCENE 6: DIAGNOSIS & COST",
            title = "Diagnosis: RCT Required",
            speaker = "Dr. Smile",
            dialogue = "\"The radiograph confirms Irreversible Pulpitis. You need an immediate Root Canal Treatment. Total cost: $5,000.\"",
            description = "Dr. Smile shows the periapical radiograph and hands over the clinical estimate card: $5,000."
        ),
        CinematicStoryScene(
            sceneNumber = 7,
            sceneTag = "SCENE 7: TOTAL SHOCK",
            title = "The $5,000 Sticker Shock",
            speaker = "Alex",
            dialogue = "\"WHAT?! $5,000?! For just one tooth?! I don't have that kind of cash on me!\"",
            description = "Alex leaps back in sheer disbelief, jaw dropping to the floor at the staggering $5,000 price tag."
        ),
        CinematicStoryScene(
            sceneNumber = 8,
            sceneTag = "SCENE 8: THE RUNNER CALL",
            title = "\"I'll Be Back With the Money!\"",
            speaker = "Alex",
            dialogue = "\"Hold my appointment, Dr. Smile! I'll run through the city and collect every single dollar!\"",
            description = "Determined to save the tooth, Alex bolts out onto the open 3D highway to collect $25 gold coins!"
        )
    )

    val currentScene = scenes[sceneIndex]

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = Color(0xFF0A0E21)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                    )
                )
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Scene Tag & Progress Indicators
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentScene.sceneTag,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in scenes.indices) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .height(4.dp)
                                    .width(if (i == sceneIndex) 28.dp else 12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (i <= sceneIndex) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                }

                // Center Artwork Canvas Card
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // High-fidelity Canvas Illustration with responsive scaling
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF141926))
                                .scale(if (sceneIndex == 1) 1f else pulseScale),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawStorySceneArt(sceneIndex, size, painZapOffset)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dialogue Speech Bubble Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (currentScene.speaker == "Dr. Smile") Color(0xFF00B4D8)
                                                else if (currentScene.speaker == "Alex") Color(0xFFFF9F1C)
                                                else Color(0xFF9D4EDD)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (currentScene.speaker == "Dr. Smile") "👨‍⚕️"
                                            else if (currentScene.speaker == "Alex") "🏃"
                                            else "🎙️",
                                            fontSize = 14.sp
                                        )
                                    }
                                    Text(
                                        text = currentScene.speaker,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentScene.dialogue,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = currentScene.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // Bottom Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (sceneIndex > 0) {
                        OutlinedButton(
                            onClick = { sceneIndex-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("⬅️ Back", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
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
                            .weight(if (sceneIndex > 0) 1.6f else 1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sceneIndex == scenes.size - 1) Color(0xFFFFD700) else Color(0xFF00E5FF)
                        )
                    ) {
                        Text(
                            text = if (sceneIndex < scenes.size - 1) "Next Scene ➡️" else "Start 3D Runner! 🏃💨",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

/**
 * Renders custom Canvas artwork for all 8 narrative scenes with responsive density scaling.
 */
fun DrawScope.drawStorySceneArt(sceneIndex: Int, size: Size, shakeOffset: Float) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    // Responsive scale factor based on canvas dimensions
    val s = (min(w, h) / 300f).coerceIn(1f, 3.5f)

    when (sceneIndex) {
        0 -> {
            // SCENE 1: Exterior of colorful, modern dental clinic
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF87CEEB), Color(0xFFE0F7FA))
                ),
                size = size
            )
            // Sun & clouds
            drawCircle(Color(0xFFFFD54F), radius = 26f * s, center = Offset(w * 0.85f, h * 0.18f))
            drawCircle(Color.White.copy(alpha = 0.85f), radius = 22f * s, center = Offset(w * 0.25f, h * 0.15f))
            drawCircle(Color.White.copy(alpha = 0.85f), radius = 30f * s, center = Offset(w * 0.35f, h * 0.13f))

            // Ground / lawn & pavement
            val groundY = h * 0.70f
            drawRect(Color(0xFF4CAF50), topLeft = Offset(0f, groundY), size = Size(w, h - groundY))
            drawRect(Color(0xFFB0BEC5), topLeft = Offset(0f, groundY + 30f * s), size = Size(w, h - groundY - 30f * s))

            // Modern Clinic Building (Wide & Proportional)
            val bWidth = (200f * s).coerceAtMost(w * 0.88f)
            val bHeight = 150f * s
            val bLeft = cx - bWidth / 2f
            val bTop = groundY - bHeight + 10f * s

            drawRoundRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(bLeft, bTop),
                size = Size(bWidth, bHeight),
                cornerRadius = CornerRadius(14f * s, 14f * s)
            )

            // Clinic Sign Marquee
            drawRoundRect(
                color = Color(0xFF00838F),
                topLeft = Offset(bLeft + 12f * s, bTop + 8f * s),
                size = Size(bWidth - 24f * s, 26f * s),
                cornerRadius = CornerRadius(6f * s, 6f * s)
            )
            // Dental Tooth/Cross Emblem
            drawRect(Color.White, topLeft = Offset(cx - 3f * s, bTop + 13f * s), size = Size(6f * s, 16f * s))
            drawRect(Color.White, topLeft = Offset(cx - 8f * s, bTop + 18f * s), size = Size(16f * s, 6f * s))

            // Rows of Glass Windows
            val winW = (bWidth - 40f * s) / 2f
            val winH = 26f * s
            drawRoundRect(Color(0xFF80DEEA), Offset(bLeft + 12f * s, bTop + 42f * s), Size(winW, winH), CornerRadius(6f * s, 6f * s))
            drawRoundRect(Color(0xFF80DEEA), Offset(cx + 8f * s, bTop + 42f * s), Size(winW, winH), CornerRadius(6f * s, 6f * s))

            drawRoundRect(Color(0xFF80DEEA), Offset(bLeft + 12f * s, bTop + 76f * s), Size(winW, winH), CornerRadius(6f * s, 6f * s))
            drawRoundRect(Color(0xFF80DEEA), Offset(cx + 8f * s, bTop + 76f * s), Size(winW, winH), CornerRadius(6f * s, 6f * s))

            // Glass Entrance Sliding Doors
            val doorW = 44f * s
            val doorH = 45f * s
            drawRoundRect(
                color = Color(0xFF4DD0E1),
                topLeft = Offset(cx - doorW / 2f, bTop + bHeight - doorH),
                size = Size(doorW, doorH),
                cornerRadius = CornerRadius(4f * s, 4f * s)
            )
            drawLine(Color(0xFF00838F), Offset(cx, bTop + bHeight - doorH), Offset(cx, bTop + bHeight), strokeWidth = 3f * s)

            // Landscaping bushes
            drawCircle(Color(0xFF2E7D32), radius = 22f * s, center = Offset(bLeft - 10f * s, groundY + 5f * s))
            drawCircle(Color(0xFF388E3C), radius = 24f * s, center = Offset(bLeft + bWidth + 10f * s, groundY + 5f * s))
        }

        1 -> {
            // SCENE 2: Alex experiencing severe tooth pain
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))
                ),
                size = size
            )
            // Night window with crescent moon
            drawRoundRect(
                color = Color(0xFF233554),
                topLeft = Offset(25f * s, 25f * s),
                size = Size(55f * s, 55f * s),
                cornerRadius = CornerRadius(10f * s, 10f * s)
            )
            drawCircle(Color(0xFFFFEE58), radius = 16f * s, center = Offset(52f * s, 52f * s))
            drawCircle(Color(0xFF233554), radius = 12f * s, center = Offset(57f * s, 48f * s))

            // Nightstand & Glowing Digital Clock: 03:00 AM
            drawRect(Color(0xFF3E2723), topLeft = Offset(w - 75f * s, cy + 10f * s), size = Size(65f * s, 80f * s))
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(w - 70f * s, cy - 14f * s),
                size = Size(55f * s, 22f * s),
                cornerRadius = CornerRadius(5f * s, 5f * s)
            )
            drawLine(Color(0xFFFF1744), Offset(w - 64f * s, cy - 3f * s), Offset(w - 24f * s, cy - 3f * s), strokeWidth = 4f * s)

            // Alex clutching swollen cheek in pain
            val headX = cx - 10f * s + (shakeOffset * s * 0.5f)
            val headY = cy - 10f * s

            // Hair
            drawCircle(Color(0xFF3E2723), radius = 42f * s, center = Offset(headX, headY - 10f * s))
            // Face
            drawCircle(Color(0xFFFFCC80), radius = 38f * s, center = Offset(headX, headY))
            // Swollen red cheek
            drawCircle(Color(0xFFFF5252).copy(alpha = 0.6f), radius = 18f * s, center = Offset(headX + 22f * s, headY + 12f * s))
            // Pained squinting eyes
            drawLine(Color.Black, Offset(headX - 18f * s, headY - 6f * s), Offset(headX - 6f * s, headY - 2f * s), strokeWidth = 3f * s)
            drawLine(Color.Black, Offset(headX + 6f * s, headY - 2f * s), Offset(headX + 18f * s, headY - 6f * s), strokeWidth = 3f * s)
            // Grimacing mouth
            drawLine(Color(0xFFB71C1C), Offset(headX - 12f * s, headY + 18f * s), Offset(headX + 14f * s, headY + 14f * s), strokeWidth = 4f * s)
            // Clutching Hand over cheek
            drawCircle(Color(0xFFFFB74D), radius = 16f * s, center = Offset(headX + 26f * s, headY + 14f * s))

            // Radiating pulsating lightning zaps
            val pathZap = Path().apply {
                moveTo(headX + 38f * s, headY - 5f * s)
                lineTo(headX + 58f * s, headY - 25f * s)
                lineTo(headX + 50f * s, headY - 18f * s)
                lineTo(headX + 72f * s, headY - 38f * s)
            }
            drawPath(pathZap, color = Color(0xFFFFEB3B), style = Stroke(width = 4f * s))
            drawCircle(Color(0xFFFF5252).copy(alpha = 0.5f), radius = 55f * s, center = Offset(headX + 25f * s, headY + 12f * s), style = Stroke(width = 3f * s))
            drawCircle(Color(0xFFFF1744).copy(alpha = 0.3f), radius = 75f * s, center = Offset(headX + 25f * s, headY + 12f * s), style = Stroke(width = 2f * s))
        }

        2 -> {
            // SCENE 3: Alex walking into the clinic
            drawRect(Color(0xFFECEFF1), size = size)
            // Polished floor
            drawRect(Color(0xFFCFD8DC), topLeft = Offset(0f, cy + 20f * s), size = Size(w, h - cy - 20f * s))

            // Reception Counter
            drawRoundRect(
                color = Color(0xFF00838F),
                topLeft = Offset(20f * s, cy - 20f * s),
                size = Size(85f * s, 80f * s),
                cornerRadius = CornerRadius(10f * s, 10f * s)
            )
            drawRect(Color(0xFF00ACC1), topLeft = Offset(14f * s, cy - 26f * s), size = Size(95f * s, 10f * s))

            // Dental poster on wall
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(30f * s, 30f * s),
                size = Size(65f * s, 50f * s),
                cornerRadius = CornerRadius(6f * s, 6f * s)
            )
            drawCircle(Color(0xFF00BCD4), radius = 14f * s, center = Offset(62f * s, 55f * s))

            // Alex walking in clutching jaw
            val px = cx + 45f * s
            val py = cy + 15f * s
            drawCircle(Color(0xFF3E2723), radius = 24f * s, center = Offset(px, py - 52f * s))
            drawCircle(Color(0xFFFFCC80), radius = 20f * s, center = Offset(px, py - 46f * s))
            drawCircle(Color(0xFFFFB74D), radius = 8f * s, center = Offset(px + 12f * s, py - 40f * s))
            drawRoundRect(Color(0xFF00838F), Offset(px - 14f * s, py - 24f * s), Size(28f * s, 42f * s), CornerRadius(6f * s, 6f * s))
            drawLine(Color(0xFF1565C0), Offset(px - 6f * s, py + 18f * s), Offset(px - 18f * s, py + 55f * s), strokeWidth = 8f * s, cap = StrokeCap.Round)
            drawLine(Color(0xFF1565C0), Offset(px + 6f * s, py + 18f * s), Offset(px + 14f * s, py + 55f * s), strokeWidth = 8f * s, cap = StrokeCap.Round)
        }

        3 -> {
            // SCENE 4: Professional dentist Dr. Smile inside clinic
            drawRect(Color(0xFF263238), size = size)
            drawRect(Color(0xFF37474F), topLeft = Offset(0f, 0f), size = Size(w, cy + 30f * s))
            drawRect(Color(0xFF455A64), topLeft = Offset(0f, cy + 30f * s), size = Size(w, h - cy - 30f * s))

            // Diagnostic X-ray monitor on wall
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(25f * s, 35f * s),
                size = Size(85f * s, 65f * s),
                cornerRadius = CornerRadius(8f * s, 8f * s)
            )
            drawRect(Color(0xFF0D47A1), topLeft = Offset(30f * s, 40f * s), size = Size(75f * s, 55f * s))
            drawCircle(Color.White.copy(alpha = 0.8f), radius = 12f * s, center = Offset(68f * s, 65f * s))
            drawRect(Color.White.copy(alpha = 0.8f), topLeft = Offset(58f * s, 68f * s), size = Size(20f * s, 20f * s))
            drawCircle(Color(0xFFFF1744), radius = 4f * s, center = Offset(68f * s, 68f * s))

            // Dental Operatory Chair
            val chairX = cx + 45f * s
            val chairY = cy + 20f * s
            drawRoundRect(Color(0xFF0097A7), Offset(chairX - 35f * s, chairY - 10f * s), Size(70f * s, 32f * s), CornerRadius(10f * s, 10f * s))
            drawRoundRect(Color(0xFF00838F), Offset(chairX + 15f * s, chairY - 42f * s), Size(24f * s, 42f * s), CornerRadius(8f * s, 8f * s))
            drawRect(Color(0xFF78909C), Offset(chairX - 8f * s, chairY + 22f * s), Size(16f * s, 32f * s))
            drawRect(Color(0xFF455A64), Offset(chairX - 25f * s, chairY + 54f * s), Size(50f * s, 10f * s))

            // Dr. Smile in white dental coat
            val dx = cx - 35f * s
            val dy = cy + 10f * s
            drawCircle(Color(0xFF5D4037), radius = 22f * s, center = Offset(dx, dy - 55f * s))
            drawCircle(Color(0xFFFFCC80), radius = 18f * s, center = Offset(dx, dy - 50f * s))
            drawRoundRect(Color.White, Offset(dx - 12f * s, dy - 48f * s), Size(24f * s, 14f * s), CornerRadius(4f * s, 4f * s))
            drawRoundRect(Color.White, Offset(dx - 18f * s, dy - 30f * s), Size(36f * s, 55f * s), CornerRadius(6f * s, 6f * s))
            drawCircle(Color(0xFF00BCD4), radius = 5f * s, center = Offset(dx - 6f * s, dy - 15f * s))
            drawLine(Color(0xFF9E9E9E), Offset(dx + 16f * s, dy - 20f * s), Offset(dx + 30f * s, dy - 35f * s), strokeWidth = 3f * s)
            drawCircle(Color(0xFFE0E0E0), radius = 6f * s, center = Offset(dx + 32f * s, dy - 38f * s))
        }

        4 -> {
            // SCENE 5: Alex sits in dental chair; Dr. Smile examines the molar
            drawRect(Color(0xFF1E293B), size = size)
            drawCircle(Color(0xFFFFEE58).copy(alpha = 0.25f), radius = 90f * s, center = Offset(cx, cy - 20f * s))
            drawRoundRect(Color(0xFFCFD8DC), Offset(cx - 25f * s, 18f * s), Size(50f * s, 18f * s), CornerRadius(8f * s, 8f * s))
            drawCircle(Color(0xFFFFEE58), radius = 12f * s, center = Offset(cx, 28f * s))

            // Reclined Dental Chair with Alex
            val rx = cx - 10f * s
            val ry = cy + 25f * s
            drawRoundRect(Color(0xFF0284C7), Offset(rx - 50f * s, ry - 14f * s), Size(100f * s, 36f * s), CornerRadius(12f * s, 12f * s))
            drawCircle(Color(0xFFFFCC80), radius = 16f * s, center = Offset(rx - 36f * s, ry - 22f * s))
            drawRoundRect(Color(0xFFE0F2FE), Offset(rx - 22f * s, ry - 16f * s), Size(28f * s, 22f * s), CornerRadius(4f * s, 4f * s))

            // Dr. Smile examining tooth
            val sx = cx + 45f * s
            val sy = cy
            drawCircle(Color(0xFFFFCC80), radius = 16f * s, center = Offset(sx - 10f * s, sy - 32f * s))
            drawRoundRect(Color.White, Offset(sx - 16f * s, sy - 30f * s), Size(16f * s, 10f * s), CornerRadius(3f * s, 3f * s))
            drawRoundRect(Color.White, Offset(sx - 18f * s, sy - 14f * s), Size(34f * s, 48f * s), CornerRadius(6f * s, 6f * s))
            drawLine(Color.White, Offset(sx - 14f * s, sy - 5f * s), Offset(rx - 22f * s, ry - 20f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)
            drawLine(Color(0xFFE2E8F0), Offset(rx - 22f * s, ry - 20f * s), Offset(rx - 28f * s, ry - 22f * s), strokeWidth = 3f * s)
        }

        5 -> {
            // SCENE 6: Dr. Smile explains pulpal diagnosis and shows $5,000 RCT cost
            drawRect(Color(0xFF0F172A), size = size)

            val cardW = (230f * s).coerceAtMost(w * 0.90f)
            val cardH = 160f * s
            val cardLeft = cx - cardW / 2f
            val cardTop = cy - cardH / 2f - 10f * s

            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(16f * s, 16f * s)
            )
            // Golden Header Bar
            drawRoundRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardW, 36f * s),
                cornerRadius = CornerRadius(16f * s, 16f * s)
            )
            drawRect(Color(0xFFFFD700), Offset(cardLeft, cardTop + 18f * s), Size(cardW, 18f * s))

            // Tooth graphic in invoice
            drawCircle(Color.White, radius = 22f * s, center = Offset(cardLeft + 45f * s, cardTop + 78f * s))
            drawCircle(Color(0xFFEF4444), radius = 9f * s, center = Offset(cardLeft + 45f * s, cardTop + 78f * s))

            // Radiograph frame
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(cardLeft + 85f * s, cardTop + 50f * s),
                size = Size(cardW - 100f * s, 50f * s),
                cornerRadius = CornerRadius(6f * s, 6f * s)
            )
            drawLine(Color(0xFF38BDF8), Offset(cardLeft + 95f * s, cardTop + 65f * s), Offset(cardLeft + cardW - 25f * s, cardTop + 65f * s), strokeWidth = 3f * s)
            drawLine(Color(0xFF38BDF8), Offset(cardLeft + 95f * s, cardTop + 82f * s), Offset(cardLeft + cardW - 45f * s, cardTop + 82f * s), strokeWidth = 3f * s)

            // Price Tag Box ($5,000)
            drawRoundRect(
                color = Color(0xFF0284C7),
                topLeft = Offset(cardLeft + 16f * s, cardTop + cardH - 58f * s),
                size = Size(cardW - 32f * s, 44f * s),
                cornerRadius = CornerRadius(10f * s, 10f * s)
            )
            drawCircle(Color(0xFFFFD700), radius = 14f * s, center = Offset(cardLeft + 45f * s, cardTop + cardH - 36f * s))
            drawCircle(Color(0xFFFFA000), radius = 10f * s, center = Offset(cardLeft + 45f * s, cardTop + cardH - 36f * s))
            drawLine(Color(0xFFFFD700), Offset(cardLeft + 70f * s, cardTop + cardH - 36f * s), Offset(cardLeft + cardW - 30f * s, cardTop + cardH - 36f * s), strokeWidth = 6f * s, cap = StrokeCap.Round)
        }

        6 -> {
            // SCENE 7: Alex reacts with shock ("$5,000?!")
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFDC2626), Color(0xFF450A0A)),
                    center = Offset(cx, cy),
                    radius = w * 0.7f
                ),
                size = size
            )

            // Shock lines radiating outward
            for (angle in 0..360 step 30) {
                val rad = Math.toRadians(angle.toDouble())
                val x1 = cx + (Math.cos(rad) * 75f * s).toFloat()
                val y1 = cy + (Math.sin(rad) * 75f * s).toFloat()
                val x2 = cx + (Math.cos(rad) * 140f * s).toFloat()
                val y2 = cy + (Math.sin(rad) * 140f * s).toFloat()
                drawLine(Color(0xFFFFE4E6).copy(alpha = 0.6f), Offset(x1, y1), Offset(x2, y2), strokeWidth = 3f * s)
            }

            // Alex's ultra-shocked face
            drawCircle(Color(0xFF3E2723), radius = 48f * s, center = Offset(cx, cy - 20f * s))
            drawCircle(Color(0xFFFFCC80), radius = 42f * s, center = Offset(cx, cy - 12f * s))

            // Bulging wide eyes
            drawCircle(Color.White, radius = 16f * s, center = Offset(cx - 18f * s, cy - 20f * s))
            drawCircle(Color.White, radius = 16f * s, center = Offset(cx + 18f * s, cy - 20f * s))
            drawCircle(Color.Black, radius = 4.5f * s, center = Offset(cx - 18f * s, cy - 20f * s))
            drawCircle(Color.Black, radius = 4.5f * s, center = Offset(cx + 18f * s, cy - 20f * s))

            // Dropping gaping mouth (O shape)
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(cx - 16f * s, cy + 2f * s),
                size = Size(32f * s, 42f * s),
                cornerRadius = CornerRadius(16f * s, 16f * s)
            )
            drawCircle(Color(0xFFF43F5E), radius = 10f * s, center = Offset(cx, cy + 28f * s))

            // Sweat droplets
            drawCircle(Color(0xFF38BDF8), radius = 6f * s, center = Offset(cx - 48f * s, cy - 30f * s))
            drawCircle(Color(0xFF38BDF8), radius = 5f * s, center = Offset(cx + 48f * s, cy - 26f * s))
        }

        7 -> {
            // SCENE 8: Alex says "I'll be back with the money!", leaves clinic, transitions into runner
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF38BDF8), Color(0xFFBAE6FD), Color(0xFF4ADE80))
                ),
                size = size
            )
            // Road stretching to horizon
            val roadPath = Path().apply {
                moveTo(cx, cy - 20f * s)
                lineTo(w * 0.92f, h)
                lineTo(w * 0.08f, h)
                close()
            }
            drawPath(roadPath, color = Color(0xFF334155))
            drawLine(Color(0xFFFFD700), Offset(cx, cy - 20f * s), Offset(cx, h), strokeWidth = 4f * s)

            // Alex running heroically
            val ax = cx
            val ay = cy + 40f * s
            drawCircle(Color(0xFF3E2723), radius = 22f * s, center = Offset(ax, ay - 42f * s))
            drawCircle(Color(0xFFFFCC80), radius = 18f * s, center = Offset(ax, ay - 36f * s))
            drawRoundRect(Color(0xFF00838F), Offset(ax - 15f * s, ay - 18f * s), Size(30f * s, 36f * s), CornerRadius(6f * s, 6f * s))
            drawLine(Color(0xFF1D4ED8), Offset(ax - 6f * s, ay + 18f * s), Offset(ax - 22f * s, ay + 50f * s), strokeWidth = 7f * s, cap = StrokeCap.Round)
            drawLine(Color(0xFF1D4ED8), Offset(ax + 6f * s, ay + 18f * s), Offset(ax + 20f * s, ay + 44f * s), strokeWidth = 7f * s, cap = StrokeCap.Round)

            // Golden Coins floating on horizon
            drawCircle(Color(0xFFFFD700), radius = 18f * s, center = Offset(cx - 55f * s, cy + 10f * s))
            drawCircle(Color(0xFFF59E0B), radius = 14f * s, center = Offset(cx - 55f * s, cy + 10f * s))
            drawCircle(Color(0xFFFFD700), radius = 22f * s, center = Offset(cx + 60f * s, cy - 5f * s))
            drawCircle(Color(0xFFF59E0B), radius = 18f * s, center = Offset(cx + 60f * s, cy - 5f * s))
        }
    }
}
