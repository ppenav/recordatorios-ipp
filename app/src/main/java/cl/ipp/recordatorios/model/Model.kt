package cl.ipp.recordatorios.model

data class Reminder(
    val id: Int,
    val title: String,
    val category: Category,
    val priority: Priority,
    val status: Status,
    val description: String? = null
) {
    init {
        require(validateTitle(title) == null)
        require(validateDescription(description.orEmpty()) == null)
    }
}

sealed class Category(val label: String) {
    object Work : Category("Trabajo")
    object Personal : Category("Personal")
    object Health : Category("Salud")
    object Study : Category("Estudio")
}

sealed class Priority(val label: String, val weight: Int) {
    object High : Priority("Alta", 3)
    object Medium : Priority("Media", 2)
    object Low : Priority("Baja", 1)
}

sealed class Status(val label: String) {
    object Pending : Status("Pendiente")
    object Done : Status("Completado")
    object Overdue : Status("Atrasado")
}

val categories: List<Category> = listOf(Category.Work, Category.Personal, Category.Health, Category.Study)

val priorities: List<Priority> = listOf(Priority.High, Priority.Medium, Priority.Low)
