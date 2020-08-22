package app.milanherke.mystudiez

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.milanherke.mystudiez.models.Exam
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
 * This ViewModel was created to get all [Exam] objects from the database
 * and belongs to [app.milanherke.mystudiez.fragments.ExamsFragment].
 */
class ExamsViewModel(application: Application) : AndroidViewModel(application) {

    private val examsList = MutableLiveData<ArrayList<Exam>>()
    val examsListLiveData: LiveData<ArrayList<Exam>>
        get() = examsList

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
     * Gets all exams from the database
     * and passes the result to [examsList].
     *
     * @param listener [DataFetching] interface to handle interaction events
     */
    fun loadExams(listener: DataFetching) {
        GlobalScope.launch {
            listener.onLoad()
            val exams: ArrayList<Exam> = arrayListOf()
            val database = Firebase.database
            val ref = database.getReference("exams/${FirebaseUtils.getUserId()}")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    listener.onFailure(e)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (subjectSnapshot in dataSnapshot.children) {
                        for (examSnapshot in subjectSnapshot.children) {
                            val exam = examSnapshot.getValue<Exam>()
                            if (exam != null) {
                                exams.add(exam)
                            }
                        }
                    }
                    examsList.postValue(exams)
                    listener.onSuccess()
                }
            })
        }
    }

}