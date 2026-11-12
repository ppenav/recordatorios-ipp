package cl.ipp.recordatorios.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.ipp.recordatorios.RecordatoriosApplication
import cl.ipp.recordatorios.data.ReminderRepository
import cl.ipp.recordatorios.model.Category
import cl.ipp.recordatorios.model.Priority
import cl.ipp.recordatorios.model.validateCategory
import cl.ipp.recordatorios.model.validateDescription
import cl.ipp.recordatorios.model.validatePriority
import cl.ipp.recordatorios.model.validateTitle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class CreateReminderUiState(
    val title: String = "",
    val description: String = "",
    val category: Category? = null,
    val priority: Priority? = null,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val priorityError: String? = null,
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val saveError: String? = null
)

class CreateReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReminderUiState())
    val uiState: StateFlow<CreateReminderUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) = _uiState.update { it.copy(title = title).validated() }

    fun onDescriptionChange(description: String) = _uiState.update { it.copy(description = description).validated() }

    fun onCategoryChange(category: Category) = _uiState.update { it.copy(category = category).validated() }

    fun onPriorityChange(priority: Priority) = _uiState.update { it.copy(priority = priority).validated() }

    fun save() {
        val state = _uiState.value
        val category = state.category ?: return
        val priority = state.priority ?: return
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true, canSave = false, saveError = null) }
        viewModelScope.launch {
            try {
                repository.saveReminder(
                    title = state.title.trim(),
                    description = state.description.trim().ifEmpty { null },
                    category = category,
                    priority = priority
                )
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isSaving = false, saveError = "No se pudo guardar el recordatorio. Intenta nuevamente.").validated()
                }
            }
        }
    }

    private fun CreateReminderUiState.validated(): CreateReminderUiState {
        val titleError = validateTitle(title)
        val descriptionError = validateDescription(description)
        val categoryError = validateCategory(category)
        val priorityError = validatePriority(priority)
        val isValid = listOf(titleError, descriptionError, categoryError, priorityError).all { it == null }
        return copy(
            titleError = titleError,
            descriptionError = descriptionError,
            categoryError = categoryError,
            priorityError = priorityError,
            canSave = isValid && !isSaving
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RecordatoriosApplication
                CreateReminderViewModel(app.repository)
            }
        }
    }
}
