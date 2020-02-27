package app.milanherke.mystudiez

import android.app.Application
import android.content.ContentValues
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddEditLessonViewModel(application: Application) : AndroidViewModel(application) {

    fun saveLesson(lesson: Lesson): Lesson {
        val values = ContentValues()
        if (lesson.subjectId != 0L && lesson.day != 0 && lesson.starts.isNotEmpty() && lesson.ends.isNotEmpty() && lesson.location.isNotEmpty()) {
            values.put(LessonsContract.Columns.LESSON_SUBJECT, lesson.subjectId)
            values.put(LessonsContract.Columns.LESSON_DAY, lesson.day)
            values.put(LessonsContract.Columns.LESSON_STARTS, lesson.starts)
            values.put(LessonsContract.Columns.LESSON_ENDS, lesson.ends)
            values.put(LessonsContract.Columns.LESSON_LOCATION, lesson.location)

            if (lesson.lessonId == 0L) {
                GlobalScope.launch {
                    val uri = getApplication<Application>().contentResolver.insert(
                        LessonsContract.CONTENT_URI,
                        values
                    )
                    if (uri != null) {
                        lesson.lessonId = LessonsContract.getId(uri)
                    }
                }
            } else {
                // Lesson already has an id, which means it has been created before so we are just updating it
                GlobalScope.launch {
                    getApplication<Application>().contentResolver.update(
                        LessonsContract.buildUriFromId(
                            lesson.lessonId
                        ), values, null, null
                    )
                }
            }
        }

        return lesson
    }

}