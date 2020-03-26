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
 * This ViewModel was created to load a subject's
 * lessons, tasks and exams from the database
 * and it belongs to [SubjectDetailsFragment].
 */
class SubjectDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val selectedLessonsList = MutableLiveData<ArrayList<Lesson>>()
    val selectedLessonsLiveData: LiveData<ArrayList<Lesson>>
        get() = selectedLessonsList

    private val selectedTasksList = MutableLiveData<ArrayList<Task>>()
    val selectedTasksLiveData: LiveData<ArrayList<Task>>
        get() = selectedTasksList

    private val selectedExamsList = MutableLiveData<ArrayList<Exam>>()
    val selectedExamsLiveData: LiveData<ArrayList<Exam>>
        get() = selectedExamsList

    /**
     * Classes that use this ViewModel's functions
     * must implement this interface to allow interactions
     * to be communicated between the classes.
     */
    interface DataFetching {
        fun onLoad()
        fun onSuccess()
        fun onFailure(e: DatabaseError)
    }

    /**
     * Calls the necessary functions
     * to load all lessons, tasks and exams
     * of a subject.
     */
    fun loadAllDetails(subjectId: String, listener: DataFetching) {
        loadSelectedLessons(subjectId, listener)
        loadSelectedTasks(subjectId, listener)
        loadSelectedExams(subjectId, listener)
    }

    /**
     * Gets a selected subject's lessons.
     *
     * @param subjectId Selected subject
     * @param listener [DataFetching] interface to handle interaction events
     */
    private fun loadSelectedLessons(
        subjectId: String,
        listener: DataFetching
    ) {
        GlobalScope.launch {
            listener.onLoad()
            val lessons: ArrayList<Lesson> = arrayListOf()
            val database = Firebase.database
            val ref = database.getReference("lessons/${FirebaseUtils.getUserId()}/$subjectId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    listener.onFailure(e)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (lessonSnapshot in dataSnapshot.children) {
                        val lesson = lessonSnapshot.getValue<Lesson>()
                        if (lesson != null) {
                            lessons.add(lesson)
                        }
                    }
                    selectedLessonsList.postValue(lessons)
                    listener.onSuccess()
                }
            })
        }
    }

    /**
     * Gets a selected subject's tasks.
     *
     * @param subjectId Selected subject
     * @param listener [DataFetching] interface to handle interaction events
     */
    private fun loadSelectedTasks(
        subjectId: String,
        listener: DataFetching
    ) {
        GlobalScope.launch {
            listener.onLoad()
            val tasks: ArrayList<Task> = arrayListOf()
            val database = Firebase.database
            val ref = database.getReference("tasks/${FirebaseUtils.getUserId()}/$subjectId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    listener.onFailure(e)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (taskSnapshot in dataSnapshot.children) {
                        val task = taskSnapshot.getValue<Task>()
                        if (task != null) {
                            tasks.add(task)
                        }
                    }
                    selectedTasksList.postValue(tasks)
                    listener.onSuccess()
                }
            })
        }
    }

    /**
     * This function gets a selected subject's exam.
     *
     * @param subjectId Selected subject
     * @param listener [DataFetching] interface to handle interaction events
     */
    private fun loadSelectedExams(
        subjectId: String,
        listener: DataFetching
    ) {
        GlobalScope.launch {
            listener.onLoad()
            val exams: ArrayList<Exam> = arrayListOf()
            val database = Firebase.database
            val ref = database.getReference("exams/${FirebaseUtils.getUserId()}/$subjectId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    listener.onFailure(e)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (examSnapshot in dataSnapshot.children) {
                        val exam = examSnapshot.getValue<Exam>()
                        if (exam != null) {
                            exams.add(exam)
                        }
                    }
                    selectedExamsList.postValue(exams)
                    listener.onSuccess()
                }
            })
        }
    }

    /**
     * This function deletes a subject
     * and all of its lessons, tasks and exams
     *
     * @param subjectId Selected subject
     */
    fun deleteSubject(subjectId: String) {
        GlobalScope.launch {
            val database = Firebase.database
            database.getReference("subjects/${FirebaseUtils.getUserId()}/${subjectId}")
                .setValue(null)
            database.getReference("lessons/${FirebaseUtils.getUserId()}/${subjectId}")
                .setValue(null)
            database.getReference("tasks/${FirebaseUtils.getUserId()}/${subjectId}").setValue(null)
            database.getReference("exams/${FirebaseUtils.getUserId()}/${subjectId}").setValue(null)
        }
    }

}