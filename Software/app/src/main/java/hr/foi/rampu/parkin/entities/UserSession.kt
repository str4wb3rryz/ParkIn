package hr.foi.rampu.parkin.entities

class UserSession private constructor(){
    companion object {
        private var instance: UserSession? = null

        fun getInstance(): UserSession {
            if (instance == null) {
                instance = UserSession()
            }
            return instance!!
        }
    }

    var currentUser: User? = null

    fun loginUser(user: User) {
        currentUser = user
    }

    fun logoutUser() {
        currentUser = null
    }

    fun isLoggedIn(): Boolean {
        return currentUser != null
    }
    fun isAdmin(): Boolean {
        return currentUser?.role == "admin"
    }
}