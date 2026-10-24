package com.valentinerutto.majibuai.data.ai

import kotlin.coroutines.cancellation.CancellationException

class AiRepository(private val firebaseEngine: FirebaseGeminiAIEngine) {

    suspend fun explain(messages: List<ChatMessage>): AiResult<String> {

        return try {
            val request = AiChatRequest(messages)

            val response = firebaseEngine.generate(request)

            if (response.text.isBlank()){
                AiResult.Failure(AiError.EmptyResponse)
            }else{
                AiResult.Success(response.text)
            }


        }catch (e: CancellationException){
          throw e
        }
        catch (e: Exception) {
            AiResult.Failure(e.toAiError())

        }
    }

}


