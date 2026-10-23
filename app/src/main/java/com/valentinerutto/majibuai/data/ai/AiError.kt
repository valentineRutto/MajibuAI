package com.valentinerutto.majibuai.data.ai

sealed interface AiError {
    data object Network : AiError
    data object QuotaExceeded : AiError
    data object ModelUnavailable : AiError
    data object EmptyResponse : AiError
    data class Unknown(val message: String?) : AiError
}