package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Subject(
    var name: String,
    var teacher: String,
    var colorCode: Int,
    var subjectId: Long = 0
) : Parcelable