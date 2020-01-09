package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.FileDescriptor
import java.sql.Date

@Parcelize
data class Task(
    var name: String,
    var description: String,
    var type: String,
    var subject: Subject,
    var dueDate: Date,
    var reminder: Date,
    var id: Long = 0
) : Parcelable