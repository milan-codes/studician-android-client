package app.milanherke.mystudiez

import android.app.Application
import android.content.ContentValues
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddEditSubjectViewModel(application: Application) : AndroidViewModel(application) {

    fun saveSubject(subject: Subject): Subject {
        val values = ContentValues()
        if(subject.name.isNotEmpty() && subject.teacher.isNotEmpty()) {
            values.put(SubjectsContract.Columns.SUBJECT_NAME, subject.name)
            values.put(SubjectsContract.Columns.SUBJECT_TEACHER, subject.teacher)
            values.put(SubjectsContract.Columns.SUBJECT_COLORCODE, subject.colorCode)

            if (subject.subjectId == 0L) {
                GlobalScope.launch {
                    val uri = getApplication<Application>().contentResolver.insert(SubjectsContract.CONTENT_URI, values)
                    if (uri != null) {
                        subject.subjectId = SubjectsContract.getId(uri)
                    }
                }
            } else {
                // Subject already has an id, which means it has been created before so we are just updating it
                GlobalScope.launch {
                    getApplication<Application>().contentResolver.update(SubjectsContract.buildUriFromId(subject.subjectId), values, null, null)
                }
            }
        }

        return subject
    }

}