package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Button
import androidx.annotation.IdRes
import java.text.SimpleDateFormat
import java.util.*

class CalendarUtils {

    companion object {
        fun getDate(
            parentActivity: Activity,
            @IdRes buttonId: Int,
            cal: Calendar
        ): DatePickerDialog.OnDateSetListener {
            val button = parentActivity.findViewById<Button>(buttonId)

            return DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                button.text =
                    SimpleDateFormat("dd'/'MM'/'yyyy", Locale.getDefault()).format(cal.time)
            }
        }

        @SuppressLint("SetTextI18n")
        fun getTime(
            parentActivity: Activity,
            @IdRes buttonId: Int,
            cal: Calendar
        ): TimePickerDialog.OnTimeSetListener {
            val button = parentActivity.findViewById<Button>(buttonId)

            return TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                button.text =
                    "${button.text} ${SimpleDateFormat("HH:mm", Locale.ENGLISH).format(cal.time)}"
            }
        }
    }
}