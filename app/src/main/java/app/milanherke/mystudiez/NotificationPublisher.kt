package app.milanherke.mystudiez

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * A [BroadcastReceiver] subclass.
 * Receives and handles broadcast intents sent by [Context.sendBroadcast].
 */
class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager: NotificationManager =
            (context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        val notification: Notification = intent!!.getParcelableExtra(NOTIFICATION)
        val id: Int = intent.getIntExtra(NOTIFICATION_ID, 0)
        notificationManager.notify(id, notification)
    }

    companion object {
        const val NOTIFICATION = "notification"
        const val NOTIFICATION_ID = "notification-intent"
    }
}