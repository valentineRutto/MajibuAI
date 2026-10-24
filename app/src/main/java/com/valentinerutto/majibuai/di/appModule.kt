package com.valentinerutto.majibuai.di

import com.valentinerutto.majibuai.data.ai.AiRepository
import com.valentinerutto.majibuai.data.ai.FirebaseGeminiAIEngine
import com.valentinerutto.majibuai.data.ai.FirebaseGeminiEngine
import com.valentinerutto.majibuai.data.ai.openrouter.OpenRouterApi
import com.valentinerutto.majibuai.ui.MajibuViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single<FirebaseGeminiAIEngine> { FirebaseGeminiEngine() }
    single { AiRepository(get()) }
    viewModel { MajibuViewModel(get()) }

    single {
        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    single<OpenRouterApi> {
        get<Retrofit>().create(
            OpenRouterApi::class.java
        )
    }
}
