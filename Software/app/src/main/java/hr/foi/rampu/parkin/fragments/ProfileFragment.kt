package hr.foi.rampu.parkin.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import hr.foi.rampu.parkin.Login
import hr.foi.rampu.parkin.R
import hr.foi.rampu.parkin.entities.User
import hr.foi.rampu.parkin.entities.UserSession

class ProfileFragment : Fragment() {

    private lateinit var buttonSignOut: Button
    private lateinit var userSession: UserSession
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userSession = UserSession.getInstance()

        val fullNameDisplay = view.findViewById<TextView>(R.id.fragment_profile_fullname_info)
        val emailDisplay = view.findViewById<TextView>(R.id.fragment_profile_email_info)
        val dobDisplay = view.findViewById<TextView>(R.id.fragment_profile_dob_info)
        val usernameDisplay = view.findViewById<TextView>(R.id.fragment_profile_username_info)

        val user = userSession.currentUser ?: return

        fullNameDisplay.text = user.fullName
        emailDisplay.text = user.email
        dobDisplay.text = user.dob
        usernameDisplay.text = user.username

        buttonSignOut = view.findViewById(R.id.fragment_profile_button_signout)
        buttonSignOut.setOnClickListener {
            userSession.logoutUser()
            Toast.makeText(requireContext(), "You have been logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(),Login::class.java)
            startActivity(intent)
        }
    }
}