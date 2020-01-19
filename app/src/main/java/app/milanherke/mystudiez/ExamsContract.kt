package app.milanherke.mystudiez

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

object ExamsContract {

    internal const val TABLE_NAME = "Exams"

    /**
     * The uri which is used to access the database
     */
    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    /**
     * Fields
     */
    object Columns {
        const val ID = BaseColumns._ID
        const val EXAM_NAME = "Name"
        const val EXAM_DESCRIPTION = "Description"
        const val EXAM_SUBJECT = "SubjectId"
        const val EXAM_DATE = "ExamDate"
        const val EXAM_REMINDER = "ExamReminder"
    }

    fun getId(uri: Uri): Long {
        return ContentUris.parseId(uri)
    }

    fun buildUriFromId(id: Long): Uri {
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }
}