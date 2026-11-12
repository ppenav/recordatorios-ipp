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
import cl.ipp.recordatorios.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ReminderDetailUiState {
    data object Loading : ReminderDetailUiState
    data object NotFound : ReminderDetailUiState
    data class Found(val reminder: Reminder) : ReminderDetailUiState
}

class ReminderDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ReminderRepository
) : ViewModel() {

    private val reminderId: Int = checkNotNull(savedStateHandle.get<Int>(Routes.ARG_ID))

    private val _uiState = MutableStateFlow<ReminderDetailUiState>(ReminderDetailUiState.Loading)
    val uiState: StateFlow<ReminderDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val reminder = repository.getReminder(reminderId)
            _uiState.update {
                if (reminder != null) ReminderDetailUiState.Found(reminder) else ReminderDetailUiState.NotFound
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RecordatoriosApplication
                ReminderDetailViewModel(createSavedStateHandle(), app.repository)
            }
        }
    }
}
