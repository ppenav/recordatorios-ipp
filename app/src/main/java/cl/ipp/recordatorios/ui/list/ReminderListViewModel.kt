package cl.ipp.recordatorios.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.ipp.recordatorios.RecordatoriosApplication
import cl.ipp.recordatorios.data.ReminderRepository
import cl.ipp.recordatorios.data.Scenario
import cl.ipp.recordatorios.data.ScenarioSimulator
import cl.ipp.recordatorios.model.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface ReminderListUiState {
    data object Loading : ReminderListUiState
    data object Empty : ReminderListUiState
    data class Success(val reminders: List<Reminder>) : ReminderListUiState
    data class Error(val message: String) : ReminderListUiState
}

class ReminderListViewModel(
    private val repository: ReminderRepository,
    private val simulator: ScenarioSimulator
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReminderListUiState>(ReminderListUiState.Loading)
    val uiState: StateFlow<ReminderListUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.update { ReminderListUiState.Loading }
        viewModelScope.launch {
            val result = try {
                val reminders = repository.getReminders()
                if (reminders.isEmpty()) ReminderListUiState.Empty else ReminderListUiState.Success(reminders)
            } catch (e: IOException) {
                ReminderListUiState.Error("No se pudieron cargar los recordatorios. Intenta nuevamente.")
            }
            _uiState.update { result }
        }
    }

    fun simulate(scenario: Scenario) {
        simulator.scenario = scenario
        load()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RecordatoriosApplication
                ReminderListViewModel(app.repository, app.repository)
            }
        }
    }
}
