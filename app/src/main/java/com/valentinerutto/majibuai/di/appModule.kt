package com.valentinerutto.majibuai.di

import com.valentinerutto.majibuai.data.ai.AiRepository
import com.valentinerutto.majibuai.data.ai.FirebaseGeminiAIEngine
import com.valentinerutto.majibuai.data.ai.FirebaseGeminiEngine
import com.valentinerutto.majibuai.ui.MajibuViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<FirebaseGeminiAIEngine> { FirebaseGeminiEngine() }
    single { AiRepository(get()) }
    viewModel { MajibuViewModel(get()) }
}
