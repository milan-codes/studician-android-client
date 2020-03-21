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
 * This ViewModel was created to provide [MainActivity]
 * with the currently used subject, lesson, task or exam.
 * In Addition, this ViewModel contains a function that
 * gets all [Subject] objects from the database.
 */
class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val subject = MutableLiveData<Subject>()
    val subjectLiveData: LiveData<Subject>
        get() = subject

    private val lesson = MutableLiveData<Lesson>()
    val lessonLiveData: LiveData<Lesson>
        get() = lesson

    private val task = MutableLiveData<Task>()
    val taskLiveData: LiveData<Task>
        get() = task

    private val exam = MutableLiveData<Exam>()
    val examLiveData: LiveData<Exam>
        get() = exam

    /**
     * Classes that use [getAllSubjects]
     * must implement this interface to allow interactions
     * to be communicated between the classes.
     */
    interface OnDataRetrieved {
        fun onSuccess(subjects: MutableMap<String, Subject>)
    }

    /**
     * Updates [subject] with the passed in value.
     * [MainActivity] will be notified about the change,
     * because it's observing [subjectLiveData].
     *
     * @param newSubject New [Subject] to be used
     */
    fun swapSubject(newSubject: Subject) {
        subject.postValue(newSubject)
    }

    /**
     * Updates [lesson] with the passed in value.
     * [MainActivity] will be notified about the change,
     * because it's observing [lessonLiveData].
     *
     * @param newLesson New [Lesson] to be used
     */
    fun swapLesson(newLesson: Lesson) {
        lesson.postValue(newLesson)
    }

    /**
     * Updates [task] with the passed in value.
     * [MainActivity] will be notified about the change,
     * because it's observing [taskLiveData].
     *
     * @param newTask New [Task] to be used
     */
    fun swapTask(newTask: Task) {
        task.postValue(newTask)
    }

    /**
     * Updates [exam] with the passed in value.
     * [MainActivity] will be notified about the change,
     * because it's observing [examLiveData].
     *
     * @param newExam New [Exam] to be used
     */
    fun swapExam(newExam: Exam) {
        exam.postValue(newExam)
    }

    /**
     * This function gets all [Subject] objects from the database.
     *
     * @param listener Classes that use this function must implement [OnDataRetrieved]
     * @throws IllegalArgumentException when any of the retrieved subjects is null
     */
    fun getAllSubjects(listener: OnDataRetrieved) {
        val subjects: MutableMap<String, Subject> = mutableMapOf()
        GlobalScope.launch {
            val database = Firebase.database
            val ref = database.getReference("subjects/${FirebaseUtils.getUserId()}")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    throw IllegalStateException("There was an error while trying to read all subjects from database: $e")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    subjects.clear()
                    for (subjectSnapshot in dataSnapshot.children) {
                        val subject = subjectSnapshot.getValue<Subject>()
                        if (subject != null) {
                            subjects[subject.id] = subject
                        } else {
                            throw IllegalArgumentException("Cannot pass nullable item to subjects ArrayList")
                        }
                    }
                    listener.onSuccess(subjects)
                }
            })
        }
    }

}