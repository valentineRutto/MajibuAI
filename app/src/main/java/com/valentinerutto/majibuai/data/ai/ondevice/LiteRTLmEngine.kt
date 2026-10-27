package com.valentinerutto.majibuai.data.ai.ondevice

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.valentinerutto.majibuai.data.ai.AiProvider
import com.valentinerutto.majibuai.data.ai.models.AiChatRequest
import com.valentinerutto.majibuai.data.ai.models.AiChatResponse
import com.valentinerutto.majibuai.data.ai.models.AiError
import com.valentinerutto.majibuai.data.ai.models.AiProviderException
import com.valentinerutto.majibuai.data.ai.models.ChatRole
import com.valentinerutto.majibuai.data.ai.models.OnDeviceAvailability
import com.valentinerutto.majibuai.data.ai.models.OnDeviceModelState
import com.valentinerutto.majibuai.data.ai.models.toDisplayMessage
import com.valentinerutto.majibuai.util.Constants.LITERTLM_MODEL_ID
import com.valentinerutto.majibuai.util.Constants.MAJIBU_SYSTEM_INSTRUCTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class LiteRTLmEngine (private val modelManager: OnDeviceModelManager){
    private val mutex = Mutex()
    private var engine: Engine? = null

    private val _modelState = MutableStateFlow(OnDeviceModelState())
    val modelState = _modelState.asStateFlow()

    suspend fun refreshModelStatus() = mutex.withLock {
        _modelState.value = OnDeviceModelState(

            availability = when {

                engine != null -> OnDeviceAvailability.AVAILABLE

                modelManager.isDownloaded() -> OnDeviceAvailability.DOWNLOADED

                else -> OnDeviceAvailability.DOWNLOADABLE
            }
        )
    }
    suspend fun downloadModel(){
        _modelState.value = OnDeviceModelState(
            availability = OnDeviceAvailability.DOWNLOADING
        )

        try {
            modelManager.downloadModel { downloaded, total ->
                _modelState.value = OnDeviceModelState(
                    availability = OnDeviceAvailability.DOWNLOADING,
                    bytesDownloaded = downloaded,
                    bytesToDownload = total
                )
            }
            _modelState.value = OnDeviceModelState(
                availability = OnDeviceAvailability.DOWNLOADED
            )

        initialize()

        }catch (e: CancellationException) {
            refreshModelStatus()
            throw e

        }catch (e: Exception) {
            val error = AiError.OnDeviceDownloadFailed(e.message)

            _modelState.value = OnDeviceModelState(
                availability = OnDeviceAvailability.DOWNLOAD_FAILED,
                errorMessage =  error.toDisplayMessage()
            )
            throw AiProviderException(error, e)
        }
    }

    suspend fun initialize() =  mutex.withLock{
        if (engine != null) return@withLock

        if (!modelManager.isDownloaded()) {
            _modelState.value = OnDeviceModelState(
                availability = OnDeviceAvailability.DOWNLOADABLE
            )
            throw AiProviderException(AiError.OnDeviceModelNotReady)

        }

        _modelState.value = OnDeviceModelState(
            availability = OnDeviceAvailability.INITIALIZING
        )
        try {
            withContext(Dispatchers.Default) {
                val newEngine = Engine(
                    EngineConfig(
                        modelPath = modelManager.modelPath(),
                        backend = Backend.CPU()
                    )
                )

                newEngine.initialize()
                engine = newEngine
            }

            _modelState.value = OnDeviceModelState(
                availability = OnDeviceAvailability.AVAILABLE
            )

        }catch (e: kotlinx.coroutines.CancellationException) {

            _modelState.value = OnDeviceModelState(
                availability = OnDeviceAvailability.DOWNLOADED
            )
            throw e
        }catch (e: Exception){
            val error = AiError.OnDeviceInitializationFailed(e.message)

            _modelState.value = OnDeviceModelState(
                availability = OnDeviceAvailability.UNAVAILABLE,
                errorMessage = error.toDisplayMessage()
            )

            throw AiProviderException(error, e)
        }


    }
    fun streamChat(
        request: AiChatRequest
    ): Flow<AiChatResponse> = flow {
        val question = request.messages.lastOrNull()
            ?.takeIf { it.role == ChatRole.USER }
            ?.content
            ?.takeIf { it.isNotBlank() }
            ?: throw AiProviderException(
                AiError.InvalidRequest()
            )

        mutex.withLock {
            val localEngine = engine
                ?: throw AiProviderException(
                    AiError.OnDeviceModelNotReady
                )

            val config = ConversationConfig(
                systemInstruction = Contents.of(MAJIBU_SYSTEM_INSTRUCTION)
            )

            try {
                localEngine.createConversation(config).use { conversation ->
                    conversation.sendMessageAsync(question).collect { chunk ->
                        val text = chunk.toString()

                        if (text.isNotEmpty()) {
                            emit(
                                AiChatResponse(
                                    text = text,
                                    provider = AiProvider.ON_DEVICE,
                                    modelId = LITERTLM_MODEL_ID
                                )
                            )
                        }
                    }
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                throw AiProviderException(
                    AiError.OnDeviceInferenceFailed(e.message),
                    e
                )
            }
        }
    }.flowOn(Dispatchers.Default)
    suspend fun close() = mutex.withLock {
        withContext(Dispatchers.Default) {
            engine?.close()
            engine = null
        }

        _modelState.value = OnDeviceModelState(
            availability = if (modelManager.isDownloaded()) {
                OnDeviceAvailability.DOWNLOADED
            } else {
                OnDeviceAvailability.DOWNLOADABLE
            }
        )
    }

}