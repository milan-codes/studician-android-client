package app.milanherke.mystudiez

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TaskDetailsViewModel(application: Application) : AndroidViewModel(application) {

    fun deleteTask(taskId: Long) {
        // Deleting task
        GlobalScope.launch {
            getApplication<Application>().contentResolver?.delete(
                TasksContract.buildUriFromId(
                    taskId
                ), null, null
            )
        }
    }

}