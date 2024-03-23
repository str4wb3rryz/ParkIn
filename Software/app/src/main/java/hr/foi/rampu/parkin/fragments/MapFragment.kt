package hr.foi.rampu.parkin.fragments

import hr.foi.rampu.parkin.helpers.FirebaseMapDataHandler
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import androidx.core.graphics.ColorUtils
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hr.foi.rampu.parkin.R
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.foi.rampu.parkin.adapters.MarkerInfoWindowAdapter
import hr.foi.rampu.parkin.entities.PolylineDecoder
import hr.foi.rampu.parkin.services.DirectionsApiResponse
import hr.foi.rampu.parkin.services.DirectionsService
import hr.foi.rampu.parkin.entities.UserSession
import hr.foi.rampu.parkin.entities.MarkerInfo
import hr.foi.rampu.parkin.helpers.InputValidator
import hr.foi.rampu.parkin.helpers.NewMarkerHelper
import hr.foi.rampu.parkin.services.DirectionsFetcher
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import javax.net.ssl.SSLProtocolException

private const val LOCATION_PERMISSION_REQUEST_CODE = 1
private const val LOCATION_UPDATE_INTERVAL = 2500L
private const val MINIMUM_ZOOM_LEVEL = 14f

class MapFragment : Fragment() {
    private var currentRoutePolyline: Polyline? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private lateinit var FAB_get_location: FloatingActionButton
    private lateinit var FR_map: FrameLayout
    private var currentLocation: Location? = null
    private var currentLatLng: LatLng = LatLng(0.0, 0.0)
    private var destinationLatLng: LatLng = LatLng(0.0, 0.0)
    private var isFollowingUser = false
    private var isButtonPressed = false
    private lateinit var userSession: UserSession

    private lateinit var btnAddNewLocation: FloatingActionButton
    private lateinit var btnDisplayRoute: FloatingActionButton
    private var tappedCoordinates: LatLng? = null
    private var pickImagesLauncher: ActivityResultLauncher<Intent>?=null

    private var isListeningForMapClick = false
    private var isMarkerClickListener = false
    private var isRouteDisplayed = false
    private lateinit var imageContainer: LinearLayout
    val selectedImages: MutableList<Uri> = mutableListOf()

    private lateinit var fabButton: FloatingActionButton
    private lateinit var closeFilterFormButton: FloatingActionButton
    private lateinit var filterMenu: MaterialCardView

    private var firebaseMapDataHandler: FirebaseMapDataHandler = FirebaseMapDataHandler(
        FirebaseDatabase.getInstance().getReference("Markers"))

    private val CIRCLE_RADIUS = 100.0

    private lateinit var redCircle: Circle
    private lateinit var yellowCircle: Circle
    private lateinit var greenCircle: Circle
    private lateinit var blackCircle: Circle

    private var isOnStreetFilterOn = false
    private var isParking_lotFilterOn = false
    private var isFringe_parkingFilterOn = false
    private var isParking_garageFilterOn = false
    private var isCarportFilterOn = false

    private var isZone0FilterOn = false
    private var isZone1FilterOn = false
    private var isZone2FilterOn = false
    private var isZone3FilterOn = false

    private var isPrice2OrLowerFilterOn = false
    private var isPrice4OrLowerFilterOn = false
    private var isPrice6OrLowerFilterOn = false
    private var isPrice8OrLowerFilterOn = false
    private var isPriceFreeFilterOn = false

    private var isTime12FilterOn = false
    private var isTime16FilterOn = false
    private var isTime19FilterOn = false

    private val markers = mutableListOf<Marker>()

    fun getCurrentTime(): String {
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return currentTime.format(formatter)
    }

    private fun updateMarkerVisibility() {
        for (marker in markers) {
            val snippet = marker.snippet ?: ""

            val isOnStreet = snippet.contains("On-street", ignoreCase = true)
            val isParking_lot = snippet.contains("Parking lot", ignoreCase = true)
            val isFringe_parking = snippet.contains("Fringe parking", ignoreCase = true)
            val isParking_garage = snippet.contains("Parking garage", ignoreCase = true)
            val isCarport = snippet.contains("Carport", ignoreCase = true)

            val isZone0 = snippet.contains("Zone 0", ignoreCase = true)
            val isZone1 = snippet.contains("Zone 1", ignoreCase = true)
            val isZone2 = snippet.contains("Zone 2", ignoreCase = true)
            val isZone3 = snippet.contains("Zone 3", ignoreCase = true)

            var priceString = snippet.substringBefore("€/h").trim()
            Log.d("PriceDebug", "Price String: $priceString")

            priceString = priceString.substringAfterLast("|#|").trim()
            val price = priceString.toDoubleOrNull() ?: 0.0

            var timeString = snippet.substringAfter(" - ").substringBefore("|#|").trim()
            val time = timeString.substringAfter(" - ").trim()
            Log.d("PriceDebug", "Price String: $priceString, Price: $price")
            Log.d("TimeDebug", "Time: $time, and the timeString: $timeString")
            val currentTime = getCurrentTime()
            val isPrice2OrLower = price <= 2.0
            val isPrice4OrLower = price <= 4.0
            val isPrice6OrLower = price <= 6.0
            val isPrice8OrLower = price <= 8.0
            val isPriceFree = price <= 0.0 || currentTime >= time

            val isTime12 = LocalTime.parse(time) <= LocalTime.parse("12:00")
            val isTime16 = LocalTime.parse(time) <= LocalTime.parse("16:00")
            val isTime19 = LocalTime.parse(time) <= LocalTime.parse("19:00")

            val isZoneSelected = listOf(isZone0FilterOn, isZone1FilterOn, isZone2FilterOn, isZone3FilterOn).any { it }
            val isPriceSelected = listOf(isPrice2OrLowerFilterOn, isPrice4OrLowerFilterOn, isPrice6OrLowerFilterOn, isPrice8OrLowerFilterOn, isPriceFreeFilterOn).any { it }
            val isTimeSelected = listOf(isTime12FilterOn, isTime16FilterOn, isTime19FilterOn).any { it }
            val isTypeSelected = listOf(
                isOnStreetFilterOn,
                isParking_lotFilterOn,
                isFringe_parkingFilterOn,
                isParking_garageFilterOn,
                isCarportFilterOn
            ).any { it }


            val isVisible =
                (!isTypeSelected || (isOnStreet && isOnStreetFilterOn) || (isParking_lot && isParking_lotFilterOn) || (isFringe_parking && isFringe_parkingFilterOn) || (isParking_garage && isParking_garageFilterOn) || (isCarport && isCarportFilterOn)) &&
                        (!isZoneSelected || (isZone0 && isZone0FilterOn) || (isZone1 && isZone1FilterOn) || (isZone2 && isZone2FilterOn) || (isZone3 && isZone3FilterOn)) &&
                        (!isPriceSelected || (isPrice2OrLower && isPrice2OrLowerFilterOn) || (isPrice4OrLower && isPrice4OrLowerFilterOn) || (isPrice6OrLower && isPrice6OrLowerFilterOn) || (isPrice8OrLower && isPrice8OrLowerFilterOn) || (isPriceFree && isPriceFreeFilterOn)) &&
                        (!isTimeSelected || (isTime12 && isTime12FilterOn) || (isTime16 && isTime16FilterOn) || (isTime19 && isTime19FilterOn))

            marker.isVisible = isVisible
        }
    }

    internal fun retrieveMarkersFromFirebase() {
        Log.d("FIREBASERetriv","We inside function")
        firebaseMapDataHandler.retrieveMarkers(
            { mark ->
                //spremlejnInfomarkera=mark
                Log.d("FIREBASERetriv","${mark}")
                // Log.d("FIREBASERetriv","${spremlejnInfomarkera}")

                mark.forEach { markerInfo ->
                    Log.d("FIREBASERetriv","$markerInfo")
                    Log.d("FIREBASERetriv","We inside FOR EACH YES")


                    var LatLong= LatLng(markerInfo.coordinates.latitude, markerInfo.coordinates.longitude)
                    val marker = googleMap?.addMarker(
                        MarkerOptions()
                            .position(LatLong)
                            .title(markerInfo.name)
                            .snippet("${markerInfo.description}|#|"+
                                    "${markerInfo.type}|#|"+
                                    "${markerInfo.parkingSpots}|#|" +
                                    "${markerInfo.parkingSpotsS}|#|" +
                                    "${markerInfo.parkingSpotsR}|#|" +
                                    "${markerInfo.parkingZone}|#|"+
                                    "${markerInfo.parkingPrice}€/h|#|"+
                                    "${markerInfo.FullTime}|#|"+
                                    markerInfo.userSelectedImages
                            )
                    )

                    if(marker!=null){
                        markers.add(marker)
                    }
                }
                //putMarkersOnMap(mark)

            },
            { error ->
                Log.e("12345NOTWORK", "Error retrieving markers: $error")
            }
        )
    }

    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        googleMap!!.isMyLocationEnabled
        map.uiSettings.isCompassEnabled = true

        map.setOnMapClickListener { latLng ->
            tappedCoordinates = latLng
            if (isListeningForMapClick) {
                showAddNewParkingLocation(latLng)
            }
        }
        map.setOnMarkerClickListener() { marker ->
            if (isMarkerClickListener) {
                Log.e("MARKER", "${marker.position}")
                displayRouteToMarker(marker.position, map)
                Toast.makeText(requireContext(), "Displaying route to ${marker.title}", Toast.LENGTH_SHORT).show()
                isMarkerClickListener = false
                true
            } else {
                false
            }
        }
        val vzLocation = LatLng(46.299977682647395, 16.34468216350508)
        val zoomLevel = 12.0f
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(vzLocation, zoomLevel)
        map.moveCamera(cameraUpdate)


        val markerInfoWindowAdapter = MarkerInfoWindowAdapter(requireContext())
        map.setInfoWindowAdapter(markerInfoWindowAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        retrieveMarkersFromFirebase()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        FAB_get_location=view.findViewById(R.id.fragment_map_fab_get_current_location)
        FR_map=view.findViewById(R.id.fragment_map_fl_fl_for_map)
        btnAddNewLocation = view.findViewById(R.id.fragment_map_fab_add_new_location)
        btnDisplayRoute = view.findViewById(R.id.fragment_map_fab_display_route)
        userSession = UserSession.getInstance()

        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map_fl_display_map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


        mapFragment?.getMapAsync { map ->
            googleMap = map
            drawCirclesOnMap()
            retrieveMarkersFromFirebase()

        }

        filterMenu = view.findViewById(R.id.fragment_map_filter_menu)
        fabButton = view.findViewById(R.id.fragment_map_fab_filter)
        closeFilterFormButton= view.findViewById(R.id.filter_marker_menu_ib_close)

        val btnFilterOnStreet: Button = view.findViewById(R.id.filter_marker_menu_btn_Filter_OnStreet)
        val btnFilterParking_lot: Button = view.findViewById(R.id.filter_marker_menu_btn_Filter_Parking_lot)
        val btnFilterCarport: Button = view.findViewById(R.id.filter_marker_menu_btn_Filter_Carport)
        val btnFilterFringe_parking: Button = view.findViewById(R.id.filter_marker_menu_btn_Filter_Fringe_parking)
        val btnFilterParking_garage: Button = view.findViewById(R.id.filter_marker_menu_btn_Filter_Parking_garage)

        val btnFilterZone0: Button = view.findViewById(R.id.filter_marker_menu_btn_FilterZone0)
        val btnFilterZone1: Button = view.findViewById(R.id.filter_marker_menu_btn_FilterZone1)
        val btnFilterZone2: Button = view.findViewById(R.id.filter_marker_menu_btn_FilterZone2)
        val btnFilterZone3: Button = view.findViewById(R.id.filter_marker_menu_btn_FilterZone3)

        val btnPrice2OrLower: Button = view.findViewById(R.id.filter_marker_menu_btn_Price2OrLower)
        val btnPrice4OrLower: Button = view.findViewById(R.id.filter_marker_menu_btn_Price4OrLower)
        val btnPrice6OrLower: Button = view.findViewById(R.id.filter_marker_menu_btn_Price6OrLower)
        val btnPrice8OrLower: Button = view.findViewById(R.id.filter_marker_menu_btn_Price8OrLower)
        val btnPriceFree: Button = view.findViewById(R.id.filter_marker_menu_btn_PriceFree)
        val btnTime19: Button = view.findViewById(R.id.filter_marker_menu_btn_Time19)
        val btnTime12: Button = view.findViewById(R.id.filter_marker_menu_btn_Time12)
        val btnTime16: Button = view.findViewById(R.id.filter_marker_menu_btn_Time16)
        val btnClearFilters: Button = view.findViewById(R.id.filter_marker_menu_btn_ClearFilters)

        val activeColor = ContextCompat.getColorStateList(requireContext(), R.color.activeFilterButtonColor)
        val inactiveColor = ContextCompat.getColorStateList(requireContext(), R.color.inactiveFilterButtonColor)

        btnFilterOnStreet.setOnClickListener {
            isOnStreetFilterOn = !isOnStreetFilterOn
            updateMarkerVisibility()
            val color = if (isOnStreetFilterOn) activeColor else inactiveColor
            btnFilterOnStreet.setBackgroundTintList(color)
        }
        btnFilterParking_lot.setOnClickListener {
            isParking_lotFilterOn = !isParking_lotFilterOn
            updateMarkerVisibility()
            val color = if (isParking_lotFilterOn) activeColor else inactiveColor
            btnFilterParking_lot.setBackgroundTintList(color)
        }
        btnFilterCarport.setOnClickListener {
            isCarportFilterOn = !isCarportFilterOn
            updateMarkerVisibility()
            val color = if (isCarportFilterOn) activeColor else inactiveColor
            btnFilterCarport.setBackgroundTintList(color)
        }
        btnFilterFringe_parking.setOnClickListener {
            isFringe_parkingFilterOn = !isFringe_parkingFilterOn
            updateMarkerVisibility()
            val color = if (isFringe_parkingFilterOn) activeColor else inactiveColor
            btnFilterFringe_parking.setBackgroundTintList(color)
        }
        btnFilterParking_garage.setOnClickListener {
            isParking_garageFilterOn = !isParking_garageFilterOn
            updateMarkerVisibility()
            val color = if (isParking_garageFilterOn) activeColor else inactiveColor
            btnFilterParking_garage.setBackgroundTintList(color)
        }

        btnFilterZone0.setOnClickListener {
            isZone0FilterOn = !isZone0FilterOn
            updateMarkerVisibility()
            val color = if (isZone0FilterOn) activeColor else inactiveColor
            btnFilterZone0.setBackgroundTintList(color)
        }
        btnFilterZone1.setOnClickListener {
            isZone1FilterOn = !isZone1FilterOn
            updateMarkerVisibility()
            val color = if (isZone1FilterOn) activeColor else inactiveColor
            btnFilterZone1.setBackgroundTintList(color)
        }
        btnFilterZone2.setOnClickListener {
            isZone2FilterOn = !isZone2FilterOn
            updateMarkerVisibility()
            val color = if (isZone2FilterOn) activeColor else inactiveColor
            btnFilterZone2.setBackgroundTintList(color)
        }
        btnFilterZone3.setOnClickListener {
            isZone3FilterOn = !isZone3FilterOn
            updateMarkerVisibility()
            val color = if (isZone3FilterOn) activeColor else inactiveColor
            btnFilterZone3.setBackgroundTintList(color)
        }

        btnPrice2OrLower.setOnClickListener {
            isPrice2OrLowerFilterOn = !isPrice2OrLowerFilterOn
            updateMarkerVisibility()
            val color = if (isPrice2OrLowerFilterOn) activeColor else inactiveColor
            btnPrice2OrLower.setBackgroundTintList(color)
        }
        btnPrice4OrLower.setOnClickListener {
            isPrice4OrLowerFilterOn = !isPrice4OrLowerFilterOn
            updateMarkerVisibility()
            val color = if (isPrice4OrLowerFilterOn) activeColor else inactiveColor
            btnPrice4OrLower.setBackgroundTintList(color)
        }
        btnPrice6OrLower.setOnClickListener {
            isPrice6OrLowerFilterOn = !isPrice6OrLowerFilterOn
            updateMarkerVisibility()
            val color = if (isPrice6OrLowerFilterOn) activeColor else inactiveColor
            btnPrice6OrLower.setBackgroundTintList(color)
        }
        btnPrice8OrLower.setOnClickListener {
            isPrice8OrLowerFilterOn = !isPrice8OrLowerFilterOn
            updateMarkerVisibility()
            val color = if (isPrice8OrLowerFilterOn) activeColor else inactiveColor
            btnPrice8OrLower.setBackgroundTintList(color)
        }
        btnPriceFree.setOnClickListener{
            isPriceFreeFilterOn = !isPriceFreeFilterOn
            updateMarkerVisibility()
            val color = if (isPriceFreeFilterOn) activeColor else inactiveColor
            btnPriceFree.setBackgroundTintList(color)
        }

        btnTime12.setOnClickListener{
            isTime12FilterOn = !isTime12FilterOn
            updateMarkerVisibility()
            val color = if (isTime12FilterOn) activeColor else inactiveColor
            btnTime12.setBackgroundTintList(color)
        }
        btnTime16.setOnClickListener{
            isTime16FilterOn = !isTime16FilterOn
            updateMarkerVisibility()
            val color = if (isTime16FilterOn) activeColor else inactiveColor
            btnTime16.setBackgroundTintList(color)
        }
        btnTime19.setOnClickListener{
            isTime19FilterOn = !isTime19FilterOn
            updateMarkerVisibility()
            val color = if (isTime19FilterOn) activeColor else inactiveColor
            btnTime19.setBackgroundTintList(color)
        }

        btnClearFilters.setOnClickListener {
            isOnStreetFilterOn = false
            isParking_lotFilterOn = false
            isFringe_parkingFilterOn = false
            isParking_garageFilterOn = false
            isCarportFilterOn = false

            isZone0FilterOn = false
            isZone1FilterOn = false
            isZone2FilterOn = false
            isZone3FilterOn = false

            isPrice2OrLowerFilterOn = false
            isPrice4OrLowerFilterOn = false
            isPrice6OrLowerFilterOn = false
            isPrice8OrLowerFilterOn = false
            isPriceFreeFilterOn = false

            isTime12FilterOn = false
            isTime16FilterOn = false
            isTime19FilterOn = false

            btnFilterOnStreet.setBackgroundTintList(inactiveColor)
            btnFilterParking_lot.setBackgroundTintList(inactiveColor)
            btnFilterCarport.setBackgroundTintList(inactiveColor)
            btnFilterFringe_parking.setBackgroundTintList(inactiveColor)
            btnFilterParking_garage.setBackgroundTintList(inactiveColor)

            btnFilterZone0.setBackgroundTintList(inactiveColor)
            btnFilterZone1.setBackgroundTintList(inactiveColor)
            btnFilterZone2.setBackgroundTintList(inactiveColor)
            btnFilterZone3.setBackgroundTintList(inactiveColor)

            btnPrice2OrLower.setBackgroundTintList(inactiveColor)
            btnPrice4OrLower.setBackgroundTintList(inactiveColor)
            btnPrice6OrLower.setBackgroundTintList(inactiveColor)
            btnPrice8OrLower.setBackgroundTintList(inactiveColor)
            btnPriceFree.setBackgroundTintList(inactiveColor)

            btnTime12.setBackgroundTintList(inactiveColor)
            btnTime16.setBackgroundTintList(inactiveColor)
            btnTime19.setBackgroundTintList(inactiveColor)


            updateMarkerVisibility()
        }

        fabButton.setOnClickListener {
            filterMenu.visibility = if (filterMenu.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
        }

        closeFilterFormButton.setOnClickListener {
            filterMenu.visibility = if (filterMenu.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
        }


        FAB_get_location.setOnClickListener {
            isButtonPressed = !isButtonPressed
            isFollowingUser = true
            requestLocationUpdates()
            if (isButtonPressed) {
                Toast.makeText(requireContext(), "Finding current location...", Toast.LENGTH_SHORT).show()
                FAB_get_location.setImageResource(R.drawable.ic_baseline_my_location_24)
            } else {
                FAB_get_location.setImageResource(R.drawable.ic_baseline_location_searching_24)
            }
        }
        btnAddNewLocation.setOnClickListener{
            isListeningForMapClick = true
            Toast.makeText(requireContext(), "Tap on the map to choose a location", Toast.LENGTH_SHORT).show()
        }

        btnDisplayRoute.setOnClickListener {
            if(currentLatLng.latitude != 0.0 && currentLatLng.longitude != 0.0) {
                if (isRouteDisplayed) {
                    removeCurrentRoute()
                    isRouteDisplayed = false
                    isMarkerClickListener = false
                    btnDisplayRoute.setImageResource(R.drawable.ic_baseline_directions_off_24)
                    Toast.makeText(requireContext(), "Stopping the display of the route...", Toast.LENGTH_SHORT).show()
                } else {
                    if(isMarkerClickListener){
                        isMarkerClickListener = false
                        btnDisplayRoute.setImageResource(R.drawable.ic_baseline_directions_off_24)
                    } else {
                        isMarkerClickListener = true
                        btnDisplayRoute.setImageResource(R.drawable.ic_baseline_assistant_direction_24)
                        Toast.makeText(requireContext(), "Click on the marker to display the route", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Wait for your location to be found", Toast.LENGTH_SHORT).show()
            }

        }

        pickImagesLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uris = result.data?.clipData?.let { clipData ->
                        (0 until clipData.itemCount).map { clipData.getItemAt(it).uri }
                    } ?: result.data?.data?.let { listOf(it) } ?: emptyList()

                    selectedImages.clear()
                    selectedImages.addAll(uris)

                    processSelectedImages(selectedImages)

                }
            }

        if(userSession.isAdmin()){
            btnAddNewLocation.visibility = View.VISIBLE
        }else{
            btnAddNewLocation.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pickImagesLauncher?.unregister()
    }

    private fun processSelectedImages(images: List<Uri>) {
        imageContainer.removeAllViews()
        if (images.isNotEmpty()) {
            for (uri in images) {
                Log.i("IMAGE_ChECK", "the image is: $uri and it was ok")
                val imageView = ImageView(requireContext())
                imageView.layoutParams = LinearLayout.LayoutParams(
                    400, 400
                )
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setImageURI(uri)
                imageContainer.addView(imageView)

                imageView.setOnLongClickListener {
                    imageContainer.removeView(it)
                    true
                }
            }
        }
    }
    private fun displayRouteToMarker(destination: LatLng, map: GoogleMap) {
        destinationLatLng = destination
        Log.e("ROUTE", "Displaying route to $destination")
        val origin = currentLatLng
        Log.e("ROUTE", "Origin: $origin")

        val app = requireContext().packageManager.getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
        val bundle = app.metaData
        val apiKey = bundle.getString("com.google.android.geo.API_KEY") ?: "42069"
        Log.e("APIKEY", "Api key: $apiKey")

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val directionsFetcher = DirectionsFetcher(retrofit)
        directionsFetcher.fetchDirections(origin, destination, apiKey) { result ->
            result.onSuccess { directionsResponse ->
                Log.e("ROUTE", "Response: $directionsResponse")
                Log.e("ROUTE", "Map: $map")
                currentRoutePolyline?.remove()
                drawRouteOnMap(map, directionsResponse)
            }.onFailure { throwable ->
                Log.e("ROUTE", "Error: ${throwable.message}")
            }
        }
    }

    private fun removeCurrentRoute() {
        currentRoutePolyline?.remove()
        currentRoutePolyline = null
        isRouteDisplayed = false
    }

    fun drawRouteOnMap(map: GoogleMap, directionsResponse: DirectionsApiResponse) {
        if (directionsResponse.routes.isNotEmpty()) {
            val route = directionsResponse.routes[0]
            val polyline = route.overview_polyline.points
            val polylineDecoder = PolylineDecoder()
            val points = polylineDecoder.decodePolyline(polyline)

            val polylineOptions = PolylineOptions().apply {
                width(20f)
                color(0xFFFF0000.toInt())
                addAll(points)
            }

            currentRoutePolyline = map.addPolyline(polylineOptions)
            isRouteDisplayed = true

            // Log information
            Log.e("RouteInfo", "Route drawn from ${route.legs[0].start_address} to ${route.legs[0].end_address}")
            Log.e("RouteInfo", "Distance: ${route.legs[0].distance.text}, Duration: ${route.legs[0].duration.text}")

        } else {
            Log.e("RouteInfo", "No routes found")
        }
    }


    private fun showAddNewParkingLocation(tappedCoordinates: LatLng){
        isListeningForMapClick = false
        val addNewLocationView = LayoutInflater.from(context).inflate(R.layout.add_new_location, null)

        val tvCoordinates: TextView = addNewLocationView.findViewById(R.id.fragment_add_location_et_coordinates)
        val etName: EditText = addNewLocationView.findViewById(R.id.fragment_add_location_et_name)
        val etDescription: EditText = addNewLocationView.findViewById(R.id.fragment_add_location_et_description)
        val etNumberOfParkingSpots: EditText = addNewLocationView.findViewById(R.id.fragment_add_location_et_numberOfParkingSpots)
        val etNumberOfParkingSpotsSpecial: EditText = addNewLocationView.findViewById(R.id.fragment_add_location_et_numberOfParkingSpots_special)
        val etNumberOfParkingSpotsReserved: EditText = addNewLocationView.findViewById(R.id.fragment_add_location_et_numberOfParkingSpots_reserved)
        val etZone: Spinner = addNewLocationView.findViewById(R.id.fragment_add_location_sp_zone)
        val spType: Spinner = addNewLocationView.findViewById(R.id.fragment_add_location_sp_type)
        val etPrice: EditText = addNewLocationView.findViewById(R.id.fragment_add_location_et_price)
        val etHoursStart: EditText = addNewLocationView.findViewById(R.id.fragment_add_location_et_working_hours_start)
        val etHoursEnd: EditText = addNewLocationView.findViewById(R.id.fragment_add_location_et_working_hours_stop)
        val selectImageButton: Button = addNewLocationView.findViewById(R.id.fragment_add_location_btn_selectImages)

        imageContainer = addNewLocationView.findViewById(R.id.fragment_add_location_ll_imageContainer)
        imageContainer.removeAllViews()

        val helper=NewMarkerHelper(addNewLocationView)

        val currentCoordinates = "${tappedCoordinates.latitude}, ${tappedCoordinates.longitude}"
        tvCoordinates.text = currentCoordinates

        val inputValidator = InputValidator(requireContext())

        val addLocationDialog  = AlertDialog.Builder(requireContext())
            .setView(addNewLocationView)
            .setTitle(getString(R.string.add_location))
            .setPositiveButton("Save") { _, _ ->
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()


        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            //pickImages.launch(intent)
            pickImagesLauncher?.launch(intent)
        }

        addLocationDialog.setOnShowListener {
            val saveButton = addLocationDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val name = etName.text.toString()
                val description = etDescription.text.toString()
                val parkingSpots = etNumberOfParkingSpots.text.toString()
                val parkingSpotsS = etNumberOfParkingSpotsSpecial.text.toString()
                val parkingSpotsR = etNumberOfParkingSpotsReserved.text.toString()
                val parkingZone = etZone.selectedItem?.toString() ?: "" //etZone.text.toString()
                val type = spType.selectedItem?.toString() ?: ""
                val parkingPrice = etPrice.text.toString()
                val FullTime = etHoursStart.text.toString() +" - " + etHoursEnd.text.toString()

                val isValidInput = inputValidator.validateInput(
                    etName.text.toString(),
                    etNumberOfParkingSpots.text.toString(),
                    etNumberOfParkingSpotsSpecial.text.toString(),
                    etNumberOfParkingSpotsReserved.text.toString(),
                    etDescription.text.toString()
                )
                if (isValidInput) {
                    if (
                        etHoursStart.text.isNotEmpty()
                        && etHoursEnd.text.isNotEmpty()
                        && parkingPrice.isNotEmpty()
                    ) {
                        addMarkerToMap(tappedCoordinates, name, description, type, parkingSpots, parkingSpotsS, parkingSpotsR, parkingZone, parkingPrice, FullTime, selectedImages)
                        selectedImages.clear()
                        addLocationDialog.dismiss()
                    }else{
                        Toast.makeText(requireContext(), "Enter start, end hours and price!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid input. Please check your input.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        addLocationDialog.show()
        helper.populateZoneSpinner()
        helper.populateTypeSpinner()
        helper.activateTimeListeners()
    }

    private fun uriListToStringList(uris: MutableList<Uri>): List<String> {
        return uris.map { it.toString() }
    }


    private fun addMarkerToMap(
        coordinates: LatLng,
        name: String,
        description: String,
        type: String,
        parkingSpots: String,
        parkingSpotsS: String,
        parkingSpotsR: String,
        parkingZone: String,
        parkingPrice: String,
        FullTime: String,
        userSelectedImages: MutableList<Uri>)
    {
        for (uri in userSelectedImages) {
            Log.d("MarkerLog", "Selected Image URI: $uri")
        }
        val imageUrls = uriListToStringList(userSelectedImages)
        val markerInfo = MarkerInfo(
            id = null,
            coordinates=coordinates,
            name = name,
            description = description,
            type = type,
            parkingSpots = parkingSpots,
            parkingSpotsS = parkingSpotsS,
            parkingSpotsR = parkingSpotsR,
            parkingZone = parkingZone,
            parkingPrice = parkingPrice,
            FullTime = FullTime,
            userSelectedImages = imageUrls
        )


        val marker =  googleMap?.addMarker(
            MarkerOptions()
                .position(coordinates)
                .title(name)
                .snippet("$description|#|" +
                        "$type|#|" +
                        "$parkingSpots|#|"+
                        "$parkingSpotsS|#|"+
                        "$parkingSpotsR|#|"+
                        "$parkingZone|#|" +
                        "$parkingPrice€/h|#|" +
                        "$FullTime|#|" +
                        userSelectedImages
                )
        )

        val firebaseHandler = FirebaseMapDataHandler(FirebaseDatabase.getInstance().getReference("Markers"))
        firebaseHandler.addMarker(
            markerInfo,
            successCallback = {
                Log.d("DatabaseAdd", "It works")
            },
            errorCallback = { exception ->
                Log.d("DatabaseAdd", "It not works")
            }
        )

        marker?.tag = userSelectedImages
        if (marker != null)  markers.add(marker)

        val markerInfoWindowAdapter = MarkerInfoWindowAdapter(requireContext())
        googleMap?.setInfoWindowAdapter(markerInfoWindowAdapter)
        Toast.makeText(requireContext(), "You have added the new location on the map.", Toast.LENGTH_SHORT).show()
    }


    private fun drawCirclesOnMap() {
        redCircle = drawCircle(LatLng(46.306011005287836, 16.339885890483856), Color.RED)
        redCircle = drawCircle(LatLng(46.311627745809886, 16.347179152071476), Color.RED)
        redCircle = drawCircle(LatLng(46.30825063733509, 16.346017755568027), Color.RED)
        yellowCircle = drawCircle(LatLng(46.3080185721686, 16.341440230607986), Color.YELLOW)
        yellowCircle = drawCircle(LatLng(46.30879281369499, 16.35517045855522), Color.YELLOW)
        greenCircle = drawCircle(LatLng(46.30180015631705, 16.34126253426075), Color.GREEN)
        greenCircle = drawCircle(LatLng(46.31839083155792, 16.35573372244835), Color.GREEN)
        blackCircle = drawCircle(LatLng(46.31270924726502, 16.338398940861225), Color.BLACK)
        blackCircle = drawCircle(LatLng(46.32130190837514, 16.333974972367287), Color.BLACK)
    }

    private fun drawCircle(center: LatLng, color: Int): Circle {
        val circleOptions = CircleOptions()
            .center(center)
            .radius(CIRCLE_RADIUS)
            .strokeColor(color)
            .strokeWidth(5f)
            .fillColor(ColorUtils.setAlphaComponent(color, 50))

        return googleMap?.addCircle(circleOptions) ?: throw IllegalStateException("GoogleMap not initialized")
    }



    private fun requestLocationUpdates(){
        if(isLocationEnabled()){
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap?.isMyLocationEnabled = isFollowingUser
                fusedLocationClient.requestLocationUpdates(
                    getLocationRequest(),
                    locationCallback,
                    null
                )
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }else{
            showLocationNotAvailableMessage()
        }
    }
    private fun getLocationRequest(): LocationRequest {
        return LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
            .build()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { newLocation ->
                if (currentLocation == null || currentLocation!!.distanceTo(newLocation) > 0) {
                    currentLocation = newLocation
                    Log.e("LOCATION", "Coordinates: ${newLocation.latitude}, ${newLocation.longitude}")
                    currentLatLng = LatLng(newLocation.latitude, newLocation.longitude)
                    updateMapLocation(newLocation)
                    if(isRouteDisplayed){
                        displayRouteToMarker(destinationLatLng, googleMap!!)
                    }
                }
            }
        }
    }

    private fun cameraUpdate(currentLatLng: LatLng) {
        if(isFollowingUser) {
            var zoom = googleMap!!.cameraPosition!!.zoom
            if(zoom < MINIMUM_ZOOM_LEVEL) zoom = MINIMUM_ZOOM_LEVEL
            googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, zoom))
        }

    }
    private fun updateMapLocation(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)

        googleMap?.setOnCameraIdleListener {
            if(isButtonPressed==true){
                cameraUpdate(currentLatLng)
            }
            else if(isButtonPressed==false){
                isFollowingUser=false
            }
        }

        if(isButtonPressed==true){
            cameraUpdate(currentLatLng)
        }

        googleMap?.setOnCameraMoveListener{
            if(isButtonPressed==true){
                cameraUpdate(currentLatLng)
            }
            else if(isButtonPressed==false){
                isFollowingUser=false
            }
        }
    }

    private fun showLocationNotAvailableMessage() {
        Toast.makeText(
            requireContext(),
            "Turn on your location.",
            Toast.LENGTH_LONG
        ).show()
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


}