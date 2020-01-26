package app.milanherke.mystudiez

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import java.lang.Exception
import java.lang.IllegalStateException

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    fun subjectFromId(id: Long): Subject? {
        val projection = arrayOf(
            SubjectsContract.Columns.ID,
            SubjectsContract.Columns.SUBJECT_NAME,
            SubjectsContract.Columns.SUBJECT_TEACHER,
            SubjectsContract.Columns.SUBJECT_COLORCODE
        )

        val cursor = getApplication<Application>().contentResolver.query(
            SubjectsContract.buildUriFromId(id),
            projection,
            null,
            null,
            null
        )

        if (cursor != null) {
            if (!cursor.moveToNext()) {
                throw IllegalStateException("Couldn't move cursor to position")
            }
            try {
                val subject = Subject(
                    cursor.getString(cursor.getColumnIndex(SubjectsContract.Columns.SUBJECT_NAME)),
                    cursor.getString(cursor.getColumnIndex(SubjectsContract.Columns.SUBJECT_TEACHER)),
                    cursor.getInt(cursor.getColumnIndex(SubjectsContract.Columns.SUBJECT_COLORCODE))
                )
                // Id is not set in the instructor
                subject.subjectId =
                    cursor.getLong(cursor.getColumnIndex(SubjectsContract.Columns.ID))
                return subject
            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception(e)
            } finally {
                cursor.close()
            }
        }
        return null
    }


}