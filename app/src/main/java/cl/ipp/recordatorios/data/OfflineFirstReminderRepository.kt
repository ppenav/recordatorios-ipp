package cl.ipp.recordatorios.data

import cl.ipp.recordatorios.data.local.PendingAction
import cl.ipp.recordatorios.data.local.ReminderDao
import cl.ipp.recordatorios.data.local.ReminderEntity
import cl.ipp.recordatorios.data.preferences.PreferencesRepository
import cl.ipp.recordatorios.data.preferences.SortOrder
import cl.ipp.recordatorios.data.remote.ApiError
import cl.ipp.recordatorios.data.remote.ApiResult
import cl.ipp.recordatorios.data.remote.ReminderApi
import cl.ipp.recordatorios.data.remote.safeApiCall
import cl.ipp.recordatorios.model.Reminder
import cl.ipp.recordatorios.model.ReminderDraft
import cl.ipp.recordatorios.model.Status
import cl.ipp.recordatorios.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.UUID

class OfflineFirstReminderRepository(
    private val dao: ReminderDao,
    private val api: ReminderApi,
    private val preferences: PreferencesRepository,
    private val scope: CoroutineScope
) : ReminderRepository {

    private val syncMutex = Mutex()

    @Volatile
    private var syncRequested = false

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeReminders(): Flow<List<Reminder>> =
        preferences.preferences.flatMapLatest { prefs ->
            dao.observeVisible(prefs.showCompleted).map { entities ->
                entities.map { it.toDomain() }.sortedWith(prefs.sortOrder.comparator())
            }
        }

    override fun observeReminder(id: String): Flow<Reminder?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun create(draft: ReminderDraft): String {
        val now = System.currentTimeMillis()
        val entity = ReminderEntity(
            id = UUID.randomUUID().toString(),
            title = draft.title,
            description = draft.description,
            category = draft.category.code,
            priority = draft.priority.code,
            status = Status.Pending.code,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING.name,
            pendingAction = PendingAction.CREATE.name,
            isDeleted = false,
            lastError = null
        )
        dao.insert(entity)
        requestSync()
        return entity.id
    }

    override suspend fun update(id: String, draft: ReminderDraft) = modify(id) {
        it.copy(
            title = draft.title,
            description = draft.description,
            category = draft.category.code,
            priority = draft.priority.code
        )
    }

    override suspend fun setStatus(id: String, status: Status) = modify(id) { it.copy(status = status.code) }

    override suspend fun delete(id: String) {
        val current = dao.getById(id) ?: return
        if (current.pendingAction == PendingAction.CREATE.name) {
            dao.deleteById(id)
            return
        }
        dao.update(
            current.copy(
                isDeleted = true,
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING.name,
                pendingAction = PendingAction.DELETE.name
            )
        )
        requestSync()
    }

    override fun requestSync() {
        syncRequested = true
        scope.launch { runSync() }
    }

    private suspend fun modify(id: String, change: (ReminderEntity) -> ReminderEntity) {
        val current = dao.getById(id) ?: return
        val action = if (current.pendingAction == PendingAction.CREATE.name) PendingAction.CREATE else PendingAction.UPDATE
        dao.update(
            change(current).copy(
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING.name,
                pendingAction = action.name,
                lastError = null
            )
        )
        requestSync()
    }

    private suspend fun runSync() {
        if (!syncMutex.tryLock()) return
        var failed = false
        try {
            while (syncRequested && !failed) {
                syncRequested = false
                _syncState.value = SyncState.Syncing
                val error = pushPending() ?: pullRemote()
                failed = error != null
                _syncState.value = if (error == null) SyncState.Synced(System.currentTimeMillis()) else SyncState.Failed(error.message)
            }
        } finally {
            syncMutex.unlock()
        }
        if (syncRequested && !failed) runSync()
    }

    private suspend fun pushPending(): ApiError? {
        var firstError: ApiError? = null
        for (entity in dao.getPending()) {
            val deleting = entity.pendingAction == PendingAction.DELETE.name
            val result = if (deleting) {
                safeApiCall { api.deleteReminder(entity.id) }
            } else {
                safeApiCall { api.upsertReminder(entity.id, entity.toDto()) }
            }
            val alreadyDeleted = deleting && result is ApiResult.Failure && result.code == 404
            when {
                result is ApiResult.Success || alreadyDeleted ->
                    if (deleting) dao.deleteById(entity.id) else dao.markSynced(entity.id, entity.updatedAt)
                result is ApiResult.Failure -> {
                    if (result.error != ApiError.NO_CONNECTION) dao.markFailed(entity.id, result.error.message)
                    if (firstError == null) firstError = result.error
                    if (result.error.stopsSync) return result.error
                }
            }
        }
        return firstError
    }

    private suspend fun pullRemote(): ApiError? =
        when (val result = safeApiCall { api.getReminders() }) {
            is ApiResult.Failure -> result.error
            is ApiResult.Success -> {
                val remote = result.value.orEmpty().filter { it.isValid() }
                remote.forEach { dto ->
                    val local = dao.getById(dto.id)
                    val canReplace = local == null ||
                        (local.pendingAction == PendingAction.NONE.name && dto.updatedAt >= local.updatedAt)
                    if (canReplace) dao.upsert(dto.toEntity())
                }
                dao.deleteSyncedNotIn(remote.map { it.id })
                null
            }
        }

    private fun SortOrder.comparator(): Comparator<Reminder> = when (this) {
        SortOrder.NEWEST -> compareByDescending { it.createdAt }
        SortOrder.PRIORITY -> compareByDescending<Reminder> { it.priority.weight }.thenByDescending { it.createdAt }
        SortOrder.TITLE -> compareBy { it.title.lowercase() }
    }
}
