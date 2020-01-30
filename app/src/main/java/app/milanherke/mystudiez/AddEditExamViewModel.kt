package app.milanherke.mystudiez

import android.app.Application
import android.content.ContentValues
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddEditExamViewModel(application: Application) : AndroidViewModel(application) {

    fun saveExam(exam: Exam): Exam {
        val values = ContentValues()
        if (exam.name.isNotEmpty() && exam.subjectId != 0L && exam.date.isNotEmpty()) {
            values.put(ExamsContract.Columns.EXAM_NAME, exam.name)
            values.put(ExamsContract.Columns.EXAM_DESCRIPTION, exam.description)
            values.put(ExamsContract.Columns.EXAM_SUBJECT, exam.subjectId)
            values.put(ExamsContract.Columns.EXAM_DATE, exam.date)
            values.put(ExamsContract.Columns.EXAM_REMINDER, exam.reminder)

            if (exam.examId == 0L) {
                GlobalScope.launch {
                    val uri = getApplication<Application>().contentResolver.insert(
                        ExamsContract.CONTENT_URI,
                        values
                    )
                    if (uri != null) {
                        exam.examId = ExamsContract.getId(uri)
                    }
                }
            } else {
                // Exam already has an id, which means it has been created before so we are just updating it
                GlobalScope.launch {
                    getApplication<Application>().contentResolver.update(
                        ExamsContract.buildUriFromId(
                            exam.examId
                        ), values, null, null
                    )
                }
            }
        }

        return exam
    }

}