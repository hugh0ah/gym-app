package com.fittracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittracker.app.data.remote.gemini.RoutineChangeProposal
import com.fittracker.app.ui.theme.*

@Composable
fun ConfirmProposalCard(
    proposal: RoutineChangeProposal,
    onAccept: (RoutineChangeProposal) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = when (proposal.dayOfWeek) {
        1 -> "Lunes"
        2 -> "Martes"
        3 -> "Miércoles"
        4 -> "Jueves"
        5 -> "Viernes"
        6 -> "Sábado"
        7 -> "Domingo"
        else -> "Día ${proposal.dayOfWeek}"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CreamSurface)
            .border(1.dp, CreamBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AccentCoral,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "PROPUESTA DE MODIFICACIÓN",
                    color = AccentCoral,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = proposal.exerciseName,
                color = TextDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProposalDetailItem(label = "Día", value = dayName)
                ProposalDetailItem(label = "Series", value = "${proposal.proposedSets}")
                ProposalDetailItem(label = "Reps", value = "${proposal.proposedReps}")
                ProposalDetailItem(label = "Peso", value = "${proposal.proposedWeight} kg")
            }

            HorizontalDivider(color = CreamBorder)

            Text(
                text = "Justificación del Asistente:",
                color = TextDarkMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = proposal.justification,
                color = TextDark,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextDarkMuted
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Descartar", fontSize = 13.sp)
                }

                Button(
                    onClick = { onAccept(proposal) },
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCoral,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aceptar y Aplicar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ProposalDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextDarkMuted, fontSize = 11.sp)
        Text(text = value, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
