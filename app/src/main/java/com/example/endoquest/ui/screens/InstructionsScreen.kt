package com.example.endoquest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InstructionsScreen(
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0D131F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎮 How to Play",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            InstructionCard(
                step = "1",
                emoji = "🏃‍♂️",
                title = "Round 1: 3D Highway Runner",
                description = "Swipe Left / Right to switch between 3 lanes.\nSwipe Up to JUMP over empty gum sockets.\nSwipe Down to SLIDE under giant decayed teeth.\nDodge rotating burs! Officer Floss inspects every 15s (+$500 per correct answer)."
            )

            Spacer(modifier = Modifier.height(14.dp))

            InstructionCard(
                step = "2",
                emoji = "🧩",
                title = "Round 2: Three-Door Enamel Maze",
                description = "Use the Virtual Touch Joystick at the bottom to navigate corridors. Evade the Pac-Man-style oral bacterium pursuer! Unlock Door 1, Door 2, and Door 3 to open the Exit Portal ($3,500 target balance)."
            )

            Spacer(modifier = Modifier.height(14.dp))

            InstructionCard(
                step = "3",
                emoji = "🧟‍♂️",
                title = "Round 3: Zombie Dental Combat",
                description = "Face 3 persistent biofilm mutants (Dr. Decay, Tartar Titan, E. faecalis Overlord). Answer clinical questions within 15 seconds (+$500 each) to reach the full $5,000 clinic goal."
            )

            Spacer(modifier = Modifier.height(14.dp))

            InstructionCard(
                step = "4",
                emoji = "🦷",
                title = "Round 4: Clinical RCT Simulation",
                description = "Enter Dr. Smile's operatory with zero quiz interruptions! Use the interactive procedure slider to execute chemo-mechanical debridement, apical irrigation, and Ca(OH)₂ medicament placement."
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text(text = "Back to Menu ⬅️", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InstructionCard(step: String, emoji: String, title: String, description: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF80DEEA)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
