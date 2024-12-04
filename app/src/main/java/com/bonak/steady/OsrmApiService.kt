package com.bonak.steady

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OsrmApiService {
    @GET("route/v1/driving/{start};{end}")
    suspend fun getRoute(
        @Path("start") start: String,
        @Path("end") end: String,
        @Query("overview") overview: String = "full"
    ): OsrmResponse
}