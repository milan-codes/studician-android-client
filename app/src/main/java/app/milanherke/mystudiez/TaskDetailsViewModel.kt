package app.milanherke.mystudiez

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [AndroidViewModel] subclass.
 * This ViewModel was created to delete [Task] objects in the database
 * and belongs to [TaskDetailsFragment].
 */
class TaskDetailsViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Use this function to delete a [Task].
     *
     * @param task Task that the user wants to delete
     */
    fun deleteTask(task: Task) {
        GlobalScope.launch {
            val database = Firebase.database
            database.getReference("tasks/${FirebaseUtils.getUserId()}/${task.subjectId}/${task.id}")
                .setValue(null)
        }
    }

}