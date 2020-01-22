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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SubjectDetailsViewModel(application: Application) : AndroidViewModel(application) {

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

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean>
        get() = _loading

    // Live Data
    val subjectFilter = MutableLiveData<Long>()

    private val databaseCursorSelectedLessons = MutableLiveData<Cursor>()
    val cursorSelectedLessons: LiveData<Cursor>
        get() = databaseCursorSelectedLessons

    private val databaseCursorSelectedTasks = MutableLiveData<Cursor>()
    val cursorSelectedTasks: LiveData<Cursor>
        get() = databaseCursorSelectedTasks

    private val databaseCursorSelectedExams = MutableLiveData<Cursor>()
    val cursorSelectedExams: LiveData<Cursor>
        get() = databaseCursorSelectedExams


    // Register the Content Observers
    init {
        getApplication<Application>().contentResolver.registerContentObserver(
            LessonsContract.CONTENT_URI,
            true,
            contentObserverSelectedLessons
        )
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
    }

    // Unregister the content observers
    override fun onCleared() {
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverSelectedLessons
        )
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverSelectedTasks
        )
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverSelectedExams
        )
    }

    fun loadAllDetails(selected: Long) {
        _loading.postValue(true)
        loadSelectedLessons(selected)
        loadSelectedTasks(selected)
        loadSelectedExams(selected)
        _loading.postValue(false)
    }

    private fun loadSelectedLessons(selected: Long) {
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
                "${LessonsContract.Columns.LESSON_SUBJECT} = ?",
                arrayOf(selected.toString()),
                null
            )
            databaseCursorSelectedLessons.postValue(cursor)
        }
    }

    private fun loadSelectedTasks(selected: Long) {
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
                arrayOf(selected.toString()),
                null
            )
            databaseCursorSelectedTasks.postValue(cursor)
        }
    }

    private fun loadSelectedExams(selected: Long) {
        val projection = arrayOf(
            ExamsContract.Columns.ID,
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
                arrayOf(selected.toString()),
                null
            )
            databaseCursorSelectedExams.postValue(cursor)
        }
    }

    fun deleteSubject(subjectId: Long) {
        // Deleting a task on another thread
        // There is no need for the ViewModel to wait for the delete operation to complete so we can use this approach

        // Deleting associated classes, tasks, exams
        GlobalScope.launch {
            getApplication<Application>().contentResolver?.delete(LessonsContract.CONTENT_URI, "SubjectId = ?", arrayOf(subjectId.toString()))
            getApplication<Application>().contentResolver?.delete(TasksContract.CONTENT_URI, "SubjectId = ?", arrayOf(subjectId.toString()))
            getApplication<Application>().contentResolver?.delete(ExamsContract.CONTENT_URI, "SubjectId = ?", arrayOf(subjectId.toString()))
        }

        // Deleting subject
        GlobalScope.launch {
            getApplication<Application>().contentResolver?.delete(SubjectsContract.buildUriFromId(subjectId), null, null)
        }
    }

}