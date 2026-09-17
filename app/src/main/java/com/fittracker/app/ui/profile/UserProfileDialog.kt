package com.fittracker.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fittracker.app.data.local.entities.UserProfile
import com.fittracker.app.ui.theme.*

@Composable
fun UserProfileDialog(
    initialProfile: UserProfile,
    onDismiss: () -> Unit,
    onSaveProfile: (UserProfile) -> Unit
) {
    var gender by remember { mutableStateOf(initialProfile.gender) }
    var ageText by remember { mutableStateOf(initialProfile.age.toString()) }
    var heightText by remember { mutableStateOf(initialProfile.heightCm.toInt().toString()) }
    var weightText by remember { mutableStateOf(initialProfile.weightKg.toString()) }
    var activityLevel by remember { mutableStateOf(initialProfile.activityLevel) }
    var goal by remember { mutableStateOf(initialProfile.goal) }

    // Perfil temporal calculado en vivo
    val tempProfile = remember(gender, ageText, heightText, weightText, activityLevel, goal) {
        val age = ageText.toIntOrNull() ?: initialProfile.age
        val height = heightText.toDoubleOrNull() ?: initialProfile.heightCm
        val weight = weightText.toDoubleOrNull() ?: initialProfile.weightKg
        UserProfile(
            id = 1,
            gender = gender,
            age = age,
            heightCm = height,
            weightKg = weight,
            activityLevel = activityLevel,
            goal = goal
        )
    }

    val liveBmr = tempProfile.calculateBmr()
    val liveTdee = tempProfile.calculateTdee()
    val liveTargets = tempProfile.calculateRecommendedTargets()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(26.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
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
                                .background(AccentCoral.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Perfil y Cálculo Metabólico",
                                color = TextDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Fórmula Mifflin-St Jeor (Sexo y Peso)",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Selector de Sexo Biológico (clave para la fórmula metabólica)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sexo Biológico",
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isMale = gender.equals("MALE", ignoreCase = true)
                        ProfileOptionChip(
                            label = "Hombre ♂",
                            subtitle = "+5 kcal en fórmula",
                            isSelected = isMale,
                            color = ElectricBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { gender = "MALE" }
                        )
                        ProfileOptionChip(
                            label = "Mujer ♀",
                            subtitle = "-161 kcal en fórmula",
                            isSelected = !isMale,
                            color = AccentCoral,
                            modifier = Modifier.weight(1f),
                            onClick = { gender = "FEMALE" }
                        )
                    }
                }

                // Campos Numéricos: Peso, Altura, Edad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Peso (kg)", fontSize = 11.sp, color = TextDarkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedBorderColor = AccentCoral,
                            unfocusedBorderColor = CreamBorder
                        )
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Altura (cm)", fontSize = 11.sp, color = TextDarkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedBorderColor = AccentCoral,
                            unfocusedBorderColor = CreamBorder
                        )
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Edad (años)", fontSize = 11.sp, color = TextDarkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedBorderColor = AccentCoral,
                            unfocusedBorderColor = CreamBorder
                        )
                    )
                }

                // Nivel de Actividad Física
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Nivel de Actividad Física",
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ActivityChip("Sedentario", "SEDENTARY", activityLevel, Modifier.weight(1f)) { activityLevel = it }
                        ActivityChip("Ligero", "LIGHT", activityLevel, Modifier.weight(1f)) { activityLevel = it }
                        ActivityChip("Moderado", "MODERATE", activityLevel, Modifier.weight(1f)) { activityLevel = it }
                        ActivityChip("Activo", "ACTIVE", activityLevel, Modifier.weight(1f)) { activityLevel = it }
                    }
                }

                // Objetivo Físico
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Objetivo Nutricional",
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoalChip(
                            title = "Déficit / Grasa",
                            desc = "-450 kcal",
                            code = "FAT_LOSS",
                            currentGoal = goal,
                            color = CalorieOrange,
                            modifier = Modifier.weight(1f)
                        ) { goal = it }
                        GoalChip(
                            title = "Mantenimiento",
                            desc = "Normocalórica",
                            code = "MAINTENANCE",
                            currentGoal = goal,
                            color = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        ) { goal = it }
                        GoalChip(
                            title = "Volumen / Masa",
                            desc = "+300 kcal",
                            code = "MUSCLE_GAIN",
                            currentGoal = goal,
                            color = SecondaryEmerald,
                            modifier = Modifier.weight(1f)
                        ) { goal = it }
                    }
                }

                // Tarjeta de Resultados Metabólicos en Vivo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CreamBg)
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
                                text = "Resultados Calculados",
                                color = TextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(containerColor = AccentCoral.copy(alpha = 0.15f)) {
                                Text("Mifflin-St Jeor", color = AccentCoral, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetabolicStatBox(
                                label = "BMR (Reposo)",
                                value = "${liveBmr.toInt()} kcal",
                                modifier = Modifier.weight(1f),
                                color = TextDark
                            )
                            MetabolicStatBox(
                                label = "TDEE (Gasto Total)",
                                value = "${liveTdee.toInt()} kcal",
                                modifier = Modifier.weight(1f),
                                color = ElectricBlue
                            )
                            MetabolicStatBox(
                                label = "Meta Diaria",
                                value = "${liveTargets.calorieTarget.toInt()} kcal",
                                modifier = Modifier.weight(1f),
                                color = AccentCoral
                            )
                        }

                        HorizontalDivider(color = CreamBorder)

                        // Macros recomendados
                        Text(
                            text = "Macros Calculados según peso (${tempProfile.weightKg} kg):",
                            color = TextDarkMuted,
                            fontSize = 11.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🥩 Proteína: ${liveTargets.proteinTarget.toInt()}g (~2g/kg)",
                                color = ElectricBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "🥑 Grasa: ${liveTargets.fatTarget.toInt()}g (~0.9g/kg)",
                                color = FatPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "🍚 Carbos: ${liveTargets.carbTarget.toInt()}g",
                                color = CalorieOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Botón Guardar y Aplicar
                Button(
                    onClick = {
                        onSaveProfile(tempProfile)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Guardar y Actualizar Objetivos de la App",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileOptionChip(
    label: String,
    subtitle: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) color.copy(alpha = 0.15f) else CreamSurface)
            .border(
                1.5.dp,
                if (isSelected) color else CreamBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                color = if (isSelected) color else TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextDarkMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ActivityChip(
    label: String,
    code: String,
    current: String,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    val isSelected = current.equals(code, ignoreCase = true)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) AccentCoral.copy(alpha = 0.15f) else CreamSurface)
            .border(
                1.dp,
                if (isSelected) AccentCoral else CreamBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick(code) }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) AccentCoral else TextDarkMuted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun GoalChip(
    title: String,
    desc: String,
    code: String,
    currentGoal: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    val isSelected = currentGoal.equals(code, ignoreCase = true)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.15f) else CreamSurface)
            .border(
                1.2.dp,
                if (isSelected) color else CreamBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick(code) }
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                color = if (isSelected) color else TextDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = desc,
                color = TextDarkMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun MetabolicStatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CreamSurface)
            .border(1.dp, CreamBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = label, color = TextDarkMuted, fontSize = 10.sp)
            Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
