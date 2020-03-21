package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Data [Parcelable] subclass.
 * The main purpose of this class is to hold data.
 *
 * @property subjectId ID of a [Subject] object (mandatory)
 * @property week Represents whether the lesson is on Week A or B (defaults to A)
 * @property day Day of the lesson stored as an integer, 1: Sunday - 7: Saturday (mandatory)
 * @property starts Time when the lesson starts (mandatory)
 * @property ends Time when the lesson ends (mandatory)
 * @property location Location of the lesson (mandatory)
 * @property id Unique ID, automatically set
 */
@Parcelize
data class Lesson(
    var subjectId: String,
    var week: String,
    var day: Int,
    var starts: String,
    var ends: String,
    var location: String,
    var id: String = ""
) : Parcelable {
    constructor() : this("", "", -1, "", "", "", "")
}