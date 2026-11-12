package cl.ipp.recordatorios

import android.app.Application
import cl.ipp.recordatorios.data.InMemoryReminderRepository

class RecordatoriosApplication : Application() {
    val repository = InMemoryReminderRepository()
}
