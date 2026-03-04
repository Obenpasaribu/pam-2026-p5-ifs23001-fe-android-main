package org.delcom.pam_p5_ifs23001.helper

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import com.google.gson.Gson
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.delcom.pam_p5_ifs23001.network.data.ResponseMessage
import retrofit2.HttpException

object SuspendHelper {
    enum class SnackBarType(val title: String) {
        ERROR(title = "error"),
        SUCCESS(title = "success"),
        INFO(title = "info"),
        WARNING(title = "warning")
    }

    suspend fun showSnackBar(snackbarHost: SnackbarHostState, type: SnackBarType,  message: String){
        coroutineScope {
            launch {
                snackbarHost.showSnackbar(
                    message = "${type.title}|$message",
                    actionLabel = "Close",
                    duration = SnackbarDuration.Indefinite
                )
            }

            launch {
                delay(5_000)
                snackbarHost.currentSnackbarData?.dismiss()
            }
        }
    }

    suspend fun <T> safeApiCall(apiCall: suspend () -> ResponseMessage<T?>): ResponseMessage<T?> {
        return try {
            apiCall()
        } catch (e: HttpException) {
            val errorResponse = e.response()?.errorBody()?.string()
            
            // Perbaikan: Cek apakah errorResponse adalah JSON yang valid sebelum di-parse
            val message = try {
                if (!errorResponse.isNullOrBlank() && errorResponse.trim().startsWith("{")) {
                    val jsonError = Gson().fromJson(errorResponse, ResponseMessage::class.java)
                    jsonError?.message ?: "Server error (${e.code()})"
                } else {
                    errorResponse ?: "Server error (${e.code()})"
                }
            } catch (jsonEx: Exception) {
                errorResponse ?: "Format error: ${e.code()}"
            }

            ResponseMessage(
                status = "error",
                message = message
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseMessage(
                status = "error",
                message = e.message ?: "Unknown error"
            )
        }
    }
}