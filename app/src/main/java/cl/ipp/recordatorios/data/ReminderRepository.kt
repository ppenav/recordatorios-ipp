package cl.ipp.recordatorios.data

import cl.ipp.recordatorios.model.Reminder
import cl.ipp.recordatorios.model.ReminderDraft
import cl.ipp.recordatorios.model.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Synced(val at: Long) : SyncState
    data class Failed(val message: String) : SyncState
}

interface ReminderRepository {
    val syncState: StateFlow<SyncState>
    fun observeReminders(): Flow<List<Reminder>>
    fun observeReminder(id: String): Flow<Reminder?>
    suspend fun create(draft: ReminderDraft): String
    suspend fun update(id: String, draft: ReminderDraft)
    suspend fun setStatus(id: String, status: Status)
    suspend fun delete(id: String)
    fun requestSync()
}
