package app.milanherke.mystudiez

import android.content.Context

class TaskUtils {

    companion object {

        fun getTaskType(type: Int, context: Context): String {
            return when (type) {
                1 -> context.resources.getString(R.string.taskTypeAssignment)
                2 -> context.resources.getString(R.string.taskTypeRevision)
                else -> throw IllegalArgumentException("Parameter type ($type) must be 1 or 2")
            }
        }

    }

}