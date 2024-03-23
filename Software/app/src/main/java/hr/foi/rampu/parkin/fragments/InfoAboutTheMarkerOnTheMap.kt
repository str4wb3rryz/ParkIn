package hr.foi.rampu.parkin.fragments

import hr.foi.rampu.parkin.helpers.FirebaseMapDataHandler
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import hr.foi.rampu.parkin.R
import hr.foi.rampu.parkin.entities.UserSession
import hr.foi.rampu.parkin.helpers.DeleteMarkerHelper
import hr.foi.rampu.parkin.helpers.FavoriteHandler


class InfoAboutTheMarkerOnTheMap : BottomSheetDialogFragment(){
    private lateinit var btnAddFavorite: ImageButton
    private lateinit var userSession: UserSession
    private lateinit var markerCoordinates: LatLng
    private lateinit var marker: LatLng
    private lateinit var user: String
    private val firebaseMapDataHandler: FirebaseMapDataHandler = FirebaseMapDataHandler(
        FirebaseDatabase.getInstance().getReference("Markers")
    )
    private var isPressed: Boolean = false
    private var isFavorite: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_info_about_the_marker_on_the_map,
            container,
            false
        )
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userSession = UserSession.getInstance()
        val btnDeleteMarker: FloatingActionButton = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_btn_delete_marker)

        btnAddFavorite = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_btn_add_favorite)
        val markerId = arguments?.getString("coordinates") ?: ""
        markerCoordinates = parseCoordinates(markerId)
        Log.e("markerId", marker.toString())

        btnDeleteMarker.setOnClickListener {
            DeleteMarkerButton()
        }
        val userId = userSession.currentUser?.id
        user = userId.toString()
        Log.e("userId", user)

        if(userSession.isAdmin()){
            btnDeleteMarker.visibility = View.VISIBLE
        }else{
            btnDeleteMarker.visibility = View.GONE
        }

        markerCoordinates = parseCoordinates(markerId)
        Log.e("markerId", marker.toString())

        saveButtonState(isPressed, isFavorite)

        btnAddFavorite.setOnClickListener {
            isPressed = true
            updateFavoriteButton(isPressed, isFavorite)
        }

        val titleTextView: TextView = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_tv_title)
        val coordinatesTextView: TextView = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_tv_coordinates)
        val descriptionOfParking: TextView = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_tv_description)
        val typeOfParking: TextView = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_tv_place_type_of_parking)
        val numberOfParkingSpots: TextView = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_tv_number_of_parking_spots)
        val numberOfParkingSpotsSpecial: TextView = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_tv_number_of_parking_spots_special)
        val numberOfParkingSpotsReserved: TextView = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_tv_number_of_parking_spots_reserved)

        val parkingZone: TextView = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_tv_zone_designation)
        val parkingPrice: TextView = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_tv_price)
        val parkingWorkingHours: TextView = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_tv_working_hours)
        val imageContainer: LinearLayout = view.findViewById(R.id.fragment_info_about_the_marker_on_the_map_ll_imageContainer)

        imageContainer.removeAllViews()

        val title = arguments?.getString("title") ?: ""
        val coordinates = arguments?.getString("coordinates") ?: ""
        val description = arguments?.getString("description") ?: ""
        val type = arguments?.getString("type") ?: ""
        val spotsNumber = arguments?.getString("numberOfSpots") ?: ""
        val spotsNumberSpecial = arguments?.getString("spotsSpecial") ?: ""
        val spotsNumberReserved = arguments?.getString("spotsReserved") ?: ""
        val zone = arguments?.getString("zone") ?: ""
        val price = arguments?.getString("price") ?: ""
        val workingHours = arguments?.getString("workingHours") ?: ""
        val images: List<Uri> = arguments?.getParcelableArrayList("images")?: emptyList()

        titleTextView.text = title
        coordinatesTextView.text = coordinates
        descriptionOfParking.text = description
        typeOfParking.text = type
        numberOfParkingSpots.text = spotsNumber
        numberOfParkingSpotsSpecial.text = spotsNumberSpecial
        numberOfParkingSpotsReserved.text = spotsNumberReserved
        parkingZone.text = zone
        parkingPrice.text = price
        parkingWorkingHours.text = workingHours

        if (images != null && images.isNotEmpty() && images[0].toString()!="") {
            for ((index, uri) in images.withIndex()) {
                for(uri in images){
                    if(uri.toString()=="[]") return;
                }
                val cleanedUri = removeFirstAndLastCharacters("$uri")
                val imageView = ImageView(requireContext())
                imageView.layoutParams = LinearLayout.LayoutParams(
                    500,
                    500
                )
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                Picasso.get()
                    .load(cleanedUri)
                    .error(R.drawable.ic_baseline_map_24)
                    .into(imageView)

                imageContainer.addView(imageView)
            }
        }
    }
    private fun DeleteMarkerButton() {
            DeleteMarkerHelper.DeleteMarkerFromDB(marker)
            Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
    }
    private fun parseCoordinates(Coordinates: String): LatLng {
        val coordinates = Coordinates.removePrefix("Coordinates: ").split(", ")
        val latitude = coordinates[0].toDouble()
        val longitude = coordinates[1].toDouble()
        marker = LatLng(latitude, longitude)
        return LatLng(latitude, longitude)
    }
    private fun updateFavoriteButton(pressed: Boolean, favorite: Boolean) {
        if (!isAdded || !isVisible) {
            return
        }
        if ((!pressed && favorite) || (pressed && !favorite)) {
            btnAddFavorite.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_favorite_24
                )
            )
            FavoriteHandler.addMarkerToFavorites(marker, user)
            if((pressed && !favorite)){
                Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
                isFavorite = true
            }
        } else {
            btnAddFavorite.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_favorite_border_24
                )
            )
            FavoriteHandler.removeMarkerFromFavorites(marker, user)
            Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
            isFavorite = false
        }
    }
    private fun saveButtonState(pressed: Boolean, favorite: Boolean) {
        FavoriteHandler.getUserFavorites(user) { favorites ->
            favorites.forEach { favorite ->
                if(favorite.coordinates == marker && isPressed == false){
                    isFavorite = true
                    updateFavoriteButton(isPressed, isFavorite)
                }
            }
        }
    }


    fun removeFirstAndLastCharacters(input: String): String {
        if ('[' in input || ']' in input) {
            val cleanedString = input.filterNot { it == '[' || it == ']' }
            return cleanedString.trim() // Trim any extra whitespace
        } else {
            return input.trim()
        }
    }
    private fun deleteMarker() {
        val coordinates = arguments?.getString("coordinates") ?: return
        val latLng = coordinates.split(", ")
        val latitude = latLng.getOrNull(0)
        val longitude = latLng.getOrNull(1)

        if (latitude == null || longitude == null) {
            Log.e("CoordinatesStatus", "Invalid coordinates: $coordinates")
            Toast.makeText(requireContext(), "Invalid coordinates", Toast.LENGTH_SHORT).show()
            return
        }else{
            Log.d("CoordinatesStatus", "Valid coordinates: $coordinates")
            Toast.makeText(requireContext(), "Valid coordinates", Toast.LENGTH_SHORT).show()
        }

        val firebaseMapDataHandler = FirebaseMapDataHandler(FirebaseDatabase.getInstance().getReference("Markers"))

        dismiss()
    }
}