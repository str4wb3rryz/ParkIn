package hr.foi.rampu.parkin.helpers

import hr.foi.rampu.parkin.helpers.FirebaseMapDataHandler
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.foi.rampu.parkin.entities.MarkerInfo
import hr.foi.rampu.parkin.entities.MyLatLng
import hr.foi.rampu.parkin.entities.UserSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.asTask

class FavoriteHandler {
    companion object {
        private val userId = UserSession.getInstance().currentUser?.id
        private val databaseMarkers: DatabaseReference = FirebaseDatabase.getInstance().getReference("Markers")
        private val databaseUser: DatabaseReference = FirebaseDatabase.getInstance().getReference("users/${userId}")
        private val databaseFavorites: DatabaseReference = FirebaseDatabase.getInstance().getReference("users/$userId/favorites")
        private var firebaseMapDataHandler: FirebaseMapDataHandler = FirebaseMapDataHandler(databaseMarkers)

        fun addMarkerToFavorites(marker: LatLng, user: String) {
            Log.e("addMarkerToFavoritesHandler", marker.toString())
            Log.e("addMarkerToFavoritesHandler", user)
            Log.e("lat", marker.latitude.toString())
            Log.e("lon", marker.longitude.toString())

            firebaseMapDataHandler.retrieveMarkerByCoordinates(marker, { markerInfo ->
                if (markerInfo != null) {
                    Log.e("MarkerInfo", "Marker found: $markerInfo")
                    databaseUser.child("favorites").child(markerInfo.id!!).setValue(markerInfo)
                } else {
                    Log.e("MarkerNotFound", "No marker found at the provided coordinates")
                }
            }, { exception ->
                Log.e("DatabaseError", "Error retrieving marker: $exception")
            })
        }

        fun getUserFavorites(userId: String, callback: (List<MarkerInfo>) -> Unit) {
            databaseFavorites.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorites = mutableListOf<MarkerInfo>()
                    snapshot.children.forEach { child ->
                        val latitude = child.child("coordinates/latitude").getValue(Double::class.java) ?: return@forEach
                        val longitude = child.child("coordinates/longitude").getValue(Double::class.java) ?: return@forEach
                        val coordinates = LatLng(latitude, longitude)
                            val name = child.child("name").getValue(String::class.java)
                            val description = child.child("description").getValue(String::class.java)
                            val type = child.child("type").getValue(String::class.java)
                            val parkingPrice = child.child("parkingPrice").getValue(String::class.java)
                            val parkingSpots = child.child("parkingSpots").getValue(String::class.java)
                            val parkingSpotsR = child.child("parkingSpotsR").getValue(String::class.java)
                            val parkingSpotsS = child.child("parkingSpotsS").getValue(String::class.java)
                            val parkingZone = child.child("parkingZone").getValue(String::class.java)
                            val FullTime = child.child("fullTime").getValue(String::class.java)
                            val favorite = MarkerInfo(
                                id = child.key,
                                coordinates = coordinates,
                                name = name.toString(),
                                type = type.toString(),
                                description = description.toString(),
                                parkingPrice = parkingPrice.toString(),
                                FullTime = FullTime.toString(),
                                parkingZone = parkingZone.toString(),
                                parkingSpots = parkingSpots.toString(),
                                parkingSpotsS = parkingSpotsS.toString(),
                                parkingSpotsR = parkingSpotsR.toString(),
                            )
                            favorites.add(favorite)

                    }
                    callback(favorites)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseError", error.toString())
                    callback(emptyList())
                }
            })
        }

        fun removeMarkerFromFavorites(marker: LatLng, user: String) {
            Log.e("addMarkerToFavoritesHandler", marker.toString())
            Log.e("addMarkerToFavoritesHandler", user)
            Log.e("lat", marker.latitude.toString())
            Log.e("lon", marker.longitude.toString())

            firebaseMapDataHandler.retrieveMarkerByCoordinates(marker, { markerInfo ->
                if (markerInfo != null) {
                    Log.e("MarkerInfo", "Marker found: $markerInfo")
                    //databaseUser.child("favorites").child(markerInfo.id!!).setValue(markerInfo)
                    databaseUser.child("favorites").child(markerInfo.id!!).removeValue()
                    Log.e("MarkerInfo", "Marker removed from favorites")
                } else {
                    Log.e("MarkerNotFound", "No marker found at the provided coordinates")
                }
            }, { exception ->
                Log.e("DatabaseError", "Error retrieving marker: $exception")
            })
        }


    }
}