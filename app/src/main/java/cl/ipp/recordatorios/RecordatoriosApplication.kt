package cl.ipp.recordatorios

import android.app.Application
import android.content.Context
import cl.ipp.recordatorios.data.OfflineFirstReminderRepository
import cl.ipp.recordatorios.data.ReminderRepository
import cl.ipp.recordatorios.data.local.AppDatabase
import cl.ipp.recordatorios.data.network.ConnectivityObserver
import cl.ipp.recordatorios.data.preferences.PreferencesRepository
import cl.ipp.recordatorios.data.preferences.userPreferencesStore
import cl.ipp.recordatorios.data.remote.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class AppContainer(context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val connectivity = ConnectivityObserver(context)

    val preferencesRepository = PreferencesRepository(context.userPreferencesStore)

    val reminderRepository: ReminderRepository = OfflineFirstReminderRepository(
        dao = AppDatabase.getInstance(context).reminderDao(),
        api = NetworkModule.createApi(BuildConfig.API_BASE_URL),
        preferences = preferencesRepository,
        scope = applicationScope
    )

    fun syncWhenOnline() {
        applicationScope.launch {
            connectivity.isOnline.filter { it }.collect { reminderRepository.requestSync() }
        }
    }
}

class RecordatoriosApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.syncWhenOnline()
    }
}
