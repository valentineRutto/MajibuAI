package com.valentinerutto.majibuai.data.ai

import com.google.firebase.ai.type.APINotConfiguredException
import com.google.firebase.ai.type.InvalidLocationException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.ServiceConnectionHandshakeFailedException
import com.google.firebase.ai.type.ServiceDisabledException
import java.io.IOException

sealed interface AiError {
    data object Network : AiError
    data object QuotaExceeded : AiError
    data object ModelUnavailable : AiError

    data object InvalidRequest : AiError
    data object EmptyResponse : AiError
    data object AppCheckRejected : AiError
    data class Unknown(val message: String?) : AiError
}

sealed interface AiResult<out T> {
    data class Success<T>(val value: T) : AiResult<T>
    data class Failure(val error: AiError) : AiResult<Nothing>
}

fun Exception.toAiError(): AiError = when (this) {
    is QuotaExceededException -> AiError.QuotaExceeded

    is IOException,
    is RequestTimeoutException,
    is ServiceConnectionHandshakeFailedException -> AiError.Network

    is APINotConfiguredException,
    is ServiceDisabledException,
    is InvalidLocationException -> AiError.ModelUnavailable

    is ServerException -> {
        val details = message.orEmpty()
        if (details.contains("attestation", ignoreCase = true) ||
            details.contains("App Check", ignoreCase = true)
        ) {
            AiError.AppCheckRejected
        } else {
            AiError.Unknown(message)
        }
    }

    else -> AiError.Unknown(message)
}

 fun AiError.toDisplayMessage(): String = when (this) {
  AiError.Network -> "Check your internet connection and try again."
   AiError.QuotaExceeded -> "The AI usage limit was reached. Try again later."
    AiError.ModelUnavailable -> "The AI service is unavailable right now."
    AiError.EmptyResponse -> "The AI returned an empty response. Try again."
    AiError.AppCheckRejected ->
        "App verification failed. Check that this device's debug token is registered."
     is AiError.Unknown -> "Something went wrong. Please try again."
     else -> {"Something went wrong. Please try again."}
 }