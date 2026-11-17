package cl.ipp.recordatorios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipp.recordatorios.data.preferences.UserPreferences
import cl.ipp.recordatorios.navigation.RecordatoriosNavHost
import cl.ipp.recordatorios.ui.theme.RecordatoriosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferencesRepository = (application as RecordatoriosApplication).container.preferencesRepository
        setContent {
            val preferences by preferencesRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
            RecordatoriosTheme(themeMode = preferences.themeMode) {
                RecordatoriosNavHost()
            }
        }
    }
}
