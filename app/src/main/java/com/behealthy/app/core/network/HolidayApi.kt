package com.behealthy.app.core.network

import retrofit2.http.GET
import retrofit2.http.Path

interface HolidayApi {
    @GET("api/holiday/year/{year}")
    suspend fun getHolidays(@Path("year") year: Int): HolidayResponse
}

data class HolidayResponse(
    val code: Int,
    val holiday: Map<String, HolidayDetail>
)

data class HolidayDetail(
    val holiday: Boolean,
    val name: String,
    val wage: Int,
    val date: String,
    val rest: Int? = null
)
