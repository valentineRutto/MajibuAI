package com.valentinerutto.majibuai.network

import android.util.Log
import com.valentinerutto.majibuai.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {

    const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/"

    fun provideRetrofit(baseUrl: String, okHttpClient: OkHttpClient): Retrofit {

        "application/json".toMediaType()
        return Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }


    fun provideOPenRouterAIOkHttpClient(): OkHttpClient {
        val apiKey = BuildConfig.OPENROUTER_API_KEY.trim()

        require(apiKey.isNotBlank()) {
            "OpenRouter API key is missing"
        }
        return OkHttpClient.Builder()

            .addInterceptor { chain ->

                   val request = chain.request().newBuilder()
                        .header(
                            "Authorization",
                            "Bearer ${apiKey}"
                        )
                        .header("Content-Type", "application/json")

                        .build()
                val response = chain.proceed(request)
                response
            }
            .addInterceptor(createLoggingInterceptor())

            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }


}
