package com.valentinerutto.majibuai.data.ai.openrouter

import retrofit2.http.POST

class OpenRouterApi {

@POST("/api/v1/chat/completions")
suspend fun chatCompletions() {
}
}