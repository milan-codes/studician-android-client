package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Data [Parcelable] subclass.
 * The main purpose of this class is to hold data.
 *
 * @property name Name of the exam (mandatory)
 * @property description A short description, notes (optional)
 * @property subjectId ID of a [Subject] object (mandatory)
 * @property date The date of the exam (mandatory)
 * @property reminder Date of reminder (optional)
 * @property id Unique ID, automatically set
 */
@Parcelize
data class Exam(
    var name: String,
    var description: String,
    var subjectId: String,
    var date: String,
    var reminder: String,
    var id: String = ""
) : Parcelable {
    constructor() : this("", "", "", "", "", "")
}