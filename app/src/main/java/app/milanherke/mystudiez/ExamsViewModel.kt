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

class ExamsViewModel(application: Application) : AndroidViewModel(application) {

    private val contentObserverExams = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadExams()
        }
    }

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursorExams: LiveData<Cursor>
        get() = databaseCursor

    init {
        // Register the content observer for exams
        getApplication<Application>().contentResolver.registerContentObserver(
            ExamsContract.CONTENT_URI,
            true,
            contentObserverExams
        )
    }

    override fun onCleared() {
        getApplication<Application>().contentResolver.unregisterContentObserver(
            contentObserverExams
        )
    }

    fun loadExams() {
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
                null,
                null,
                "${ExamsContract.Columns.ID} DESC"
            )
            databaseCursor.postValue(cursor)
        }
    }
}