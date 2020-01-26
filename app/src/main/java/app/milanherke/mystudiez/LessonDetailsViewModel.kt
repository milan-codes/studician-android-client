package app.milanherke.mystudiez

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LessonDetailsViewModel(application: Application) : AndroidViewModel(application) {

    fun deleteLesson(lessonId: Long) {
        // Deleting a lesson on another thread
        // There is no need for the ViewModel to wait for the delete operation to complete so we can use this approach

        // Deleting lessons
        GlobalScope.launch {
            getApplication<Application>().contentResolver?.delete(
                LessonsContract.buildUriFromId(
                    lessonId
                ), null, null
            )
        }
    }

}