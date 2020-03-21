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
 * This ViewModel was created to get all [Exam] objects from the database
 * and belongs to [ExamsFragment].
 */
class ExamsViewModel(application: Application) : AndroidViewModel(application) {

    private val examsList = MutableLiveData<ArrayList<Exam>>()
    val examsListLiveData: LiveData<ArrayList<Exam>>
        get() = examsList

    /**
     * Gets all exams from the database
     * and passes the result to [examsList]
     */
    fun loadExams() {
        GlobalScope.launch {
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
                                exams.add(exam)
                            }
                        }
                    }
                    examsList.postValue(exams)
                }
            })
        }
    }

}