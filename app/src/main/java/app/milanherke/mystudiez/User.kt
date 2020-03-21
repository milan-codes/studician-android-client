package app.milanherke.mystudiez

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Data [Parcelable] subclass.
 * The main purpose of this class is to hold data.
 *
 * @property name Name of the user, automatically set when logging in
 * @property email Email of the user, automatically set when logging in
 */
@Parcelize
data class User(
    var name: String,
    var email: String
) : Parcelable