package com.unistay.android.di

import com.unistay.android.data.remote.api.UniStayApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Domain máy chủ đám mây MonsterASP
    private const val BASE_URL = "http://tro24h.runasp.net/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Giúp log toàn bộ lỗi gọi API ra Logcat
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Tự động convert JSON thành Kotlin Object
            .build()
    }

    @Provides
    @Singleton
    fun provideUniStayApi(retrofit: Retrofit): UniStayApi {
        return retrofit.create(UniStayApi::class.java)
    }
}