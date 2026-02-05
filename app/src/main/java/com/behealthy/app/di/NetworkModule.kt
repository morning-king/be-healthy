package com.behealthy.app.di

import com.behealthy.app.core.network.WeatherCnApi
import com.behealthy.app.core.network.HolidayTimelessApi
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

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherCnApi(okHttpClient: OkHttpClient): WeatherCnApi {
        return Retrofit.Builder()
            .baseUrl("http://www.weather.com.cn/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherCnApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHolidayTimelessApi(okHttpClient: OkHttpClient): HolidayTimelessApi {
        return Retrofit.Builder()
            .baseUrl("https://timelessq.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HolidayTimelessApi::class.java)
    }
}
