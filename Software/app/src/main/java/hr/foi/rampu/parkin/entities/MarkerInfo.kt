package hr.foi.rampu.parkin.entities

import com.google.android.gms.maps.model.LatLng

data class MarkerInfo(
    val id: String? = null,
    val coordinates: LatLng,
    val name: String = "",
    val description: String = "",
    val type: String = "",
    val parkingSpots: String = "",
    val parkingSpotsS: String = "",
    val parkingSpotsR: String = "",
    val parkingZone: String = "",
    val parkingPrice: String = "",
    val FullTime: String = "",
    val userSelectedImages: List<String> = emptyList()
) {
    constructor() : this(
        null,
        LatLng(0.0,0.0),
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        emptyList()
    )
}
data class MyLatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
){
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}