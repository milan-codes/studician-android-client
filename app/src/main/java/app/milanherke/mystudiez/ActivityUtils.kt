package app.milanherke.mystudiez

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import app.milanherke.mystudiez.models.Exam
import app.milanherke.mystudiez.models.Task


class ActivityUtils {

    companion object {

        private const val TASK_NOTIFICATION_PRE_CODE = 100
        private const val EXAM_NOTIFICATION_PRE_CODE = 200
        const val ACTIVITY_NAME_BUNDLE_ID = "ACTIVITY_NAME_BUNDLE_ID"
        const val FRAGMENT_TO_LOAD_BUNDLE_ID = "FRAGMENT_TO_LOAD_BUNDLE_ID"
        const val SUBJECT_PARAM_BUNDLE_ID = "SUBJECT_PARAM_BUNDLE_ID"
        const val LESSON_PARAM_BUNDLE_ID = "LESSON_PARAM_BUNDLE_ID"
        const val TASK_PARAM_BUNDLE_ID = "TASK_PARAM_BUNDLE_ID"
        const val EXAM_PARAM_BUNDLE_ID = "EXAM_PARAM_BUNDLE_ID"

        fun scheduleNotification(
            activity: Activity,
            notification: Notification,
            delay: Long,
            task: Task? = null,
            exam: Exam? = null
        ) {
            val requestCode = when {
                task != null -> {
                    Integer.parseInt("${TASK_NOTIFICATION_PRE_CODE}${task.id}".filter { it.isDigit() })
                }
                exam != null -> {
                    Integer.parseInt("${EXAM_NOTIFICATION_PRE_CODE}${exam.id}".filter { it.isDigit() })
                }
                else -> {
                    throw IllegalStateException("Unrecognised notification type")
                }
            }
            val notificationIntent = Intent(activity, NotificationPublisher::class.java)
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, requestCode)
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification)

            val pendingIntent = PendingIntent.getBroadcast(
                activity,
                requestCode,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val futureInMillis = SystemClock.elapsedRealtime() + delay
            val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent)
        }

        @TargetApi(Build.VERSION_CODES.O)
        fun createNotification(activity: Activity, contentTitle: String, contentText: String): Notification {
            val builder = NotificationCompat.Builder(activity, NotificationUtils.CHANNEL_ID)
                .setSmallIcon(R.drawable.reminder_icon)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(contentText)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            return builder.build()
        }
    }


}