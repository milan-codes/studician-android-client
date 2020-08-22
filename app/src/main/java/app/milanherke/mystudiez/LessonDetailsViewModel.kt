package app.milanherke.mystudiez

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.milanherke.mystudiez.models.Lesson
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [AndroidViewModel] subclass.
 * This ViewModel was created to delete [Lesson] objects in the database
 * and belongs to [app.milanherke.mystudiez.fragments.LessonDetailsFragment].
 */
class LessonDetailsViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Use this function to delete a [Lesson].
     *
     * @param lesson Lesson that the user wants to delete
     */
    fun deleteLesson(lesson: Lesson) {
        GlobalScope.launch {
            val database = Firebase.database
            database.getReference("lessons/${FirebaseUtils.getUserId()}/${lesson.subjectId}/${lesson.id}")
                .setValue(null)
        }
    }

}