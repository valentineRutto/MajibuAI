package com.valentinerutto.majibuai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentinerutto.majibuai.data.ai.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


data class AskUiState(
    val question: String = "",
    val conversation: List<AskExchange> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AskExchange(
    val question: String,
    val answer: String = ""
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
        saveAnswer(exchange.question, exchange.answer)
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

        val question = _uiState.value.question.trim()

        if (question.isBlank() || _uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    question = "",
                    conversation = it.conversation + AskExchange(question),
                    isLoading = true,
                    error = null
                )
            }

            try {

                val answer = repository.explain(question)

                _uiState.update {
                    it.copy(
                        conversation = it.conversation.dropLast(1) + it.conversation.last().copy(answer = answer)
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Something went wrong")
                }
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}
