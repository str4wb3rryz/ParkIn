package hr.foi.rampu.parkin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jakewharton.rxbinding2.widget.RxTextView
import hr.foi.rampu.parkin.databinding.ActivityLoginBinding
import hr.foi.rampu.parkin.entities.User
import hr.foi.rampu.parkin.entities.UserSession
import io.reactivex.Observable

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userSession : UserSession

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        userSession = UserSession.getInstance()

        val usernameStream = RxTextView.textChanges(binding.activityLoginUsername)
            .skipInitialValue()
            .map { username ->
                username.isEmpty()
            }

        val passwordStream = RxTextView.textChanges(binding.activityLoginPassword)
            .skipInitialValue()
            .map { password ->
                password.isEmpty()
            }

        val invalidFieldsStream = Observable.combineLatest(
            usernameStream,
            passwordStream,
            { usernameInvalid: Boolean, passwordInvalid: Boolean ->
                !usernameInvalid && !passwordInvalid
            })

        invalidFieldsStream.subscribe { isValid ->
            if (isValid) {
                binding.activityLoginButton.isEnabled = true
                binding.activityLoginButton.backgroundTintList = ContextCompat.getColorStateList(this,R.color.color_background)
            } else {
                binding.activityLoginButton.isEnabled = false
                binding.activityLoginButton.backgroundTintList = ContextCompat.getColorStateList(this,R.color.color_disabled)
            }
        }

        binding.activityLoginButton.setOnClickListener {
            val usernameLogin = findViewById<EditText>(R.id.activity_login_username).text.toString()
            val passwordLogin = findViewById<EditText>(R.id.activity_login_password).text.toString()
            if (usernameLogin.isNotEmpty() && passwordLogin.isNotEmpty()) {
                loginUser(usernameLogin, passwordLogin)
            } else {
                Toast.makeText(this, "Username and password must not be empty", Toast.LENGTH_SHORT).show()
            }
        }
        binding.activityLoginButtonRegister.setOnClickListener {
            val Intent = Intent(this,Register::class.java)
            startActivity(Intent)
        }
    }

    private fun loginUser(username: String, password: String) {
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user?.password == password && user != null) {
                            userSession.loginUser(user)
                            Toast.makeText(this@Login, "Welcome back, ${user?.fullName}", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Login, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            return
                        } else {
                            Toast.makeText(this@Login, "Invalid username or password", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@Login, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}