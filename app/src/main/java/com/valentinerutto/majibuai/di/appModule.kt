package com.valentinerutto.majibuai.di

import com.valentinerutto.majibuai.BuildConfig
import com.valentinerutto.majibuai.data.ai.AiRepository
import com.valentinerutto.majibuai.data.ai.firebase.FirebaseGeminiAIEngine
import com.valentinerutto.majibuai.data.ai.firebase.FirebaseGeminiEngine
import com.valentinerutto.majibuai.data.ai.ondevice.LiteRTLmEngine
import com.valentinerutto.majibuai.data.ai.ondevice.OnDeviceModelManager
import com.valentinerutto.majibuai.data.ai.openrouter.OpenRouterApi
import com.valentinerutto.majibuai.data.ai.openrouter.OpenRouterEngine
import com.valentinerutto.majibuai.network.RetrofitClient
import com.valentinerutto.majibuai.ui.MajibuViewModel
import com.valentinerutto.majibuai.util.NetworkMonitor
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.scope.get
import org.koin.dsl.module
import retrofit2.Retrofit
import kotlin.coroutines.EmptyCoroutineContext.get

val appModule = module {
    single<FirebaseGeminiAIEngine> { FirebaseGeminiEngine() }
    single { OpenRouterEngine(get(),get()) }
    single {
        OnDeviceModelManager(
            context = androidContext(),
            modelUrl = "https://pub-49d1704b74144aaabcb5e4e6a63132c5.r2.dev/gemma3-1b-it-int4.litertlm"
        )
    }

    single {
        LiteRTLmEngine(modelManager = get())
    }
    single { NetworkMonitor(androidContext()) }
    single { AiRepository(get(),get(),get(),get()) }



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

