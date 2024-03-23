package hr.foi.rampu.parkin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.parkin.R
import hr.foi.rampu.parkin.entities.MarkerInfo

class FavoriteAdapter(private val favorites: List<MarkerInfo>) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val favoriteId = view.findViewById(R.id.tv_favorite_id) as TextView
        private val favoriteTitle = view.findViewById(R.id.tv_favorite_name) as TextView
        private val favoriteDescription = view.findViewById(R.id.tv_favorite_description) as TextView
        private val favoriteWorkingHours = view.findViewById(R.id.tv_favorite_working_hours) as TextView
        private val favoriteZone = view.findViewById(R.id.tv_favorite_zone) as TextView
        private val favoritePrice = view.findViewById(R.id.tv_favorite_price) as TextView
        private val favoriteParkingSpots = view.findViewById(R.id.tv_favorite_parking_spots) as TextView

        fun bind(favorite: MarkerInfo) {
            favoriteId.text = favorite.coordinates.toString()
            favoriteTitle.text = favorite.name
            favoriteDescription.text = favorite.description
            favoriteWorkingHours.text = favorite.FullTime
            favoriteZone.text = favorite.parkingZone
            favoritePrice.text = favorite.parkingPrice
            favoriteParkingSpots.text = favorite.parkingSpots
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorite_list_item, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position])
    }

    override fun getItemCount() = favorites.size
}