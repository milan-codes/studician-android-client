package app.milanherke.mystudiez

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

object TasksContract {

    internal const val TABLE_NAME = "Tasks"

    /**
     * The uri used to access the Tasks table
     */
    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    /**
     * Fields
     */
    object Columns {
        const val ID = BaseColumns._ID
        const val TASK_NAME = "Name"
        const val TASK_DESCRIPTION = "Description"
        const val TASK_TYPE = "Type"
        const val TASK_SUBJECT = "Subject"
        const val TASK_DUEDATE = "DueDate"
        const val TASK_REMINDER = "Reminder"
    }

    fun getId(uri: Uri): Long {
        return ContentUris.parseId(uri)
    }

    fun buildUriFromId(id: Long): Uri {
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }

}