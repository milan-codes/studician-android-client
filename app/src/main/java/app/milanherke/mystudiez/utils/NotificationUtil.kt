package app.milanherke.mystudiez.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * A [ContextWrapper] subclass.
 * Creates notification channels for Android O and higher.
 */
@RequiresApi(Build.VERSION_CODES.O)
class NotificationUtils(base: Context) : ContextWrapper(base) {

    private val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    /**
     * Creates a notification channel.
     * All notifications must be
     * assigned to a channel starting in Android O.
     */
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
        const val CHANNEL_ID = "app.milanherke.mystudiez.100200"
        const val CHANNEL_NAME = "app.milanherke.mystudiez.TasksAndExams"
    }
}