package cl.ipp.recordatorios

data class Reminder(
    val id: Int,
    val title: String,
    val category: Category,
    val priority: Priority,
    val status: Status,
    val note: String? = null
) {
    init {
        require(title.trim().length >= 4)
        require((note?.length ?: 0) <= 200)
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
