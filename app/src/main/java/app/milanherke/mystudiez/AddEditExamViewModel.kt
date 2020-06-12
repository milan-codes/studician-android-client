package app.milanherke.mystudiez

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [AndroidViewModel] subclass.
 * This ViewModel was created to save/update [Exam] objects in the database
 * and belongs to [AddEditExamActivity].
 */
class AddEditExamViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Use this function to save/update an [Exam].
     *
     * @param exam Exam that the user wants to save
     * @return Saved exam
     */
    fun saveExam(exam: Exam): Exam {
        GlobalScope.launch {
            val database = Firebase.database
            // If the exam's ID is empty, we're creating a new exam, otherwise, we're updating an existing one
            if (exam.id.isEmpty()) {
                val key = database.getReference("subjects/${FirebaseUtils.getUserId()}").push().key
                if (key != null) {
                    exam.id = key
                    database.getReference("exams/${FirebaseUtils.getUserId()}/${exam.subjectId}/$key")
                        .setValue(exam)
                }
            } else {
                database.getReference("exams/${FirebaseUtils.getUserId()}/${exam.subjectId}/${exam.id}")
                    .setValue(exam)
            }
        }
        return exam
    }

}