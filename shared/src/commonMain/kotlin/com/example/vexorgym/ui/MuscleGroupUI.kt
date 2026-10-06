package com.example.vexorgym.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

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

object MuscleIcons {
    private val BodyBaseColor = Color(0xFFE2E8F0) // Color base del torso

    val Chest = ImageVector.Builder(
        name = "Chest",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Torso
        path(fill = SolidColor(BodyBaseColor)) {
            moveTo(8f, 2f); lineTo(16f, 2f); lineTo(22f, 7f); lineTo(20f, 15f)
            lineTo(18f, 22f); lineTo(6f, 22f); lineTo(4f, 15f); lineTo(2f, 7f); close()
        }
        // Pecho (azul)
        path(fill = SolidColor(Color(0xFF3B82F6))) {
            moveTo(11.5f, 6f); curveTo(8f, 6f, 5f, 7f, 4.5f, 10f)
            curveTo(4f, 13f, 8f, 14f, 11.5f, 13.5f); close()
            moveTo(12.5f, 6f); curveTo(16f, 6f, 19f, 7f, 19.5f, 10f)
            curveTo(20f, 13f, 16f, 14f, 12.5f, 13.5f); close()
        }
    }.build()

    val Back = ImageVector.Builder(
        name = "Back",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Torso
        path(fill = SolidColor(BodyBaseColor)) {
            moveTo(8f, 2f); lineTo(16f, 2f); lineTo(22f, 7f); lineTo(20f, 15f)
            lineTo(18f, 22f); lineTo(6f, 22f); lineTo(4f, 15f); lineTo(2f, 7f); close()
        }
        // Lats (púrpura)
        path(fill = SolidColor(Color(0xFFA855F7))) {
            moveTo(11.5f, 4f); lineTo(4f, 8f); lineTo(6f, 16f); lineTo(11.5f, 20f); close()
            moveTo(12.5f, 4f); lineTo(20f, 8f); lineTo(18f, 16f); lineTo(12.5f, 20f); close()
        }
    }.build()

    val Legs = ImageVector.Builder(
        name = "Legs",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Piernas base
        path(fill = SolidColor(BodyBaseColor)) {
            moveTo(8f, 2f); lineTo(16f, 2f); lineTo(14f, 12f); lineTo(15f, 22f)
            lineTo(9f, 22f); lineTo(10f, 12f); close()
        }
        // Cuádriceps (verde)
        path(fill = SolidColor(Color(0xFF22C55E))) {
            moveTo(8.5f, 2f); lineTo(15.5f, 2f); curveTo(16f, 6f, 15f, 10f, 14f, 12f)
            lineTo(10f, 12f); curveTo(9f, 10f, 8f, 6f, 8.5f, 2f); close()
        }
    }.build()

    val Shoulders = ImageVector.Builder(
        name = "Shoulders",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Torso
        path(fill = SolidColor(BodyBaseColor)) {
            moveTo(8f, 2f); lineTo(16f, 2f); lineTo(22f, 7f); lineTo(20f, 15f)
            lineTo(18f, 22f); lineTo(6f, 22f); lineTo(4f, 15f); lineTo(2f, 7f); close()
        }
        // Deltoides (índigo)
        path(fill = SolidColor(Color(0xFF4F46E5))) {
            moveTo(2f, 7f); curveTo(2f, 4f, 5f, 3f, 8f, 2f); lineTo(7f, 8f); lineTo(4f, 10f); close()
            moveTo(22f, 7f); curveTo(22f, 4f, 19f, 3f, 16f, 2f); lineTo(17f, 8f); lineTo(20f, 10f); close()
        }
    }.build()

    val Biceps = ImageVector.Builder(
        name = "Biceps",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Brazo base
        path(fill = SolidColor(BodyBaseColor)) {
            moveTo(4f, 20f); lineTo(10f, 22f); lineTo(16f, 12f); lineTo(22f, 10f)
            lineTo(20f, 4f); lineTo(12f, 6f); lineTo(6f, 16f); close()
        }
        // Bíceps (celeste)
        path(fill = SolidColor(Color(0xFF0EA5E9))) {
            moveTo(12f, 6f); curveTo(16f, 2f, 20f, 4f, 16f, 12f); lineTo(12f, 8f); close()
        }
    }.build()

    val Triceps = ImageVector.Builder(
        name = "Triceps",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Brazo base
        path(fill = SolidColor(BodyBaseColor)) {
            moveTo(4f, 20f); lineTo(10f, 22f); lineTo(16f, 12f); lineTo(22f, 10f)
            lineTo(20f, 4f); lineTo(12f, 6f); lineTo(6f, 16f); close()
        }
        // Tríceps (rosa)
        path(fill = SolidColor(Color(0xFFEC4899))) {
            moveTo(6f, 16f); curveTo(2f, 10f, 8f, 6f, 12f, 6f); lineTo(10f, 14f); close()
        }
    }.build()

    val Calves = ImageVector.Builder(
        name = "Calves",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Pantorrillas base
        path(fill = SolidColor(BodyBaseColor)) {
            moveTo(6f, 2f); lineTo(10f, 2f); lineTo(10f, 22f); lineTo(6f, 22f); close()
            moveTo(14f, 2f); lineTo(18f, 2f); lineTo(18f, 22f); lineTo(14f, 22f); close()
        }
        // Músculo pantorrilla (amarillo)
        path(fill = SolidColor(Color(0xFFEAB308))) {
            moveTo(6f, 4f); curveTo(2f, 8f, 2f, 14f, 6f, 16f); lineTo(10f, 16f)
            curveTo(12f, 14f, 12f, 8f, 10f, 4f); close()
            moveTo(14f, 4f); curveTo(12f, 8f, 12f, 14f, 14f, 16f); lineTo(18f, 16f)
            curveTo(22f, 14f, 22f, 8f, 18f, 4f); close()
        }
    }.build()

    val Abs = ImageVector.Builder(
        name = "Abs",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Torso
        path(fill = SolidColor(BodyBaseColor)) {
            moveTo(8f, 2f); lineTo(16f, 2f); lineTo(22f, 7f); lineTo(20f, 15f)
            lineTo(18f, 22f); lineTo(6f, 22f); lineTo(4f, 15f); lineTo(2f, 7f); close()
        }
        // Abdominales (esmeralda)
        path(fill = SolidColor(Color(0xFF10B981))) {
            moveTo(8f, 10f); lineTo(11f, 10f); lineTo(11f, 13f); lineTo(8f, 13f); close()
            moveTo(13f, 10f); lineTo(16f, 10f); lineTo(16f, 13f); lineTo(13f, 13f); close()
            moveTo(8f, 14f); lineTo(11f, 14f); lineTo(11f, 17f); lineTo(8f, 17f); close()
            moveTo(13f, 14f); lineTo(16f, 14f); lineTo(16f, 17f); lineTo(13f, 17f); close()
            moveTo(8f, 18f); lineTo(11f, 18f); lineTo(11f, 21f); lineTo(8f, 21f); close()
            moveTo(13f, 18f); lineTo(16f, 18f); lineTo(16f, 21f); lineTo(13f, 21f); close()
        }
    }.build()
}

@Composable
fun MuscleGroupImage(muscleGroup: String, modifier: Modifier = Modifier, iconSize: Int = 32) {
    val (bgColor, _) = getMuscleGroupColors(muscleGroup)
    Box(
        modifier = modifier.background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val icon = when (muscleGroup.lowercase()) {
            "pecho" -> MuscleIcons.Chest
            "espalda" -> MuscleIcons.Back
            "hombros" -> MuscleIcons.Shoulders
            "pierna", "cuádriceps", "femorales" -> MuscleIcons.Legs
            "bíceps" -> MuscleIcons.Biceps
            "tríceps" -> MuscleIcons.Triceps
            "pantorrillas" -> MuscleIcons.Calves
            "abdominales" -> MuscleIcons.Abs
            "glúteos" -> MuscleIcons.Legs
            else -> MuscleIcons.Chest
        }
        Icon(
            imageVector = icon,
            contentDescription = muscleGroup,
            tint = Color.Unspecified, // <-- CRÍTICO: Permite que el ImageVector muestre sus propios colores (silueta gris + músculo de color)
            modifier = Modifier.size(iconSize.dp)
        )
    }
}