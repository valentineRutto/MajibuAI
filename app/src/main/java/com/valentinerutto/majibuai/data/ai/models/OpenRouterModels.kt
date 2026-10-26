package com.valentinerutto.majibuai.data.ai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val stream: Boolean = false
)

@Serializable
data class OpenRouterMessage(
    val role: String,
    val content: String? = null,
    val reasoning: String? = null
)

// Non-streaming models
@Serializable
data class OpenRouterResponse(
    val model: String? = null,
    val choices: List<OpenRouterChoice> = emptyList(),
    val error: JsonObject? = null
)

@Serializable
data class OpenRouterChoice(
    val message: OpenRouterMessage
)

// Streaming models
@Serializable
data class OpenRouterStreamChunk(
    val model: String? = null,
    val choices: List<OpenRouterStreamChoice> = emptyList(),
    val error: OpenRouterStreamError? = null
)

@Serializable
data class OpenRouterStreamChoice(
    val delta: OpenRouterStreamDelta = OpenRouterStreamDelta()
)

@Serializable
data class OpenRouterStreamDelta(
    val content: String? = null
)

@Serializable
data class OpenRouterStreamError(
    val message: String = "OpenRouter streaming failed"
)