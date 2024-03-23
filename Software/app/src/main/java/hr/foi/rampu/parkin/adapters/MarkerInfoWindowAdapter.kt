package hr.foi.rampu.parkin.adapters

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import hr.foi.rampu.parkin.R
import hr.foi.rampu.parkin.entities.MarkerInfo
import hr.foi.rampu.parkin.fragments.InfoAboutTheMarkerOnTheMap


class MarkerInfoWindowAdapter (private val context: Context): GoogleMap.InfoWindowAdapter {
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
    override fun getInfoContents(marker: Marker): View {

        val snippetLines = marker.snippet?.split("|#|") ?: emptyList()
        val description = snippetLines.getOrNull(0) ?: ""
        val type = snippetLines.getOrNull(1) ?: ""
        val spots = snippetLines.getOrNull(2) ?: ""
        val spotsSpecial = snippetLines.getOrNull(3) ?: ""
        val spotsReserved = snippetLines.getOrNull(4) ?: ""
        val zone = snippetLines.getOrNull(5) ?: ""
        val price = snippetLines.getOrNull(6) ?: ""
        val workingHours = snippetLines.getOrNull(7) ?: ""
        val images = snippetLines.getOrNull(8)?.split(",")?.map { Uri.parse(it) } ?: emptyList()

        showBottomSheet(
            "Title: ${marker.title}",
            "Coordinates: ${marker.position.latitude}, ${marker.position.longitude}",
            "Description: $description",
            "Type: $type",
            "Number of spots: $spots",
            "Number of special spots: $spotsSpecial",
            "Number of reserved spots: $spotsReserved",
            "Zone: $zone",
            "Price: $price",
            "Working Hours: $workingHours",
            images
        )
        return View(context)

    }

    private fun showBottomSheet(
        title: String?,
        coordinates: String?,
        description: String?,
        type: String?,
        numberOfSpots: String?,
        spotsSpecial: String?,
        spotsReserved: String?,
        zone: String?,
        price: String?,
        workingHours: String?,
        images: List<Uri>
        ) {
        val bottomSheetFragment = InfoAboutTheMarkerOnTheMap()
        val bundle = Bundle()

        bundle.putString("title", title)
        bundle.putString("coordinates", coordinates)
        bundle.putString("description", description)
        bundle.putString("type", type)
        bundle.putString("numberOfSpots", numberOfSpots)
        bundle.putString("spotsSpecial", spotsSpecial)
        bundle.putString("spotsReserved", spotsReserved)
        bundle.putString("zone", zone)
        bundle.putString("price", price)
        bundle.putString("workingHours", workingHours)
        bundle.putParcelableArrayList("images", ArrayList(images))

        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show((context as FragmentActivity).supportFragmentManager.beginTransaction(), bottomSheetFragment.tag)
    }
}