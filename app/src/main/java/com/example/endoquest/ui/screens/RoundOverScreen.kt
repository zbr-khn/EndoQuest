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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endoquest.ui.viewmodel.GameViewModel

@Composable
fun RoundOverScreen(
    viewModel: GameViewModel,
    onRestartRound: () -> Unit,
    onReturnToMenu: () -> Unit
) {
    val info = viewModel.roundOverInfo

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0D131F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "❌🦷🚨", fontSize = 64.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ROUND ${info.roundNumber} OVER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF5252)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = info.subtitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = Color(0xFFFFD54F)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reason Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "📋 Reason for Round Failure:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF80DEEA)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = info.reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    if (info.explanation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "💡 Clinical Rationale:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFD54F)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = info.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score/Progress Display
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Funds at Round Failure:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "🪙 $${info.moneyAtFailure}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFFD700)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.restartCurrentRound()
                    onRestartRound()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text(
                    text = "🔄 Restart Round ${info.roundNumber}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    viewModel.resetGame()
                    onReturnToMenu()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text(text = "Return to Main Menu", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
