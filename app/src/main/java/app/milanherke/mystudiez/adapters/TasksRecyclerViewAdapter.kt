package app.milanherke.mystudiez.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import app.milanherke.mystudiez.*
import app.milanherke.mystudiez.Fragments.OVERVIEW
import app.milanherke.mystudiez.Fragments.SUBJECT_DETAILS
import app.milanherke.mystudiez.adapters.TasksRecyclerViewAdapter.OnTaskClickListener
import kotlinx.android.synthetic.main.details_list_item.view.*

/**
 * A [BaseAdapter] subclass.
 * This class serves as an adapter for RecyclerViews
 * which were created to display tasks.
 *
 * @property usedIn The fragment where an object of this class has been created
 * @property listener Fragments that use this class must implement [OnTaskClickListener]
 * @property tasks An [ArrayList] containing all [Task] objects to display
 * @property subjects A [MutableMap], whose key is a SubjectID and value is a [Subject] object
 */
class TasksRecyclerViewAdapter(
    private val usedIn: Fragments,
    private val listener: OnTaskClickListener,
    private var tasks: ArrayList<Task> = arrayListOf(),
    private var subjects: MutableMap<String, Subject> = mutableMapOf()
) : BaseAdapter<Task>(tasks) {

    /**
     * This interface must be implemented by activities/fragments that contain
     * this RecyclerViewAdapter to allow an interaction in this class to be
     * communicated to the activity/fragment.
     */
    interface OnTaskClickListener {
        fun onTaskClickListener(task: Task)
    }

    override fun setViewHolder(parent: ViewGroup): BaseViewHolder<Task> {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.details_list_item, parent, false)
        return TaskViewHolder(view)
    }

    /**
     * Changes the data set. Calls [BaseAdapter.swapDataList].
     * Swaps in a new [ArrayList] that contains [Task] objects.
     *
     * @param tasks An [ArrayList] that contains [Task] objects
     */
    fun swapTasks(tasks: ArrayList<Task>) {
        super.swapDataList(tasks)
    }

    /**
     * Swaps in a new [MutableMap], that contains [Subject] objects,
     * which are needed in [TaskViewHolder].
     *
     * @param subjects A new map that contains [Subject] objects
     */
    fun swapSubjects(subjects: MutableMap<String, Subject>) {
        // Secondary data list, which contains no useful information for the BaseAdapter, it is only necessary when binding a task
        this.subjects = subjects
        notifyDataSetChanged()
    }

    private inner class TaskViewHolder(override val containerView: View) :
        BaseViewHolder<Task>(containerView) {

        override fun bind(data: Task) {
            // If recycler view is used in SubjectDetailsFragment
            // Meaning we do not have to load nor display the subject details
            if (usedIn == SUBJECT_DETAILS) {
                containerView.details_list_title.text = data.name
                containerView.details_list_header1.text =
                    CalendarUtils.dateToString(
                        data.dueDate,
                        false
                    )
                containerView.details_list_header2.text =
                    TaskUtils.getTaskType(
                        data.type,
                        containerView.context
                    )
                containerView.details_list_subject_indicator.visibility = View.GONE

                // Setting new constraints because subjectIndicator's visibility is set to gone
                val params =
                    containerView.details_list_title.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom =
                    R.id.details_list_header1
                params.leftToLeft =
                    R.id.details_list_constraint
                params.bottomToBottom =
                    R.id.details_list_constraint
                params.marginStart = super.setMargin(16F)
                params.bottomMargin = super.setMargin(16F)
                containerView.details_list_title.layoutParams = params
            } else {
                val subjects = subjects
                val subject: Subject? = subjects[data.subjectId]
                if (subject != null) {
                    if (usedIn == OVERVIEW) {
                        containerView.details_list_title.text = data.name
                        containerView.details_list_header1.text = subject.name
                        containerView.details_list_header2.visibility = View.GONE
                    } else {
                        containerView.details_list_title.text = data.name
                        containerView.details_list_header1.text =
                            containerView.resources.getString(
                                R.string.details_subject_item_time,
                                subject.name,
                                TaskUtils.getTaskType(
                                    data.type,
                                    containerView.context
                                )
                            )
                        containerView.details_list_header2.text =
                            CalendarUtils.dateToString(
                                data.dueDate,
                                false
                            )
                    }

                    //Creating a clone drawable because we do not want to affect other instances of the original drawable
                    val clone =
                        containerView.resources.getDrawable(R.drawable.placeholder_circle, null)
                            .mutatedClone()
                    clone.setColor(subject.colorCode, containerView.context)
                    containerView.details_list_subject_indicator.setImageDrawable(clone)
                }
            }

            containerView.details_list_container.setOnClickListener {
                listener.onTaskClickListener(data)
            }
        }

    }
}