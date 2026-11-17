package cl.ipp.recordatorios.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.ipp.recordatorios.RecordatoriosApplication
import cl.ipp.recordatorios.data.preferences.PreferencesRepository
import cl.ipp.recordatorios.data.preferences.SortOrder
import cl.ipp.recordatorios.data.preferences.ThemeMode
import cl.ipp.recordatorios.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val preferencesRepository: PreferencesRepository) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun onShowCompletedChange(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowCompleted(show) }
    }

    fun onSortOrderChange(order: SortOrder) {
        viewModelScope.launch { preferencesRepository.setSortOrder(order) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RecordatoriosApplication
                SettingsViewModel(app.container.preferencesRepository)
            }
        }
    }
}
