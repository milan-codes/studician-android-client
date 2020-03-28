package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Button
import androidx.annotation.IdRes
import java.text.SimpleDateFormat
import java.util.*

/**
 * Simple class that holds frequently used functions in a companion object
 * for handling [DatePickerDialog] and [TimePickerDialog] events.
 */
class CalendarUtils {

    companion object {

        interface DateSet {
            fun onSuccess(date: Date)
        }

        /**
         * Creates an OnDateSetListener for a [DatePickerDialog].
         *
         * @param parentActivity Host activity of the fragment in which the function is used
         * @param buttonId The selected date will be displayed on this button
         * @param cal The calendar on which the time is set
         * @param listener Defines what happens after the date is successfully set, defaults to null if not passed,
         *                 because in some cases it is not needed to do anything but to display the date on the passed button
         * @return An OnDateSetListener
         */
        fun getDateSetListener(
            parentActivity: Activity,
            @IdRes buttonId: Int,
            cal: Calendar,
            listener: DateSet? = null
        ): DatePickerDialog.OnDateSetListener {
            val button = parentActivity.findViewById<Button>(buttonId)

            return DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                button.text =
                    SimpleDateFormat("dd'/'MM'/'yyyy", Locale.getDefault()).format(cal.time)

                listener?.onSuccess(cal.time)
            }
        }

        /**
         * Creates an OnTimeSetListener for a [TimePickerDialog].
         *
         * @param parentActivity Host activity of the fragment in which the function is used
         * @param buttonId The selected time will be displayed on this button
         * @param cal The calendar on which the time is set
         * @return An OnTimeSetListener
         */
        @SuppressLint("SetTextI18n")
        fun getTimeSetListener(
            parentActivity: Activity,
            @IdRes buttonId: Int,
            cal: Calendar,
            dateBefore: Boolean
        ): TimePickerDialog.OnTimeSetListener {
            val button = parentActivity.findViewById<Button>(buttonId)

            return TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                if (dateBefore) {
                    button.text =
                        "${button.text} ${SimpleDateFormat(
                            "HH:mm",
                            Locale.ENGLISH
                        ).format(cal.time)}"
                } else {
                    button.text = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(cal.time)
                }
            }
        }

        /**
         * A simple function that returns a day in string from an int.
         *
         * @param num Number of the day (Must be between 1 and 7)
         * @param context Context needed to access resources
         * @return The name of the day
         * @throws IllegalStateException when parameter [num] is not between 1 and 7
         */
        fun getDayFromNumberOfDay(num: Int, context: Context): String {
            return when (num) {
                1 -> context.resources.getString(R.string.dayOptionSunday)
                2 -> context.resources.getString(R.string.dayOptionMonday)
                3 -> context.resources.getString(R.string.dayOptionTuesday)
                4 -> context.resources.getString(R.string.dayOptionWednesday)
                5 -> context.resources.getString(R.string.dayOptionThursday)
                6 -> context.resources.getString(R.string.dayOptionFriday)
                7 -> context.resources.getString(R.string.dayOptionSaturday)
                else -> throw IllegalArgumentException("Parameter num $num must be between one and seven")
            }
        }
    }
}