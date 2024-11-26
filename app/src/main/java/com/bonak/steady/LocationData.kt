package com.bonak.steady

import org.osmdroid.util.GeoPoint

data class LocationData(val name: String, val geoPoint: GeoPoint)


val shelterLocations = listOf(
    LocationData("Benguet Provincial Evacuation Center", GeoPoint(16.455663207729323, 120.57064059493023)),
    LocationData("Itogon Evacuation Center", GeoPoint(16.38864906388607, 120.66801626548097)),
    LocationData("MDRRMC Training and Evacuation Center", GeoPoint(16.453220844515716, 120.57004689493021)),
    LocationData("Cordillera Disaster Response and Development Services (CDRDS)", GeoPoint(16.419332865847824, 120.58452190658619)),
    LocationData("City Disaster Risk Reduction and Management Office", GeoPoint(16.40953951504398, 120.58864596411141)),
    LocationData("Kabayan Evacuation Center", GeoPoint(16.629297063969673, 120.8313902789971)),
    LocationData("Loakan Proper Barangay Hall", GeoPoint(16.37685729158721, 120.61353215331293))
)

val safeLocations = listOf(
    LocationData("Safe 1", GeoPoint(48.8606, 2.3376)),
    LocationData("Safe 2", GeoPoint(48.8625, 2.2875))
)

val dangerLocations = listOf(
    LocationData("Danger 1", GeoPoint(48.8530, 2.3499)),
    LocationData("Danger 2", GeoPoint(48.8510, 2.3560))
)