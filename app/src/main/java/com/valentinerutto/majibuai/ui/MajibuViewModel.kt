package com.valentinerutto.majibuai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentinerutto.majibuai.data.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


data class AskUiState(
    val question: String = "",
    val submittedQuestion: String? = null,
    val answer: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
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
        val state = _uiState.value
        val question = state.submittedQuestion?.takeIf { it.isNotBlank() } ?: return
        val answer = state.answer.takeIf { it.isNotBlank() } ?: return

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
                    submittedQuestion = question,
                    isLoading = true,
                    answer = "",
                    error = null
                )
            }

            try {
                val answer = repository.explain(question)

                _uiState.update {
                    it.copy(answer = answer)
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
