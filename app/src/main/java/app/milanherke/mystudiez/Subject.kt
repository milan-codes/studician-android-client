package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Data [Parcelable] subclass.
 * The main purpose of this class is to hold data.
 *
 * @property name Name of the subject (mandatory)
 * @property teacher Name of the subject's teacher (mandatory)
 * @property colorCode Subject will be marked with this color (mandatory)
 * @property id Unique ID, automatically set
 */
@Parcelize
data class Subject(
    var name: String,
    var teacher: String,
    var colorCode: Int,
    var id: String = ""
) : Parcelable {
    constructor() : this("", "", -1, "")
}