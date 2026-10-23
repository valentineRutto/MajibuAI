package com.valentinerutto.majibuai.data.ai

import kotlinx.coroutines.delay

class AiRepository(private val firebaseEngine: FirebaseGeminiAIEngine) {

    suspend fun explain(messages: List<ChatMessage>): String {
        require(messages.isNotEmpty()) { "Conversation cannot be empty." }

        return firebaseEngine
            .generate(AiChatRequest(messages))
            .text
    }

}


