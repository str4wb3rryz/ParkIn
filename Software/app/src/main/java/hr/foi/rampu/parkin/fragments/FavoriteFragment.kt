package hr.foi.rampu.parkin.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import hr.foi.rampu.parkin.R
import hr.foi.rampu.parkin.adapters.FavoriteAdapter
import hr.foi.rampu.parkin.entities.UserSession
import hr.foi.rampu.parkin.helpers.FavoriteHandler


class FavoriteFragment : Fragment() {
    private lateinit var userSession: UserSession
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtNoFavorites: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userSession = UserSession.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        recyclerView = view.findViewById(R.id.fragment_favorite_rv_favorites_list)
        txtNoFavorites = view.findViewById(R.id.fragment_favorite_tv_no_favorites)

        val user = userSession.currentUser?.id
        Log.e("User", user.toString())
        val databaseUser = firebaseDatabase.getReference("users/${user}/favorites")
        Log.e("DatabaseUser", databaseUser.toString())

        if (user != null) {
            FavoriteHandler.getUserFavorites(user) { favorites ->
                Log.e("FavoriteFragment", "User favorite: $favorites")
                if (favorites.isEmpty()) {
                    txtNoFavorites.visibility = View.VISIBLE
                } else {
                    txtNoFavorites.visibility = View.INVISIBLE
                }
                favorites.forEach { favorite ->
                    Log.e("FavoriteFragment", "Favorite: ${favorite.name} - ${favorite.description}")
                }
                recyclerView.adapter = FavoriteAdapter(favorites)
                recyclerView.layoutManager = LinearLayoutManager(view.context)
            }
        } else {
            Log.e("FavoriteFragment", "User ID is null")
        }
    }


}