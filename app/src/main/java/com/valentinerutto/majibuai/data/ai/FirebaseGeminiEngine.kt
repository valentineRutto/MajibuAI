package com.valentinerutto.majibuai.data.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

interface FirebaseGeminiAIEngine {
    suspend fun generate(request: AiChatRequest): AiChatResponse
}

class FirebaseGeminiEngine(
    private val modelId: String = DEFAULT_MODEL_ID,
) : FirebaseGeminiAIEngine {
    private val model by lazy {

        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelId)

    }

    override suspend fun generate(request: AiChatRequest): AiChatResponse {
        val currentMessage = request.messages.lastOrNull()

            ?: throw IllegalArgumentException("A chat request must contain a user message.")

        require(currentMessage.role == ChatRole.USER) {
            "The last chat message must be from the user."
        }

        require(currentMessage.content.isNotBlank()) {
            "The user message must not be blank."
        }

        val history = request.messages
            .dropLast(1)
            .filter { it.content.isNotBlank() }
            .map { message ->
                val role = when (message.role) {
                    ChatRole.USER -> "user"
                    ChatRole.ASSISTANT -> "model"
                }
                content(role = role) { text(message.content) }
            }

        val chat = model.startChat(history = history)
        val response = chat.sendMessage(currentMessage.content)

        return AiChatResponse(
            text = response.text.orEmpty(),
            provider = PROVIDER,
            modelId = modelId,
        )
    }

    private companion object {
        const val DEFAULT_MODEL_ID = "gemini-3.8-flash"
        const val PROVIDER = "Firebase AI Logic (Google AI)"
    }
}
