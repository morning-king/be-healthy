package com.behealthy.app.core.network

import retrofit2.http.GET
import retrofit2.http.Path

interface HolidayTimelessApi {
    @GET("api/holiday/{year}/{month}")
    suspend fun getHolidays(@Path("year") year: String, @Path("month") month: String): HolidayTimelessResponse
}

data class HolidayTimelessResponse(
    val code: Int,
    val holiday: Map<String, HolidayItem>?
)

data class HolidayItem(
    val holiday: Boolean,
    val name: String,
    val wage: Int,
    val date: String,
    val rest: Int? = null // Some APIs use 'rest'
)
