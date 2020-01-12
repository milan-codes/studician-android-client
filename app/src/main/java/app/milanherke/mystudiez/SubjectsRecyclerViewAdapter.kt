package app.milanherke.mystudiez

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.subject_list_items.view.*
import java.lang.IllegalStateException

class SubjectViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(subject: Subject, days: ArrayList<String>) {
        containerView.sli_name.text = subject.name
        for (day in days) {
            when(day) {
                "Monday" -> {
                    containerView.sli_monday.setBackgroundResource(R.drawable.has_lesson_on_day)
                }
                "Tuesday" -> {
                    containerView.sli_tuesday.setBackgroundResource(R.drawable.has_lesson_on_day)
                }
                "Wednesday" -> {
                    containerView.sli_wednesday.setBackgroundResource(R.drawable.has_lesson_on_day)
                }
                "Thursday" -> {
                    containerView.sli_thursday.setBackgroundResource(R.drawable.has_lesson_on_day)
                }
                "Friday" -> {
                    containerView.sli_friday.setBackgroundResource(R.drawable.has_lesson_on_day)
                }
                "Saturday" -> {
                    containerView.sli_saturday.setBackgroundResource(R.drawable.has_lesson_on_day)
                }
                "Sunday" -> {
                    containerView.sli_sunday.setBackgroundResource(R.drawable.has_lesson_on_day)
                }
            }
        }
    }
}

class SubjectsRecyclerViewAdapter(private var cursor: Cursor?) : RecyclerView.Adapter<SubjectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.subject_list_items, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val cursor = cursor
        if (cursor == null || cursor.count == 0) {
            // TODO - Provide instructions
        }else {
            if(!cursor.moveToPosition(position)) {
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

            holder.bind(subject, days)
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursor
        val count = if (cursor == null || cursor.count == 0) 0 else cursor.count
        return count
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * The returned old Cursor is *not* closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one
     * If the given new Cursor is the same instance as the previously set Cursor, null is also returned
     */
    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursor) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursor
        cursor = newCursor
        if(newCursor != null) {
            //notify the observers about the new cursor
            notifyDataSetChanged()
        }else {
            //notify the observers about the lack of a data set
            notifyItemRangeRemoved(0,numItems)
        }
        return oldCursor
    }

}