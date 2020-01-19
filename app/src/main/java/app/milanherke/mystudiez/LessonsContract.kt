package app.milanherke.mystudiez

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

object LessonsContract {

    internal const val TABLE_NAME = "Lessons"

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
        const val ID = BaseColumns._ID
        const val LESSON_SUBJECT = "SubjectId"
        const val LESSON_WEEK = "Week"
        const val LESSON_DAY = "Day"
        const val LESSON_STARTS = "Starts"
        const val LESSON_ENDS = "Ends"
        const val LESSON_LOCATION = "Location"
    }

    fun getId(uri: Uri): Long {
        return ContentUris.parseId(uri)
    }

    fun buildUriFromId(id: Long): Uri {
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }
}