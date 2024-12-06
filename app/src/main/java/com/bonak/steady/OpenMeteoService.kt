package com.bonak.steady

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "precipitation_probability",
        @Query("timezone") timezone: String = "Asia/Tokyo"
    ): OpenMeteoResponse
}