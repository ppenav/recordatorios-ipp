package cl.ipp.recordatorios.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipp.recordatorios.data.categoryColors
import cl.ipp.recordatorios.model.Reminder
import cl.ipp.recordatorios.ui.components.CategoryDot
import cl.ipp.recordatorios.ui.components.StatusMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    viewModel: ReminderDetailViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                modifier = contentModifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun ReminderDetail(reminder: Reminder, modifier: Modifier = Modifier) {
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
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(label = "Identificador", value = "#${reminder.id}")
                DetailRow(label = "Categoría", value = reminder.category.label)
                DetailRow(label = "Prioridad", value = reminder.priority.label)
                DetailRow(label = "Estado", value = reminder.status.label)
                DetailRow(label = "Descripción", value = reminder.description ?: "Sin descripción")
            }
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
