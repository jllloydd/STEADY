package com.bonak.steady

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.OkHttpClient

interface NominatimService {
    @GET("search")
    suspend fun searchLocations(
        @Query("q") query: String,
        @Query("format") format: String = "json"
    ): List<LocationSuggestion>
}

data class LocationSuggestion(
    val display_name: String,
    val lat: String,
    val lon: String
)

object NominatimApi {
    val service: NominatimService by lazy {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "YourAppName/1.0 (your.email@example.com)")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(NominatimService::class.java)
    }
}