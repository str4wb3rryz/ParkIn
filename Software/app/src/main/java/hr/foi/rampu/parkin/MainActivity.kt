package hr.foi.rampu.parkin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import hr.foi.rampu.parkin.fragments.AddLocationFragment
import hr.foi.rampu.parkin.fragments.FavoriteFragment
import hr.foi.rampu.parkin.fragments.MapFragment
import hr.foi.rampu.parkin.fragments.ProfileFragment
import hr.foi.rampu.parkin.fragments.NovostiFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var frameLayout: FrameLayout
    private val fragmentList = ArrayList<Fragment>()
    private lateinit var selectedFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM Token: $token")
            Toast.makeText(baseContext, "FCM Token: $token", Toast.LENGTH_SHORT).show()
        }

        bottomNavigationView = findViewById(R.id.activity_main_bnv_navigacija)
        frameLayout = findViewById(R.id.activity_main_fl_content)

        fragmentList.add(MapFragment())
        fragmentList.add(ProfileFragment())
        fragmentList.add(FavoriteFragment())
        fragmentList.add(NovostiFragment())


        selectedFragment = fragmentList[0]
        fragmentsupportManager()

        bottomNavigationView.setOnItemSelectedListener{ menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_navigation_bar_option_map -> {
                    selectedFragment = fragmentList[0]
                }
                R.id.bottom_navigation_bar_option_profile -> {
                    selectedFragment = fragmentList[1]
                }
                R.id.bottom_navigation_bar_option_favorite -> {
                    selectedFragment = fragmentList[2]
                }
                R.id.bottom_navigation_bar_option_novosti->{
                    selectedFragment=fragmentList[3]
                }
            }
            fragmentsupportManager()
            true
        }
    }
    private fun fragmentsupportManager(){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.activity_main_fl_content, selectedFragment)
            .commit()
    }
}