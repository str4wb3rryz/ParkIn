package hr.foi.rampu.parkin.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsService {
    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String,
        @Query("key") apiKey: String
    ): Call<DirectionsApiResponse>
}

data class DirectionsApiResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<Leg>,
    val overview_polyline: Polyline
)

data class Leg(
    val start_address: String,
    val end_address: String,
    val distance: Distance,
    val duration: Duration
)

data class Distance(
    val text: String
)

data class Duration(
    val text: String
)

data class Polyline(
    var points: String
) {
    fun delete() {
        points = ""
    }
}

