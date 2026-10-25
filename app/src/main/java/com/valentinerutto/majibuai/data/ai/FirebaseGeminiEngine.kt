package com.valentinerutto.majibuai.data.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.valentinerutto.majibuai.data.ai.models.AiChatRequest
import com.valentinerutto.majibuai.data.ai.models.AiChatResponse
import com.valentinerutto.majibuai.data.ai.models.ChatRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface FirebaseGeminiAIEngine {
    suspend fun streamChat(request: AiChatRequest): Flow<AiChatResponse>
}

class FirebaseGeminiEngine(
    private val modelId: String = DEFAULT_MODEL_ID,
) : FirebaseGeminiAIEngine {
    private val model by lazy {

        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelId, systemInstruction = content{ text(MAJIBU_SYSTEM_INSTRUCTION)})

    }

    override suspend fun streamChat(request: AiChatRequest): Flow<AiChatResponse> = flow {

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

        chat.sendMessageStream(currentMessage.content).collect { response ->
            response.text?.takeIf { it.isNotEmpty() }?.let { chunck ->
                emit(
                    AiChatResponse(
                        text = chunck,
                        provider = AiProvider.FIREBASE_AI,
                        modelId = modelId
                    )
                )
            }

        }
    }

    private companion object {
        const val DEFAULT_MODEL_ID = "gemini-3.5-flash"

        val MAJIBU_SYSTEM_INSTRUCTION = """
        You are MajibuAI, a friendly technical learning assistant.

        Your responsibilities:
        - Explain technical concepts accurately.
        - Use simple language before introducing advanced concepts.
        - Provide Kotlin , jetpack compose examples for Android questions when neccessary.
        - Structure explanations using headings and bullet points.
        - Include practical examples where helpful.
        - Keep answers concise unless the user asks for detail.
        - Admit uncertainty rather than inventing facts.
    """.trimIndent()
    }
}
