package com.valentinerutto.majibuai.data.ai.openrouter

class OpenRouterApi {

@POST("https://openrouter.ai/api/v1/chat/completions")
suspend fun chatCompletions(
    @Body request: ChatCompletionsRequest
): ChatCompletionsResponse
}