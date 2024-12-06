package com.bonak.steady

import org.osmdroid.util.GeoPoint

data class LocationData(val name: String, val geoPoint: GeoPoint, val address: String)


val shelterLocations = listOf(
    LocationData("Benguet Provincial Evacuation Center", GeoPoint(16.455663207729323, 120.57064059493023), "FH4C+772, La Trinidad, Benguet"),
    LocationData("Itogon Evacuation Center", GeoPoint(16.38864906388607, 120.66801626548097), "9MQ9+F62, Itogon, Benguet"),
    LocationData("MDRRMC Training and Evacuation Center", GeoPoint(16.453220844515716, 120.57004689493021), "FH3C+72M, Wangal-Motorpool Rd, La Trinidad, Benguet"),
    LocationData("Cordillera Disaster Response and Development Services (CDRDS)", GeoPoint(16.419332865847824, 120.58452190658619), "311 Ferguson Rd, Baguio, Benguet"),
    LocationData("City Disaster Risk Reduction and Management Office", GeoPoint(16.40953951504398, 120.58864596411141), "CH5Q+RF2, Jose Felipe St, Baguio, Benguet"),
    LocationData("Kabayan Evacuation Center", GeoPoint(16.629297063969673, 120.8313902789971), "JRHJ+PG, Kabayan, Benguet"),
    LocationData("Loakan Proper Barangay Hall", GeoPoint(16.37685729158721, 120.61353215331293), "29 Purok Bubon, Laoakan, Baguio, 2600 Benguet")
)

val hospitalLocations = listOf(
    LocationData("Baguio General Hospital and Medical Center", GeoPoint(16.40115753517385, 120.59569601910988), "Gov. Pack Rd, Baguio, 2600 Benguet"),
    LocationData("Benguet General Hospital", GeoPoint(16.450759939478445, 120.58914362376635), "FH2Q+8M2, Halsema Highway, La Trinidad, 2601 Benguet"),
    LocationData("Saint Louis University Sacred Heart Medical Center", GeoPoint(16.417345575728888, 120.59793552376568), "CH8X+W5M, Assumption Rd, Baguio, 2600 Benguet"),
    LocationData("Cordillera Hospital of the Divine Grace (CHDG)", GeoPoint(16.45255511567883, 120.57440253725771), "FH3F+2QM, La Trinidad, 2601 Benguet"),
    LocationData("Notre Dame de Chartres Hospital", GeoPoint(16.415262932584664, 120.59830250698016), "25 Gen. Luna Rd, Baguio, 2600 Benguet"),
    LocationData("Fort Del Pilar Station Hospital", GeoPoint(16.365276066838998, 120.62002825260086), "9J79+QQ4 PMA, Baguio, 2600 Benguet"),
    LocationData("Baguio Medical Center", GeoPoint(16.402134017876868, 120.59760806903105), "Kennon Rd, Baguio, Benguet"),
    LocationData("Pines City Doctor's Hospital", GeoPoint(16.426954438802454, 120.59454272376597), "CHGV+QR7, Magsaysay Ave, Baguio, Benguet"),
    LocationData("Loakan District Health Center", GeoPoint(16.376818019855047, 120.6139691526011), "9JG7+MHP, Loakan Rd, Baguio, Benguet"),
    LocationData("Cabato Medical Clinic", GeoPoint(16.41716432129306, 120.59665518143824), "Barangay, Rillera Building, Dagohoy St, Baguio, 2600 Benguet"),
    LocationData("Kapangan District Hospital", GeoPoint(16.574894261815526, 120.59261562376922), "161 Central, Kapangan, 2613 Benguet"),
    LocationData("Camp Dangwa Hospital", GeoPoint(16.465173938173102, 120.59872622857165), "Camp Dangwa Community, La Trinidad, Benguet"),
    LocationData("Baguio Central Triage", GeoPoint(16.404682305107723, 120.60009952376544), "CJ32+R2P, Convention Center Rd, Baguio, Benguet"),
    LocationData("Northern Benguet District Hospital", GeoPoint(16.787259994554375, 120.81623348629078), "QRP8+RFP, Halsema Highway, Buguias, Benguet"),
    LocationData("Atok Sub District Health Center", GeoPoint(16.42657352089492, 120.55044233725701), "Sayangan, Paoay, Atok, Benguet 2612")
)