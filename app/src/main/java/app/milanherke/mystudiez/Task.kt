package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Task(
    var name: String,
    var description: String,
    var type: Int,
    var subjectId: Long,
    var dueDate: String,
    var reminder: String,
    var taskId: Long = 0
) : Parcelable