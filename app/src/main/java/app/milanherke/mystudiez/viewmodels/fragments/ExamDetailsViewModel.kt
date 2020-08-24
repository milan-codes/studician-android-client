package app.milanherke.mystudiez.viewmodels.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.milanherke.mystudiez.utils.FirebaseUtils
import app.milanherke.mystudiez.models.Exam
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [AndroidViewModel] subclass.
 * This ViewModel was created to delete [Exam] objects in the database
 * and belongs to [app.milanherke.mystudiez.fragments.ExamDetailsFragment].
 */
class ExamDetailsViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Use this function to delete an [Exam].
     *
     * @param exam Exam that the user wants to delete
     */
    fun deleteExam(exam: Exam) {
        GlobalScope.launch {
            val database = Firebase.database
            database.getReference("exams/${FirebaseUtils.getUserId()}/${exam.subjectId}/${exam.id}")
                .setValue(null)
        }
    }

}