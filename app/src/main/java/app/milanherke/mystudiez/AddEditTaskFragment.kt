package app.milanherke.mystudiez

import android.annotation.SuppressLint
            import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import app.milanherke.mystudiez.CalendarUtils.Companion.CalendarInteractions
import app.milanherke.mystudiez.Fragments.TASK_DETAILS
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_edit_task.*
import java.util.*

// Fragment initialization parameters
private const val ARG_TASK = "task"
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * This fragment was created to add or edit tasks.
 * Activities that contain this fragment must implement the
 * [AddEditTaskFragment.TaskSaved] interface
 * to handle interaction events.
 * Use the [AddEditTaskFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditTaskFragment : Fragment() {
    private var task: Task? = null
    private var subject: Subject? = null
    private var dueDate: Date? = null
    private var reminder: Date? = null
    private var listener: TaskSaved? = null
    private var listOfSubjects: MutableMap<String, Subject>? = null
    private var selectedSubjectId: String? = null
    private var taskType: Int = 0
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(AddEditTaskViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }
    private var progressBarHandler: ProgressBarHandler? = null

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface TaskSaved {
        fun onSaveTaskClickListener(task: Task, subject: Subject)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TaskSaved) {
            listener = context
        } else {
            throw RuntimeException("$context must implement TaskSaved")
        }
    }

    /**
     * If a new [Task] is created, we need to get all [Subject] objects
     * from Firebase to let the user choose the task's subject.
     * New task is created when the fragment was called
     * from any other fragment than [TaskDetailsFragment].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)

        // Avoiding problems with smart-cast
        val task = task
        if (task != null) {
            dueDate = task.dueDate
            reminder = task.reminder
        }

        subject = arguments?.getParcelable(ARG_SUBJECT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Avoiding problems with smart cast
        val task = task
        val taskReminder = task?.reminder
        val subject = subject
        val listOfSubjects = listOfSubjects

        if (task == null && subject == null) {
            // Fragment was called from TasksFragment
            // User wants to create a new task
            activity!!.toolbar.setTitle(R.string.add_new_task_title)
            if (listOfSubjects != null) {
                // We need to disable the subject btn if listOfSubjects is empty
                // Because the user could not choose from any subjects
                if (listOfSubjects.isEmpty()) {
                    new_task_subject_btn.text = getString(R.string.no_subjects_to_select_from)
                    new_task_subject_btn.background =
                        resources.getDrawable(R.drawable.circular_disabled_button, null)
                    new_task_subject_btn.setTextColor(
                        resources.getColor(
                            R.color.colorTextSecondary,
                            null
                        )
                    )
                    new_task_subject_btn.isEnabled = false
                }
            }
        } else if (task != null && subject != null) {
            // Fragment called from TaskDetailsFragment
            // User wants to edit an existing task
            activity!!.toolbar.title =
                resources.getString(R.string.edit_subject_title, subject.name)
            new_task_name.setText(task.name)
            new_task_desc.setText(task.description)
            new_task_type_btn.text = TaskUtils.getTaskType(task.type, context!!)
            taskType = task.type
            new_task_subject_btn.text = subject.name
            new_task_subject_btn.background =
                resources.getDrawable(R.drawable.circular_disabled_button, null)
            new_task_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
            new_task_subject_btn.isEnabled = false
            new_task_due_date_btn.text = CalendarUtils.dateToString(task.dueDate, true)
            new_task_reminder_btn.text = if (taskReminder == null) getString(R.string.add_edit_lesson_btn) else CalendarUtils.dateToString(taskReminder, true)

        } else if (task == null && subject != null) {
            // Fragment called from SubjectDetailsFragment
            // User wants to create a new task
            activity!!.toolbar.setTitle(R.string.add_new_task_title)
            new_task_subject_btn.text = subject.name
            new_task_subject_btn.isEnabled = false
            new_task_subject_btn.background =
                resources.getDrawable(R.drawable.circular_disabled_button, null)
            new_task_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
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

        // If the last fragment in the backStack is not TaskDetails
        // then we need to fetch all of the subjects to let the users choose one
        // for the exam they are about to create
        if (FragmentBackStack.getInstance(activity!!).peek() != TASK_DETAILS) {
            sharedViewModel.getAllSubjects(object : SharedViewModel.RetrievingData {
                override fun onLoad() {
                    val activity = activity
                    if (activity != null) {
                        progressBarHandler = ProgressBarHandler(activity)
                        progressBarHandler!!.showProgressBar()
                    }
                }

                override fun onSuccess(subjects: MutableMap<String, Subject>) {
                    listOfSubjects = subjects
                    progressBarHandler!!.hideProgressBar()
                }

                override fun onFailure(e: DatabaseError) {
                    Toast.makeText(
                        context,
                        getString(R.string.firebase_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // User must set a subject if a new task is created
        if (new_task_subject_btn.isEnabled) {
            new_task_subject_btn.setOnClickListener {
                showSubjectsPopUp(it)
            }
        }

        // User must set the type of the task
        new_task_type_btn.setOnClickListener {
            showTaskTypesPopUp(it)
        }

        val calendarListener: CalendarInteractions = object: CalendarInteractions {
            override fun onDateSet(date: Date) {
                dueDate = date
            }

            override fun onTimeSet(date: Date) {
                reminder = date
            }
        }

        // User must set the due date
        new_task_due_date_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                context!!,
                CalendarUtils.getDateSetListener(activity!!, R.id.new_task_due_date_btn, cal, calendarListener),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // User can set a reminder
        new_task_reminder_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                context,
                CalendarUtils.getTimeSetListener(activity!!, R.id.new_task_reminder_btn, cal, true, calendarListener),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
            DatePickerDialog(
                context!!,
                CalendarUtils.getDateSetListener(activity!!, R.id.new_task_reminder_btn, cal),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        new_task_save_btn.setOnClickListener {
            saveTask()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        FragmentBackStack.getInstance(context!!).pop()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task The task to be edited, or null when creating a new one.
         * @param subject Subject associated with the task. Null if fragment was called from [TasksFragment]
         * @return A new instance of fragment AddEditTaskFragment.
         */
        @JvmStatic
        fun newInstance(task: Task? = null, subject: Subject? = null) =
            AddEditTaskFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                    putParcelable(ARG_SUBJECT, subject)
                }
            }
    }

    /**
     * Creates a new [Task] object with the details to be saved, then
     * calls the [viewModel]'s saveTask function to save it.
     */
    private fun saveTask() {
        if (requiredFieldsAreFilled()) {
            val newTask = taskFromUi()
            if (newTask != task) {
                task = viewModel.saveTask(newTask)
                listener?.onSaveTaskClickListener(task!!, subject!!)
            } else {
                Toast.makeText(
                    context!!,
                    getString(R.string.did_not_change),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context!!,
                getString(R.string.required_fields_are_not_filled),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * If we're creating a new [Task], we set its SubjectID to [selectedSubjectId],
     * which holds the selected subject's ID, chosen by the user from [listOfSubjects].
     * Furthermore, we set the task's ID to an empty string.
     * If we're updating a [Task], we're not changing its SubjectID, nor its own ID
     *
     * @return A [Task] object created from UI
     */
    private fun taskFromUi(): Task {
        val task = Task(
            new_task_name.text.toString(),
            new_task_desc.text.toString(),
            if (taskType == 1 || taskType == 2) taskType else throw IllegalArgumentException("Parameter taskType ($taskType) must be one or two"),
            selectedSubjectId ?: (subject?.id ?: ""),
            dueDate!!,
            reminder
        )
        task.id = this.task?.id ?: ""
        return task
    }

    /**
     * Simple function to check whether the required fields are filled.
     *
     * @return True if required fields are filled, otherwise false
     */
    private fun requiredFieldsAreFilled(): Boolean {
        if (new_task_name.text.isNotEmpty()
            && new_task_type_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_task_type_btn.text.isNotEmpty()
            && new_task_subject_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_task_subject_btn.text.isNotEmpty()
            && new_task_due_date_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_task_due_date_btn.text.isNotEmpty()
        ) {
            return true
        }
        return false
    }

    /**
     * This function allows the user to choose the type of the task from a pop up menu.
     * The type can be either Assignment or Revision.
     */
    private fun showTaskTypesPopUp(view: View) {
        val popupMenu = PopupMenu(activity!!, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.task_type_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.task_type_assignment -> {
                    taskType = 1
                    new_task_type_btn.setText(R.string.taskTypeAssignment)
                }
                R.id.task_type_revision -> {
                    taskType = 2
                    new_task_type_btn.setText(R.string.taskTypeRevision)
                }
            }
            true
        }
    }

    /**
     * Allows the user to choose a subject from a pop up menu,
     * if a new exam is created.
     */
    private fun showSubjectsPopUp(view: View) {
        // Avoiding problems with smart cast
        val listOfSubjects = listOfSubjects
        if (listOfSubjects != null) {
            val popupMenu = PopupMenu(activity!!, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.empty_menu, popupMenu.menu)

            // Adding the subjects to the list if it is not null
            for (subject in listOfSubjects) {
                popupMenu.menu.add(subject.value.name).setOnMenuItemClickListener {
                    new_task_subject_btn.text = subject.value.name
                    selectedSubjectId = subject.value.id
                    this.subject = subject.value
                    true
                }
            }
            popupMenu.show()
        }
    }

}