package com.valentinerutto.majibuai.ui

import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentinerutto.majibuai.data.ai.models.AiError
import com.valentinerutto.majibuai.data.ai.AiRepository
import com.valentinerutto.majibuai.util.AiProvider
import com.valentinerutto.majibuai.data.ai.models.AiResult
import com.valentinerutto.majibuai.data.ai.models.ChatMessage
import com.valentinerutto.majibuai.data.ai.models.ChatRole
import com.valentinerutto.majibuai.data.ai.models.OnDeviceAvailability
import com.valentinerutto.majibuai.data.ai.models.toAiError
import com.valentinerutto.majibuai.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


data class AskUiState(
    val question: String = "",
    val conversation: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: AiError? = null,
    val selectedProvider: AiProvider = AiProvider.OPENROUTER,
    val showModelDownloadDialog: Boolean = false,
    val interruptedQuestion: String? = null

)

data class SavedQuestion(
    val question: String,
    val answer: String,
    val savedAt: Long = System.currentTimeMillis()
)

class MajibuViewModel(
    private val repository: AiRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(AskUiState())
    val uiState = _uiState.asStateFlow()

    private val _savedQuestions = MutableStateFlow<List<SavedQuestion>>(emptyList())
    val savedQuestions = _savedQuestions.asStateFlow()
    private var streamingJob: Job? = null

    val onDeviceModelState = repository.onDeviceModelState

    val isOnline: StateFlow<Boolean> =
        networkMonitor.observeConnectivity()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = networkMonitor.isOnline()
            )
    private var modelPreparationJob: Job? = null
    private var providerSelectionVersion = 0
    init {
       modelPreparationJob= viewModelScope.launch {
            try {
                repository.prepareOnDevice()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.toAiError())
                }
            }
        }
    }



    fun saveCurrentAnswer() {
        val conversation = _uiState.value.conversation

        val answer = conversation.lastOrNull()
            ?.takeIf { it.role == ChatRole.ASSISTANT }
            ?.content
            ?: return

        val question = conversation
            .dropLast(1)
            .lastOrNull { it.role == ChatRole.USER }
            ?.content
            ?: return

        saveAnswer(question, answer)
    }

    fun saveAnswer(question: String, answer: String) {

        if (question.isBlank() || answer.isBlank()) return

        _savedQuestions.update { saved ->
            if (saved.any { it.question == question && it.answer == answer }) saved
            else listOf(SavedQuestion(question, answer)) + saved
        }

    }

    fun onQuestionChange(value: String) {
        _uiState.update {
            it.copy(question = value)
        }
    }

    fun selectProvider(provider: AiProvider) {

        val selectionVersion = ++providerSelectionVersion

        if (_uiState.value.isLoading) return

        if (provider != AiProvider.ON_DEVICE) {
            _uiState.update {
                it.copy(
                    selectedProvider = provider,
                    showModelDownloadDialog = false,
                    error = null
                )
            }
            return
        }
        when (onDeviceModelState.value.availability) {

            OnDeviceAvailability.AVAILABLE -> {
                selectOnDevice()
            }

            OnDeviceAvailability.DOWNLOADED -> {
                initializeOnDevice()
            }

            OnDeviceAvailability.CHECKING -> {
                viewModelScope.launch {
                    modelPreparationJob?.join()

                    selectProvider(AiProvider.ON_DEVICE)
                }
            }

            OnDeviceAvailability.DOWNLOADING,
            OnDeviceAvailability.INITIALIZING -> {
                viewModelScope.launch {
                    modelPreparationJob?.join()

                    if (
                        selectionVersion == providerSelectionVersion &&
                        onDeviceModelState.value.availability ==
                        OnDeviceAvailability.AVAILABLE
                    ) {
                        selectOnDevice()
                    }
                }            }
            else -> {
                _uiState.update {
                    it.copy(
                        showModelDownloadDialog = true,
                        error = null
                    )
                }
            }}



    }
    private fun selectOnDevice() {
        _uiState.update {
            it.copy(
                selectedProvider = AiProvider.ON_DEVICE,
                showModelDownloadDialog = false,
                error = null
            )
        }
    }
    private fun initializeOnDevice() {
        if (modelPreparationJob?.isActive == true) return

        modelPreparationJob = viewModelScope.launch {
            try {
                repository.prepareOnDevice()

                if (
                    onDeviceModelState.value.availability ==
                    OnDeviceAvailability.AVAILABLE
                ) {
                    selectOnDevice()
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.toAiError())
                }
            }
        }
    }
    fun dismissModelDownloadDialog() {
        _uiState.update {
            it.copy(showModelDownloadDialog = false)
        }
    }
    fun retryModelDownload() {
        if (modelPreparationJob?.isActive == true) return

        confirmModelDownload()
    }
    fun confirmModelDownload() {
        dismissModelDownloadDialog()

        if (modelPreparationJob?.isActive == true) return

        val selectionVersion = providerSelectionVersion

        modelPreparationJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(error = null) }

                repository.downloadOnDeviceModel()

                if (
                    selectionVersion == providerSelectionVersion &&
                    onDeviceModelState.value.availability ==
                    OnDeviceAvailability.AVAILABLE
                ) {
                    selectOnDevice()
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (
                    onDeviceModelState.value.availability !=
                    OnDeviceAvailability.DOWNLOAD_FAILED
                ) {
                    _uiState.update {
                        it.copy(error = e.toAiError())
                    }
                }
            }
        }
    }
    fun cancelModelDownload() {
        if (
            onDeviceModelState.value.availability ==
            OnDeviceAvailability.DOWNLOADING
        ) {
            modelPreparationJob?.cancel()
        }    }

    fun ask() {
        val currentState = _uiState.value
        val question = currentState.question.trim()

        if (question.isBlank() || currentState.isLoading) return

        val userMessage = ChatMessage(
            role = ChatRole.USER,
            content = question
        )

        val assistantMessage = ChatMessage(
            role = ChatRole.ASSISTANT,
            content = ""
        )

        // Send conversation history without the empty assistant message
        val requestMessages =
            currentState.conversation
                .filter { it.content.isNotBlank() } +
                    userMessage

        _uiState.update {
            it.copy(
                question = "",
                conversation = requestMessages + assistantMessage,
                isLoading = true,
                error = null,        interruptedQuestion = null

            )
        }

        streamingJob=viewModelScope.launch {
            try {
                repository.explain(
                    requestMessages,
                    currentState.selectedProvider
                ).collect { result ->

                    when (result) {
                        is AiResult.Success -> {
                            _uiState.update { state ->
                                state.copy(
                                    conversation = state.conversation.map { message ->
                                        if (message.id == assistantMessage.id) {
                                            message.copy(
                                                content = message.content + result.value,
                                                provider = result.provider ?: message.provider
                                            )
                                        } else {
                                            message
                                        }
                                    }
                                )
                            }
                        }

                        is AiResult.Failure -> {
                            handleError(
                                error = result.error,
                                question = question,
                                userMessage = userMessage,
                                assistantMessage = assistantMessage
                            )
                        }

                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleError(
                    error = AiError.Unknown(e.message.orEmpty()),
                    question = question,
                    userMessage = userMessage,
                    assistantMessage = assistantMessage
                )
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

private fun handleError(
    error: AiError,
    question: String,
    userMessage: ChatMessage,
    assistantMessage: ChatMessage
) {
    _uiState.update { state ->

        val hasPartialAnswer = state.conversation.any {
            it.id == assistantMessage.id &&
                    it.content.isNotBlank()
        }

        if (hasPartialAnswer) {
            // Keep the partial answer if streaming already started
            state.copy(error = error,   interruptedQuestion = question
            )

        } else {
            // Restore the question if nothing was generated
            state.copy(
                question = question,
                conversation = state.conversation.filterNot {
                    it.id == userMessage.id ||
                            it.id == assistantMessage.id
                },
                error = error,
                interruptedQuestion = null

            )
        }
    }
}
    fun retryInterruptedQuestion() {
        val state = _uiState.value

        if (state.isLoading) return

        val question = state.interruptedQuestion ?: return
        if (state.selectedProvider != AiProvider.HYBRID) return

        val lastAssistantIndex = state.conversation
            .indexOfLast { it.role == ChatRole.ASSISTANT }

        if (lastAssistantIndex < 1) return

        val userMessage = state.conversation[lastAssistantIndex - 1]
        val assistantMessage = state.conversation[lastAssistantIndex]

        if (
            userMessage.role != ChatRole.USER ||
            userMessage.content != question
        ) return

        _uiState.update {
            it.copy(
                question = question,
                conversation = it.conversation.filterNot { message ->
                    message.id == userMessage.id ||
                            message.id == assistantMessage.id
                },
                interruptedQuestion = null,
                error = null
            )
        }

        ask()
    }
    fun stopGenerating() {
        val job = streamingJob ?: return
        _uiState.update { it.copy(isLoading = false) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_uiState.value.selectedProvider == AiProvider.ON_DEVICE) {
                    repository.cancelOnDeviceGeneration()
                }
            } finally {
                job.cancel()
            }
        }


    }
}
