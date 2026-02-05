package com.behealthy.app.core.repository

import com.behealthy.app.core.network.WeatherCnApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class WeatherInfo(
    val condition: WeatherCondition,
    val temperature: Int,
    val location: String = "武汉",
    val humidity: String = "",
    val wind: String = ""
)

enum class WeatherCondition {
    Sunny, Cloudy, Rainy, Snowy, Overcast, Unknown
}

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherCnApi
) {

    // Wuhan City Code
    private val cityCode = "101200101"

    // Simple In-Memory Cache
    private var cachedWeather: WeatherInfo? = null
    private var lastFetchTime: Long = 0
    private val CACHE_DURATION = 60 * 60 * 1000L // 1 Hour

    fun getWeatherForDate(date: LocalDate): Flow<WeatherInfo> = flow {
        // Only fetch real data for today
        if (date == LocalDate.now()) {
            val currentTime = System.currentTimeMillis()
            if (cachedWeather != null && (currentTime - lastFetchTime) < CACHE_DURATION) {
                emit(cachedWeather!!)
                return@flow
            }

            try {
                val response = weatherApi.getWeather(cityCode)
                val info = response.weatherinfo
                
                // SK API provides: temp, WD, WS, SD
                // It does NOT provide condition text (Sunny/Rainy).
                // We default to Unknown or Cloudy, but we display the rich data we have (Wind, Humidity).
                val weather = WeatherInfo(
                    condition = WeatherCondition.Unknown, // SK API limitation
                    temperature = info.temp.toDoubleOrNull()?.toInt() ?: 0,
                    location = info.city,
                    humidity = info.SD,
                    wind = "${info.WD} ${info.WS}"
                )
                
                cachedWeather = weather
                lastFetchTime = currentTime
                emit(weather)
            } catch (e: Exception) {
                e.printStackTrace()
                // Emit cached if available even if expired, on error
                cachedWeather?.let { emit(it) }
            }
        } else {
            // Future/Past: Simulation
            // We use the same simulation logic as before for consistency in the Calendar Grid
            val simulated = generateSimulatedWeather(date.year, date.monthValue)
            simulated[date]?.let { emit(it) }
        }
    }
    
    suspend fun getWeatherForMonth(year: Int, month: Int): Map<LocalDate, WeatherInfo> {
        // Always simulate for monthly grid because SK API doesn't support forecast
        return generateSimulatedWeather(year, month)
    }

    private fun generateSimulatedWeather(year: Int, month: Int): Map<LocalDate, WeatherInfo> {
        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        val map = mutableMapOf<LocalDate, WeatherInfo>()
        
        var date = start
        while (!date.isAfter(end)) {
            val seed = date.toEpochDay()
            // Random-ish but deterministic weather based on season and date
            val isSummer = month in 6..8
            val isWinter = month in 12..2
            
            val tempBase = when(month) {
                1 -> 5; 2 -> 8; 3 -> 12; 4 -> 18; 5 -> 24; 6 -> 28
                7 -> 32; 8 -> 31; 9 -> 26; 10 -> 20; 11 -> 14; 12 -> 8
                else -> 15
            }
            
            val tempVariation = (seed % 7).toInt() - 3 // -3 to +3
            val temperature = tempBase + tempVariation
            
            val conditionCode = (seed % 10).toInt()
            val condition = when {
                isSummer && conditionCode < 3 -> WeatherCondition.Rainy
                isWinter && conditionCode < 2 -> WeatherCondition.Snowy
                conditionCode < 5 -> WeatherCondition.Sunny
                conditionCode < 8 -> WeatherCondition.Cloudy
                else -> WeatherCondition.Overcast
            }
            
            map[date] = WeatherInfo(condition, temperature)
            date = date.plusDays(1)
        }
        return map
    }
}
