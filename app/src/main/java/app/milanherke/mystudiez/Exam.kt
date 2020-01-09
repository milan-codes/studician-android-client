package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Date

@Parcelize
data class Exam(
    var name: String,
    var description: String,
    var subject: Subject,
    var date: Date,
    var reminder: Date,
    var id: Long = 0
) : Parcelable