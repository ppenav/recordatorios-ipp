package cl.ipp.recordatorios.model

data class Reminder(
    val id: String,
    val title: String,
    val category: Category,
    val priority: Priority,
    val status: Status,
    val description: String? = null,
    val createdAt: Long = 0L,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
) {
    init {
        require(validateTitle(title) == null)
        require(validateDescription(description.orEmpty()) == null)
    }
}

data class ReminderDraft(
    val title: String,
    val description: String?,
    val category: Category,
    val priority: Priority
)

sealed class Category(val code: String, val label: String) {
    object Work : Category("WORK", "Trabajo")
    object Personal : Category("PERSONAL", "Personal")
    object Health : Category("HEALTH", "Salud")
    object Study : Category("STUDY", "Estudio")
}

sealed class Priority(val code: String, val label: String, val weight: Int) {
    object High : Priority("HIGH", "Alta", 3)
    object Medium : Priority("MEDIUM", "Media", 2)
    object Low : Priority("LOW", "Baja", 1)
}

sealed class Status(val code: String, val label: String) {
    object Pending : Status("PENDING", "Pendiente")
    object Done : Status("DONE", "Completado")
    object Overdue : Status("OVERDUE", "Atrasado")
}

enum class SyncStatus(val label: String) {
    SYNCED("Sincronizado"),
    PENDING("Pendiente de sincronizar"),
    FAILED("Error al sincronizar")
}

val categories: List<Category> = listOf(Category.Work, Category.Personal, Category.Health, Category.Study)

val priorities: List<Priority> = listOf(Priority.High, Priority.Medium, Priority.Low)

val statuses: List<Status> = listOf(Status.Pending, Status.Done, Status.Overdue)

fun categoryOf(code: String): Category? = categories.find { it.code == code }

fun priorityOf(code: String): Priority? = priorities.find { it.code == code }

fun statusOf(code: String): Status? = statuses.find { it.code == code }
