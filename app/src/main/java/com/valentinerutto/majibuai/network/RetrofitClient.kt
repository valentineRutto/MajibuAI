package com.valentinerutto.majibuai.network

import com.valentinerutto.majibuai.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {

    const val OPENROUTER_BASE_URL = "https://openrouter.ai/"

    fun provideRetrofit(baseUrl: String, okHttpClient: OkHttpClient): Retrofit {

        "application/json".toMediaType()
        return Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    fun createOkClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(createLoggingInterceptor())
            .build()
    }

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }


    fun provideOPenRouterAIOkHttpClient(): OkHttpClient {

        return OkHttpClient.Builder()
            .addInterceptor(createLoggingInterceptor())
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .addHeader(
                            "Authorization",
                            "Bearer ${BuildConfig.OPENROUTER_API_KEY}"
                        )
                        .build()
                )
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }


}