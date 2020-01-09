package app.milanherke.mystudiez

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import java.lang.IllegalArgumentException

/**
 * ContentProvider for the app.
 * Only class that should know about [AppDatabase]
 */

private const val TAG = "AppProvider"

// Unique name of the provider
const val CONTENT_AUTHORITY = "app.milanherke.mystudiez.provider"

private const val SUBJECTS = 100
private const val SUBJECTS_ID = 101

private const val LESSONS = 200
private const val LESSONS_ID = 201

private const val TASKS = 300
private const val TASKS_ID = 301

private const val EXAMS = 400
private const val EXAMS_ID = 401

val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")
class AppProvider: ContentProvider() {

    private val uriMatcher by lazy {buildUriMatcher()}

    private fun buildUriMatcher(): UriMatcher {
        Log.i(TAG, "buildUriMatcher: starts")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)

        // e.g. content://app.milanherke.mystudiez.provider/Subjects
        matcher.addURI(CONTENT_AUTHORITY,SubjectsContract.TABLE_NAME, SUBJECTS)
        // e.g. content://app.milanherke.mystudiez.provider/Subjects/5
        matcher.addURI(CONTENT_AUTHORITY, "${SubjectsContract.TABLE_NAME}/#", SUBJECTS_ID)

        // e.g. content://app.milanherke.mystudiez.provider/Lessons
        matcher.addURI(CONTENT_AUTHORITY,LessonsContract.TABLE_NAME, LESSONS)
        // e.g. content://app.milanherke.mystudiez.provider/Lessons/5
        matcher.addURI(CONTENT_AUTHORITY, "${LessonsContract.TABLE_NAME}/#", LESSONS_ID)

        // e.g. content://app.milanherke.mystudiez.provider/Tasks
        matcher.addURI(CONTENT_AUTHORITY,TasksContract.TABLE_NAME, TASKS)
        // e.g. content://app.milanherke.mystudiez.provider/Tasks/5
        matcher.addURI(CONTENT_AUTHORITY, "${TasksContract.TABLE_NAME}/#", TASKS_ID)

        // e.g. content://app.milanherke.mystudiez.provider/Exams
        matcher.addURI(CONTENT_AUTHORITY,ExamsContract.TABLE_NAME, EXAMS)
        // e.g. content://app.milanherke.mystudiez.provider/Exams/5
        matcher.addURI(CONTENT_AUTHORITY, "${ExamsContract.TABLE_NAME}/#", EXAMS_ID)


        return matcher
    }

    override fun onCreate(): Boolean {
        Log.i(TAG, "onCreate: starts")
        return true
    }

    override fun getType(uri: Uri): String? {
        // Get MIME Type

        return when(uriMatcher.match(uri)) {

            SUBJECTS -> SubjectsContract.CONTENT_TYPE
            SUBJECTS_ID -> SubjectsContract.CONTENT_ITEM_TYPE

            LESSONS -> LessonsContract.CONTENT_TYPE
            LESSONS_ID -> LessonsContract.CONTENT_ITEM_TYPE

            TASKS -> TasksContract.CONTENT_TYPE
            TASKS_ID -> TasksContract.CONTENT_ITEM_TYPE

            EXAMS -> ExamsContract.CONTENT_TYPE
            EXAMS_ID -> ExamsContract.CONTENT_ITEM_TYPE

            else -> throw IllegalArgumentException("unknown Uri: $uri")
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        Log.i(TAG, "query: called with $uri")
        val match = uriMatcher.match(uri)
        Log.i(TAG, "query: match is $match")

        val queryBuilder = SQLiteQueryBuilder()

        when (match) {

            SUBJECTS -> queryBuilder.tables = SubjectsContract.TABLE_NAME
            SUBJECTS_ID -> {
                queryBuilder.tables = SubjectsContract.TABLE_NAME
                val subjectId = SubjectsContract.getId(uri)
                queryBuilder.appendWhere("${SubjectsContract.Columns.SUBJECT_ID} = ")
                queryBuilder.appendWhereEscapeString("$subjectId")
            }

            LESSONS -> queryBuilder.tables = LessonsContract.TABLE_NAME
            LESSONS_ID -> {
                queryBuilder.tables = LessonsContract.TABLE_NAME
                val lessonId = LessonsContract.getId(uri)
                queryBuilder.appendWhere("${LessonsContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$lessonId")
            }

            TASKS -> queryBuilder.tables = TasksContract.TABLE_NAME
            TASKS_ID -> {
                queryBuilder.tables = TasksContract.TABLE_NAME
                val taskId = TasksContract.getId(uri)
                queryBuilder.appendWhere("${TasksContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$taskId")
            }

            EXAMS -> queryBuilder.tables = ExamsContract.TABLE_NAME
            EXAMS_ID -> {
                queryBuilder.tables = ExamsContract.TABLE_NAME
                val examId = ExamsContract.getId(uri)
                queryBuilder.appendWhere("${ExamsContract.Columns.EXAM_ID} = ")
                queryBuilder.appendWhereEscapeString("$examId")
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        val db = AppDatabase.getInstance(context!!).readableDatabase
        val cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, null)
        Log.i(TAG, "query: rows in returned cursor = ${cursor.count}") // TODO remove this line

        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.i(TAG, "insert: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.i(TAG, "insert: match is $match")

        val recordId: Long
        val returnUri: Uri

        when (match) {

            SUBJECTS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                recordId = db.insert(SubjectsContract.TABLE_NAME, null, values)
                if (recordId != -1L){
                    returnUri = SubjectsContract.buildUriFromId(recordId)
                }else {
                    throw SQLException("Failed to insert, Uri was $uri")
                }
            }

            LESSONS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                recordId = db.insert(LessonsContract.TABLE_NAME, null, values)
                if (recordId != -1L){
                    returnUri = LessonsContract.buildUriFromId(recordId)
                }else {
                    throw SQLException("Failed to insert, Uri was $uri")
                }
            }

            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                recordId = db.insert(TasksContract.TABLE_NAME, null, values)
                if (recordId != -1L){
                    returnUri = TasksContract.buildUriFromId(recordId)
                }else {
                    throw SQLException("Failed to insert, Uri was $uri")
                }
            }

            EXAMS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                recordId = db.insert(ExamsContract.TABLE_NAME, null, values)
                if (recordId != -1L){
                    returnUri = ExamsContract.buildUriFromId(recordId)
                }else {
                    throw SQLException("Failed to insert, Uri was $uri")
                }
            }

            else -> throw IllegalArgumentException("Unknown uri: $uri")

        }
        // TODO: notify content resolver if something has changed
        Log.i(TAG, "Exiting insert, returning $returnUri")
        return returnUri
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        Log.i(TAG, "update: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.i(TAG, "update: match is $match")

        val count: Int
        var selectionCriteria: String

        when (match) {

            SUBJECTS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.update(SubjectsContract.TABLE_NAME, values, selection, selectionArgs)
            }
            SUBJECTS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = SubjectsContract.getId(uri)
                selectionCriteria = "${SubjectsContract.Columns.SUBJECT_ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count =
                    db.update(SubjectsContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }

            LESSONS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.update(LessonsContract.TABLE_NAME, values, selection, selectionArgs)
            }
            LESSONS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = LessonsContract.getId(uri)
                selectionCriteria = "${LessonsContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count =
                    db.update(LessonsContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }

            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs)
            }
            TASKS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count =
                    db.update(TasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }

            EXAMS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.update(ExamsContract.TABLE_NAME, values, selection, selectionArgs)
            }
            EXAMS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = ExamsContract.getId(uri)
                selectionCriteria = "${ExamsContract.Columns.EXAM_ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count =
                    db.update(ExamsContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }

            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }

        // TODO: notify content resolver if something has changed
        Log.i(TAG, "Exiting update, returning $count")
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        Log.i(TAG, "delete: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.i(TAG, "delete: match is $match")

        val count: Int
        var selectionCriteria: String

        when (match) {

            SUBJECTS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.delete(SubjectsContract.TABLE_NAME, selection, selectionArgs)
            }
            SUBJECTS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = SubjectsContract.getId(uri)
                selectionCriteria = "${SubjectsContract.Columns.SUBJECT_ID} = $id"

                if(selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count = db.delete(SubjectsContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }

            LESSONS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.delete(LessonsContract.TABLE_NAME, selection, selectionArgs)
            }
            LESSONS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = LessonsContract.getId(uri)
                selectionCriteria = "${LessonsContract.Columns.ID} = $id"

                if(selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count = db.delete(LessonsContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }

            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs)
            }
            TASKS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"

                if(selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count = db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }

            EXAMS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.delete(ExamsContract.TABLE_NAME, selection, selectionArgs)
            }
            EXAMS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = ExamsContract.getId(uri)
                selectionCriteria = "${ExamsContract.Columns.EXAM_ID} = $id"

                if(selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count = db.delete(ExamsContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }

            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }

        // TODO: notify content resolver if something has changed
        Log.i(TAG, "Exiting delete, returning count")
        return count
    }
}