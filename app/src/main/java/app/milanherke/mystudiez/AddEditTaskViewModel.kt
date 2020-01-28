package app.milanherke.mystudiez

import android.app.Application
import android.content.ContentValues
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddEditTaskViewModel(application: Application) : AndroidViewModel(application) {

    fun saveTask(task: Task): Task {
        val values = ContentValues()
        if (task.name.isNotEmpty() && task.type.isNotEmpty() && task.subjectId != 0L && task.dueDate.isNotEmpty()) {
            values.put(TasksContract.Columns.TASK_NAME, task.name)
            values.put(TasksContract.Columns.TASK_DESCRIPTION, task.description)
            values.put(TasksContract.Columns.TASK_TYPE, task.type)
            values.put(TasksContract.Columns.TASK_SUBJECT, task.subjectId)
            values.put(TasksContract.Columns.TASK_DUEDATE, task.dueDate)
            values.put(TasksContract.Columns.TASK_REMINDER, task.reminder)

            if (task.taskId == 0L) {
                GlobalScope.launch {
                    val uri = getApplication<Application>().contentResolver.insert(
                        TasksContract.CONTENT_URI,
                        values
                    )
                    if (uri != null) {
                        task.taskId = TasksContract.getId(uri)
                    }
                }
            } else {
                // Task already has an id, which means it has been created before so we are just updating it
                GlobalScope.launch {
                    getApplication<Application>().contentResolver.update(
                        TasksContract.buildUriFromId(
                            task.taskId
                        ), values, null, null
                    )
                }
            }
        }

        return task
    }

}