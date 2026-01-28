package com.behealthy.app.core.repository

import com.behealthy.app.core.database.entity.DailyActivityEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class SportsData(
    val steps: Int,
    val distanceMeters: Int,
    val calories: Int,
    val durationMinutes: Int
)

@Singleton
class SportsDataRepository @Inject constructor(
    private val dailyActivityRepository: DailyActivityRepository
) {
    // Expose data for TODAY from local database (synced via Worker)
    val currentSportsData: Flow<SportsData> = dailyActivityRepository.getDailyActivity(
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ).map { entity ->
        entity?.let {
            SportsData(
                steps = it.steps,
                distanceMeters = it.distanceMeters,
                calories = it.calories,
                durationMinutes = it.durationMinutes
            )
        } ?: SportsData(0, 0, 0, 0)
    }
}
