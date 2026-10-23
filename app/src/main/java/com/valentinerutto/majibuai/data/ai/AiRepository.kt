package com.valentinerutto.majibuai.data.ai

import kotlinx.coroutines.delay

class AiRepository() {

    suspend fun explain(question: String): String {

        delay(800)

        return when {
            question.contains("flow", ignoreCase = true) ->
                "Kotlin Flow helps you handle data that " +
                        "changes over time, such as streaming AI responses."

            question.contains("compose", ignoreCase = true) ->
                "Jetpack Compose is Android's modern " +
                        "declarative UI toolkit."

            else ->
                "Great question! Here's a simple explanation " +
                        "of \"$question\". This is a demo response."
        }

    }
}