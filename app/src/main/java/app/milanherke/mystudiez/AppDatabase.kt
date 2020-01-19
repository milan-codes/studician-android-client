package app.milanherke.mystudiez

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.Exception
import java.lang.IllegalArgumentException

/**
 * Basic database class for the application
 * The only class that should use this is [AppProvider]
 */

private const val TAG = "AppDatabase"
private const val DATABASE_NAME = "MyStudiez.db"
private const val DATABASE_VERSION = 1

internal class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        Log.i(TAG, "onCreate: starts")
        val subjectSQL = createSubjectSQL()
        val lessonSQL = createLessonSQL()
        val taskSQL = createTaskSQL()
        val examSQL = createExamSQL()
        try {
            db?.execSQL("PRAGMA foreign_keys = ON;")
            db?.execSQL(subjectSQL)
            db?.execSQL(lessonSQL)
            db?.execSQL(taskSQL)
            db?.execSQL(examSQL)
            Log.i(TAG, "Tables created successfully")
        } catch (e: SQLiteException) {
            e.printStackTrace()
            throw SQLiteException("Unknown error occurred.")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.i(TAG, "onUpgrade: starts")
        when (oldVersion) {
            1 -> {
                // Haven't yet thought about upgrading database - upgrade logic yet to be decided
            }
            else -> throw IllegalArgumentException("onUpgrade() called with unknown new version: $newVersion")
        }
    }

    private fun createSubjectSQL(): String {
        try {
            val sSQL = """CREATE TABLE ${SubjectsContract.TABLE_NAME} (
                ${SubjectsContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
                ${SubjectsContract.Columns.SUBJECT_NAME} TEXT NOT NULL,
                ${SubjectsContract.Columns.SUBJECT_TEACHER} TEXT NOT NULL,
                ${SubjectsContract.Columns.SUBJECT_COLORCODE} INT NOT NULL);
            """.replaceIndent(" ")
            Log.i(TAG, sSQL)
            return sSQL
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Unknown exception occurred.")
        }
    }

    private fun createLessonSQL(): String {
        try {
            val sSQL = """CREATE TABLE ${LessonsContract.TABLE_NAME} (
                ${LessonsContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
                ${LessonsContract.Columns.LESSON_SUBJECT} INTEGER,
                ${LessonsContract.Columns.LESSON_WEEK} TEXT,
                ${LessonsContract.Columns.LESSON_DAY} TEXT NOT NULL,
                ${LessonsContract.Columns.LESSON_STARTS} TEXT NOT NULL,
                ${LessonsContract.Columns.LESSON_ENDS} TEXT NOT NULL,
                ${LessonsContract.Columns.LESSON_LOCATION} TEXT NOT NULL,
                FOREIGN KEY(${LessonsContract.Columns.LESSON_SUBJECT}) REFERENCES ${SubjectsContract.TABLE_NAME}(${SubjectsContract.Columns.ID}));
            """.replaceIndent(" ")
            Log.i(TAG, sSQL)
            return sSQL
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Unknown exception occurred.")
        }
    }

    private fun createTaskSQL(): String {
        try {
            val sSQL = """CREATE TABLE ${TasksContract.TABLE_NAME} (
                ${TasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
                ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
                ${TasksContract.Columns.TASK_DESCRIPTION} TEXT,
                ${TasksContract.Columns.TASK_TYPE} TEXT NOT NULL,
                ${TasksContract.Columns.TASK_SUBJECT} INTEGER,
                ${TasksContract.Columns.TASK_DUEDATE} TEXT NOT NULL,
                ${TasksContract.Columns.TASK_REMINDER} TEXT,
                FOREIGN KEY(${TasksContract.Columns.TASK_SUBJECT}) REFERENCES ${SubjectsContract.TABLE_NAME}(${SubjectsContract.Columns.ID}));
            """.replaceIndent(" ")
            Log.i(TAG, sSQL)
            return sSQL
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Unknown exception occurred.")
        }
    }

    private fun createExamSQL(): String {
        try {
            val sSQL = """CREATE TABLE ${ExamsContract.TABLE_NAME} (
                ${ExamsContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
                ${ExamsContract.Columns.EXAM_NAME} TEXT NOT NULL,
                ${ExamsContract.Columns.EXAM_DESCRIPTION} TEXT,
                ${ExamsContract.Columns.EXAM_SUBJECT} INTEGER,
                ${ExamsContract.Columns.EXAM_DATE} TEXT NOT NULL,
                ${ExamsContract.Columns.EXAM_REMINDER} TEXT,
                FOREIGN KEY(${ExamsContract.Columns.EXAM_SUBJECT}) REFERENCES ${SubjectsContract.TABLE_NAME}(${SubjectsContract.Columns.ID}));
            """.trimMargin()
            Log.i(TAG, sSQL)
            return sSQL
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Unknown exception occurred.")
        }
    }


    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)
}