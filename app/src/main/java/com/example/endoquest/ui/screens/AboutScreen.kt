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
fun AboutScreen(
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
                text = "📚 Clinical Curriculum",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF)
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Clinical Endodontics Curriculum",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "EndoQuest: The RCT Run was created to bridge theoretical endodontic textbooks and clinical practice for dental students, dental hygiene professionals, and residents.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Core Clinical Modules Covered:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Level 1: The Pulp Chamber (Tissue & Smear Layer: NaOCl organic dissolution vs 17% EDTA inorganic demineralization)\n" +
                                "2. Level 2: The Apical Danger Zone (Chemical Safety & Hazards: 2-3mm apical safety, side-vented reflux, NaOCl + CHX PCA precipitation hazard)\n" +
                                "3. Level 3: The Inter-Appointment Vault (Medicaments: Calcium Hydroxide alkaline pH ~12.5 OH⁻ release, Ledermix corticosteroid-antibiotic, Metapex)",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Architecture & Sound System",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Built natively using Kotlin, Jetpack Compose, Material 3, Coroutines, and real-time synthesized low-latency PCM audio on Android AudioTrack.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

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
        }
    }
}
