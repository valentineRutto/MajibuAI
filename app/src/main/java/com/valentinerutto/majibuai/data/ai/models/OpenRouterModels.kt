package com.valentinerutto.majibuai.data.ai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>
)

@Serializable
data class OpenRouterMessage(
    val role: String,
    val content: String? = null,
    val reasoning: String? = null,
)

@Serializable
data class OpenRouterResponse(
    val model: String? = null,
    val choices: List<OpenRouterChoice> = emptyList(),
    val error: JsonObject? = null,
)

@Serializable
data class OpenRouterChoice(
    val message: OpenRouterMessage
)
