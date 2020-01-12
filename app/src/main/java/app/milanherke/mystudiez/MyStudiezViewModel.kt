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


private const val TAG = "ViewModel"
class MyStudiezViewModel(application: Application) : AndroidViewModel(application) {

    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadSubjects()
        }
    }

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
    get() = databaseCursor

    init {
        getApplication<Application>().contentResolver.registerContentObserver(TasksContract.CONTENT_URI, true, contentObserver)
        loadSubjects()
    }

    override fun onCleared() {
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }

    fun loadSubjects() {
        val projection = arrayOf(SubjectsContract.Columns.SUBJECT_ID, SubjectsContract.Columns.SUBJECT_NAME, SubjectsContract.Columns.SUBJECT_TEACHER, SubjectsContract.Columns.SUBJECT_COLORCODE)
        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(SubjectsContract.CONTENT_URI, projection, null, null, null)
            databaseCursor.postValue(cursor)
        }
    }
}