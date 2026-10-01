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
fun GameOverScreen(
    viewModel: GameViewModel,
    onRetry: () -> Unit,
    onReturnToMenu: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0D131F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "💥🦷⚡", fontSize = 64.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Dental Obstacle Collision!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF5252)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Alex tripped over dental equipment on the highway!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Pro Tip Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Clinical Obstacle Tips:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Empty Gum Socket ➔ Swipe UP to Jump\n• Decayed Tooth Arch ➔ Swipe DOWN to Slide\n• Giant Rotating Bur ➔ Swipe LEFT/RIGHT to Dodge",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    viewModel.resetRunner()
                    onRetry()
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text(text = "Try Again 🏃‍♂️💨", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = {
                    viewModel.resetGame()
                    onReturnToMenu()
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text(text = "Return to Main Menu", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
