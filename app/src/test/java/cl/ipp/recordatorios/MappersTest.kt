package cl.ipp.recordatorios

import cl.ipp.recordatorios.data.isValid
import cl.ipp.recordatorios.data.local.PendingAction
import cl.ipp.recordatorios.data.remote.ReminderDto
import cl.ipp.recordatorios.data.toDomain
import cl.ipp.recordatorios.data.toDto
import cl.ipp.recordatorios.data.toEntity
import cl.ipp.recordatorios.model.Category
import cl.ipp.recordatorios.model.Priority
import cl.ipp.recordatorios.model.Status
import cl.ipp.recordatorios.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    private val dto = ReminderDto(
        id = "a1b2",
        title = "  Control dental  ",
        description = "",
        category = "HEALTH",
        priority = "HIGH",
        status = "PENDING",
        createdAt = 1_000L,
        updatedAt = 2_000L
    )

    @Test
    fun dtoSeGuardaComoSincronizadoYLimpio() {
        val entity = dto.toEntity()
        assertEquals("Control dental", entity.title)
        assertEquals(null, entity.description)
        assertEquals(SyncStatus.SYNCED.name, entity.syncStatus)
        assertEquals(PendingAction.NONE.name, entity.pendingAction)
    }

    @Test
    fun entidadSeConvierteEnModeloDeDominio() {
        val reminder = dto.toEntity().toDomain()
        assertEquals(Category.Health, reminder.category)
        assertEquals(Priority.High, reminder.priority)
        assertEquals(Status.Pending, reminder.status)
    }

    @Test
    fun idaYVueltaConservaElIdentificador() {
        assertEquals(dto.id, dto.toEntity().toDto().id)
    }

    @Test
    fun dtoInvalidoNoSeIncorpora() {
        assertTrue(dto.isValid())
        assertFalse(dto.copy(title = "No").isValid())
        assertFalse(dto.copy(category = "OTRA").isValid())
    }
}
