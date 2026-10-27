package com.valentinerutto.majibuai.data.ai.openrouter

import com.valentinerutto.majibuai.util.AiProvider
import com.valentinerutto.majibuai.data.ai.models.AiError
import com.valentinerutto.majibuai.data.ai.models.AiChatRequest
import com.valentinerutto.majibuai.data.ai.models.AiChatResponse
import com.valentinerutto.majibuai.data.ai.models.AiProviderException
import com.valentinerutto.majibuai.data.ai.models.ChatRole
import com.valentinerutto.majibuai.data.ai.models.OpenRouterMessage
import com.valentinerutto.majibuai.data.ai.models.OpenRouterRequest
import com.valentinerutto.majibuai.data.ai.models.OpenRouterStreamChunk
import com.valentinerutto.majibuai.util.Constants.MAJIBU_SYSTEM_INSTRUCTION
import com.valentinerutto.majibuai.util.Constants.OPENROUTER_MODEL_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json


class OpenRouterEngine(
    private val api: OpenRouterApi,
    private val json: Json
) {

     fun streamChat(request: AiChatRequest): Flow<AiChatResponse> = flow {
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
                model = OPENROUTER_MODEL_ID,
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

        result.use { responseBody ->
            val source = responseBody.source()
            var completed = false

            while (true) {

                currentCoroutineContext().ensureActive()

                val line = source.readUtf8Line() ?: break
                if (!line.startsWith("data:")) continue

                val data = line.removePrefix("data:").trim()
                if (data == "[DONE]") {
                    completed = true
                    break
                }
                if (data.isBlank()) continue

                val chunk = json.decodeFromString<OpenRouterStreamChunk>(data)
                chunk.error?.let { error ->
                    throw AiProviderException(AiError.Unknown(error.message))
                }

                val text = chunk.choices.firstOrNull()?.delta?.content ?: continue
                if (text.isNotEmpty()) {
                    emit(
                        AiChatResponse(
                            text = text,
                            provider = AiProvider.OPENROUTER,
                            modelId = chunk.model ?: OPENROUTER_MODEL_ID,
                        )
                    )
                }
            }

            if (!completed) {
                throw AiProviderException(
                    AiError.Unknown("OpenRouter stream ended without a completion marker.")
                )
            }
        }

    }.flowOn(Dispatchers.IO)


}
