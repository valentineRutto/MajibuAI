package com.valentinerutto.majibuai.data.ai.openrouter

import com.valentinerutto.majibuai.data.ai.AiProvider
import com.valentinerutto.majibuai.data.ai.models.AiError
import com.valentinerutto.majibuai.data.ai.models.AiErrorDetails
import com.valentinerutto.majibuai.data.ai.models.AiChatRequest
import com.valentinerutto.majibuai.data.ai.models.AiChatResponse
import com.valentinerutto.majibuai.data.ai.models.AiProviderException
import com.valentinerutto.majibuai.data.ai.models.ChatRole
import com.valentinerutto.majibuai.data.ai.models.OpenRouterMessage
import com.valentinerutto.majibuai.data.ai.models.OpenRouterRequest
import com.valentinerutto.majibuai.data.ai.models.OpenRouterResponse
import com.valentinerutto.majibuai.data.ai.models.OpenRouterStreamChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json


class OpenRouterEngine(
    private val api: OpenRouterApi,
    private val json: Json
) {


    suspend fun streamChat(request: AiChatRequest): Flow<AiChatResponse> = flow {
        val latestMessage = request.messages.lastOrNull()
            ?: throw IllegalArgumentException("A chat request must contain a user message.")

        require(latestMessage.role == ChatRole.USER) {
            "The last chat message must be from the user."
        }
        require(latestMessage.content.isNotBlank()) {
            "The user message must not be blank."
        }



        val messages = listOf(
            OpenRouterMessage(
                role = "system",
                content = MAJIBU_SYSTEM_INSTRUCTION
            )
        ) + request.messages
            .filter { it.content.isNotBlank() }
            .map { message ->
                OpenRouterMessage(
                    role = when (message.role) {
                        ChatRole.USER -> "user"
                        ChatRole.ASSISTANT -> "assistant"
                    },
                    content = message.content
                )
            }

        val httpResponse = api.streamChatCompletions(
            OpenRouterRequest(
                model = MODEL_ID,
                messages = messages,
                stream = true
            )
        )

        if (!httpResponse.isSuccessful) {
            throw AiProviderException(
                error = httpResponse.code().toAiError(),
            )
        }



        val result = httpResponse.body()
            ?: throw AiProviderException(AiError.EmptyResponse)

result.use {
    val source = it.source()
    var completed = false

    while (true){
        currentCoroutineContext().ensureActive()

        val line = source.readUtf8Line()?: break

        if (!line.startsWith("data:")) continue

        val data = line.removePrefix("data:").trim()

        if (data == "[DONE]"){
            completed = true
            break
        }

        if (data.isBlank()) continue

        val chunk = json.decodeFromString<OpenRouterStreamChunk>(data)

        chunk.error?.let { error ->


            throw AiProviderException(

                AiError.Unknown( error.message))

        }

val text = chunk.choices.firstOrNull()?.delta?.content?: continue

        if (!text.isNullOrEmpty()){
            emit(
                AiChatResponse(
                    text = text,
                    provider = AiProvider.OPENROUTER,
                    modelId = chunk.model ?: MODEL_ID,
                )
            )
        }

        if (!completed){
            throw AiProviderException(AiError.Unknown("Stream completed without proper termination."))
        }


    }
}

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
