package com.bonak.steady

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "http://router.project-osrm.org/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val osrmApiService: OsrmApiService = retrofit.create(OsrmApiService::class.java)
}