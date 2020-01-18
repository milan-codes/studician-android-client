package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Exam(
    var name: String,
    var description: String,
    var subject: String,
    var date: String,
    var reminder: String,
    var id: Long = 0
) : Parcelable