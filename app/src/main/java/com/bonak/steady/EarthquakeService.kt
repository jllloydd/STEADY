package com.bonak.steady

import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


// Add this helper function
private fun getLastMonthDate(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -3)  // Go back 3 months instead of 1
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
}


// Add interface for USGS API
interface EarthquakeService {
    @GET("query")
    suspend fun getEarthquakes(
        @Query("format") format: String = "geojson",
        @Query("latitude") latitude: Double = 16.4023,  // Baguio City coordinates
        @Query("longitude") longitude: Double = 120.5960,
        @Query("maxradiuskm") radius: Int = 150,  // Focus on CAR region
        @Query("starttime") startTime: String = getLastMonthDate(),
        @Query("minmagnitude") minMagnitude: Double = 2.0,  // Lowered to show more earthquakes
        @Query("orderby") orderBy: String = "time",  // Order by most recent
        @Query("limit") limit: Int = 50  // Show more results
    ): EarthquakeResponse
}