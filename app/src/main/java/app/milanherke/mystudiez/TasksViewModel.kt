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

class TasksViewModel(application: Application) : AndroidViewModel(application){

    private val contentObserverTasks = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadTasks()
        }
    }

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursorTasks: LiveData<Cursor>
        get() = databaseCursor

    init {
        // Register the content observer for tasks
        getApplication<Application>().contentResolver.registerContentObserver(
            TasksContract.CONTENT_URI,
            true,
            contentObserverTasks
        )
    }

    override fun onCleared() {
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverTasks
        )
    }

    fun loadTasks() {
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
                null,
                null,
                "${TasksContract.Columns.ID} DESC"
            )
            databaseCursor.postValue(cursor)
        }
    }

}