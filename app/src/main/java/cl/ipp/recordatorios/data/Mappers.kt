package cl.ipp.recordatorios.data

import cl.ipp.recordatorios.data.local.PendingAction
import cl.ipp.recordatorios.data.local.ReminderEntity
import cl.ipp.recordatorios.data.remote.ReminderDto
import cl.ipp.recordatorios.model.Category
import cl.ipp.recordatorios.model.Priority
import cl.ipp.recordatorios.model.Reminder
import cl.ipp.recordatorios.model.Status
import cl.ipp.recordatorios.model.SyncStatus
import cl.ipp.recordatorios.model.categoryOf
import cl.ipp.recordatorios.model.priorityOf
import cl.ipp.recordatorios.model.statusOf
import cl.ipp.recordatorios.model.validateDescription
import cl.ipp.recordatorios.model.validateTitle

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    title = title,
    category = categoryOf(category) ?: Category.Personal,
    priority = priorityOf(priority) ?: Priority.Medium,
    status = statusOf(status) ?: Status.Pending,
    description = description,
    createdAt = createdAt,
    syncStatus = SyncStatus.entries.find { it.name == syncStatus } ?: SyncStatus.PENDING
)

fun ReminderEntity.toDto(): ReminderDto = ReminderDto(
    id = id,
    title = title,
    description = description,
    category = category,
    priority = priority,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ReminderDto.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    title = title.trim(),
    description = description?.trim()?.ifEmpty { null },
    category = category,
    priority = priority,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.SYNCED.name,
    pendingAction = PendingAction.NONE.name,
    isDeleted = false,
    lastError = null
)

fun ReminderDto.isValid(): Boolean =
    validateTitle(title) == null &&
        validateDescription(description.orEmpty()) == null &&
        categoryOf(category) != null &&
        priorityOf(priority) != null &&
        statusOf(status) != null
