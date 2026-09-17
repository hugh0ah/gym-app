package com.fittracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fittracker.app.data.local.entities.Exercise
import com.fittracker.app.ui.theme.*

data class PresetExerciseItem(
    val id: String,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double,
    val dayOfWeek: Int
)

data class PresetRoutineTemplate(
    val name: String,
    val description: String,
    val daysPerWeek: Int,
    val level: String,
    val exercises: List<PresetExerciseItem>
)

@Composable
fun PresetRoutinesDialog(
    onDismiss: () -> Unit,
    onImportRoutine: (PresetRoutineTemplate) -> Unit
) {
    val presets = remember {
        listOf(
            PresetRoutineTemplate(
                name = "Push / Pull / Legs (PPL Clásica)",
                description = "El plan de iniciación oficial de OpenGym. Divide el cuerpo en Empuje, Tirón y Pierna.",
                daysPerWeek = 3,
                level = "Intermedio",
                exercises = listOf(
                    // Lunes: Push (dayOfWeek = 1)
                    PresetExerciseItem("barbell_bench_press", "Press de banca con barra", 4, 8, 70.0, 1),
                    PresetExerciseItem("incline_dumbbell_press", "Press inclinado mancuernas", 3, 10, 24.0, 1),
                    PresetExerciseItem("chest_dips", "Fondos en paralelas", 3, 10, 0.0, 1),
                    PresetExerciseItem("overhead_press", "Press militar con barra", 3, 10, 45.0, 1),
                    PresetExerciseItem("lateral_raise", "Elevaciones laterales", 3, 12, 12.0, 1),
                    PresetExerciseItem("tricep_pushdown", "Extensiones de tríceps en polea", 3, 12, 25.0, 1),

                    // Miércoles: Pull (dayOfWeek = 3)
                    PresetExerciseItem("pull_up", "Dominadas prono", 4, 8, 0.0, 3),
                    PresetExerciseItem("barbell_row", "Remo con barra", 4, 8, 65.0, 3),
                    PresetExerciseItem("lat_pulldown", "Jalón al pecho en polea", 3, 10, 60.0, 3),
                    PresetExerciseItem("seated_cable_row", "Remo sentado polea gironda", 3, 10, 55.0, 3),
                    PresetExerciseItem("barbell_bicep_curl", "Curl de bíceps con barra", 3, 12, 30.0, 3),

                    // Viernes: Legs (dayOfWeek = 5)
                    PresetExerciseItem("barbell_squat", "Sentadilla trasera con barra", 4, 8, 90.0, 5),
                    PresetExerciseItem("leg_press", "Prensa de piernas 45º", 3, 10, 160.0, 5),
                    PresetExerciseItem("romanian_deadlift", "Peso muerto rumano", 3, 10, 80.0, 5),
                    PresetExerciseItem("leg_extension", "Extensiones de cuádriceps", 3, 12, 50.0, 5),
                    PresetExerciseItem("leg_curl", "Curl femoral tumbado", 3, 12, 45.0, 5)
                )
            ),
            PresetRoutineTemplate(
                name = "Torso / Pierna Frecuencia 2 (4 Días)",
                description = "Máxima evidencia científica para ganar masa muscular y fuerza equilibrada.",
                daysPerWeek = 4,
                level = "Intermedio / Avanzado",
                exercises = listOf(
                    // Lunes: Torso 1 (1)
                    PresetExerciseItem("barbell_bench_press", "Press de banca", 4, 6, 80.0, 1),
                    PresetExerciseItem("barbell_row", "Remo con barra", 4, 8, 70.0, 1),
                    PresetExerciseItem("overhead_press", "Press militar", 3, 8, 50.0, 1),
                    PresetExerciseItem("lat_pulldown", "Jalón al pecho", 3, 10, 65.0, 1),
                    PresetExerciseItem("chest_dips", "Fondos", 3, 10, 0.0, 1),

                    // Martes: Pierna 1 (2)
                    PresetExerciseItem("barbell_squat", "Sentadilla trasera", 4, 6, 100.0, 2),
                    PresetExerciseItem("romanian_deadlift", "Peso muerto rumano", 4, 8, 85.0, 2),
                    PresetExerciseItem("leg_press", "Prensa 45º", 3, 10, 180.0, 2),
                    PresetExerciseItem("leg_curl", "Curl femoral", 3, 12, 50.0, 2),

                    // Jueves: Torso 2 (4)
                    PresetExerciseItem("incline_dumbbell_press", "Press inclinado", 4, 8, 28.0, 4),
                    PresetExerciseItem("pull_up", "Dominadas", 4, 8, 0.0, 4),
                    PresetExerciseItem("lateral_raise", "Elevaciones laterales", 4, 12, 12.0, 4),
                    PresetExerciseItem("barbell_bicep_curl", "Curl bíceps", 3, 10, 32.5, 4),
                    PresetExerciseItem("tricep_pushdown", "Tríceps polea", 3, 12, 27.5, 4),

                    // Viernes: Pierna 2 (5)
                    PresetExerciseItem("deadlift", "Peso muerto convencional", 4, 5, 110.0, 5),
                    PresetExerciseItem("barbell_squat", "Sentadilla frontal / trasera", 3, 8, 85.0, 5),
                    PresetExerciseItem("leg_extension", "Extensiones cuádriceps", 3, 12, 55.0, 5),
                    PresetExerciseItem("leg_curl", "Curl femoral", 3, 12, 50.0, 5)
                )
            ),
            PresetRoutineTemplate(
                name = "Full Body Clásica (3 Días)",
                description = "Ideal para entrenar todo el cuerpo en cada sesión con los grandes ejercicios básicos.",
                daysPerWeek = 3,
                level = "Todos los niveles",
                exercises = listOf(
                    // Lunes (1)
                    PresetExerciseItem("barbell_squat", "Sentadilla", 3, 8, 80.0, 1),
                    PresetExerciseItem("barbell_bench_press", "Press banca", 3, 8, 70.0, 1),
                    PresetExerciseItem("barbell_row", "Remo con barra", 3, 8, 60.0, 1),
                    PresetExerciseItem("overhead_press", "Press militar", 3, 10, 40.0, 1),

                    // Miércoles (3)
                    PresetExerciseItem("deadlift", "Peso muerto", 3, 6, 95.0, 3),
                    PresetExerciseItem("incline_dumbbell_press", "Press inclinado", 3, 10, 22.0, 3),
                    PresetExerciseItem("pull_up", "Dominadas", 3, 8, 0.0, 3),
                    PresetExerciseItem("chest_dips", "Fondos", 3, 10, 0.0, 3),

                    // Viernes (5)
                    PresetExerciseItem("barbell_squat", "Sentadilla", 3, 8, 82.5, 5),
                    PresetExerciseItem("barbell_bench_press", "Press banca", 3, 8, 72.5, 5),
                    PresetExerciseItem("lat_pulldown", "Jalón polea", 3, 10, 60.0, 5),
                    PresetExerciseItem("barbell_bicep_curl", "Curl bíceps", 3, 12, 28.0, 5)
                )
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(26.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Rutinas de OpenGym",
                                color = TextDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Planes probados para hipertrofia y fuerza",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(presets) { preset ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(CreamSurfaceVariant)
                                .border(1.dp, CreamBorder, RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.name,
                                        color = TextDark,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Badge(containerColor = ElectricBlueContainer) {
                                        Text("${preset.daysPerWeek} días/sem", color = ElectricBlueDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(
                                    text = preset.description,
                                    color = TextDarkMuted,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = "${preset.exercises.size} ejercicios programados · Nivel: ${preset.level}",
                                    color = ElectricBlueDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Button(
                                    onClick = {
                                        onImportRoutine(preset)
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cargar esta Rutina", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
