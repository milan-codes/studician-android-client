package app.milanherke.mystudiez

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [AndroidViewModel] subclass.
 * This ViewModel was created to get all [Task] objects from the database
 * and belongs to [TasksViewModel].
 */
class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val tasksList = MutableLiveData<ArrayList<Task>>()
    val tasksListLiveData: LiveData<ArrayList<Task>>
        get() = tasksList

    /**
     * Gets all tasks from the database
     * and passes the result to [tasksList]
     */
    fun loadTasks() {
        GlobalScope.launch {
            val tasks: ArrayList<Task> = arrayListOf()
            val database = Firebase.database
            val ref = database.getReference("tasks/${FirebaseUtils.getUserId()}")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    throw IllegalStateException("There was an error while trying to load all lessons: $e")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (subjectSnapshot in dataSnapshot.children) {
                        for (taskSnapshot in subjectSnapshot.children) {
                            val task = taskSnapshot.getValue<Task>()
                            if (task != null) {
                                tasks.add(task)
                            }
                        }
                    }
                    tasksList.postValue(tasks)
                }
            })
        }
    }

}