package cl.ipp.recordatorios.data

import cl.ipp.recordatorios.model.Category
import cl.ipp.recordatorios.model.Priority
import cl.ipp.recordatorios.model.Reminder
import cl.ipp.recordatorios.model.Status
import kotlinx.coroutines.delay
import java.io.IOException

class InMemoryReminderRepository : ReminderRepository, ScenarioSimulator {

    override var scenario: Scenario = Scenario.Data

    private val reminders = sampleReminders.toMutableList()

    override suspend fun getReminders(): List<Reminder> {
        delay(800)
        return when (scenario) {
            Scenario.Data -> reminders.toList()
            Scenario.Empty -> emptyList()
            Scenario.Error -> throw IOException("Fuente de datos no disponible")
        }
    }

    override suspend fun getReminder(id: Int): Reminder? {
        delay(300)
        return reminders.find { it.id == id }
    }

    override suspend fun saveReminder(
        title: String,
        description: String?,
        category: Category,
        priority: Priority
    ): Reminder {
        delay(1200)
        if (scenario == Scenario.Error) throw IOException("No se pudo guardar")
        val reminder = Reminder(
            id = (reminders.maxOfOrNull { it.id } ?: 0) + 1,
            title = title,
            category = category,
            priority = priority,
            status = Status.Pending,
            description = description
        )
        reminders.add(reminder)
        return reminder
    }
}
