package app.milanherke.mystudiez

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.subject_list_items.view.*

private const val VIEW_TYPE_NOT_EMPTY = 0
private const val VIEW_TYPE_EMPTY = 1

// The list which is being used to store the data required to make use of the day indicators
private val lessonsFromCursor: MutableList<Lesson> = mutableListOf()

class SubjectsRecyclerViewAdapter(
    private var cursorSubjects: Cursor?,
    private var cursorLessons: Cursor?,
    private val listener: OnSubjectClickListener
) : RecyclerView.Adapter<SubjectsRecyclerViewAdapter.ViewHolder>() {

    interface OnSubjectClickListener {
        fun onSubjectClick(subject: Subject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_subject_list_item, parent, false)
                EmptySubjectViewHolder(view)
            }
            VIEW_TYPE_NOT_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.subject_list_items, parent, false)
                SubjectViewHolder(view)
            }
            else -> throw IllegalStateException("Couldn't recognise the view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cursor = cursorSubjects
        when (getItemViewType(position)) {
            VIEW_TYPE_EMPTY -> {
                // We are not putting any data into the empty view, therefore we do not need to do anything here
            }
            VIEW_TYPE_NOT_EMPTY -> {
                if (cursor != null) {
                    if (!cursor.moveToPosition(position)) {
                        throw IllegalStateException("Couldn't move cursor to position $position")
                    }

                    // Create Subject from the data in the cursor
                    val subject = Subject(
                        cursor.getString(cursor.getColumnIndex(SubjectsContract.Columns.SUBJECT_NAME)),
                        cursor.getString(cursor.getColumnIndex(SubjectsContract.Columns.SUBJECT_TEACHER)),
                        cursor.getInt(cursor.getColumnIndex(SubjectsContract.Columns.SUBJECT_COLORCODE))
                    )
                    // Id is not set in the instructor
                    subject.subjectId =
                        cursor.getLong(cursor.getColumnIndex(SubjectsContract.Columns.ID))

                    val days = arrayListOf<String>()
                    for (lesson in lessonsFromCursor) {
                        if (lesson.subjectId == subject.subjectId) {
                            days.add(lesson.day)
                        }
                    }

                    holder.bind(subject, days, listener)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursorSubjects
        return if (cursor == null || cursor.count == 0) 1 else cursor.count
    }

    override fun getItemViewType(position: Int): Int {
        val cursor = cursorSubjects
        return if (cursor == null || cursor.count == 0) VIEW_TYPE_EMPTY else VIEW_TYPE_NOT_EMPTY
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
            lessonsFromCursor.clear()
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
                    cursor.getLong(cursor.getColumnIndex(LessonsContract.Columns.LESSON_SUBJECT)),
                    "A",
                    cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_DAY)),
                    cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_STARTS)),
                    cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_ENDS)),
                    cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_LOCATION))
                )
                lesson.lessonId = cursor.getLong(cursor.getColumnIndex(LessonsContract.Columns.ID))
                lessonsFromCursor.add(lesson)
            }
        } catch (e: Exception) {
            throw Exception("Unknown error occurred while processing lessons cursor in SubjectsRecyclerView")
        }
    }

    open class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        open fun bind(
            subject: Subject,
            days: ArrayList<String>,
            listener: OnSubjectClickListener
        ) {
        }
    }

    private class SubjectViewHolder(override val containerView: View) : ViewHolder(containerView) {

        override fun bind(
            subject: Subject,
            days: ArrayList<String>,
            listener: OnSubjectClickListener
        ) {
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

            containerView.sli_linearlayout.setOnClickListener {
                listener.onSubjectClick(subject)
            }
        }

    }

    // We do not need to override the bind method since we're not putting any data into the empty view
    private class EmptySubjectViewHolder(override val containerView: View) :
        ViewHolder(containerView)
}