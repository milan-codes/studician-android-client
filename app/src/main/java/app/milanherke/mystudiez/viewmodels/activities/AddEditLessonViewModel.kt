package app.milanherke.mystudiez.viewmodels.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.milanherke.mystudiez.FirebaseUtils
import app.milanherke.mystudiez.models.Lesson
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [AndroidViewModel] subclass.
 * This ViewModel was created to save/update [Lesson] objects in the database
 * and belongs to [AddEditLessonActivity].
 */
class AddEditLessonViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Use this function to save/update a [Lesson].
     *
     * @param lesson Lesson that the user wants to save
     * @return Saved lesson
     */
    fun saveLesson(lesson: Lesson): Lesson {
        GlobalScope.launch {
            val database = Firebase.database
            // If the lessons's ID is empty, we're creating a new lesson, otherwise, we're updating an existing one
            if (lesson.id.isEmpty()) {
                val key = database.getReference("subjects/${FirebaseUtils.getUserId()}").push().key
                if (key != null) {
                    lesson.id = key
                    database.getReference("lessons/${FirebaseUtils.getUserId()}/${lesson.subjectId}/$key")
                        .setValue(lesson)
                }
            } else {
                database.getReference("lessons/${FirebaseUtils.getUserId()}/${lesson.subjectId}/${lesson.id}")
                    .setValue(lesson)
            }
        }
        return lesson
    }

}