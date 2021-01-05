package app.milanherke.mystudiez.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import app.milanherke.mystudiez.utils.FirebaseUtils
import app.milanherke.mystudiez.R
import app.milanherke.mystudiez.utils.FirebaseUtils.Companion.RC_SIGN_IN
import app.milanherke.mystudiez.viewmodels.fragments.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*

/**
 * A simple [Fragment] subclass.
 * The main purpose of this fragment is to handle signing in and out.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(SettingsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (FirebaseUtils.userIsLoggedIn()) showUI() else hideUI()

        google_sign_in.setOnClickListener {
            login()
            showUI()
        }

        firebaseSignOut.setOnClickListener {
            logout()
            hideUI()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                viewModel.addUser(user)
            } else {
                throw IllegalArgumentException("Error logging in")
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SettingsFragment.
         */
        @JvmStatic
        fun newInstance() =
            SettingsFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    /**
     * Calls [viewModel]'s generateLogInIntent function,
     * then tries logging in.
     */
    private fun login() {
        val logInIntent = viewModel.generateLogInIntent()
        startActivityForResult(
            logInIntent,
            RC_SIGN_IN
        )
    }

    /**
     * Calls [viewModel]'s logout function,
     * then tries logging out
     */
    private fun logout() {
        viewModel.logout(object : SettingsViewModel.LoggingOut {
            override fun onSuccess() {
                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure() {
                Toast.makeText(context, "Something went wrong, try again", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    /**
     * Hides bottom navigation bar, fab button and sign out button,
     * because the user is logged out.
     */
    @SuppressLint("RestrictedApi")
    private fun hideUI() {
        activity!!.bar.visibility = View.GONE
        activity!!.fab.visibility = View.GONE
        google_sign_in.visibility = View.VISIBLE
        firebaseSignOut.visibility = View.GONE
    }

    /**
     * Shows bottom navigation bar, fab button and sign out button,
     * because the user is logged in.
     */
    @SuppressLint("RestrictedApi")
    private fun showUI() {
        activity!!.bar.visibility = View.VISIBLE
        activity!!.fab.visibility = View.VISIBLE
        google_sign_in.visibility = View.GONE
        firebaseSignOut.visibility = View.VISIBLE
    }
}
