package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_task_details.*

// Fragment initialization parameters
private const val ARG_TASK = "task"
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * This fragment was created to list the details of a [Task].
 * The user can delete a task from this fragment
 * or launch a new fragment ([AddEditTaskFragment]) to edit it.
 * Activities that contain this fragment must implement the
 * [TaskDetailsFragment.TaskDetailsInteraction] interface
 * to handle interaction events.
 * Use the [TaskDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TaskDetailsFragment : Fragment() {

    private var task: Task? = null
    private var subject: Subject? = null
    private var listener: TaskDetailsInteraction? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(TaskDetailsViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface TaskDetailsInteraction {
        fun taskIsLoaded(task: Task)
        fun swapSubject(subject: Subject)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TaskDetailsInteraction) {
            listener = context
        } else {
            throw RuntimeException("$context must implement TaskDetailsInteraction")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)
        subject = arguments?.getParcelable(ARG_SUBJECT)
        listener?.taskIsLoaded(task!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Avoiding problems with smart-cast
        val task = task
        val subject = subject

        if (task != null && subject != null) {
            activity!!.toolbar.setTitle(R.string.task_details_title)
            task_details_name_value.text = task.name
            task_details_desc_value.text = task.description
            task_details_type_value.text = TaskUtils.getTaskType(task.type, context!!)
            task_details_subject_value.text = subject.name
            task_details_due_date_value.text = task.dueDate
            task_details_reminder_value.text = task.reminder
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        // Hiding bottom navigation bar and fab button
        activity!!.bar.visibility = View.GONE
        activity!!.fab.visibility = View.GONE

        // Passing the subject to MainActivity
        // MainActivity always needs to have the currently used subject
        val subject = subject
        if (subject != null) {
            sharedViewModel.swapSubject(subject)
        }


        task_details_del_subject_btn.setOnClickListener {
            deleteTask()
        }

        task_details_edit_subject_btn.setOnClickListener {
            editTask()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        FragmentBackStack.getInstance(context!!).push(Fragments.TASK_DETAILS)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task The task, whose details are displayed.
         * @return A new instance of fragment TaskDetailsFragment.
         */
        @JvmStatic
        fun newInstance(task: Task, subject: Subject? = null) =
            TaskDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                    putParcelable(ARG_SUBJECT, subject)
                }
            }
    }

    /**
     * Allows the user to delete a [Task] object from the database.
     * First, it calls the [viewModel]'s deleteTask function
     * and then it opens a new fragment based on [FragmentBackStack]
     */
    private fun deleteTask() {
        // Deleting task
        viewModel.deleteTask(task!!)

        // Opening previous fragment
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(activity!!).peek()) {
            Fragments.SUBJECT_DETAILS -> activity!!.replaceFragmentWithTransition(
                SubjectDetailsFragment.newInstance(subject!!),
                R.id.fragment_container
            )
            Fragments.TASKS -> {
                activity!!.replaceFragmentWithTransition(
                    TasksFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.OVERVIEW -> {
                activity!!.replaceFragmentWithTransition(
                    OverviewFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            else -> throw IllegalStateException("onDeleteTaskClick tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * Allows the user to edit a [Task] object
     * by opening [AddEditTaskFragment].
     */
    private fun editTask() {
        activity!!.replaceFragmentWithTransition(
            AddEditTaskFragment.newInstance(task!!, subject!!),
            R.id.fragment_container
        )
    }
}
