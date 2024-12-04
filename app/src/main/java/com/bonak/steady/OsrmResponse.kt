package com.bonak.steady

data class OsrmResponse(
    val routes: List<Route>
)

data class Route(
    val geometry: String
)