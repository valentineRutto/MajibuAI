package com.valentinerutto.majibuai.data.ai.openrouter

import com.valentinerutto.majibuai.data.ai.AiProvider
import com.valentinerutto.majibuai.data.ai.models.AiChatRequest
import com.valentinerutto.majibuai.data.ai.models.AiChatResponse
import com.valentinerutto.majibuai.data.ai.models.ChatRole
import com.valentinerutto.majibuai.data.ai.models.OpenRouterMessage
import com.valentinerutto.majibuai.data.ai.models.OpenRouterRequest
import java.io.IOException

class OpenRouterEngine(
    private val api: OpenRouterApi) {

    suspend fun generate(request: AiChatRequest): AiChatResponse {
        val latestMessage = request.messages.lastOrNull()
            ?: throw IllegalArgumentException("A chat request must contain a user message.")

        require(latestMessage.role == ChatRole.USER) {
            "The last chat message must be from the user."
        }
        require(latestMessage.content.isNotBlank()) {
            "The user message must not be blank."
        }

        val messages = buildList {

            add(OpenRouterMessage(role = "system", content = MAJIBU_SYSTEM_INSTRUCTION))

            request.messages
                .filter { it.content.isNotBlank() }
                .forEach { message ->
                    add(
                        OpenRouterMessage(
                            role = when (message.role) {
                                ChatRole.USER -> "user"
                                ChatRole.ASSISTANT -> "assistant"
                            },
                            content = message.content,
                        )
                    )
                }
        }

        val response = api.generateChatCompletions(
            OpenRouterRequest(
                model = MODEL_ID,
                messages = messages,
            )
        )

        val assistantMessage = response.choices.firstOrNull()?.message
            ?: throw IOException("OpenRouter returned no chat completion choices.")

        return AiChatResponse(
            text = assistantMessage.content,
            provider = AiProvider.OPENROUTER,
            modelId = response.model ?: MODEL_ID,
        )

    }

    private companion object {
        private const val MODEL_ID =
            "nvidia/nemotron-3.5-lightning:free"

        val MAJIBU_SYSTEM_INSTRUCTION = """
            You are MajibuAI, a friendly technical learning assistant.
            Explain technical concepts accurately, using simple language before advanced details.
            Provide Kotlin and Jetpack Compose examples for Android questions when useful.
            Structure explanations with headings and bullets when they improve readability.
            Keep answers concise unless the user asks for detail, and admit uncertainty rather than inventing facts.
        """.trimIndent()
    }
}
