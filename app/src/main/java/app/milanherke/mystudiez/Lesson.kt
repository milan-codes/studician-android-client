package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Lesson(
    var name: String,
    var week: String,
    var day: String,
    var starts: String,
    var ends: String,
    var location: String,
    var id: Long = 0
) : Parcelable