package com.bonak.steady

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SerpApiService {
    @GET("search")
    fun getGoogleNews(
        @Query("q") query: String,
        @Query("tbm") tbm: String = "nws", // 'nws' for news
        @Query("api_key") apiKey: String
    ): Call<SerpApiResponse>
}