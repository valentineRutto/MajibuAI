package com.valentinerutto.majibuai.data.ai.openrouter

import com.valentinerutto.majibuai.data.ai.models.AiError
import com.valentinerutto.majibuai.data.ai.models.AiErrorDetails


 fun Int.toAiError(): AiError {
    val details = AiErrorDetails(statusCode = this)

    return when (this) {
        401 -> AiError.Authentication(details)
        402 -> AiError.PaymentRequired(details)
        403 -> AiError.PermissionDenied(details)
        404 -> AiError.ModelUnavailable(details)
        429 -> AiError.RateLimited(null, details)
        400, 413, 422 -> AiError.InvalidRequest(details)
        408, in 500..599 -> AiError.ProviderUnavailable(details)
        else -> AiError.Unknown(
            message = "OpenRouter request failed: HTTP $this",
            details = details
        )
    }
}