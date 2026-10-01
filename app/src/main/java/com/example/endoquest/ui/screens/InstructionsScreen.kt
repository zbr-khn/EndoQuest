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
                title = "3D Runner Controls",
                description = "Swipe Left / Right to switch between the 3 lanes.\nSwipe Up to JUMP over empty gum sockets.\nSwipe Down to SLIDE under giant decayed teeth.\nDodge giant rotating dental burs!"
            )

            Spacer(modifier = Modifier.height(14.dp))

            InstructionCard(
                step = "2",
                emoji = "🪙",
                title = "Economy & $25 Golden Coins",
                description = "Every spinning metallic gold coin adds exactly $25 to your treatment fund. Collect 20 coins to hit $500 milestones, on your journey to the $5,000 RCT goal."
            )

            Spacer(modifier = Modifier.height(14.dp))

            InstructionCard(
                step = "3",
                emoji = "👮‍♂️",
                title = "Dental Police Road Inspection",
                description = "At $500, a Dental Police officer sprints onto the highway with a glowing stop baton! Pass the clinical inspection quiz to earn +$50 reward and an officer salute before resuming."
            )

            Spacer(modifier = Modifier.height(14.dp))

            InstructionCard(
                step = "4",
                emoji = "🦷",
                title = "5-Stage RCT Clinical Masterclass",
                description = "Once $5,000 is collected, return to Dr. Smile. Complete the 5 clinical stages: Access Cavity Preparation, Working Length, Cleaning & Shaping, Obturation, and Final Restoration with interactive simulations!"
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
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF00E5FF),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
