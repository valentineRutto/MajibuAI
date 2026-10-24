package com.valentinerutto.majibuai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentinerutto.majibuai.data.ai.AiError
import com.valentinerutto.majibuai.data.ai.AiRepository
import com.valentinerutto.majibuai.data.ai.AiResult
import com.valentinerutto.majibuai.data.ai.ChatMessage
import com.valentinerutto.majibuai.data.ai.ChatRole
import com.valentinerutto.majibuai.data.ai.toDisplayMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


data class AskUiState(
    val question: String = "",
    val conversation: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: AiError? = null
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

    fun saveCurrentAnswer() {
        val exchange = _uiState.value.conversation.lastOrNull() ?: return
      //  saveAnswer(exchange.question, exchange.answer)
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

    fun ask() {

        val currentState = _uiState.value

        val question = _uiState.value.question.trim()

        if (question.isBlank() || currentState.isLoading) return

        val userMessage = ChatMessage(
            role = ChatRole.USER,
            content = question,
        )

        val requestMessages = currentState.conversation + userMessage

        _uiState.update {
            it.copy(
                question = "",
                conversation = requestMessages,
                isLoading = true,
                error = null,
            )
        }

        viewModelScope.launch {

            try {

                when (val result = repository.explain(requestMessages)) {
                    is AiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                conversation = it.conversation + ChatMessage(
                                    role = ChatRole.ASSISTANT,
                                    content = result.value,
                                ),
                            )
                        }

                    }

                    is AiResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                question = question,
                                conversation = it.conversation.filterNot {
                                    it.id == userMessage.id
                                },
                                error = result.error,
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }

        }
    }
}
