package app.milanherke.mystudiez

import android.app.Activity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class ProgressBarHandler(private val activity: Activity) {

    fun showProgressBar() {
        activity.runOnUiThread {
            activity.progressBar_bg.visibility = View.VISIBLE
            activity.progressBar.visibility = View.VISIBLE
        }
    }

    fun hideProgressBar() {
        activity.runOnUiThread {
            activity.progressBar_bg.visibility = View.GONE
            activity.progressBar.visibility = View.GONE
        }
    }

}