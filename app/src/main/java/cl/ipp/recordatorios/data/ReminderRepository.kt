package cl.ipp.recordatorios.data

import cl.ipp.recordatorios.model.Category
import cl.ipp.recordatorios.model.Priority
import cl.ipp.recordatorios.model.Reminder

interface ReminderRepository {
    suspend fun getReminders(): List<Reminder>
    suspend fun getReminder(id: Int): Reminder?
    suspend fun saveReminder(title: String, description: String?, category: Category, priority: Priority): Reminder
}

enum class Scenario(val label: String) {
    Data("Con datos"),
    Empty("Vacío"),
    Error("Error")
}

interface ScenarioSimulator {
    var scenario: Scenario
}
