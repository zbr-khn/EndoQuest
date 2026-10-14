package com.example.endoquest.ui.screens

import android.graphics.Paint
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endoquest.ui.viewmodel.GameViewModel
import kotlin.math.*

/**
 * Clinical Root Canal Treatment (RCT) Simulation Screen.
 * Strictly ZERO quiz questions: focuses 100% on hands-on clinical instrumentation
 * and chemical debridement simulation via interactive slider controls.
 *
 * Upgraded with High-Fidelity 2.5D Layered Anatomical Engine:
 * - Real-time sagittal cutaway cross-section of tooth, pulp chamber, root canals, and alveolar gingiva.
 * - Stage 1: High-speed contra-angle turbine & diamond access bur + NaOCl tissue dissolution and bubbling.
 * - Stage 2: NiTi file (ISO Blue #30) + Side-Vented Safety Needle positioned 2mm short of apex discharging lateral reflux jets.
 * - Stage 3: Intracanal delivery syringe placing dense radiopaque Calcium Hydroxide paste [pH 12.5] backfilled from apex.
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
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚙️ Anatomical RCT Simulation",
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // High-Fidelity 2.5D Anatomical Cutaway Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clinical Real-time Feedback Status Badge
                    val statusText = when (stage.stageNumber) {
                        1 -> when {
                            procedureSliderProgress < 0.30f -> "Access Opening: Diamond bur penetrating enamel & entering chamber..."
                            procedureSliderProgress < 0.75f -> "NaOCl Irrigation: Active tissue saponification dissolving necrotic pulp..."
                            else -> "17% EDTA Flush: Demineralizing smear layer & opening dentinal tubules!"
                        }
                        2 -> when {
                            procedureSliderProgress < 0.35f -> "Pathfinding canal with #30 NiTi file & verifying working length..."
                            procedureSliderProgress < 0.85f -> "Side-vented needle at -2mm safe zone. Coronal reflux active!"
                            else -> "Apex Locator confirms safe length. Zero periapical extrusion achieved!"
                        }
                        3 -> when {
                            procedureSliderProgress < 0.25f -> "Introducing delivery cannula into the apical third..."
                            procedureSliderProgress < 0.85f -> "Backfilling dense Calcium Hydroxide paste [pH 12.5] from apex upward..."
                            else -> "Canal completely obturated with Ca(OH)₂! Antimicrobial disinfection active."
                        }
                        else -> ""
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (procedureSliderProgress >= 0.99f) Color(0xFF1B5E20) else Color(0xFF111C2E)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (procedureSliderProgress >= 0.99f) "✅" else "🔬",
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (procedureSliderProgress >= 0.99f) Color(0xFFB9F6CA) else Color(0xFF80DEEA)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
 * Draws High-Fidelity 2.5D Layered Anatomical Engine for Root Canal Treatment (RCT) Simulation.
 * Implements:
 * - Dental arch context (pink scalloped gingiva, adjacent teeth crowns).
 * - Central operative molar/premolar anatomical cutaway:
 *   buccal & lingual cusps, developmental groove, glossy pearl enamel layer, warm ivory dentin core,
 *   cervical line (CEJ), and tapering curved root canal leading to apical foramen.
 * - Stage 1: High-speed turbine handpiece & diamond access bur with cooling spray, NaOCl bubbling & necrotic tissue saponification.
 * - Stage 2: NiTi file (ISO Blue #30) with stopper + Side-vented safety needle terminating 2mm short of apex with upward lateral reflux jets + EAL HUD.
 * - Stage 3: Intracanal delivery syringe densely packing white Calcium Hydroxide paste [pH 12.5] from apex to chamber.
 */
fun DrawScope.drawRctProcedureSimulation(
    stageNumber: Int,
    progress: Float,
    size: Size
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h * 0.46f
    val scale = (w / 340f).coerceIn(0.95f, 1.45f)

    // 1. BACKGROUND CLINICAL ENVIRONMENT
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0D1424), Color(0xFF060912))
        ),
        topLeft = Offset.Zero,
        size = size
    )

    // 2. GINGIVAL TISSUE (Gums) & ADJACENT TEETH CONTEXT
    val gumY = cy + 16f * scale

    // Adjacent teeth crowns (left and right silhouettes)
    fun drawAdjacentToothCrown(tx: Float, ty: Float) {
        val tw = 58f * scale
        val th = 66f * scale
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1), Color(0xFFCFD8DC)),
                center = Offset(tx - tw * 0.2f, ty - th * 0.2f),
                radius = tw
            ),
            topLeft = Offset(tx - tw / 2f, ty - th / 2f),
            size = Size(tw, th),
            cornerRadius = CornerRadius(16f * scale, 16f * scale)
        )
        // Root below gum line
        drawRect(
            color = Color(0xFFFFF9C4),
            topLeft = Offset(tx - tw * 0.28f, ty + th / 2f - 4f),
            size = Size(tw * 0.56f, 60f * scale)
        )
    }

    drawAdjacentToothCrown(cx - 105f * scale, cy - 22f * scale)
    drawAdjacentToothCrown(cx + 105f * scale, cy - 22f * scale)

    // Gingiva Base (Alveolar mucosal tissue)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFC2185B), Color(0xFF880E4F), Color(0xFF4A148C))
        ),
        topLeft = Offset(0f, gumY),
        size = Size(w, h - gumY)
    )

    // Scalloped interdental papillae & marginal gingiva
    val scallopStep = 44f * scale
    var gx = -20f * scale
    while (gx < w + 40f * scale) {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFF06292), Color(0xFFE91E63), Color(0xFFC2185B)),
                center = Offset(gx + scallopStep / 2f, gumY),
                radius = scallopStep * 0.7f
            ),
            topLeft = Offset(gx, gumY - 14f * scale),
            size = Size(scallopStep * 1.15f, 24f * scale)
        )
        gx += scallopStep * 0.9f
    }

    // Gingival stippling dots (healthy keratinized tissue texture)
    for (i in 0..18) {
        val dotX = (cx - 140f * scale + (i * 17f * scale) % (280f * scale))
        val dotY = gumY + 12f * scale + ((i * 19f) % (40f * scale))
        drawCircle(
            color = Color(0xFFF8BBD0).copy(alpha = 0.25f),
            radius = 1.5f * scale,
            center = Offset(dotX, dotY)
        )
    }

    // 3. CENTRAL OPERATIVE TOOTH CUTAWAY ANATOMY
    val crownTop = cy - 74f * scale
    val crownW = 80f * scale
    val rootLen = 136f * scale
    val apexY = crownTop + 44f * scale + rootLen

    // Dentin Core (Warm ivory-yellow anatomical contour)
    val dentinPath = Path().apply {
        moveTo(cx - crownW * 0.24f, crownTop)
        quadraticTo(cx, crownTop + 13f * scale, cx + crownW * 0.24f, crownTop)
        quadraticTo(cx + crownW * 0.46f, crownTop + 14f * scale, cx + crownW * 0.42f, crownTop + 44f * scale) // CEJ right
        quadraticTo(cx + 24f * scale, crownTop + 90f * scale, cx + 5.5f * scale, apexY)                         // right root
        lineTo(cx - 5.5f * scale, apexY)                                                                         // apical constriction
        quadraticTo(cx - 24f * scale, crownTop + 90f * scale, cx - crownW * 0.42f, crownTop + 44f * scale) // left CEJ
        quadraticTo(cx - crownW * 0.46f, crownTop + 14f * scale, cx - crownW * 0.24f, crownTop)
        close()
    }

    drawPath(
        dentinPath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFECB3), Color(0xFFFFF8E1), Color(0xFFFFE082))
        )
    )
    drawPath(
        dentinPath,
        color = Color(0xFFD7CCC8),
        style = Stroke(width = 1.5f * scale)
    )

    // Enamel Casing & Cusps (Glossy pearl-white outer shell)
    val enamelPath = Path().apply {
        moveTo(cx - crownW * 0.48f, crownTop + 10f * scale)
        quadraticTo(cx - crownW * 0.25f, crownTop - 5f * scale, cx, crownTop + 12f * scale)
        quadraticTo(cx + crownW * 0.25f, crownTop - 5f * scale, cx + crownW * 0.48f, crownTop + 10f * scale)
        quadraticTo(cx + crownW * 0.44f, crownTop + 40f * scale, cx + crownW * 0.32f, crownTop + 42f * scale)
        quadraticTo(cx + crownW * 0.30f, crownTop + 16f * scale, cx, crownTop + 14f * scale)
        quadraticTo(cx - crownW * 0.30f, crownTop + 16f * scale, cx - crownW * 0.32f, crownTop + 42f * scale)
        quadraticTo(cx - crownW * 0.44f, crownTop + 40f * scale, cx - crownW * 0.48f, crownTop + 10f * scale)
        close()
    }

    drawPath(
        enamelPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFFB0BEC5))
        )
    )

    // Specular highlight gleam on cusps
    drawPath(
        Path().apply {
            moveTo(cx - crownW * 0.38f, crownTop + 4f * scale)
            quadraticTo(cx - crownW * 0.24f, crownTop - 2f * scale, cx - crownW * 0.12f, crownTop + 6f * scale)
        },
        color = Color.White.copy(alpha = 0.75f),
        style = Stroke(width = 2.5f * scale, cap = StrokeCap.Round)
    )

    // 4. OCCLUSAL ACCESS CAVITY & INTERNAL CANAL LUMEN
    val accessW = 26f * scale
    val accessDepth = if (stageNumber > 1) 25f * scale else (10f * scale + 15f * scale * min(progress * 2f, 1f))

    // Dark access cavity opening
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - accessW / 2f, crownTop + 8f * scale),
        size = Size(accessW, accessDepth),
        cornerRadius = CornerRadius(4f * scale, 4f * scale)
    )

    val pulpW = 22f * scale
    val pulpTop = crownTop + 26f * scale
    val pulpFloor = crownTop + 54f * scale

    // Tapered Canal Lumen Path
    val canalLumenPath = Path().apply {
        moveTo(cx - pulpW / 2f, pulpTop)
        lineTo(cx + pulpW / 2f, pulpTop)
        quadraticTo(cx + 14f * scale, pulpFloor, cx + 5.5f * scale, crownTop + 100f * scale)
        lineTo(cx + 2.5f * scale, apexY - 5f * scale)
        lineTo(cx - 2.5f * scale, apexY - 5f * scale)
        lineTo(cx - 5.5f * scale, crownTop + 100f * scale)
        quadraticTo(cx - 14f * scale, pulpFloor, cx - pulpW / 2f, pulpTop)
        close()
    }

    when (stageNumber) {
        1 -> {
            // Stage 1: Infected necrotic pulp tissue dissolving with NaOCl
            drawPath(canalLumenPath, color = Color(0xFF880E4F)) // Deep necrotic red tissue

            // Dissolved chamber zone filled with clear bubbly irrigant
            if (progress > 0.05f) {
                val dissolvedH = (pulpFloor - pulpTop + 24f * scale) * progress
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.55f), Color(0xFF00B0FF).copy(alpha = 0.35f))
                    ),
                    topLeft = Offset(cx - pulpW / 2f - 2f * scale, pulpTop),
                    size = Size(pulpW + 4f * scale, dissolvedH),
                    cornerRadius = CornerRadius(6f * scale, 6f * scale)
                )

                // Effervescent NaOCl saponification micro-bubbles
                for (b in 0..14) {
                    val bx = cx - 9f * scale + ((b * 11f * scale) % (18f * scale))
                    val by = pulpTop + 6f * scale + ((b * 13f * scale) % max(1f, dissolvedH))
                    val br = (1.5f + (b % 3) * 1.2f) * scale
                    drawCircle(
                        color = Color(0xFFE0F7FA),
                        radius = br,
                        center = Offset(bx, by)
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = br,
                        center = Offset(bx, by),
                        style = Stroke(width = 1f)
                    )
                }
            }
        }

        2 -> {
            // Stage 2: Clean instrumented canal with Working Length Safe Zone
            drawPath(canalLumenPath, color = Color(0xFF131C2D))

            // Working Length Safety Marker (2.0 mm short of apex)
            val safeApexY = apexY - 14f * scale
            drawLine(
                color = Color(0xFF00E676),
                start = Offset(cx - 14f * scale, safeApexY),
                end = Offset(cx + 14f * scale, safeApexY),
                strokeWidth = 2.5f * scale,
                cap = StrokeCap.Round
            )

            // Apical foramen constriction ring
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                radius = 4f * scale,
                center = Offset(cx, apexY - 4f * scale),
                style = Stroke(width = 1.5f * scale)
            )
        }

        3 -> {
            // Stage 3: Calcium Hydroxide paste dense backfill
            drawPath(canalLumenPath, color = Color(0xFF131C2D))

            val fillH = (apexY - pulpTop) * progress
            val fillTopY = apexY - fillH

            if (progress > 0.05f) {
                val pasteWidthAtTop = (13f * scale * min(progress * 1.3f, 1f)).coerceAtLeast(3f)
                val pastePath = Path().apply {
                    moveTo(cx - pasteWidthAtTop, fillTopY)
                    quadraticTo(cx, fillTopY - 2f * scale, cx + pasteWidthAtTop, fillTopY)
                    lineTo(cx + 2.5f * scale, apexY - 4f * scale)
                    lineTo(cx - 2.5f * scale, apexY - 4f * scale)
                    close()
                }

                drawPath(
                    pastePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFAFAFA), Color(0xFFFFFFFF), Color(0xFFECEFF1))
                    )
                )

                // Antibacterial hydroxyl ion (OH-) sparkle particles
                for (s in 0..6) {
                    val sx = cx + if (s % 2 == 0) -pasteWidthAtTop - 2f * scale else pasteWidthAtTop + 2f * scale
                    val sy = fillTopY + s * 14f * scale
                    if (sy < apexY - 6f * scale) {
                        drawCircle(color = Color(0xFFFFD54F), radius = 1.5f * scale, center = Offset(sx, sy))
                    }
                }
            }
        }
    }

    // 5. CLINICAL INSTRUMENTATION & DYNAMIC TREATMENT ACTIONS
    when (stageNumber) {
        1 -> {
            // Stage 1: High-Speed Turbine Handpiece & Diamond Access Bur
            val burY = crownTop - 42f * scale + progress * (52f * scale)

            // Contra-Angle Handpiece Head (Satin chrome finish)
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFCFD8DC), Color(0xFFECEFF1), Color(0xFFB0BEC5))
                ),
                topLeft = Offset(cx - 22f * scale, burY - 38f * scale),
                size = Size(44f * scale, 30f * scale),
                cornerRadius = CornerRadius(6f * scale, 6f * scale)
            )

            // Push-button chuck on rear of head
            drawRoundRect(
                color = Color(0xFF78909C),
                topLeft = Offset(cx - 10f * scale, burY - 45f * scale),
                size = Size(20f * scale, 7f * scale),
                cornerRadius = CornerRadius(3f * scale, 3f * scale)
            )

            // Diamond Bur Friction-Grip Shank
            drawRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(cx - 2.5f * scale, burY - 8f * scale),
                size = Size(5f * scale, 18f * scale)
            )

            // Tapered Diamond Bur Head with abrasive texture
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF80DEEA), Color(0xFF00B0FF), Color(0xFF0091EA)),
                    center = Offset(cx - 1f, burY + 14f * scale),
                    radius = 5f * scale
                ),
                topLeft = Offset(cx - 4.5f * scale, burY + 8f * scale),
                size = Size(9f * scale, 14f * scale)
            )

            // Water cooling spray mist
            if (progress > 0.15f) {
                for (w in 0..4) {
                    val angle = (w * 40f) * PI.toFloat() / 180f
                    val mx = cx + cos(angle) * (8f * scale)
                    val my = burY + 12f * scale + sin(angle) * (6f * scale)
                    drawCircle(Color(0xFF80DEEA).copy(alpha = 0.65f), radius = 2f * scale, center = Offset(mx, my))
                }
            }
        }

        2 -> {
            // Stage 2: Dual Visualization (Reference Video NiTi File + Side-Vented Needle)

            // A. NiTi Hand File (#30 ISO Blue Handle, Yellow Rubber Stop, Fluted Shaft)
            val fileX = cx - 28f * scale
            val fileY = crownTop - 46f * scale + progress * (36f * scale)

            // ISO Blue contoured handle
            drawRoundRect(
                color = Color(0xFF0288D1),
                topLeft = Offset(fileX - 7f * scale, fileY - 42f * scale),
                size = Size(14f * scale, 36f * scale),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            // ISO Size #30 White Marking on Handle
            drawIntoCanvas { canvas ->
                val p = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 9f * scale
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText("30", fileX, fileY - 22f * scale, p)
            }

            // Calibrated Yellow Silicone Stopper
            drawRoundRect(
                color = Color(0xFFFFD600),
                topLeft = Offset(fileX - 5.5f * scale, fileY - 6f * scale),
                size = Size(11f * scale, 6f * scale),
                cornerRadius = CornerRadius(2f * scale, 2f * scale)
            )

            // Tapered NiTi shaft with spiraling cutting flutes
            val shaftLen = 42f * scale
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFEEEEEE), Color(0xFFB0BEC5), Color(0xFF90A4AE))
                ),
                start = Offset(fileX, fileY),
                end = Offset(fileX, fileY + shaftLen),
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round
            )
            // Flute spiral tick lines
            for (f in 0..4) {
                val fy = fileY + 6f * scale + f * 7f * scale
                drawLine(
                    color = Color(0xFF78909C),
                    start = Offset(fileX - 2.5f * scale, fy - 2f * scale),
                    end = Offset(fileX + 2.5f * scale, fy + 2f * scale),
                    strokeWidth = 1.5f * scale
                )
            }

            // B. Side-Vented Safety Needle (Active tool descending into canal)
            val needleTipY = pulpTop + (apexY - 14f * scale - pulpTop) * progress
            val needleTop = crownTop - 52f * scale

            // Yellow Safety Luer Lock Hub
            drawRoundRect(
                color = Color(0xFFFFD600),
                topLeft = Offset(cx - 7f * scale, needleTop - 18f * scale),
                size = Size(14f * scale, 18f * scale),
                cornerRadius = CornerRadius(3f * scale, 3f * scale)
            )

            // Stainless Steel 30G Cannula Shaft
            drawRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(cx - 1.8f * scale, needleTop),
                size = Size(3.6f * scale, needleTipY - needleTop)
            )

            // Closed Rounded Atraumatic Blunt Tip (Bullet nose avoiding apical perforation)
            drawOval(
                color = Color(0xFFB0BEC5),
                topLeft = Offset(cx - 2.2f * scale, needleTipY - 2f * scale),
                size = Size(4.4f * scale, 5f * scale)
            )

            // Lateral Discharge Vent Notch (1.5mm from tip)
            val ventY = needleTipY - 6f * scale
            drawRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(cx - 2f * scale, ventY - 2.5f * scale),
                size = Size(2f * scale, 5f * scale)
            )

            // Dynamic Lateral Reflux Jets (Twin streams discharging coronally against dentin walls)
            if (progress > 0.35f) {
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(cx - 2f * scale, ventY),
                    end = Offset(cx - 13f * scale, ventY - 14f * scale),
                    strokeWidth = 2.5f * scale,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(cx + 2f * scale, ventY),
                    end = Offset(cx + 13f * scale, ventY - 14f * scale),
                    strokeWidth = 2.5f * scale,
                    cap = StrokeCap.Round
                )
            }

            // Electronic Apex Locator (EAL) Mini Medical HUD (Top-Left)
            val ealX = 14f
            val ealY = 14f
            val ealW = 96f * scale
            val ealH = 44f * scale

            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(ealX, ealY),
                size = Size(ealW, ealH),
                cornerRadius = CornerRadius(8f * scale, 8f * scale)
            )
            drawRoundRect(
                color = Color(0xFF00251A),
                topLeft = Offset(ealX + 4f, ealY + 4f),
                size = Size(ealW - 8f, ealH - 8f),
                cornerRadius = CornerRadius(5f * scale, 5f * scale)
            )

            val isApexSafe = progress > 0.78f
            drawIntoCanvas { canvas ->
                val p = Paint().apply {
                    color = if (isApexSafe) android.graphics.Color.parseColor("#00E676") else android.graphics.Color.parseColor("#FFD600")
                    textSize = 8.5f * scale
                    isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText("EAL: ${if (isApexSafe) "0.5mm SAFE" else "APPROACH"}", ealX + 8f, ealY + 16f * scale, p)
            }

            // Digital LED bars on EAL
            for (bar in 0..4) {
                val active = (progress * 5f).toInt() >= bar
                val barColor = if (isApexSafe && bar >= 3) Color(0xFF00E676) else if (active) Color(0xFFFFD600) else Color(0xFF004D40)
                drawRect(
                    color = barColor,
                    topLeft = Offset(ealX + 8f + bar * 15f * scale, ealY + 22f * scale),
                    size = Size(10f * scale, 10f * scale)
                )
            }
        }

        3 -> {
            // Stage 3: Intracanal Medicament Syringe & Backfill Placement
            val fillTopY = apexY - (apexY - pulpTop) * progress
            val syringeTipY = (fillTopY - 8f * scale).coerceAtLeast(crownTop - 12f * scale)
            val syringeTop = crownTop - 56f * scale

            // Translucent Syringe Barrel
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0.4f))
                ),
                topLeft = Offset(cx - 10f * scale, syringeTop - 36f * scale),
                size = Size(20f * scale, 36f * scale),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )

            // Volumetric graduation markings on barrel
            for (m in 1..3) {
                val my = syringeTop - 36f * scale + m * 8f * scale
                drawLine(
                    color = Color.Black.copy(alpha = 0.4f),
                    start = Offset(cx - 8f * scale, my),
                    end = Offset(cx - 2f * scale, my),
                    strokeWidth = 1.5f * scale
                )
            }

            // White paste inside syringe reservoir
            drawRect(
                color = Color(0xFFFAFAFA),
                topLeft = Offset(cx - 8f * scale, syringeTop - 24f * scale),
                size = Size(16f * scale, 22f * scale)
            )

            // Fine flexible delivery cannula
            drawRect(
                color = Color(0xFFFFD54F),
                topLeft = Offset(cx - 1.8f * scale, syringeTop),
                size = Size(3.6f * scale, syringeTipY - syringeTop)
            )

            // Extruded paste bead forming at the needle tip
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = 3.5f * scale,
                center = Offset(cx, syringeTipY)
            )
        }
    }
}
