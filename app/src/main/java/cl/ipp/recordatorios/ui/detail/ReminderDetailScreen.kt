package cl.ipp.recordatorios.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipp.recordatorios.model.Reminder
import cl.ipp.recordatorios.model.Status
import cl.ipp.recordatorios.ui.components.CategoryDot
import cl.ipp.recordatorios.ui.components.StatusMessage
import cl.ipp.recordatorios.ui.components.SyncBadge
import cl.ipp.recordatorios.ui.components.categoryColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    viewModel: ReminderDetailViewModel,
    onEditar: (String) -> Unit,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(deleted) {
        if (deleted) onVolver()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del recordatorio") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (val state = uiState) {
            ReminderDetailUiState.Loading -> StatusMessage(
                message = "Cargando recordatorio...",
                modifier = contentModifier,
                loading = true
            )
            ReminderDetailUiState.NotFound -> StatusMessage(
                message = "El recordatorio no existe.",
                modifier = contentModifier,
                actionLabel = "Volver",
                onAction = onVolver
            )
            is ReminderDetailUiState.Found -> ReminderDetail(
                reminder = state.reminder,
                onEditar = { onEditar(state.reminder.id) },
                onToggleDone = { viewModel.toggleDone(state.reminder) },
                onEliminar = { confirmDelete = true },
                modifier = contentModifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminar recordatorio") },
            text = { Text("El recordatorio se elimina del teléfono y del servicio cuando haya conexión.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ReminderDetail(
    reminder: Reminder,
    onEditar: () -> Unit,
    onToggleDone: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryDot(color = categoryColors[reminder.category] ?: Color.Gray)
            Spacer(Modifier.width(12.dp))
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        SyncBadge(syncStatus = reminder.syncStatus)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(label = "Categoría", value = reminder.category.label)
                DetailRow(label = "Prioridad", value = reminder.priority.label)
                DetailRow(label = "Estado", value = reminder.status.label)
                DetailRow(label = "Fecha de creación", value = dateFormat.format(Date(reminder.createdAt)))
                DetailRow(label = "Descripción", value = reminder.description ?: "Sin descripción")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onEditar, modifier = Modifier.weight(1f)) {
                Text("Editar")
            }
            Button(onClick = onToggleDone, modifier = Modifier.weight(1f)) {
                Text(if (reminder.status == Status.Done) "Marcar pendiente" else "Marcar completado")
            }
        }
        TextButton(onClick = onEliminar, modifier = Modifier.fillMaxWidth()) {
            Text("Eliminar recordatorio", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
