package app.milanherke.mystudiez

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
class NotificationUtils(base: Context) : ContextWrapper(base) {

    private val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannels() {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableLights(true)
            enableVibration(true)
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        const val CHANNEL_ID = "app.milanherke.mystudiez.CHANNEL_ID"
        const val CHANNEL_NAME = "app.milanherke.mystudiez.Notification"
    }
}