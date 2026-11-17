package cl.ipp.recordatorios.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PendingAction { NONE, CREATE, UPDATE, DELETE }

@Entity(tableName = "reminders", indices = [Index("status"), Index("pendingAction")])
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val category: String,
    val priority: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: String,
    val pendingAction: String,
    val isDeleted: Boolean,
    val lastError: String?
)
