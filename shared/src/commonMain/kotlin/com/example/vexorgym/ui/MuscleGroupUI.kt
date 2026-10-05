package com.example.vexorgym.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun getMuscleGroupColors(group: String): Pair<Color, Color> {
    return when (group.lowercase()) {
        "pecho" -> Color(0xFFEAF2FF) to Color(0xFF3B82F6) // Light blue to bright blue
        "espalda" -> Color(0xFFF3E8FF) to Color(0xFFA855F7) // Light purple to purple
        "hombros" -> Color(0xFFE0E7FF) to Color(0xFF4F46E5) // Indigo tint
        "bíceps" -> Color(0xFFE0F2FE) to Color(0xFF0EA5E9) // Sky blue
        "tríceps" -> Color(0xFFFCE7F3) to Color(0xFFEC4899) // Pink
        "pierna", "cuádriceps", "femorales" -> Color(0xFFDCFCE7) to Color(0xFF22C55E) // Green
        "glúteos" -> Color(0xFFFFEDD5) to Color(0xFFF97316) // Orange
        "pantorrillas" -> Color(0xFFFEF9C3) to Color(0xFFEAB308) // Yellow
        "abdominales" -> Color(0xFFD1FAE5) to Color(0xFF10B981) // Emerald
        else -> Color(0xFFF1F5F9) to Color(0xFF64748B) // Slate
    }
}

@Composable
fun MuscleGroupImage(muscleGroup: String, modifier: Modifier = Modifier, iconSize: Int = 32) {
    val (bgColor, contentColor) = getMuscleGroupColors(muscleGroup)
    Box(
        modifier = modifier.background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val icon = when (muscleGroup.lowercase()) {
            "pecho", "espalda", "hombros" -> Icons.Filled.Accessibility
            "pierna", "cuádriceps", "femorales", "pantorrillas", "glúteos" -> Icons.Filled.DirectionsRun
            "bíceps", "tríceps" -> Icons.Filled.PanTool
            else -> Icons.Filled.FitnessCenter
        }
        Icon(
            imageVector = icon,
            contentDescription = muscleGroup,
            tint = contentColor.copy(alpha = 0.8f),
            modifier = Modifier.size(iconSize.dp)
        )
    }
}
