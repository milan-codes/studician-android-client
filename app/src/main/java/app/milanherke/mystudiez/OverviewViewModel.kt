package app.milanherke.mystudiez

import android.annotation.SuppressLint
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
import java.text.SimpleDateFormat
import java.util.*

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val contentObserverLessons = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadLessons(dateFilter.value!!)
        }
    }

    private val contentObserverTasks = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadTasks(dateFilter.value!!)
        }
    }

    private val contentObserverExams = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadExams(dateFilter.value!!)
        }
    }

    val dateFilter = MutableLiveData<Date>()

    private val databaseCursorLessons = MutableLiveData<Cursor>()
    val cursorLessons: LiveData<Cursor>
        get() = databaseCursorLessons

    private val databaseCursorExams = MutableLiveData<Cursor>()
    val cursorExams: LiveData<Cursor>
        get() = databaseCursorExams


    private val databaseCursorTasks = MutableLiveData<Cursor>()
    val cursorTasks: LiveData<Cursor>
        get() = databaseCursorTasks

    init {
        // Register the content observer for lessons
        getApplication<Application>().contentResolver.registerContentObserver(
            LessonsContract.CONTENT_URI,
            true,
            contentObserverLessons
        )

        // Register the content observer for tasks
        getApplication<Application>().contentResolver.registerContentObserver(
            TasksContract.CONTENT_URI,
            true,
            contentObserverTasks
        )

        // Register the content observer for exams
        getApplication<Application>().contentResolver.registerContentObserver(
            ExamsContract.CONTENT_URI,
            true,
            contentObserverExams
        )
    }

    override fun onCleared() {
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverLessons
        )
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverTasks
        )
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverExams
        )
    }

    fun loadAllDetails(date: Date) {
        loadLessons(date)
        loadTasks(date)
        loadExams(date)
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadLessons(date: Date) {
        val cal = Calendar.getInstance()
        cal.time = date
        val numOfDay = cal.get(Calendar.DAY_OF_WEEK)
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
                "${LessonsContract.Columns.LESSON_DAY} = ?",
                arrayOf(numOfDay.toString()),
                "${LessonsContract.Columns.LESSON_STARTS} ASC"
            )
            databaseCursorLessons.postValue(cursor)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadTasks(date: Date) {
        val currentDate = SimpleDateFormat("dd/MM/yyyy").format(date)
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
                "${TasksContract.Columns.TASK_DUEDATE} = ?",
                arrayOf(currentDate),
                "${TasksContract.Columns.TASK_NAME} DESC"
            )
            databaseCursorTasks.postValue(cursor)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadExams(date: Date) {
        val currentDate = SimpleDateFormat("dd/MM/yyyy").format(date)
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
                "${ExamsContract.Columns.EXAM_DATE} = ?",
                arrayOf(currentDate),
                "${ExamsContract.Columns.ID} DESC"
            )
            databaseCursorExams.postValue(cursor)
        }
    }
}