package cl.ipp.recordatorios.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.ipp.recordatorios.RecordatoriosApplication
import cl.ipp.recordatorios.data.ReminderRepository
import cl.ipp.recordatorios.model.Reminder
import cl.ipp.recordatorios.model.Status
import cl.ipp.recordatorios.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ReminderDetailUiState {
    data object Loading : ReminderDetailUiState
    data object NotFound : ReminderDetailUiState
    data class Found(val reminder: Reminder) : ReminderDetailUiState
}

class ReminderDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ReminderRepository
) : ViewModel() {

    private val reminderId: String = checkNotNull(savedStateHandle.get<String>(Routes.ARG_ID))

    val uiState: StateFlow<ReminderDetailUiState> = repository.observeReminder(reminderId)
        .map { reminder -> if (reminder != null) ReminderDetailUiState.Found(reminder) else ReminderDetailUiState.NotFound }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderDetailUiState.Loading)

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun toggleDone(reminder: Reminder) {
        val next = if (reminder.status == Status.Done) Status.Pending else Status.Done
        viewModelScope.launch { repository.setStatus(reminder.id, next) }
    }

    fun delete() {
        viewModelScope.launch {
            repository.delete(reminderId)
            _deleted.value = true
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RecordatoriosApplication
                ReminderDetailViewModel(createSavedStateHandle(), app.container.reminderRepository)
            }
        }
    }
}
