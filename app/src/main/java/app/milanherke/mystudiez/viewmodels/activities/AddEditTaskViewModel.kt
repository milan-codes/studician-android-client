package app.milanherke.mystudiez.viewmodels.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.milanherke.mystudiez.FirebaseUtils
import app.milanherke.mystudiez.models.Task
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [AndroidViewModel] subclass.
 * This ViewModel was created to save/update [Task] objects in the database
 * and belongs to [AddEditTaskActivity].
 */
class AddEditTaskViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Use this function to save/update a [Task].
     *
     * @param task Task that the user wants to save
     * @return Saved task
     */
    fun saveTask(task: Task): Task {
        GlobalScope.launch {
            val database = Firebase.database
            // If the task's ID is empty, we're creating a new task, otherwise we're updating an existing one
            if (task.id.isEmpty()) {
                val key = database.getReference("subjects/${FirebaseUtils.getUserId()}").push().key
                if (key != null) {
                    task.id = key
                    database.getReference("tasks/${FirebaseUtils.getUserId()}/${task.subjectId}/$key")
                        .setValue(task)
                }
            } else {
                database.getReference("tasks/${FirebaseUtils.getUserId()}/${task.subjectId}/${task.id}")
                    .setValue(task)
            }
        }
        return task
    }

}