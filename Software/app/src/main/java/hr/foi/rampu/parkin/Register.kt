package hr.foi.rampu.parkin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jakewharton.rxbinding2.widget.RxTextView
import hr.foi.rampu.parkin.databinding.ActivityRegisterBinding
import hr.foi.rampu.parkin.entities.User
import io.reactivex.Observable

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       binding = ActivityRegisterBinding.inflate(layoutInflater)
       setContentView(binding.root)

       firebaseDatabase = FirebaseDatabase.getInstance()
       databaseReference = firebaseDatabase.reference.child("users")

       val fullNameStream = RxTextView.textChanges(binding.activityRegisterNamesurname)
           .skipInitialValue()
           .map { fullName ->
               fullName.isEmpty()
           }
       fullNameStream.subscribe {
           userExists(it)
       }

       val dobStream = RxTextView.textChanges(binding.activityRegisterDob)
           .skipInitialValue()
           .map { dob ->
               !dob.toString()
                   .matches("(0[1-9]|[12][0-9]|3[01])\\/(0[1-9]|1[1,2])\\/(19|20)\\d{2}".toRegex())
           }
       dobStream.subscribe {
           dobValid(it)
       }

       val emailStream = RxTextView.textChanges(binding.activityRegisterEmail)
           .skipInitialValue()
           .map { email ->
               !Patterns.EMAIL_ADDRESS.matcher(email).matches()
           }
       emailStream.subscribe {
           emailValid(it)
       }

       val usernameStream = RxTextView.textChanges(binding.activityRegisterUsername)
           .skipInitialValue()
           .map { username ->
               username.length < 6
           }
       usernameStream.subscribe {
           textMinimal(it, "Username")
       }

       val passwordStream = RxTextView.textChanges(binding.activityRegisterPassword)
           .skipInitialValue()
           .map { password ->
               password.length < 6
           }
       passwordStream.subscribe {
           textMinimal(it, "Password")
       }

       val invalidFieldsStream = Observable.combineLatest(
           fullNameStream,
           dobStream,
           emailStream,
           usernameStream,
           passwordStream,
           { nameInvalid: Boolean, dobInvalid: Boolean, emailInvalid: Boolean, usernameInvalid: Boolean, passwordInvalid: Boolean ->
               !nameInvalid && !dobInvalid && !emailInvalid && !usernameInvalid && !passwordInvalid
           })
       invalidFieldsStream.subscribe { isValid ->
           if (isValid) {
               binding.activityRegisterButton.isEnabled = true
               binding.activityRegisterButton.backgroundTintList =
                   ContextCompat.getColorStateList(this, R.color.color_background)
           } else {
               binding.activityRegisterButton.isEnabled = false
               binding.activityRegisterButton.backgroundTintList =
                   ContextCompat.getColorStateList(this, R.color.color_disabled)
           }
       }

       binding.activityRegisterButton.setOnClickListener {
           val fullNameRegister = binding.activityRegisterNamesurname.text.toString()
           val dobRegister = binding.activityRegisterDob.text.toString()
           val emailRegister = binding.activityRegisterEmail.text.toString()
           val usernameRegister = binding.activityRegisterUsername.text.toString()
           val passwordRegister = binding.activityRegisterPassword.text.toString()
           if (fullNameRegister.isNotEmpty() && dobRegister.isNotEmpty() && emailRegister.isNotEmpty() && usernameRegister.isNotEmpty() && passwordRegister.isNotEmpty()) {
               registerUser(
                   fullNameRegister,
                   dobRegister,
                   emailRegister,
                   usernameRegister,
                   passwordRegister
               )
           } else {
               Toast.makeText(this, "Username and password must not be empty", Toast.LENGTH_SHORT)
                   .show()
           }
       }
       binding.activityRegisterButtonLogin.setOnClickListener {
           val Intent = Intent(this, Login::class.java)
           startActivity(Intent)
       }
   }

       private fun registerUser(fullName: String, dob: String, email: String, username: String, password: String) {
           databaseReference.orderByChild("username").equalTo(username)
               .addListenerForSingleValueEvent(object : ValueEventListener {
                   override fun onDataChange(snapshot: DataSnapshot) {
                       if(!snapshot.exists()){
                           val id = databaseReference.push().key
                           val role = "obican"
                           val user = User(id, fullName, dob, email, username, password, role)
                           databaseReference.child(id!!).setValue(user)
                           Toast.makeText(this@Register, "Registration successful", Toast.LENGTH_SHORT).show()
                           val intent = Intent(this@Register, Login::class.java)
                           startActivity(intent)
                           finish()
                       } else {
                           Toast.makeText(this@Register, "Username already exists", Toast.LENGTH_SHORT).show()
                       }
                   }
                   override fun onCancelled(error: DatabaseError) {
                       Toast.makeText(this@Register, "Error: ${error.message}", Toast.LENGTH_SHORT)
                           .show()
                   }
               })
       }
    private fun userExists(isNotValid: Boolean){
       binding.activityRegisterNamesurname.error = if (isNotValid) "Cant be empty" else null
   }
   private fun textMinimal(isNotValid: Boolean, text: String){
       if(text == "Username") binding.activityRegisterUsername.error = if (isNotValid) "$text must be atleast 6 letters long" else null
       else if(text == "Password") binding.activityRegisterPassword.error = if (isNotValid) "$text must be atleast 6 letters long" else null
   }

   private fun emailValid(isNotValid: Boolean) {
       binding.activityRegisterEmail.error = if (isNotValid) "Email is not valid" else null
   }

   private fun dobValid(isNotValid: Boolean){
       binding.activityRegisterDob.error = if (isNotValid) "Write date of birth in DD/MM/YYYY format" else null
   }
}




