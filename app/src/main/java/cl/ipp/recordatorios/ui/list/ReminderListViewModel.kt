package cl.ipp.recordatorios.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.ipp.recordatorios.RecordatoriosApplication
import cl.ipp.recordatorios.data.ReminderRepository
import cl.ipp.recordatorios.data.SyncState
import cl.ipp.recordatorios.model.Reminder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface ReminderListUiState {
    data object Loading : ReminderListUiState
    data object Empty : ReminderListUiState
    data class Success(val reminders: List<Reminder>) : ReminderListUiState
    data class Error(val message: String) : ReminderListUiState
}

class ReminderListViewModel(private val repository: ReminderRepository) : ViewModel() {

    val uiState: StateFlow<ReminderListUiState> = repository.observeReminders()
        .map<List<Reminder>, ReminderListUiState> { reminders ->
            if (reminders.isEmpty()) ReminderListUiState.Empty else ReminderListUiState.Success(reminders)
        }
        .catch { emit(ReminderListUiState.Error("No se pudieron leer los recordatorios guardados en el teléfono.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderListUiState.Loading)

    val syncState: StateFlow<SyncState> = repository.syncState

    fun retrySync() {
        repository.requestSync()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RecordatoriosApplication
                ReminderListViewModel(app.container.reminderRepository)
            }
        }
    }
}
