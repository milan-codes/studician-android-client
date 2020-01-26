package app.milanherke.mystudiez

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SubjectsViewModel(application: Application) : AndroidViewModel(application) {

    private val contentObserverSubject = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadSubjects()
        }
    }

    private val contentObserverSubjectLessons = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadLessonsForSubjects()
        }
    }

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursorSubjects: LiveData<Cursor>
        get() = databaseCursor

    private val databaseCursorLessons = MutableLiveData<Cursor>()
    val cursorLessons: LiveData<Cursor>
        get() = databaseCursorLessons

    init {
        // Register the content observer for subjects
        getApplication<Application>().contentResolver.registerContentObserver(
            SubjectsContract.CONTENT_URI,
            true,
            contentObserverSubject
        )

        // Register the content observer for lessons
        getApplication<Application>().contentResolver.registerContentObserver(
            LessonsContract.CONTENT_URI,
            true,
            contentObserverSubjectLessons
        )
    }

    override fun onCleared() {
        // Unregister the content observers
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverSubject
        )
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverSubjectLessons
        )
    }

    fun loadSubjects() {
        val projection = arrayOf(
            SubjectsContract.Columns.ID,
            SubjectsContract.Columns.SUBJECT_NAME,
            SubjectsContract.Columns.SUBJECT_TEACHER,
            SubjectsContract.Columns.SUBJECT_COLORCODE
        )
        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(
                SubjectsContract.CONTENT_URI,
                projection,
                null,
                null,
                "${SubjectsContract.Columns.SUBJECT_NAME} DESC"
            )
            databaseCursor.postValue(cursor)
        }
    }

    fun loadLessonsForSubjects() {
        val projection = arrayOf(
            LessonsContract.Columns.ID,
            LessonsContract.Columns.LESSON_SUBJECT,
            LessonsContract.Columns.LESSON_WEEK,
            LessonsContract.Columns.LESSON_DAY,
            LessonsContract.Columns.LESSON_STARTS,
            LessonsContract.Columns.LESSON_ENDS,
            LessonsContract.Columns.LESSON_LOCATION
        )
        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(
                LessonsContract.CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            databaseCursorLessons.postValue(cursor)
        }
    }
}