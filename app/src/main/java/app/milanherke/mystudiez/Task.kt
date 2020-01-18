package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Task(
    var name: String,
    var description: String,
    var type: String,
    var subject: String,
    var dueDate: String,
    var reminder: String,
    var id: Long = 0
) : Parcelable