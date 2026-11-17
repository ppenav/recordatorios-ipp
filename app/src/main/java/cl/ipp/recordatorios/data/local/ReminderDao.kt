package cl.ipp.recordatorios.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE isDeleted = 0 AND (:showCompleted OR status != 'DONE') ORDER BY createdAt DESC")
    fun observeVisible(showCompleted: Boolean): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id AND isDeleted = 0")
    fun observeById(id: String): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: String): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE pendingAction != 'NONE' ORDER BY updatedAt")
    suspend fun getPending(): List<ReminderEntity>

    @Insert
    suspend fun insert(reminder: ReminderEntity)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Upsert
    suspend fun upsert(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE reminders SET syncStatus = 'SYNCED', pendingAction = 'NONE', lastError = NULL WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markSynced(id: String, updatedAt: Long): Int

    @Query("UPDATE reminders SET syncStatus = 'FAILED', lastError = :error WHERE id = :id")
    suspend fun markFailed(id: String, error: String)

    @Query("DELETE FROM reminders WHERE pendingAction = 'NONE' AND id NOT IN (:remoteIds)")
    suspend fun deleteSyncedNotIn(remoteIds: List<String>)
}
