package cl.ipp.recordatorios.data.remote

import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

enum class ApiError(val message: String, val stopsSync: Boolean) {
    INVALID_REQUEST("El servicio rechazó la solicitud", false),
    SERVER("El servicio respondió con un error", true),
    TIMEOUT("El servicio tardó demasiado en responder", true),
    NO_CONNECTION("Sin conexión con el servicio", true)
}

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T?) : ApiResult<T>
    data class Failure(val error: ApiError, val code: Int? = null) : ApiResult<Nothing>
}

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> =
    try {
        val response = call()
        when (response.code()) {
            in 200..299 -> ApiResult.Success(response.body())
            in 400..499 -> ApiResult.Failure(ApiError.INVALID_REQUEST, response.code())
            else -> ApiResult.Failure(ApiError.SERVER, response.code())
        }
    } catch (e: SocketTimeoutException) {
        ApiResult.Failure(ApiError.TIMEOUT)
    } catch (e: IOException) {
        ApiResult.Failure(ApiError.NO_CONNECTION)
    }
