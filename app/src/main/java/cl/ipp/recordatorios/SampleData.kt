package cl.ipp.recordatorios

import androidx.compose.ui.graphics.Color

val sampleReminders: List<Reminder> = listOf(
    Reminder(1, "Enviar informe mensual", Category.Work, Priority.High, Status.Pending),
    Reminder(2, "Agendar control medico", Category.Health, Priority.Medium, Status.Overdue),
    Reminder(3, "Estudiar modulo 2", Category.Study, Priority.High, Status.Pending, "Repaso API 2"),
    Reminder(4, "Pagar cuentas del mes", Category.Personal, Priority.Low, Status.Done),
    Reminder(5, "Preparar reunion de equipo", Category.Work, Priority.Medium, Status.Pending),
    Reminder(6, "Renovar receta", Category.Health, Priority.Low, Status.Pending)
)

val usedCategories: Set<Category> = sampleReminders.map { it.category }.toSet()

val categoryColors: Map<Category, Color> = mapOf(
    Category.Work to Color(0xFF1565C0),
    Category.Personal to Color(0xFF6A1B9A),
    Category.Health to Color(0xFF2E7D32),
    Category.Study to Color(0xFFEF6C00)
)

fun remindersByStatus(status: Status): List<Reminder> =
    sampleReminders.filter { it.status == status }

fun remindersByPriority(priority: Priority): List<Reminder> =
    sampleReminders.filter { it.priority == priority }
