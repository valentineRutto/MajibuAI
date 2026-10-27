package com.valentinerutto.majibuai.data.ai.models

import com.google.firebase.ai.type.APINotConfiguredException
import com.google.firebase.ai.type.InvalidLocationException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ServiceConnectionHandshakeFailedException
import com.google.firebase.ai.type.ServiceDisabledException
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed interface AiError {
    data object Network : AiError
    data object QuotaExceeded : AiError
    data class ModelUnavailable(val details: AiErrorDetails? = null) : AiError

    data class InvalidRequest(val details: AiErrorDetails? = null) : AiError
    data object EmptyResponse : AiError
    data object ProviderNotConfigured : AiError
    data object AppCheckRejected : AiError
    data class Authentication(val details: AiErrorDetails? = null) : AiError
    data class PermissionDenied(val details: AiErrorDetails? = null) : AiError
    data class PaymentRequired(val details: AiErrorDetails? = null) : AiError
    data class RateLimited(
        val retryAfterSeconds: Long?,
        val details: AiErrorDetails? = null,
    ) : AiError
    data class ContextLengthExceeded(val details: AiErrorDetails? = null) : AiError
    data class ContentPolicyViolation(val details: AiErrorDetails? = null) : AiError
    data class ProviderUnavailable(val details: AiErrorDetails? = null) : AiError
    data class Unknown(
        val message: String?,
        val details: AiErrorDetails? = null,
    ) : AiError

    data object OnDeviceModelNotReady : AiError

    data class OnDeviceDownloadFailed(
        val reason: String? = null
    ) : AiError

    data class OnDeviceInitializationFailed(
        val reason: String? = null
    ) : AiError

    data class OnDeviceInferenceFailed(
        val reason: String? = null
    ) : AiError
}

data class AiErrorDetails(
    val statusCode: Int? = null,
    val errorType: String? = null,
    val providerCode: String? = null,
    val providerMessage: String? = null,
    val metadataJson: String? = null,
    val retryAfterSeconds: Long? = null,
)

class AiProviderException(
    val error: AiError,
    cause: Throwable? = null,
) : Exception("AI provider request failed", cause)

sealed interface AiResult<out T> {
    data class Success<T>(val value: T) : AiResult<T>
    data class Failure(val error: AiError) : AiResult<Nothing>
}

fun Exception.toAiError(): AiError = when (this) {
    is QuotaExceededException -> AiError.QuotaExceeded

    is IllegalArgumentException -> AiError.InvalidRequest()

    is IOException,
    is RequestTimeoutException,
    is ServiceConnectionHandshakeFailedException -> AiError.Network

    is APINotConfiguredException,
    is ServiceDisabledException,
    is InvalidLocationException -> AiError.ModelUnavailable()

    else -> if (hasAppCheckFailure()) AiError.AppCheckRejected else AiError.Unknown(message)
}

private fun Throwable.hasAppCheckFailure(): Boolean {
    var current: Throwable? = this
    while (current != null) {

        val details = current.message.orEmpty()

        if (details.contains("attestation", ignoreCase = true) ||
            details.contains("App Check", ignoreCase = true) ||
            details.contains("AppCheck", ignoreCase = true)
        ) {
            return true
        }

        current = current.cause
    }
    return false
}

fun AiError.toDisplayMessage(): String = when (this) {
    AiError.Network -> "Check your internet connection and try again."
    AiError.QuotaExceeded -> "The AI usage limit was reached. Try again later."
    is AiError.ModelUnavailable -> "The AI service is unavailable right now."
    is AiError.InvalidRequest -> "Please check your message and try again."
    AiError.EmptyResponse -> "The AI returned an empty response. Try again."
    AiError.ProviderNotConfigured -> "On-device AI is not available yet. Choose Firebase or OpenRouter."
    AiError.AppCheckRejected ->
        "App verification failed. Check that this device's debug token is registered."
    is AiError.Authentication -> "AI provider authentication failed. Check the API configuration."
    is AiError.PermissionDenied -> "The AI request was denied by the provider."
    is AiError.PaymentRequired -> "The AI provider has reached its usage or credit limit."
    is AiError.RateLimited -> "Too many AI requests. Please wait and try again."
    is AiError.ContextLengthExceeded -> "This conversation is too long. Start a new chat and try again."
    is AiError.ContentPolicyViolation -> "The AI provider could not answer that request."
    is AiError.ProviderUnavailable -> "The AI service is unavailable right now. Try again shortly."
    is AiError.Unknown -> "Something went wrong. Please try again."
    // On-device errors
   is AiError.OnDeviceModelNotReady ->
        "Download and initialize the on-device AI model first."

    is AiError.OnDeviceDownloadFailed ->
        "Could not download the on-device AI model. Check your connection and try again."

    is AiError.OnDeviceInitializationFailed ->
        "Could not initialize the on-device AI model. Try again."

    is AiError.OnDeviceInferenceFailed ->
        "On-device AI could not generate a response. Please try again."

}

 fun downloadErrorMessage(
    exception: Exception
): String = when (exception) {

    is UnknownHostException ->
        "No internet connection. Connect to Wi-Fi or mobile data and retry."

    is SocketTimeoutException ->
        "The download timed out. Check your connection and retry."

    is SocketException ->
        "The network connection was interrupted. Please retry."

    is IOException ->
        "Unable to download the model. Check your connection and try again."

    else ->
        "Model download failed. Please try again."
}
