package com.valentinerutto.majibuai.data.ai

import kotlinx.coroutines.delay

class AiRepository(private val firebaseengine: FirebaseGeminiAIEngine) {

    suspend fun explain(question: String): String {
        val request = AiChatRequest(listOf(ChatMessage(role = ChatRole.USER, content = question)))
        val response = firebaseengine.generate(request)
        return response.text
    }

}


