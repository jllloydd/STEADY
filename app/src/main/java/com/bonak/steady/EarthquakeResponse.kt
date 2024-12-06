package com.bonak.steady

data class EarthquakeResponse(
    val features: List<Feature>
)

data class Feature(
    val properties: Properties,
    val geometry: Geometry
)

data class Geometry(
    val coordinates: List<Double>
)

data class Properties(
    val mag: Double,
    val time: Long,
    val place: String
)