package com.valentinerutto.majibuai.data.ai

import com.valentinerutto.majibuai.data.ai.firebase.FirebaseGeminiAIEngine
import com.valentinerutto.majibuai.data.ai.models.AiChatRequest
import com.valentinerutto.majibuai.data.ai.models.AiError
import com.valentinerutto.majibuai.data.ai.models.AiProviderException
import com.valentinerutto.majibuai.data.ai.models.AiResult
import com.valentinerutto.majibuai.data.ai.models.ChatMessage
import com.valentinerutto.majibuai.data.ai.models.OnDeviceAvailability
import com.valentinerutto.majibuai.data.ai.models.toAiError
import com.valentinerutto.majibuai.data.ai.ondevice.LiteRTLmEngine
import com.valentinerutto.majibuai.data.ai.openrouter.OpenRouterEngine
import com.valentinerutto.majibuai.util.AiProvider
import com.valentinerutto.majibuai.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

class AiRepository(private val firebaseEngine: FirebaseGeminiAIEngine, private val openRouterEngine: OpenRouterEngine, private val liteRTLmEngine: LiteRTLmEngine,    private val networkMonitor: NetworkMonitor
) {

    val onDeviceModelState = liteRTLmEngine.modelState

    suspend fun prepareOnDevice() {
        liteRTLmEngine.refreshModelStatus()

        if (
            onDeviceModelState.value.availability ==
            OnDeviceAvailability.DOWNLOADED
        ) {
            liteRTLmEngine.initialize()
        }
    }

    suspend fun downloadOnDeviceModel() {
        liteRTLmEngine.downloadModel()
    }


    suspend fun explain(messages: List<ChatMessage>, provider: AiProvider): Flow<AiResult<String>> =

        when (provider) {
            AiProvider.FIREBASE_AI -> explainStreamFirebaseAi(messages)
            AiProvider.OPENROUTER -> explainStreamOpenRouterAi(messages)
            AiProvider.ON_DEVICE -> explainStreamOndeviceAi(messages)
            AiProvider.HYBRID ->             explainStreamHybridAi(messages)

        }

    fun explainStreamHybridAi(
        messages: List<ChatMessage>
    ): Flow<AiResult<String>> = flow {

        var lastError: AiError? = null

        // Connectivity can change during a request, so check before each cloud provider.
        val cloudProviders = listOf(AiProvider.FIREBASE_AI, AiProvider.OPENROUTER)
        for (provider in cloudProviders) {
            if (!networkMonitor.isOnline()) break

            val providerFlow = when (provider) {
                AiProvider.FIREBASE_AI -> explainStreamFirebaseAi(messages)
                AiProvider.OPENROUTER -> explainStreamOpenRouterAi(messages)
                else -> error("Expected a cloud provider")
            }

            var providerEmittedText = false
            var providerError: AiError? = null

            providerFlow.collect { result ->
                when (result) {
                    is AiResult.Success -> {
                        if (result.value.isNotEmpty()) {
                            providerEmittedText = true
                            emit(result)
                        }
                    }
                    is AiResult.Failure -> providerError = result.error
                }
            }

            // Once text is visible, keep the partial answer and let the user choose to retry.
            // The ViewModel uses this failure to show the interrupted-response UI.
            if (providerError != null && providerEmittedText) {
                emit(AiResult.Failure(providerError!!))
                return@flow
            }

            // A provider that completed after emitting text succeeded.
            if (providerError == null && providerEmittedText) return@flow
            if (providerError != null) {
                lastError = providerError
            }
        }

        // Prefer local inference whenever the connection is now offline. Also use it as
        // the final fallback if both cloud providers failed while the device stayed online.
        if (
            onDeviceModelState.value.availability == OnDeviceAvailability.AVAILABLE
        ) {
            var onDeviceError: AiError? = null
            explainStreamOndeviceAi(messages).collect { result ->
                when (result) {
                    is AiResult.Success -> emit(result)
                    is AiResult.Failure -> onDeviceError = result.error
                }
            }
            onDeviceError?.let { emit(AiResult.Failure(it)) }
            return@flow
        }

        emit(
            AiResult.Failure(
                if (!networkMonitor.isOnline()) {
                    AiError.OnDeviceModelNotReady
                } else {
                    lastError ?: AiError.EmptyResponse
                }
            )
        )

    }.catch { exception ->

        if (exception is CancellationException) {
            throw exception
        }

        emit(
            AiResult.Failure(
                    AiError.Unknown(exception.message)
            )
        )
    }

    suspend fun explainStreamFirebaseAi(messages: List<ChatMessage>): Flow<AiResult<String>> =
        flow {

            val request = AiChatRequest(messages)
            var hasContent = false

            firebaseEngine.streamChat(request).collect { response ->
                if (response.text.isNotEmpty()) {
                    emit(AiResult.Success(response.text,        provider = response.provider
                    ))
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
                emit(AiResult.Success(response.text,        provider = response.provider))
            }
        }
        if (!hasContent) emit(AiResult.Failure(AiError.EmptyResponse))
    }.catch { e ->
        if (e is CancellationException) throw e
        val error = if (e is AiProviderException) e.error else null
        emit(AiResult.Failure(error ?: AiError.Unknown(e.message.orEmpty())))
    }

    fun explainStreamOndeviceAi(messages: List<ChatMessage>): Flow<AiResult<String>> = flow {
        val request = AiChatRequest(messages)

        var hasContent = false

        liteRTLmEngine.streamChat(request).collect { response ->
            if (response.text.isNotEmpty()) {
                emit(AiResult.Success(response.text,        provider = response.provider))

                if (response.text.isNotBlank()) {
                    hasContent = true
                }
            }
        }

        if (!hasContent) {
            emit(
                AiResult.Failure(
                    AiError.EmptyResponse
                )
            )
        }

    }.catch { exception ->

        if (exception is CancellationException) {
            throw exception
        }

        val error = when (exception) {
            is AiProviderException -> exception.error
            is Exception -> exception.toAiError()
            else -> AiError.Unknown(exception.message)
        }

        emit(AiResult.Failure(error))
    }

    fun cancelOnDeviceGeneration() {
        liteRTLmEngine.cancelGeneration()
    }
}






