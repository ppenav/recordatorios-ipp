package cl.ipp.recordatorios.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

data class ReminderDto(
    val id: String,
    val title: String,
    val description: String?,
    val category: String,
    val priority: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

interface ReminderApi {

    @GET("reminders")
    suspend fun getReminders(): Response<List<ReminderDto>>

    @PUT("reminders/{id}")
    suspend fun upsertReminder(@Path("id") id: String, @Body reminder: ReminderDto): Response<ReminderDto>

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: String): Response<Unit>
}
