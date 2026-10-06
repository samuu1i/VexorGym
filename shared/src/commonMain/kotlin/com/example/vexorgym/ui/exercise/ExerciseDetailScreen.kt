package com.example.vexorgym.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vexorgym.data.model.WorkoutSession
import com.example.vexorgym.data.model.WorkoutSet
import com.example.vexorgym.di.AppContainer
import com.example.vexorgym.ui.MuscleGroupImage
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ExerciseDetailRoute(
    exerciseId: String,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = viewModel(key = exerciseId) {
        ExerciseDetailViewModel(exerciseId, AppContainer.gymRepository)
    },
) {
    val uiState by viewModel.uiState.collectAsState()

    ExerciseDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onWeightChange = viewModel::onWeightChange,
        onRepsChange = viewModel::onRepsChange,
        onAddSet = viewModel::addSet,
        onDeleteSet = viewModel::deleteSet,
        onAddSession = viewModel::addSession,
        onDeleteSession = viewModel::deleteSession,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    uiState: ExerciseDetailUiState,
    onBack: () -> Unit,
    onWeightChange: (sessionId: String, value: String) -> Unit,
    onRepsChange: (sessionId: String, value: String) -> Unit,
    onAddSet: (sessionId: String) -> Unit,
    onDeleteSet: (sessionId: String, setId: String) -> Unit,
    onAddSession: () -> Unit,
    onDeleteSession: (sessionId: String) -> Unit,
) {
    var sessionPendingDelete by remember { mutableStateOf<WorkoutSession?>(null) }
    var setPendingDelete by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        containerColor = Color(0xFFF7F7FA), // Color de fondo pastel según referencia
        topBar = {
            TopAppBar(
                title = { Text(uiState.exerciseName.ifBlank { "Ejercicio" }, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color(0xFF1C1B1F))
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menú de opciones general si aplica */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = Color(0xFF1C1B1F))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.notFound -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Ejercicio no encontrado")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onBack) { Text("Volver") }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    // INFO HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MuscleGroupImage(
                            muscleGroup = uiState.muscleGroup,
                            modifier = Modifier.size(80.dp),
                            iconSize = 40
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEFE8FF), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = uiState.muscleGroup,
                                    color = Color(0xFF6B58C1),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = Color(0xFF6B58C1), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                val lastSetText = uiState.lastSet?.let { formatSet(it) } ?: "Ninguna"
                                Text(
                                    text = "Última serie: $lastSetText",
                                    color = Color(0xFF49454F),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (uiState.sessions.isEmpty()) {
                            item {
                                Text(
                                    "Todavía no hay sesiones. Creá una para empezar a anotar.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            itemsIndexed(
                                uiState.sessions,
                                key = { _, session -> session.id },
                            ) { index, session ->
                                SessionCard(
                                    sessionNumber = index + 1,
                                    session = session,
                                    draft = uiState.draftFor(session.id),
                                    onWeightChange = { onWeightChange(session.id, it) },
                                    onRepsChange = { onRepsChange(session.id, it) },
                                    onAddSet = { onAddSet(session.id) },
                                    onDeleteSet = { setId -> setPendingDelete = session.id to setId },
                                    onDeleteSession = { sessionPendingDelete = session },
                                )
                            }
                        }
                    }

                    uiState.actionError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = onAddSession,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B58C1))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Crear nueva sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }

    sessionPendingDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionPendingDelete = null },
            title = { Text("Eliminar sesión") },
            text = { Text("Se va a borrar esta sesión y todas sus series. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteSession(session.id)
                    sessionPendingDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { sessionPendingDelete = null }) { Text("Cancelar") }
            }
        )
    }

    setPendingDelete?.let { (sessionId, setId) ->
        AlertDialog(
            onDismissRequest = { setPendingDelete = null },
            title = { Text("Eliminar serie") },
            text = { Text("¿Estás seguro de que querés eliminar esta serie?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteSet(sessionId, setId)
                    setPendingDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { setPendingDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SessionCard(
    sessionNumber: Int,
    session: WorkoutSession,
    draft: SessionSetDraft,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (setId: String) -> Unit,
    onDeleteSession: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // HEADER DE SESIÓN
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(Color(0xFFEFE8FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color(0xFF6B58C1))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sesión $sessionNumber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Text(
                        text = formatSessionDate(session.createdAtMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF79747E),
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDeleteSession, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Opciones",
                        tint = Color(0xFF49454F),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // LISTA DE SERIES
            if (session.sets.isEmpty()) {
                Text(
                    "Sin series todavía.",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                session.sets.forEachIndexed { index, set ->
                    val dismissSetState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                onDeleteSet(set.id)
                                return@rememberSwipeToDismissBoxState false
                            }
                            false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissSetState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = if (dismissSetState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (dismissSetState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    ) {
                        // SET ROW
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).background(Color(0xFFEFE8FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${index + 1}", color = Color(0xFF6B58C1), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "Serie ${index + 1}",
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF49454F),
                                fontSize = 16.sp
                            )
                            Text(
                                formatSet(set),
                                modifier = Modifier.weight(1.5f),
                                color = Color(0xFF1C1B1F),
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF49454F))
                        }
                    }
                    if (index < session.sets.size - 1) {
                        HorizontalDivider(color = Color(0xFFF3F3F3), modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // FORMULARIO DE NUEVA SERIE (Oculto por defecto)
            var isAddingSet by remember { mutableStateOf(false) }

            if (!isAddingSet) {
                TextButton(
                    onClick = { isAddingSet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFF6B58C1))
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Agregar serie", color = Color(0xFF6B58C1), fontWeight = FontWeight.SemiBold)
                }
            } else {
                HorizontalDivider(color = Color(0xFFF3F3F3))
                Spacer(Modifier.height(12.dp))
                Text(
                    "Nueva serie",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = draft.weightInput,
                        onValueChange = onWeightChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Peso") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = draft.repsInput,
                        onValueChange = onRepsChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Reps") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                draft.error?.let { error ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        error,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onWeightChange("")
                            onRepsChange("")
                            isAddingSet = false
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            onAddSet()
                            isAddingSet = false
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B58C1))
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

private fun formatSet(set: WorkoutSet): String {
    val weight = if (set.weightKg % 1.0 == 0.0) {
        set.weightKg.toInt().toString()
    } else {
        set.weightKg.toString()
    }
    return "$weight kg × ${set.repetitions} reps"
}

private fun formatSessionDate(createdAtMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(createdAtMillis)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val datePart = localDateTime.date.toString()
    val timePart = localDateTime.time.toString().take(5)
    return "$datePart · $timePart"
}
