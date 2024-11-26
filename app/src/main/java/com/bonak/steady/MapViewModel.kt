package com.bonak.steady

import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint

class MapViewModel : ViewModel() {
    var mapCenter: GeoPoint? = null
    var mapZoomLevel: Double = 9.0
}