package com.example.vexorgym.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import vexorgym.shared.generated.resources.Res
import vexorgym.shared.generated.resources.*

fun getMuscleGroupColors(group: String): Pair<Color, Color> {
    return when (group.lowercase()) {
        "pecho" -> Color(0xFFEAF2FF) to Color(0xFF3B82F6) // Light blue to bright blue
        "espalda" -> Color(0xFFF3E8FF) to Color(0xFFA855F7) // Light purple to purple
        "hombros" -> Color(0xFFE0E7FF) to Color(0xFF4F46E5) // Indigo tint
        "bíceps" -> Color(0xFFE0F2FE) to Color(0xFF0EA5E9) // Sky blue
        "tríceps" -> Color(0xFFFCE7F3) to Color(0xFFEC4899) // Pink
        "pierna", "cuádriceps", "femorales", "femoral" -> Color(0xFFDCFCE7) to Color(0xFF22C55E) // Green
        "glúteos", "glúteo" -> Color(0xFFFFEDD5) to Color(0xFFF97316) // Orange
        "pantorrillas", "gemelos" -> Color(0xFFFEF9C3) to Color(0xFFEAB308) // Yellow
        "abdominales", "abdomen" -> Color(0xFFD1FAE5) to Color(0xFF10B981) // Emerald
        else -> Color(0xFFF1F5F9) to Color(0xFF64748B) // Slate
    }
}

@Composable
fun MuscleGroupImage(muscleGroup: String, modifier: Modifier = Modifier, iconSize: Int = 32) {
    val (bgColor, _) = getMuscleGroupColors(muscleGroup)
    Box(
        modifier = modifier.background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val painter = when (muscleGroup.lowercase()) {
            "pecho" -> painterResource(Res.drawable.ic_pecho)
            "espalda" -> painterResource(Res.drawable.ic_espalda)
            "hombros" -> painterResource(Res.drawable.ic_hombro)
            "pierna", "cuádriceps" -> painterResource(Res.drawable.ic_pierna)
            "femorales", "femoral" -> painterResource(Res.drawable.ic_femoral)
            "bíceps" -> painterResource(Res.drawable.ic_bicep)
            "tríceps" -> painterResource(Res.drawable.ic_tricep)
            "pantorrillas", "gemelos" -> painterResource(Res.drawable.ic_gemelos)
            "abdominales", "abdomen" -> painterResource(Res.drawable.ic_abdomen)
            "glúteos", "glúteo" -> painterResource(Res.drawable.ic_gluteo)
            else -> painterResource(Res.drawable.ic_pecho)
        }
        Image(
            painter = painter,
            contentDescription = muscleGroup,
            contentScale = ContentScale.Crop, 
            modifier = Modifier.size((iconSize * 2.0).dp) // Aumentamos el tamaño interno visual
        )
    }
}