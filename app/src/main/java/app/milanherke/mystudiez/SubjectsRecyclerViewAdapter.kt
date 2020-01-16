package app.milanherke.mystudiez

import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.subject_list_items.view.*
import java.lang.Exception
import java.lang.IllegalStateException

class SubjectViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(subject: Subject, days: ArrayList<String>) {
        containerView.sli_name.text = subject.name
        for (day in days) {
            when (day) {
                "Monday" -> {
                    containerView.sli_monday.setBackgroundResource(R.drawable.has_lesson_on_day)
                    containerView.sli_monday.setTextColor(
                        ContextCompat.getColor(
                            containerView.context,
                            R.color.colorBackgroundPrimary
                        )
                    )
                }
                "Tuesday" -> {
                    containerView.sli_tuesday.setBackgroundResource(R.drawable.has_lesson_on_day)
                    containerView.sli_tuesday.setTextColor(
                        ContextCompat.getColor(
                            containerView.context,
                            R.color.colorBackgroundPrimary
                        )
                    )
                }
                "Wednesday" -> {
                    containerView.sli_wednesday.setBackgroundResource(R.drawable.has_lesson_on_day)
                    containerView.sli_wednesday.setTextColor(
                        ContextCompat.getColor(
                            containerView.context,
                            R.color.colorBackgroundPrimary
                        )
                    )
                }
                "Thursday" -> {
                    containerView.sli_thursday.setBackgroundResource(R.drawable.has_lesson_on_day)
                    containerView.sli_thursday.setTextColor(
                        ContextCompat.getColor(
                            containerView.context,
                            R.color.colorBackgroundPrimary
                        )
                    )
                }
                "Friday" -> {
                    containerView.sli_friday.setBackgroundResource(R.drawable.has_lesson_on_day)
                    containerView.sli_friday.setTextColor(
                        ContextCompat.getColor(
                            containerView.context,
                            R.color.colorBackgroundPrimary
                        )
                    )
                }
                "Saturday" -> {
                    containerView.sli_saturday.setBackgroundResource(R.drawable.has_lesson_on_day)
                    containerView.sli_saturday.setTextColor(
                        ContextCompat.getColor(
                            containerView.context,
                            R.color.colorBackgroundPrimary
                        )
                    )
                }
                "Sunday" -> {
                    containerView.sli_sunday.setBackgroundResource(R.drawable.has_lesson_on_day)
                    containerView.sli_sunday.setTextColor(
                        ContextCompat.getColor(
                            containerView.context,
                            R.color.colorBackgroundPrimary
                        )
                    )
                }
            }
        }
    }

}

// The list which is being used to store the data required to make use of the day indicators
private val lessonsFromCursor: MutableList<Lesson> = mutableListOf()

class SubjectsRecyclerViewAdapter(
    private var cursorSubjects: Cursor?,
    private var cursorLessons: Cursor?
) : RecyclerView.Adapter<SubjectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.subject_list_items, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val cursor = cursorSubjects
        if (cursor == null || cursor.count == 0) {
            // TODO - Provide instructions
        } else {
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("Couldn't move cursor to position $position")
            }

            // Create Subject from the data in the cursor
            val subject = Subject(
                cursor.getString(cursor.getColumnIndex(SubjectsContract.Columns.SUBJECT_NAME)),
                cursor.getString(cursor.getColumnIndex(SubjectsContract.Columns.SUBJECT_TEACHER)),
                cursor.getString(cursor.getColumnIndex(SubjectsContract.Columns.SUBJECT_COLORCODE))
            )
            // Id is not set in the instructor
            subject.id = cursor.getLong(cursor.getColumnIndex(SubjectsContract.Columns.SUBJECT_ID))

            val days = arrayListOf<String>()
            for (lesson in lessonsFromCursor) {
                if (lesson.name == subject.name) {
                    days.add(lesson.day)
                }
            }

            holder.bind(subject, days)
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursorSubjects
        return if (cursor == null || cursor.count == 0) 0 else cursor.count
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * The returned old Cursor is *not* closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one
     * If the given new Cursor is the same instance as the previously set Cursor, null is also returned
     */
    fun swapSubjectsCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursorSubjects) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursorSubjects
        cursorSubjects = newCursor
        if (newCursor != null) {
            //notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            //notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * We're loading the lessons into a MutableList which will be used later to display lesson days next to subjects (in the indicators).
     * If the number of the oldCursor's row is not equal to the newCursor's, we're deleting the content of the MutableList since it means it has been changed.
     * Note that we are not using the getItemCount() method on numItems since it is used by onBindViewHolder to retrieve the number of the subjects to display.
     * If we were to use it, it would lead to problems such as not displaying any subjects.
     * The returned old Cursor is *not* closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one
     * If the given new Cursor is the same instance as the previously set Cursor, null is also returned
     */
    fun swapLessonsCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursorLessons) {
            return null
        }
        val cursor = cursorLessons
        val numItems = if (cursor == null || cursor.count == 0) 0 else cursor.count
        val oldCursor = cursorLessons
        cursorLessons = newCursor
        if (newCursor != null) {
            if (oldCursor != null) {
                if (oldCursor.count != newCursor.count) lessonsFromCursor.clear()
            }
            loadLessonsIntoList(newCursor)
            notifyDataSetChanged()
        } else {
            //notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }


    private fun loadLessonsIntoList(cursor: Cursor) {
        try {
            while (cursor.moveToNext()) {
                val lesson = Lesson(
                    cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_NAME)),
                    cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_WEEK)),
                    cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_DAY)),
                    cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_STARTS)),
                    cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_ENDS)),
                    cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_LOCATION))
                )
                lesson.id = cursor.getLong(cursor.getColumnIndex(LessonsContract.Columns.ID))
                lessonsFromCursor.add(lesson)
            }
        } catch (e: Exception) {
            throw Exception("Unknown error occurred while processing lessons cursor in SubjectsRecyclerView")
        }
    }

}