package cl.ipp.recordatorios.model

const val TITLE_MIN_LENGTH = 4
const val DESCRIPTION_MAX_LENGTH = 200

fun validateTitle(title: String): String? =
    if (title.trim().length < TITLE_MIN_LENGTH) "El título debe tener al menos $TITLE_MIN_LENGTH caracteres" else null

fun validateDescription(description: String): String? =
    if (description.length > DESCRIPTION_MAX_LENGTH) "La descripción no puede superar los $DESCRIPTION_MAX_LENGTH caracteres" else null

fun validateCategory(category: Category?): String? =
    if (category == null || category !in categories) "Selecciona una categoría válida" else null

fun validatePriority(priority: Priority?): String? =
    if (priority == null) "Selecciona una prioridad" else null
