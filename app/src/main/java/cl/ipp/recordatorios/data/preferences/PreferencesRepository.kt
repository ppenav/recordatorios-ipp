package cl.ipp.recordatorios.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode(val label: String) {
    SYSTEM("Según el sistema"),
    LIGHT("Claro"),
    DARK("Oscuro")
}

enum class SortOrder(val label: String) {
    NEWEST("Más recientes primero"),
    PRIORITY("Prioridad"),
    TITLE("Título")
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showCompleted: Boolean = true,
    val sortOrder: SortOrder = SortOrder.NEWEST
)

val Context.userPreferencesStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

object PreferenceKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val SHOW_COMPLETED = booleanPreferencesKey("show_completed")
    val SORT_ORDER = stringPreferencesKey("sort_order")
}

fun Preferences.toUserPreferences(): UserPreferences = UserPreferences(
    themeMode = this[PreferenceKeys.THEME_MODE]?.let { value -> ThemeMode.entries.find { it.name == value } } ?: ThemeMode.SYSTEM,
    showCompleted = this[PreferenceKeys.SHOW_COMPLETED] ?: true,
    sortOrder = this[PreferenceKeys.SORT_ORDER]?.let { value -> SortOrder.entries.find { it.name == value } } ?: SortOrder.NEWEST
)

class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toUserPreferences() }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[PreferenceKeys.THEME_MODE] = mode.name }
    }

    suspend fun setShowCompleted(show: Boolean) {
        dataStore.edit { it[PreferenceKeys.SHOW_COMPLETED] = show }
    }

    suspend fun setSortOrder(order: SortOrder) {
        dataStore.edit { it[PreferenceKeys.SORT_ORDER] = order.name }
    }
}
