package hr.foi.rampu.parkin.services

import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class DirectionsFetcher(private val retrofit: Retrofit) {
    private val directionsApi = retrofit.create(DirectionsService::class.java)

    fun fetchDirections(origin: LatLng, destination: LatLng, apiKey: String, onResult: (Result<DirectionsApiResponse>) -> Unit) {
        val call = directionsApi.getDirections(
            "${origin.latitude},${origin.longitude}",
            "${destination.latitude},${destination.longitude}",
            "driving",
            apiKey
        )

        call.enqueue(object : Callback<DirectionsApiResponse> {
            override fun onResponse(call: Call<DirectionsApiResponse>, response: Response<DirectionsApiResponse>) {
                if (response.isSuccessful) {
                    onResult(Result.success(response.body()!!))
                } else {
                    onResult(Result.failure(RuntimeException("Response not successful")))
                }
            }

            override fun onFailure(call: Call<DirectionsApiResponse>, t: Throwable) {
                onResult(Result.failure(t))
            }
        })
    }
}