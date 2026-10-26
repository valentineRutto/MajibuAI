package com.valentinerutto.majibuai.ui

import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentinerutto.majibuai.data.ai.models.AiError
import com.valentinerutto.majibuai.data.ai.AiRepository
import com.valentinerutto.majibuai.data.ai.AiProvider
import com.valentinerutto.majibuai.data.ai.models.AiResult
import com.valentinerutto.majibuai.data.ai.models.ChatMessage
import com.valentinerutto.majibuai.data.ai.models.ChatRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


data class AskUiState(
    val question: String = "",
    val conversation: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: AiError? = null,
    val selectedProvider: AiProvider = AiProvider.OPENROUTER,
)

data class SavedQuestion(
    val question: String,
    val answer: String,
    val savedAt: Long = System.currentTimeMillis()
)

class MajibuViewModel(
    private val repository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AskUiState())
    val uiState = _uiState.asStateFlow()

    private val _savedQuestions = MutableStateFlow<List<SavedQuestion>>(emptyList())
    val savedQuestions = _savedQuestions.asStateFlow()
    private var streamingJob: Job? = null

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
        _uiState.update { it.copy(selectedProvider = provider, error = null) }
    }

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
        val requestMessages = currentState.conversation + userMessage

        _uiState.update {
            it.copy(
                question = "",
                conversation = requestMessages + assistantMessage,
                isLoading = true,
                error = null
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
                                                content = message.content + result.value
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

//    fun asknonstream() {
//
//        val currentState = _uiState.value
//
//        val question = _uiState.value.question.trim()
//
//        if (question.isBlank() || currentState.isLoading) return
//
//        val userMessage = ChatMessage(
//            role = ChatRole.USER,
//            content = question,
//        )
//
//        val requestMessages = currentState.conversation + userMessage
//
//        _uiState.update {
//            it.copy(
//                question = "",
//                conversation = requestMessages,
//                isLoading = true,
//                error = null,
//            )
//        }
//
//        viewModelScope.launch {
//
//            try {
//
//                when (val result = repository.explain(requestMessages, currentState.selectedProvider)) {
//                    is AiResult.Success -> {
//                        _uiState.update {
//                            it.copy(
//                                conversation = it.conversation + ChatMessage(
//                                    role = ChatRole.ASSISTANT,
//                                    content = result.value,
//                                ),
//                            )
//                        }
//
//                    }
//
//                    is AiResult.Failure -> {
//                        _uiState.update {
//                            it.copy(
//                                question = question,
//                                conversation = it.conversation.filterNot {
//                                    it.id == userMessage.id
//                                },
//                                error = result.error,
//                            )
//                        }
//                    }
//                }
//            } catch (e: CancellationException) {
//                throw e
//            } finally {
//                _uiState.update {
//                    it.copy(isLoading = false)
//                }
//            }
//
//        }
//    }
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
            state.copy(error = error)
        } else {
            // Restore the question if nothing was generated
            state.copy(
                question = question,
                conversation = state.conversation.filterNot {
                    it.id == userMessage.id ||
                            it.id == assistantMessage.id
                },
                error = error
            )
        }
    }
}

    fun stopGenerating() {
        streamingJob?.cancel()
    }
}
