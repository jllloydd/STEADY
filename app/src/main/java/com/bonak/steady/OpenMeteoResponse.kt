package com.bonak.steady

data class OpenMeteoResponse(
    val hourly: Hourly
)

data class Hourly(
    val time: List<String>,
    val precipitation_probability: List<Int>
)