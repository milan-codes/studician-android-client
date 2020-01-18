package app.milanherke.mystudiez

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.details_list_item.view.*
import kotlinx.android.synthetic.main.no_task_list_item.view.*
import java.lang.IllegalStateException

private const val VIEW_TYPE_NOT_EMPTY = 0
private const val VIEW_TYPE_EMPTY = 1

class TasksRecyclerViewAdapter(private var cursorTasks: Cursor?) :
    RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_task_list_item, parent, false)
                EmptyTaskViewHolder(view)
            }
            VIEW_TYPE_NOT_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.details_list_item, parent, false)
                TaskViewHolder(view)
            }
            else -> throw IllegalStateException("Couldn't recognise the view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cursor = cursorTasks
        when (getItemViewType(position)) {
            VIEW_TYPE_EMPTY -> {
                // We are not putting any data into the empty view, therefore we do not need to do anything here
            }
            VIEW_TYPE_NOT_EMPTY -> {
                if (cursor != null) {
                    if (!cursor.moveToPosition(position)) {
                        throw IllegalStateException("Couldn't move to position $position")
                    }

                    // Create Task from the data in the cursor
                    val task = Task(
                        cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
                        cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_TYPE)),
                        cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_SUBJECT)),
                        cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DUEDATE)),
                        cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_REMINDER))
                    )

                    // Id is not set in the constructor
                    task.id = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))

                    holder.bind(task)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursorTasks
        return if (cursor == null || cursor.count == 0) 1 else cursor.count
    }

    override fun getItemViewType(position: Int): Int {
        val cursor = cursorTasks
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
    fun swapTasksCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursorTasks) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursorTasks
        cursorTasks = newCursor
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
        open fun bind(task: Task) {}
    }

    private class TaskViewHolder(override val containerView: View) : ViewHolder(containerView) {

        override fun bind(task: Task) {
            containerView.details_list_title.text = task.name
            containerView.details_list_header1.text = task.subject
            containerView.details_list_header2.text = task.dueDate
        }

    }

    // We do not need to override the bind method since we're not putting any data into the empty view
    private class EmptyTaskViewHolder(override val containerView: View) : ViewHolder(containerView)

}