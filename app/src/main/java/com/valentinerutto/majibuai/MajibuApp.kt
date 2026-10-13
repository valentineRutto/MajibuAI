package com.valentinerutto.majibuai

import android.app.Application
import com.valentinerutto.majibuai.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MajibuApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MajibuApp)
            modules(appModule)
        }
    }
}