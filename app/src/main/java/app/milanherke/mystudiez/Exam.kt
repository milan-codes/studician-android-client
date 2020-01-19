package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Exam(
    var name: String,
    var description: String,
    var subjectId: Long,
    var date: String,
    var reminder: String,
    var examId: Long = 0
) : Parcelable