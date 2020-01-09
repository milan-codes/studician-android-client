package app.milanherke.mystudiez

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

object SubjectsContract {

    internal const val TABLE_NAME = "Subjects"

    /**
     * The uri which is used to access the Tasks table
     */
    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    /**
     * Fields
     */
    object Columns {
        const val SUBJECT_ID = BaseColumns._ID
        const val SUBJECT_NAME = "Name"
        const val SUBJECT_TEACHER = "Teacher"
        const val SUBJECT_COLORCODE = "ColorCode"
    }

    fun getId(uri: Uri): Long {
        return ContentUris.parseId(uri)
    }

    fun buildUriFromId(id: Long): Uri {
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }

}