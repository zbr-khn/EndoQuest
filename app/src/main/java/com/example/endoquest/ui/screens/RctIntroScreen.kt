package com.example.endoquest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endoquest.ui.viewmodel.GameViewModel

@Composable
fun RctIntroScreen(
    viewModel: GameViewModel,
    onStartRctStages: () -> Unit
) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🏥👨‍⚕️🦷", fontSize = 64.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Back at Radiant Smile Clinic!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🪙 Funds Collected: $${viewModel.progress.moneyCollected}!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"Incredible work, Alex!\" says Dr. Smile warmly. \"You conquered the Dental Highway, unlocked the Enamel Maze, and defeated the Decay Monsters to secure all \$5,000 for your treatment! Now, take a seat in the dental chair as we perform the ${viewModel.rctStages.size} clinical stages to save your tooth!\"",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = onStartRctStages,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text(text = "Begin Clinical RCT Stages ➡️", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            }
        }
    }
}
