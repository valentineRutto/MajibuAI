package com.valentinerutto.majibuai.data.ai

import com.valentinerutto.majibuai.data.ai.firebase.FirebaseGeminiAIEngine
import com.valentinerutto.majibuai.data.ai.models.AiChatRequest
import com.valentinerutto.majibuai.data.ai.models.AiError
import com.valentinerutto.majibuai.data.ai.models.AiProviderException
import com.valentinerutto.majibuai.data.ai.models.AiResult
import com.valentinerutto.majibuai.data.ai.models.ChatMessage
import com.valentinerutto.majibuai.data.ai.ondevice.LiteRTLmEngine
import com.valentinerutto.majibuai.data.ai.openrouter.OpenRouterEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

class AiRepository(private val firebaseEngine: FirebaseGeminiAIEngine, private val openRouterEngine: OpenRouterEngine, private val liteRTLmEngine: LiteRTLmEngine) {


    suspend fun explain(messages: List<ChatMessage>, provider: AiProvider): Flow<AiResult<String>> =
        when (provider) {
            AiProvider.FIREBASE_AI -> explainStreamFirebaseAi(messages)
            AiProvider.OPENROUTER -> explainStreamOpenRouterAi(messages)
            AiProvider.ON_DEVICE -> flow { emit(AiResult.Failure(AiError.ProviderNotConfigured)) }
        }

    suspend fun explainStreamFirebaseAi(messages: List<ChatMessage>): Flow<AiResult<String>> =
        flow {

            val request = AiChatRequest(messages)
            var hasContent = false

            firebaseEngine.streamChat(request).collect { response ->
                if (response.text.isNotEmpty()) {
                    emit(AiResult.Success(response.text))
                    if (response.text.isNotBlank()) hasContent = true
                }
            }


            if (!hasContent) {
                emit(AiResult.Failure(AiError.EmptyResponse))
            }


        }.catch { e ->
            if (e is CancellationException) throw e
            val error = if (e is AiProviderException) e.error else null
            emit(AiResult.Failure(error ?: AiError.Unknown(e.message.orEmpty())))

        }


    suspend fun explainStreamOpenRouterAi(messages: List<ChatMessage>): Flow<AiResult<String>> = flow {
        var hasContent = false
        openRouterEngine.streamChat(AiChatRequest(messages)).collect { response ->
            if (response.text.isNotEmpty()) {
                hasContent = true
                emit(AiResult.Success(response.text))
            }
        }
        if (!hasContent) emit(AiResult.Failure(AiError.EmptyResponse))
    }.catch { e ->
        if (e is CancellationException) throw e
        val error = if (e is AiProviderException) e.error else null
        emit(AiResult.Failure(error ?: AiError.Unknown(e.message.orEmpty())))
    }

}


