package app.milanherke.mystudiez

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyStudiezViewModel(application: Application) : AndroidViewModel(application) {

    private val contentObserverSubject = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (uri!! == LessonsContract.CONTENT_URI) {
                loadSubjects()
            }
        }
    }

    private val contentObserverLesson = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadLessons()
        }
    }

    private val contentObserverSelectedLessons = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadSelectedLessons(subjectFilter.value!!)
        }
    }

    private val contentObserverSelectedTasks = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadSelectedTasks(subjectFilter.value!!)
        }
    }

    private val contentObserverSelectedExams = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadSelectedExams(subjectFilter.value!!)
        }
    }

    val subjectFilter = MutableLiveData<String>("")

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursorSubjects: LiveData<Cursor>
        get() = databaseCursor

    private val databaseCursorLessons = MutableLiveData<Cursor>()
    val cursorLessons: LiveData<Cursor>
        get() = databaseCursorLessons

    private val databaseCursorSelectedLessons = MutableLiveData<Cursor>()
    val cursorSelectedLessons: LiveData<Cursor>
        get() = databaseCursorSelectedLessons

    private val databaseCursorSelectedTasks = MutableLiveData<Cursor>()
    val cursorSelectedTasks: LiveData<Cursor>
        get() = databaseCursorSelectedTasks

    private val databaseCursorSelectedExams = MutableLiveData<Cursor>()
    val cursorSelectedExams: LiveData<Cursor>
        get() = databaseCursorSelectedExams


    init {
        // Register the content observer for subjects
        getApplication<Application>().contentResolver.registerContentObserver(
            SubjectsContract.CONTENT_URI,
            true,
            contentObserverSubject
        )
        loadSubjects()

        // Register the content observer for lessons
        getApplication<Application>().contentResolver.registerContentObserver(
            LessonsContract.CONTENT_URI,
            true,
            contentObserverLesson
        )
        loadLessons()

        // Register content observer to load lessons into subject details
        getApplication<Application>().contentResolver.registerContentObserver(
            LessonsContract.CONTENT_URI,
            true,
            contentObserverSelectedLessons
        )
        // Register content observer to load tasks into subject details
        getApplication<Application>().contentResolver.registerContentObserver(
            TasksContract.CONTENT_URI,
            true,
            contentObserverSelectedLessons
        )
        getApplication<Application>().contentResolver.registerContentObserver(
            ExamsContract.CONTENT_URI,
            true,
            contentObserverSelectedExams
        )
        loadAllDetails(subjectFilter.value!!)
    }

    override fun onCleared() {
        // Unregister the content observers
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverSubject
        )
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverLesson
        )
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverSelectedLessons
        )
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverSelectedTasks
        )
    }

    fun loadSubjects() {
        val projection = arrayOf(
            SubjectsContract.Columns.SUBJECT_ID,
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

    fun loadLessons() {
        val projection = arrayOf(
            LessonsContract.Columns.ID,
            LessonsContract.Columns.LESSON_NAME,
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

    fun loadAllDetails(selected: String) {
        loadSelectedLessons(selected)
        loadSelectedTasks(selected)
        loadSelectedExams(selected)
    }

    fun loadSelectedLessons(selected: String) {
        val projection = arrayOf(
            LessonsContract.Columns.ID,
            LessonsContract.Columns.LESSON_NAME,
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
                "Name = ?",
                arrayOf(selected),
                null
            )
            databaseCursorSelectedLessons.postValue(cursor)
        }
    }

    fun loadSelectedTasks(selected: String) {
        val projection = arrayOf(
            TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESCRIPTION,
            TasksContract.Columns.TASK_TYPE,
            TasksContract.Columns.TASK_SUBJECT,
            TasksContract.Columns.TASK_DUEDATE,
            TasksContract.Columns.TASK_REMINDER
        )
        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(
                TasksContract.CONTENT_URI,
                projection,
                "${TasksContract.Columns.TASK_SUBJECT} = ?",
                arrayOf(selected),
                null
            )
            databaseCursorSelectedTasks.postValue(cursor)
        }
    }

    fun loadSelectedExams(selected: String) {
        val projection = arrayOf(
            ExamsContract.Columns.EXAM_ID,
            ExamsContract.Columns.EXAM_NAME,
            ExamsContract.Columns.EXAM_DESCRIPTION,
            ExamsContract.Columns.EXAM_SUBJECT,
            ExamsContract.Columns.EXAM_DATE,
            ExamsContract.Columns.EXAM_REMINDER
        )
        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(
                ExamsContract.CONTENT_URI,
                projection,
                "${ExamsContract.Columns.EXAM_SUBJECT} = ?",
                arrayOf(selected),
                null
            )
            databaseCursorSelectedExams.postValue(cursor)
        }
    }
}