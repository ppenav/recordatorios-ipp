package cl.ipp.recordatorios.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipp.recordatorios.data.Scenario
import cl.ipp.recordatorios.ui.components.ReminderCard
import cl.ipp.recordatorios.ui.components.StatusMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    viewModel: ReminderListViewModel,
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis recordatorios") },
                actions = {
                    TextButton(onClick = { menuExpanded = true }) {
                        Text("Simular")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        Scenario.entries.forEach { scenario ->
                            DropdownMenuItem(
                                text = { Text(scenario.label) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.simulate(scenario)
                                }
                            )
                        }
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
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
                icon = Icons.Filled.Warning,
                actionLabel = "Reintentar",
                onAction = viewModel::load
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
