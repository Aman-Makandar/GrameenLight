package com.grameenlight.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grameenlight.domain.usecase.AskAssistantUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val askAssistantUseCase: AskAssistantUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage(text = "Namaste! I am Grameen Assist. How can I help you today?", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, isUser = true)
        _messages.value = _messages.value + userMessage

        _isLoading.value = true

        viewModelScope.launch {
            val result = askAssistantUseCase(text)
            _isLoading.value = false
            
            if (result.isSuccess) {
                val botMessage = ChatMessage(text = result.getOrNull() ?: "", isUser = false)
                _messages.value = _messages.value + botMessage
            } else {
                val exception = result.exceptionOrNull()
                val errorMsg = exception?.message ?: ""
                val displayText = if (errorMsg.contains("API_KEY", ignoreCase = true) || errorMsg.contains("API key", ignoreCase = true)) {
                    "Missing Gemini API Key. Please configure it in your environment."
                } else {
                    "Assistant unavailable, try again. ($errorMsg)"
                }
                
                val errorMessage = ChatMessage(
                    text = displayText, 
                    isUser = false, 
                    isError = true
                )
                _messages.value = _messages.value + errorMessage
            }
        }
    }
}
