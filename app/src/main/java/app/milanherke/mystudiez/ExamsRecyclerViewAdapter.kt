package app.milanherke.mystudiez

import android.database.Cursor
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.details_list_item.view.*
import java.lang.IllegalStateException

private const val VIEW_TYPE_NOT_EMPTY = 0
private const val VIEW_TYPE_EMPTY = 1

class ExamsRecyclerViewAdapter(
    private var cursorExams: Cursor?,
    private var dayIndicator: Drawable?,
    private val listener: OnExamClickListener
) :
    RecyclerView.Adapter<ExamsRecyclerViewAdapter.ViewHolder>() {

    interface OnExamClickListener {
        fun onExamClickListener(exam: Exam)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_exam_list_item, parent, false)
                EmptyExamViewHolder(view)
            }
            VIEW_TYPE_NOT_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.details_list_item, parent, false)
                ExamViewHolder(view)
            }
            else -> throw IllegalStateException("Couldn't recognise the view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cursor = cursorExams
        when (getItemViewType(position)) {
            VIEW_TYPE_EMPTY -> {
                // We are not putting any data into the empty view, therefore we do not need to do anything here
            }
            VIEW_TYPE_NOT_EMPTY -> {
                if (cursor != null) {
                    if (!cursor.moveToPosition(position)) {
                        throw IllegalStateException("Couldn't move to position $position")
                    }

                    // Create Exam from the data in the cursor
                    val exam = Exam(
                        cursor.getString(cursor.getColumnIndex(ExamsContract.Columns.EXAM_NAME)),
                        cursor.getString(cursor.getColumnIndex(ExamsContract.Columns.EXAM_DESCRIPTION)),
                        cursor.getLong(cursor.getColumnIndex(ExamsContract.Columns.EXAM_SUBJECT)),
                        cursor.getString(cursor.getColumnIndex(ExamsContract.Columns.EXAM_DATE)),
                        cursor.getString(cursor.getColumnIndex(ExamsContract.Columns.EXAM_REMINDER))
                    )

                    // Id is not set in the constructor
                    exam.examId = cursor.getLong(cursor.getColumnIndex(ExamsContract.Columns.ID))

                    holder.bind(exam)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursorExams
        return if (cursor == null || cursor.count == 0) 1 else cursor.count
    }

    override fun getItemViewType(position: Int): Int {
        val cursor = cursorExams
        return if (cursor == null || cursor.count == 0) VIEW_TYPE_EMPTY else VIEW_TYPE_NOT_EMPTY
    }

    /**
     * Swap in the drawable of the fragment details
     *
     * @param drawable The new drawable object to be used
     */
    fun swapDrawable(drawable: Drawable) {
        dayIndicator = drawable
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * The returned old Cursor is *not* closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one
     * If the given new Cursor is the same instance as the previously set Cursor, null is also returned
     */
    fun swapExamsCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursorExams) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursorExams
        cursorExams = newCursor
        if (newCursor != null) {
            //notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            //notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }

    open class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        open fun bind(exam: Exam) {}
    }

    private inner class ExamViewHolder(override val containerView: View) :
        ViewHolder(containerView) {

        override fun bind(exam: Exam) {
            containerView.details_list_title.text = exam.name
            containerView.details_list_header1.text = exam.date
            containerView.details_list_header2.visibility = View.GONE

            // We're creating a clone because we do not want to affect the other instances
            containerView.details_list_subject_indicator.setImageDrawable(dayIndicator)

            containerView.details_list_container.setOnClickListener {
                listener.onExamClickListener(exam)
            }
        }

    }

    // We do not need to override the bind method since we're not putting any data into the empty view
    private class EmptyExamViewHolder(override val containerView: View) : ViewHolder(containerView)

}