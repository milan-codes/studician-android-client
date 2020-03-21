package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [AndroidViewModel] subclass.
 * This ViewModel was created to load all [Lesson], [Task] and [Exam] objects
 * for a specified date and it belongs to [OverviewFragment].
 */
class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val lessonsList = MutableLiveData<ArrayList<Lesson>>()
    val lessonsListLiveData: LiveData<ArrayList<Lesson>>
        get() = lessonsList

    private val tasksList = MutableLiveData<ArrayList<Task>>()
    val tasksListLiveData: LiveData<ArrayList<Task>>
        get() = tasksList

    private val examsList = MutableLiveData<ArrayList<Exam>>()
    val examsListLiveData: LiveData<ArrayList<Exam>>
        get() = examsList

    init {
        loadAllDetails(Date())
    }

    /**
     * Calls the necessary functions
     * to load all details.
     * (All lessons, tasks, exams)
     */
    fun loadAllDetails(date: Date) {
        loadLessons(date)
        loadTasks(date)
        loadExams(date)
    }

    /**
     * This function gets all lessons from the database
     * and passes the result to [lessonsList].
     *
     * @param date Specified date
     */
    @SuppressLint("SimpleDateFormat")
    private fun loadLessons(date: Date) {
        GlobalScope.launch {
            val cal = Calendar.getInstance()
            cal.time = date
            val numOfDay = cal.get(Calendar.DAY_OF_WEEK)
            val lessons: ArrayList<Lesson> = arrayListOf()
            val database = Firebase.database
            val ref = database.getReference("lessons/${FirebaseUtils.getUserId()}")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    throw IllegalStateException("There was an error while trying to load all lessons: $e")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (subjectSnapshot in dataSnapshot.children) {
                        for (lessonSnapshot in subjectSnapshot.children) {
                            if (lessonSnapshot != null) {
                                val lesson = lessonSnapshot.getValue<Lesson>()
                                if (lesson != null) {
                                    if (lesson.day == numOfDay) {
                                        lessons.add(lesson)
                                    }
                                }
                            }
                        }
                    }
                    lessonsList.postValue(lessons)
                }
            })
        }
    }

    /**
     * This function gets all tasks from the database
     * and passes the result to [tasksList]
     *
     * @param date Specified date
     */
    @SuppressLint("SimpleDateFormat")
    private fun loadTasks(date: Date) {
        GlobalScope.launch {
            val currentDate = SimpleDateFormat("dd/MM/yyyy").format(date)
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
                            if (taskSnapshot != null) {
                                val task = taskSnapshot.getValue<Task>()
                                Log.e("dddd", "task to string  ${task}")
                                if (task != null) {
                                    if (task.dueDate == currentDate) {
                                        tasks.add(task)
                                    }
                                }
                            }
                        }
                    }
                    tasksList.postValue(tasks)
                }
            })
        }
    }

    /**
     * This function gets all exams from the database
     * and passes the result to [examsList]
     *
     * @param date Specified date
     */
    @SuppressLint("SimpleDateFormat")
    private fun loadExams(date: Date) {
        GlobalScope.launch {
            val currentDate = SimpleDateFormat("dd/MM/yyyy").format(date)
            val exams: ArrayList<Exam> = arrayListOf()
            val database = Firebase.database
            val ref = database.getReference("exams/${FirebaseUtils.getUserId()}")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    throw IllegalStateException("There was an error while trying to load all lessons: $e")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (subjectSnapshot in dataSnapshot.children) {
                        for (examSnapshot in subjectSnapshot.children) {
                            val exam = examSnapshot.getValue<Exam>()
                            if (exam != null) {
                                if (exam.date == currentDate) {
                                    exams.add(exam)
                                }
                            }
                        }
                    }
                    examsList.postValue(exams)
                }
            })
        }
    }
}