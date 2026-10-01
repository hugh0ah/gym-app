package com.fittracker.app.ui.symmetry

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittracker.app.ui.theme.*

@Composable
fun SymmetryScreen(
    viewModel: SymmetryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val selectedMuscle = state.muscles.find { it.id == state.selectedMuscleId } ?: state.muscles.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera de Simetría y Puntuación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mapa de Calor Muscular",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Volumen semanal y simetría anatómica",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }

            // Puntuación de Simetría
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonMint.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessibilityNew,
                        contentDescription = null,
                        tint = NeonMint,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${state.balance.overallScore}% Simetría",
                        color = NeonMint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Métricas Rápidas de Balance (Empuje vs Tirón, Superior vs Inferior)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BalanceMetricCard(
                title = "Empuje vs Tirón",
                labelLeft = "Push",
                valueLeft = "${state.balance.pushSets}s",
                labelRight = "Pull",
                valueRight = "${state.balance.pullSets}s",
                ratioText = "Ratio ${String.format("%.2f", state.balance.pushPullRatio)}",
                isBalanced = state.balance.pushPullRatio in 0.8f..1.25f,
                modifier = Modifier.weight(1f)
            )

            BalanceMetricCard(
                title = "Superior vs Inferior",
                labelLeft = "Upper",
                valueLeft = "${state.balance.upperSets}s",
                labelRight = "Lower",
                valueRight = "${state.balance.lowerSets}s",
                ratioText = "Ratio ${String.format("%.2f", state.balance.upperLowerRatio)}",
                isBalanced = state.balance.upperLowerRatio in 1.0f..1.6f,
                modifier = Modifier.weight(1f)
            )
        }

        // Selector Frontal / Dorsal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { viewModel.setViewMode(BodyViewMode.ANTERIOR) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.viewMode == BodyViewMode.ANTERIOR) NeonMint else Color.Transparent,
                    contentColor = if (state.viewMode == BodyViewMode.ANTERIOR) Color.Black else TextMuted
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Vista Frontal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = { viewModel.setViewMode(BodyViewMode.POSTERIOR) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.viewMode == BodyViewMode.POSTERIOR) ElectricCyan else Color.Transparent,
                    contentColor = if (state.viewMode == BodyViewMode.POSTERIOR) Color.Black else TextMuted
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Vista Dorsal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Leyenda de Rango de Fatiga y Volumen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LegendPill(color = Color(0xFF263248), label = "Sin entrenar (0)")
            LegendPill(color = NeonMint, label = "MEV Mínimo (6-9)")
            LegendPill(color = ElectricCyan, label = "MAV Óptimo (10-18)")
            LegendPill(color = AlertRed, label = "MRV Fatiga (>18)")
        }

        // Canvas de Anatomía Humana
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                MuscleHeatmap(
                    viewMode = state.viewMode,
                    muscles = state.muscles,
                    selectedMuscleId = state.selectedMuscleId,
                    onSelectMuscle = { viewModel.selectMuscle(it) }
                )
            }
        }

        // Detalle del Músculo Seleccionado
        if (selectedMuscle != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, selectedMuscle.status.color.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedMuscle.spanishName,
                                color = TextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedMuscle.status.description,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(selectedMuscle.status.color.copy(alpha = 0.18f))
                                .border(1.dp, selectedMuscle.status.color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = selectedMuscle.status.label,
                                color = selectedMuscle.status.color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Barra de progreso y marcadores MEV / MAV / MRV
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Volumen 7 días: ${selectedMuscle.weeklySets} series", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Objetivo MAV: ${selectedMuscle.mav} series", color = ElectricCyan, fontSize = 12.sp)
                        }

                        LinearProgressIndicator(
                            progress = { selectedMuscle.progressFraction.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = selectedMuscle.status.color,
                            trackColor = DarkSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0 MEV (${selectedMuscle.mev})", color = TextSubtle, fontSize = 10.sp)
                            Text("MAV (${selectedMuscle.mav})", color = TextSubtle, fontSize = 10.sp)
                            Text("MRV (${selectedMuscle.mrv})", color = TextSubtle, fontSize = 10.sp)
                        }
                    }

                    // Ejercicios que activan este músculo
                    if (selectedMuscle.targetExercises.isNotEmpty()) {
                        Text(
                            text = "Ejercicios recientes:",
                            color = TextLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedMuscle.targetExercises.forEach { exName ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurface)
                                        .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(exName, color = TextWhite, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selector rápido de grupos musculares
        Text(
            text = "Explorar Grupos Musculares",
            color = TextLight,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.muscles.forEach { muscle ->
                val isSelected = muscle.id == state.selectedMuscleId
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectMuscle(muscle.id) },
                    label = { Text("${muscle.spanishName} (${muscle.weeklySets}s)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = muscle.status.color.copy(alpha = 0.25f),
                        selectedLabelColor = TextWhite,
                        containerColor = DarkSurface,
                        labelColor = TextMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) muscle.status.color else DarkCardBorder
                    )
                )
            }
        }
    }
}

@Composable
private fun BalanceMetricCard(
    title: String,
    labelLeft: String,
    valueLeft: String,
    labelRight: String,
    valueRight: String,
    ratioText: String,
    isBalanced: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(labelLeft, color = TextSubtle, fontSize = 10.sp)
                    Text(valueLeft, color = NeonMint, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(labelRight, color = TextSubtle, fontSize = 10.sp)
                    Text(valueRight, color = ElectricCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                color = if (isBalanced) NeonMint.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = ratioText,
                    color = if (isBalanced) NeonMint else WarningAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun LegendPill(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, color = TextMuted, fontSize = 11.sp)
    }
}
