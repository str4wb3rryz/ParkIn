package hr.foi.rampu.parkin.entities


data class User(
    val id: String? = null,
    val fullName:String? = null,
    val dob: String? = null,
    val email: String? = null,
    val username: String? = null,
    val password: String? = null,
    val role: String? = null,
)
