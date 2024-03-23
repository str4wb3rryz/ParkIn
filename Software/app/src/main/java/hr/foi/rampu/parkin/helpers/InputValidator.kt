package hr.foi.rampu.parkin.helpers

import android.content.Context
import android.widget.Toast

class InputValidator(private val context: Context) {
    fun validateInput(name: String, parkingSpots: String, parkingSpotsS: String, parkingSpotsR: String, description: String): Boolean {
        val numberRegex = Regex("\\d+")
        val invalidCharsRegex = Regex("[|#\\-]")

        if (name.isEmpty()|| invalidCharsRegex.containsMatchIn(name)) {
            showToast("Invalid name")
            return false
        }

        if (!parkingSpots.matches(numberRegex)) {
            showToast("Invalid number of parking spots")
            return false
        }

        if (!parkingSpotsS.matches(numberRegex)) {
            showToast("Invalid number of special parking spots")
            return false
        }

        if (!parkingSpotsR.matches(numberRegex)) {
            showToast("Invalid number of reserved parking spots")
            return false
        }

        if (description.isEmpty() || invalidCharsRegex.containsMatchIn(description)) {
            showToast("Invalid description")
            return false
        }
        return true
    }

    private fun showToast(message: String) {
        Toast
            .makeText(
                context, message,
                Toast.LENGTH_SHORT
            )
            .show()
    }
}
