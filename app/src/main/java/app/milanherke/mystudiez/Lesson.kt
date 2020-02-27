package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Lesson(
    var subjectId: Long,
    var week: String,
    var day: Int,
    var starts: String,
    var ends: String,
    var location: String,
    var lessonId: Long = 0
) : Parcelable