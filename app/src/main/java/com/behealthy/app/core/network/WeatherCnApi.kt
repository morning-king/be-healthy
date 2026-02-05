package com.behealthy.app.core.network

import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherCnApi {
    @GET("data/sk/{cityCode}.html")
    suspend fun getWeather(@Path("cityCode") cityCode: String): WeatherCnResponse
}

data class WeatherCnResponse(
    val weatherinfo: WeatherCnInfo
)

data class WeatherCnInfo(
    val city: String,
    val cityid: String,
    val temp: String,
    val WD: String, // Wind Direction
    val WS: String, // Wind Scale
    val SD: String, // Humidity
    val time: String
)
