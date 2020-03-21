package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Data [Parcelable] subclass.
 * The main purpose of this class is to hold data.
 *
 * @property name Name of the task (mandatory)
 * @property description Description of the task (optional)
 * @property type Type of the task, either assignment or revision (mandatory)
 * @property subjectId ID of the task's subject, a [Subject] ID (mandatory)
 * @property dueDate Due date of the task (mandatory)
 * @property reminder Date of reminder (optional)
 * @property id Unique ID, automatically set
 */
@Parcelize
data class Task(
    var name: String,
    var description: String,
    var type: Int,
    var subjectId: String,
    var dueDate: String,
    var reminder: String,
    var id: String = ""
) : Parcelable {
    constructor() : this("", "", -1, "", "", "", "")
}