package app.milanherke.mystudiez.utils

import android.content.Context
import app.milanherke.mystudiez.R

/**
 * Simple class that holds frequently used functions
 * in a companion object regarding [app.milanherke.mystudiez.models.Task] objects.
 */
class TaskUtils {

    companion object {

        /**
         * A simple function that returns a task type (in string) from an int.
         *
         * @param type Type of the day (either 1 - Assignment or 2 - Revision)
         * @param context Context is needed to access resources
         * @return The type of assignment (string)
         * @throws IllegalArgumentException when parameter [type] is neither 1 nor 2
         */
        fun getTaskType(type: Int, context: Context): String {
            return when (type) {
                1 -> context.resources.getString(R.string.taskTypeAssignment)
                2 -> context.resources.getString(R.string.taskTypeRevision)
                else -> throw IllegalArgumentException("Parameter type ($type) must be 1 or 2")
            }
        }

    }

}