package com.valentinerutto.majibuai.di

import com.valentinerutto.majibuai.data.ai.AiRepository
import com.valentinerutto.majibuai.data.ai.FirebaseGeminiAIEngine
import com.valentinerutto.majibuai.data.ai.FirebaseGeminiEngine
import com.valentinerutto.majibuai.data.ai.openrouter.OpenRouterApi
import com.valentinerutto.majibuai.data.ai.openrouter.OpenRouterEngine
import com.valentinerutto.majibuai.network.RetrofitClient
import com.valentinerutto.majibuai.ui.MajibuViewModel
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val appModule = module {
    single<FirebaseGeminiAIEngine> { FirebaseGeminiEngine() }
    single { OpenRouterEngine(get()) }
    single { AiRepository(get(),get()) }
    viewModel { MajibuViewModel(get()) }

    single {
        RetrofitClient.provideOPenRouterAIOkHttpClient()
    }
    single { Json { ignoreUnknownKeys = true } }
    single {
        RetrofitClient.provideRetrofit(
            RetrofitClient.OPENROUTER_BASE_URL,
            get(),
            get(),
        )
    }

    single<OpenRouterApi> {
        get<Retrofit>().create(
            OpenRouterApi::class.java
        )
    }

}

