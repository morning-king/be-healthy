package com.behealthy.app.core.repository

import com.behealthy.app.core.network.WeatherApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class WeatherInfo(
    val condition: WeatherCondition,
    val temperature: Int,
    val location: String = "武汉"
)

enum class WeatherCondition {
    Sunny, Cloudy, Rainy, Snowy, Overcast
}

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi
) {

    // Wuhan coordinates
    private val lat = 30.5728
    private val long = 114.2792

    fun getWeatherForDate(date: LocalDate): Flow<WeatherInfo> = flow {
        try {
            val dateStr = date.format(DateTimeFormatter.ISO_DATE)
            val response = weatherApi.getWeatherForecast(
                latitude = lat,
                longitude = long,
                startDate = dateStr,
                endDate = dateStr
            )
            
            val daily = response.daily
            if (!daily.time.isNullOrEmpty()) {
                val code = daily.weather_code.firstOrNull() ?: 0
                val max = daily.temperature_2m_max.firstOrNull() ?: 0.0
                val min = daily.temperature_2m_min.firstOrNull() ?: 0.0
                val avgTemp = ((max + min) / 2).toInt()
                
                emit(WeatherInfo(mapWmoCodeToCondition(code), avgTemp))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun getWeatherForMonth(year: Int, month: Int): Map<LocalDate, WeatherInfo> {
        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        val map = mutableMapOf<LocalDate, WeatherInfo>()
        
        try {
            val startStr = start.format(DateTimeFormatter.ISO_DATE)
            val endStr = end.format(DateTimeFormatter.ISO_DATE)
            
            val response = weatherApi.getWeatherForecast(
                latitude = lat,
                longitude = long,
                startDate = startStr,
                endDate = endStr
            )
            
            val daily = response.daily
            if (!daily.time.isNullOrEmpty()) {
                daily.time.forEachIndexed { index, timeStr ->
                    try {
                        val date = LocalDate.parse(timeStr)
                        val code = daily.weather_code.getOrNull(index) ?: 0
                        val max = daily.temperature_2m_max.getOrNull(index) ?: 0.0
                        val min = daily.temperature_2m_min.getOrNull(index) ?: 0.0
                        val avgTemp = ((max + min) / 2).toInt()
                        
                        map[date] = WeatherInfo(mapWmoCodeToCondition(code), avgTemp)
                    } catch (e: Exception) {
                        // ignore parse error
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    private fun mapWmoCodeToCondition(code: Int): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition.Sunny
            1, 2, 3 -> WeatherCondition.Cloudy
            45, 48 -> WeatherCondition.Cloudy // Fog
            51, 53, 55, 56, 57 -> WeatherCondition.Rainy // Drizzle
            61, 63, 65, 66, 67 -> WeatherCondition.Rainy // Rain
            80, 81, 82 -> WeatherCondition.Rainy // Showers
            71, 73, 75, 77 -> WeatherCondition.Snowy // Snow fall
            85, 86 -> WeatherCondition.Snowy // Snow showers
            95, 96, 99 -> WeatherCondition.Rainy // Thunderstorm
            else -> WeatherCondition.Overcast
        }
    }
}
