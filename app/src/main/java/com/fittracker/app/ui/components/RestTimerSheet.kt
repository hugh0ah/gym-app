package com.fittracker.app.ui.components

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fittracker.app.ui.theme.*
import kotlinx.coroutines.delay

object RestRecommendationHelper {
    /**
     * Calcula el tiempo de descanso recomendado en segundos y la justificación según la carga y tipo de ejercicio.
     */
    fun getRecommendation(exerciseName: String, muscleGroup: String, reps: Int, weight: Double): Pair<Int, String> {
        val lowerName = exerciseName.lowercase()
        val isHeavyCompound = lowerName.contains("bench press") ||
                lowerName.contains("squat") ||
                lowerName.contains("deadlift") ||
                lowerName.contains("overhead") ||
                lowerName.contains("press militar") ||
                lowerName.contains("sentadilla") ||
                lowerName.contains("peso muerto")

        return when {
            reps <= 6 || (isHeavyCompound && weight >= 70) -> {
                150 to "Carga pesada / Fuerza máxima (2:30 min). El sistema neuromuscular necesita recuperación completa de ATP-PC."
            }
            isHeavyCompound || reps in 7..10 -> {
                90 to "Rango de Hipertrofia (1:30 min). Balance óptimo entre fatiga metabólica y tensión mecánica."
            }
            muscleGroup.equals("Core", ignoreCase = true) || muscleGroup.equals("Arms", ignoreCase = true) || reps > 12 -> {
                60 to "Aislamiento y bombeo (1:00 min). Suficiente para recuperar grupos musculares pequeños."
            }
            else -> {
                90 to "Descanso recomendado estándar de 90 segundos."
            }
        }
    }
}

@Composable
fun RestTimerDialog(
    exerciseName: String,
    muscleGroup: String,
    reps: Int,
    weight: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val (recommendedSeconds, recommendationReason) = remember(exerciseName, reps, weight) {
        RestRecommendationHelper.getRecommendation(exerciseName, muscleGroup, reps, weight)
    }

    var totalSeconds by remember { mutableIntStateOf(recommendedSeconds) }
    var secondsLeft by remember { mutableIntStateOf(recommendedSeconds) }
    var isRunning by remember { mutableStateOf(true) }

    // Cuenta atrás reactiva
    LaunchedEffect(isRunning, secondsLeft) {
        if (isRunning && secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
            if (secondsLeft == 0) {
                // Vibración al terminar
                triggerVibration(context)
            }
        }
    }

    val progress = if (totalSeconds > 0) (secondsLeft.toFloat() / totalSeconds.toFloat()) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "RestProgress")

    val minutes = secondsLeft / 60
    val secs = secondsLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, secs)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentCoral.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Tiempo de Descanso",
                                color = TextDark,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = exerciseName,
                                color = TextDarkMuted,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Recomendación de descanso basada en carga
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CreamBg)
                        .border(1.dp, CreamBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(16.dp))
                            Text(
                                text = "RECOMENDACIÓN POR CARGA (${reps} reps @ ${weight}kg)",
                                color = AccentCoral,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = recommendationReason,
                            color = TextDark,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Reloj Circular
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 10.dp.toPx()
                        // Track background
                        drawCircle(
                            color = CreamBorder,
                            radius = (size.minDimension - strokeWidth) / 2,
                            style = Stroke(width = strokeWidth)
                        )
                        // Progress arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(AccentCoral, ElectricBlue, AccentCoral)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedTime,
                            color = if (secondsLeft == 0) AccentCoral else TextDark,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (secondsLeft == 0) "¡Listo para la serie!" else if (isRunning) "Descansando..." else "Pausado",
                            color = if (secondsLeft == 0) AccentCoral else TextDarkMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Ajustes rápidos (+30s / -15s / +60s)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            secondsLeft = (secondsLeft - 15).coerceAtLeast(0)
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("-15s", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            secondsLeft += 30
                            totalSeconds += 30
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+30s", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            secondsLeft += 60
                            totalSeconds += 60
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+1 min", fontSize = 12.sp)
                    }
                }

                // Botones principales de control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = TextDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRunning) "Pausar" else "Reanudar", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saltar / Listo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun triggerVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(400)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
