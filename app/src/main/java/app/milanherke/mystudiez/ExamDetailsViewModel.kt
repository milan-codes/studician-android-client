package app.milanherke.mystudiez

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ExamDetailsViewModel(application: Application) : AndroidViewModel(application) {

    fun deleteExam(examId: Long) {
        // Deleting exam
        GlobalScope.launch {
            getApplication<Application>().contentResolver?.delete(
                ExamsContract.buildUriFromId(examId),
                null,
                null
            )
        }
    }

}