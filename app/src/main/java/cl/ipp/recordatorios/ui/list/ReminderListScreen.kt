package cl.ipp.recordatorios.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipp.recordatorios.data.SyncState
import cl.ipp.recordatorios.ui.components.ReminderCard
import cl.ipp.recordatorios.ui.components.StatusMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    viewModel: ReminderListViewModel,
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (String) -> Unit,
    onAjustes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis recordatorios") },
                actions = {
                    IconButton(onClick = viewModel::retrySync) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Sincronizar")
                    }
                    IconButton(onClick = onAjustes) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearRecordatorio) {
                Icon(Icons.Filled.Add, contentDescription = "Crear recordatorio")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SyncBanner(syncState = syncState, onRetry = viewModel::retrySync)
            val contentModifier = Modifier.fillMaxSize()
            when (val state = uiState) {
                ReminderListUiState.Loading -> StatusMessage(
                    message = "Cargando recordatorios...",
                    modifier = contentModifier,
                    loading = true
                )
                ReminderListUiState.Empty -> StatusMessage(
                    message = "Todavía no existen recordatorios.",
                    modifier = contentModifier,
                    icon = Icons.Filled.Info,
                    actionLabel = "Crear recordatorio",
                    onAction = onCrearRecordatorio
                )
                is ReminderListUiState.Error -> StatusMessage(
                    message = state.message,
                    modifier = contentModifier,
                    icon = Icons.Filled.Warning
                )
                is ReminderListUiState.Success -> LazyColumn(
                    modifier = contentModifier,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.reminders, key = { it.id }) { reminder ->
                        ReminderCard(reminder = reminder, onClick = { onRecordatorioClick(reminder.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncBanner(syncState: SyncState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    when (syncState) {
        SyncState.Syncing -> LinearProgressIndicator(modifier = modifier.fillMaxWidth())
        is SyncState.Failed -> Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Text(
                    text = "${syncState.message}. Los cambios quedan guardados en el teléfono.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
                TextButton(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        }
        else -> Unit
    }
}
