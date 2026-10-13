package com.valentinerutto.majibuai.data

import kotlinx.coroutines.delay

class AiRepository() {

    suspend fun explain(question: String): String {

        delay(800)

        return """
            Here's a simple explanation of "$question".

            This is a simulated response.

            In the next codelab step, we'll
            connect MajibuAI to a real AI model.
        """.trimIndent()
    }
}