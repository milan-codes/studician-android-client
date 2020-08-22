package app.milanherke.mystudiez.viewmodels.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.milanherke.mystudiez.FirebaseUtils.Companion.getUserId
import app.milanherke.mystudiez.models.Lesson
import app.milanherke.mystudiez.models.Subject
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
 * This ViewModel was created to load all
 * [Subject] and [Lesson] objects from the database.
 */
class SubjectsViewModel(application: Application) : AndroidViewModel(application) {

    private val subjectsList = MutableLiveData<ArrayList<Subject>>()
    val subjectsListLiveData: LiveData<ArrayList<Subject>>
        get() = subjectsList

    private val lessonsList = MutableLiveData<ArrayList<Lesson>>()
    val lessonsListLiveData: LiveData<ArrayList<Lesson>>
        get() = lessonsList

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
     * Gets all subjects from the database
     * and passes the result to [subjectsList].
     *
     * @param listener [DataFetching] interface to handle interaction events
     */
    fun loadSubjects(listener: DataFetching) {
        GlobalScope.launch {
            listener.onLoad()
            val subjects: ArrayList<Subject> = arrayListOf()
            val database = Firebase.database
            val ref = database.getReference("subjects/${getUserId()}")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    listener.onFailure(e)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (subjectSnapshot in dataSnapshot.children) {
                        val subject = subjectSnapshot.getValue<Subject>()
                        if (subject != null) {
                            subjects.add(subject)
                        }
                    }
                    subjectsList.postValue(subjects)
                    listener.onSuccess()
                }
            })
        }
    }

    /**
     * Gets all lessons from the database
     * and passes the result to [lessonsList].
     *
     * @param listener [DataFetching] interface to handle interaction events
     */
    fun loadLessonsForSubjects(listener: DataFetching) {
        GlobalScope.launch {
            listener.onLoad()
            val lessons: ArrayList<Lesson> = arrayListOf()
            val database = Firebase.database
            val ref = database.getReference("lessons/${getUserId()}")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    listener.onFailure(e)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (subjectSnapshot in dataSnapshot.children) {
                        for (lessonSnapshot in subjectSnapshot.children) {
                            val lesson = lessonSnapshot.getValue<Lesson>()
                            if (lesson != null) {
                                lessons.add(lesson)
                            }
                        }
                    }
                    lessonsList.postValue(lessons)
                    listener.onSuccess()
                }
            })

        }
    }
}