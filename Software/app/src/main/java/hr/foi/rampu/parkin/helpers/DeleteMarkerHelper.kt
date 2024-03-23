package hr.foi.rampu.parkin.helpers

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
class DeleteMarkerHelper {
        companion object {
            private val databaseMarkers: DatabaseReference = FirebaseDatabase.getInstance().getReference("Markers")
            private var firebaseMapDataHandler: FirebaseMapDataHandler = FirebaseMapDataHandler(databaseMarkers)

            fun DeleteMarkerFromDB(marker: LatLng) {
                Log.e("addMarkerToFavoritesHandler", marker.toString())
                Log.e("lat", marker.latitude.toString())
                Log.e("lon", marker.longitude.toString())

                firebaseMapDataHandler.retrieveMarkerByCoordinates(marker, { markerInfo ->
                    if (markerInfo != null) {
                        Log.e("MarkerInfo", "Marker found: $markerInfo")
                        firebaseMapDataHandler.deleteMarker(markerInfo)
                    } else {
                        Log.e("MarkerNotFound", "No marker found at the provided coordinates")
                    }
                }, { exception ->
                    Log.e("DatabaseError", "Error retrieving marker: $exception")
                })
            }
        }
}
