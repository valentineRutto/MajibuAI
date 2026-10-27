package com.valentinerutto.majibuai.data.ai.models

import com.valentinerutto.majibuai.util.AiProvider
import java.util.UUID

enum class ChatRole {
    USER,
    ASSISTANT
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: ChatRole,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class AiChatRequest(
    val messages: List<ChatMessage>
)

data class AiChatResponse(
    val text: String,
    val provider: AiProvider,
    val modelId: String,
    val error: String? = null
)