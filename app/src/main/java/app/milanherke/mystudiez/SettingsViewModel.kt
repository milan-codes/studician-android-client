package app.milanherke.mystudiez

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * A simple [AndroidViewModel] subclass.
 * This ViewModel was created to authenticate/log out users
 * and belongs to [SettingsFragment].
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    interface LoggingOut {
        fun onSuccess()
        fun onFailure()
    }

    /**
     * This function generates an Intent
     * to handle sign-ins.
     *
     * @return A Sign-in intent
     */
    fun generateLogInIntent(): Intent {
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
    }

    /**
     * This function tries to
     * log out the user.
     *
     * @param listener Classes that use this function must implement [LoggingOut]
     */
    fun logout(listener: LoggingOut) {
        try {
            FirebaseAuth.getInstance().signOut()
            listener.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            listener.onFailure()
        }
    }

    /**
     * This function adds a user
     * to the database.
     *
     * @param user [FirebaseUser] we want to add
     * @return true if adding the user was successful, otherwise false
     */
    fun addUser(user: FirebaseUser?): Boolean {
        return if (user != null) {
            val database = Firebase.database
            val ref = database.getReference("users/${user.uid}")
            try {
                ref.setValue(User(user.displayName!!, user.email!!))
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    }

}