package com.valentinerutto.majibuai.di

import com.valentinerutto.majibuai.data.AiRepository
import com.valentinerutto.majibuai.ui.MajibuViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AiRepository() }
    viewModel { MajibuViewModel(get()) }
}
