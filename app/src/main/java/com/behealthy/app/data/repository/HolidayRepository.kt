package com.behealthy.app.data.repository

import com.behealthy.app.core.database.dao.HolidayDao
import com.behealthy.app.core.database.entity.HolidayEntity
import com.behealthy.app.core.network.HolidayTimelessApi
import com.behealthy.app.core.network.HolidayDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayRepository @Inject constructor(
    private val holidayApi: HolidayTimelessApi,
    private val holidayDao: HolidayDao
) {
    // Memory Cache
    private val holidayCache = ConcurrentHashMap<Int, Map<String, HolidayDetail>>()

    suspend fun getHolidaysForYear(year: Int): Map<String, HolidayDetail> {
        // 1. Check Memory Cache
        if (holidayCache.containsKey(year)) {
            return holidayCache[year]!!
        }

        // 2. Demo Logic for 2026 (Future)
        if (year == 2026) {
            val mockData = getMock2026Holidays()
            holidayCache[year] = mockData
            return mockData
        }

        return withContext(Dispatchers.IO) {
            // 3. Check Database Cache
            val dbHolidays = holidayDao.getHolidaysForYear(year.toString())
            if (dbHolidays.isNotEmpty()) {
                val map = dbHolidays.associate { entity ->
                    // Extract "MM-DD" from "YYYY-MM-DD"
                    val key = entity.date.substring(5)
                    key to HolidayDetail(entity.holiday, entity.name, entity.wage, entity.date)
                }
                holidayCache[year] = map
                return@withContext map
            }

            // 4. Fetch from API (Loop 12 months)
            val apiHolidays = mutableListOf<HolidayEntity>()
            val resultMap = mutableMapOf<String, HolidayDetail>()
            
            try {
                for (month in 1..12) {
                    val monthStr = month.toString() // API handles "1" or "01" usually, let's try "1"
                    val response = holidayApi.getHolidays(year.toString(), monthStr)
                    
                    if (response.code == 0 && !response.holiday.isNullOrEmpty()) {
                        response.holiday.forEach { (dateKey, item) ->
                            // dateKey might be "2024-01-01" or "01-01". Usually full date in response map?
                            // Let's assume item.date is full date.
                            val fullDate = item.date
                            val key = fullDate.substring(5) // MM-DD
                            
                            val entity = HolidayEntity(
                                date = fullDate,
                                name = item.name,
                                type = if (item.holiday) 1 else 2, // Simplified mapping
                                wage = item.wage,
                                holiday = item.holiday
                            )
                            apiHolidays.add(entity)
                            resultMap[key] = HolidayDetail(item.holiday, item.name, item.wage, fullDate)
                        }
                    }
                }
                
                // 5. Save to Database
                if (apiHolidays.isNotEmpty()) {
                    holidayDao.insertHolidays(apiHolidays)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            holidayCache[year] = resultMap
            resultMap
        }
    }

    private fun getMock2026Holidays(): Map<String, HolidayDetail> {
        val map = mutableMapOf<String, HolidayDetail>()
        
        // --- 2026 Simulated Holidays ---
        
        // Jan: New Year (Jan 1)
        map["01-01"] = HolidayDetail(true, "元旦", 3, "2026-01-01")
        
        // Feb 14: Work (Sat)
        map["02-14"] = HolidayDetail(false, "补班", 1, "2026-02-14")
        
        // Feb 15-23: Rest (Spring Festival)
        val springFestival = listOf("02-15", "02-16", "02-17", "02-18", "02-19", "02-20", "02-21", "02-22", "02-23")
        springFestival.forEach { date -> map[date] = HolidayDetail(true, "春节", 3, "2026-$date") }
        
        // Feb 28: Work (Sat)
        map["02-28"] = HolidayDetail(false, "补班", 1, "2026-02-28")
        
        // Apr: Qingming
        listOf("04-04", "04-05", "04-06").forEach { map[it] = HolidayDetail(true, "清明", 3, "2026-$it") }
        
        // May: Labor Day
        listOf("05-01", "05-02", "05-03", "05-04", "05-05").forEach { map[it] = HolidayDetail(true, "劳动节", 3, "2026-$it") }
        
        // Jun: Dragon Boat
        listOf("06-19", "06-20", "06-21").forEach { map[it] = HolidayDetail(true, "端午", 3, "2026-$it") }
        
        // Sep: Mid-Autumn
        listOf("09-25", "09-26", "09-27").forEach { map[it] = HolidayDetail(true, "中秋", 3, "2026-$it") }
        
        // Oct: National Day
        listOf("10-01", "10-02", "10-03", "10-04", "10-05", "10-06", "10-07").forEach { map[it] = HolidayDetail(true, "国庆", 3, "2026-$it") }
        
        return map
    }
}
