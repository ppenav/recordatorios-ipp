package cl.ipp.recordatorios

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import cl.ipp.recordatorios.data.preferences.PreferenceKeys
import cl.ipp.recordatorios.data.preferences.SortOrder
import cl.ipp.recordatorios.data.preferences.ThemeMode
import cl.ipp.recordatorios.data.preferences.UserPreferences
import cl.ipp.recordatorios.data.preferences.toUserPreferences
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesMappingTest {

    @Test
    fun sinDatosGuardadosUsaValoresPredeterminados() {
        assertEquals(UserPreferences(), emptyPreferences().toUserPreferences())
    }

    @Test
    fun conservaLosValoresGuardados() {
        val stored = mutablePreferencesOf(
            PreferenceKeys.THEME_MODE to "DARK",
            PreferenceKeys.SHOW_COMPLETED to false,
            PreferenceKeys.SORT_ORDER to "PRIORITY"
        )
        assertEquals(UserPreferences(ThemeMode.DARK, false, SortOrder.PRIORITY), stored.toUserPreferences())
    }

    @Test
    fun valorDesconocidoVuelveAlPredeterminado() {
        val stored = mutablePreferencesOf(
            PreferenceKeys.THEME_MODE to "MORADO",
            PreferenceKeys.SORT_ORDER to "ALEATORIO"
        )
        val result = stored.toUserPreferences()
        assertEquals(ThemeMode.SYSTEM, result.themeMode)
        assertEquals(SortOrder.NEWEST, result.sortOrder)
    }
}
