package hr.foi.rampu.parkin.helpers

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import hr.foi.rampu.parkin.entities.MarkerInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext


class FirebaseMapDataHandler(private var databaseReference: DatabaseReference) {

    fun addMarker(markerInfo: MarkerInfo, successCallback: () -> Unit, errorCallback: (Exception) -> Unit) {
        val id = databaseReference.push().key ?: return errorCallback(Exception("Failed to generate unique ID"))
        databaseReference.child(id).setValue(markerInfo)
            .addOnSuccessListener { successCallback() }
            .addOnFailureListener { exception -> errorCallback(exception) }
    }

    fun retrieveMarkers(callback: (List<MarkerInfo>) -> Unit, errorCallback: (Exception) -> Unit) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                val markers = mutableListOf<MarkerInfo>()

                snapshot.children.forEach { child ->

                    Log.d("retrivMarkerFire","${child}")

                    val id = child.key
                    val name = child.child("name").getValue(String::class.java)
                    val description = child.child("description").getValue(String::class.java)
                    val type = child.child("type").getValue(String::class.java)
                    val parkingSpots = child.child("parkingSpots").getValue(String::class.java)
                    val parkingSpotsR = child.child("parkingSpotsR").getValue(String::class.java)
                    val parkingSpotsS = child.child("parkingSpotsS").getValue(String::class.java)
                    val parkingZone = child.child("parkingZone").getValue(String::class.java)
                    val parkingPrice = child.child("parkingPrice").getValue(String::class.java)
                    val FullTime = child.child("fullTime").getValue(String::class.java)
                    Log.d("LogMapInfo1","${id}, ${name}, ${description}")

                    //val imageUrls = child.child("userSelectedImages").getValue(String::class.java) ?: return@forEach
                    val latitude = child.child("coordinates/latitude").getValue(Double::class.java) ?: return@forEach
                    val longitude = child.child("coordinates/longitude").getValue(Double::class.java) ?: return@forEach
                    val coordinates = LatLng(latitude, longitude)
                    Log.d("LogMapInfo2","${id}, ${coordinates}, ${name}, ${description}")

                    val marker = MarkerInfo(
                        id = id,
                        coordinates = coordinates,
                        name = name.toString(),
                        description = description.toString(),
                        type = type.toString(),
                        parkingSpots = parkingSpots.toString(),
                        parkingSpotsS = parkingSpotsS.toString(),
                        parkingSpotsR = parkingSpotsR.toString(),
                        parkingZone = parkingZone.toString(),
                        parkingPrice = parkingPrice.toString(),
                        FullTime = FullTime.toString(),
                    )
                    markers.add(marker)
                    Log.d("MarkerFireMARK1","$marker")

                }
                Log.d("retrivMarkerFireMARK","$markers")

                callback(markers)
            }

            override fun onCancelled(error: DatabaseError) {
                errorCallback(Exception(error.toException()))
            }
        })
    }

    fun retrieveMarkerByCoordinates(targetCoordinates: LatLng, callback: (MarkerInfo?) -> Unit, errorCallback: (Exception) -> Unit) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    val id = child.key
                    val name = child.child("name").getValue(String::class.java)
                    val description = child.child("description").getValue(String::class.java)
                    val type = child.child("type").getValue(String::class.java)
                    val parkingPrice = child.child("parkingPrice").getValue(String::class.java)
                    val parkingSpots = child.child("parkingSpots").getValue(String::class.java)
                    val parkingSpotsR = child.child("parkingSpotsR").getValue(String::class.java)
                    val parkingSpotsS = child.child("parkingSpotsS").getValue(String::class.java)
                    val parkingZone = child.child("parkingZone").getValue(String::class.java)
                    val FullTime = child.child("fullTime").getValue(String::class.java)

                    val latitude = child.child("coordinates/latitude").getValue(Double::class.java) ?: return@forEach
                    val longitude = child.child("coordinates/longitude").getValue(Double::class.java) ?: return@forEach
                    val coordinates = LatLng(latitude, longitude)

                    if (coordinates == targetCoordinates) {
                        val marker = MarkerInfo(
                            id = id,
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
                        callback(marker)
                        return
                    }
                }
                callback(null)
            }

            override fun onCancelled(error: DatabaseError) {
             //   errorCallback(Exception(error.toException()))
            }
        })
    }




    suspend fun retrieveMarkersAsync(): List<MarkerInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val markers = mutableListOf<MarkerInfo>()
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { dataSnapshot ->
                            val markerInfo = dataSnapshot.getValue(MarkerInfo::class.java)
                            markerInfo?.let {
                                markers.add(it)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
                markers
            } catch (e: Exception) {
                // Handle exceptions
                throw e
            }
        }
    }


    }
    fun deleteMarker(marker: MarkerInfo, successCallback: () -> Unit, errorCallback: (Exception) -> Unit) {
        val keyFromMarker=marker.id
        val markerRef = databaseReference.child("$keyFromMarker")
        markerRef.removeValue()
            .addOnSuccessListener {
                Log.d("MarkerDeletionInfo", "key: $keyFromMarker")
                Log.d("MarkerDeletion", "Marker deleted successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("MarkerDeletion", "Error deleting marker: $exception")
            }
    }

    private fun encodeCoordinate(coordinate: String): String {
        return coordinate.replace(".", "_dot_")
    }

    fun deleteMarker(marker: MarkerInfo) {
        val keyFromMarker=marker.id
        val markerRef = databaseReference.child("$keyFromMarker")
        markerRef.removeValue()
            .addOnSuccessListener {
                Log.d("MarkerDeletionInfo", "key: $keyFromMarker")
                Log.d("MarkerDeletion", "Marker deleted successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("MarkerDeletion", "Error deleting marker: $exception")
            }
    }
}
