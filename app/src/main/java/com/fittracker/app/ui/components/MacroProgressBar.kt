package com.fittracker.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittracker.app.ui.theme.CreamSurfaceVariant
import com.fittracker.app.ui.theme.TextDark
import com.fittracker.app.ui.theme.TextDarkMuted

@Composable
fun MacroProgressBar(
    label: String,
    currentG: Double,
    targetG: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (targetG > 0) (currentG / targetG).toFloat().coerceIn(0f, 1f) else 0f
    val percentage = if (targetG > 0) ((currentG / targetG) * 100).toInt() else 0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${currentG.toInt()}g / ${targetG.toInt()}g ($percentage%)",
                color = TextDarkMuted,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = CreamSurfaceVariant
        )
    }
}
