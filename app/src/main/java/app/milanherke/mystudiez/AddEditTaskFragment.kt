package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_edit_task.*
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_TASK = "task"
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * This fragment was created to add or edit tasks.
 * Activities that contain this fragment must implement the
 * [AddEditTaskFragment.AddEditTaskInteractions] interface
 * to handle interaction events.
 * Use the [AddEditTaskFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditTaskFragment : Fragment() {
    private var task: Task? = null
    private var subject: Subject? = null
    private var listener: AddEditTaskInteractions? = null
    private var listOfSubjects: ArrayList<Subject>? = null
    private var subjectIdClickedFromList: Long? = null
    private var taskType: Int = 0
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(AddEditTaskViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface AddEditTaskInteractions {
        fun onSaveTaskClicked(task: Task)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEditTaskInteractions) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AddEditTaskInteractions")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)
        subject = arguments?.getParcelable(ARG_SUBJECT)
        listOfSubjects = sharedViewModel.getAllSubjects()
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
        val subject = subject
        val listOfSubjects = listOfSubjects

        if (task == null && subject == null) {
            // New task is created. Fragment was called from TasksFragment
            activity!!.toolbar.setTitle(R.string.add_new_task_title)

            if (listOfSubjects != null) {
                if (listOfSubjects.size == 0) {
                    new_task_subject_btn.text = getString(R.string.no_subjects_to_select_from)
                    new_task_subject_btn.background =
                        resources.getDrawable(R.drawable.circular_disabled_button, null)
                    new_task_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
                    new_task_subject_btn.isEnabled = false
                }
            }

        } else if (task != null && subject != null) {
            // Task is edited. Fragment was called from TaskDetailsFragment
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
            new_task_due_date_btn.text = task.dueDate
            new_task_reminder_btn.text = task.reminder

        } else if (task == null && subject != null) {
            // New task is created. Fragment was called from SubjectDetailsFragment
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

        activity!!.bar.visibility = View.GONE
        activity!!.fab.visibility = View.GONE

        new_task_type_btn.setOnClickListener {
            showTaskTypesPopUp(it)
        }

        if (new_task_subject_btn.isEnabled) {
            new_task_subject_btn.setOnClickListener {
                showSubjectsPopUp(it)
            }
        }

        new_task_due_date_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                context!!, CalendarUtils.getDate(activity!!, R.id.new_task_due_date_btn, cal),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        new_task_reminder_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                context,
                CalendarUtils.getTime(activity!!, R.id.new_task_reminder_btn, cal),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
            DatePickerDialog(
                context!!, CalendarUtils.getDate(activity!!, R.id.new_task_reminder_btn, cal),
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
        FragmentsStack.getInstance(context!!).pop()
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
     * Creates a newTask object with the details to be saved, then
     * call the viewModel's saveTask function to save it
     * Task is not a data class, so we can compare the new details with the original task
     * and only save if they are different
     */
    private fun saveTask() {
        if (requiredFieldsAreFilled()) {
            val newTask = taskFromUi()
            if (newTask != task) {
                task = viewModel.saveTask(newTask)
                listener?.onSaveTaskClicked(task!!)
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

    private fun taskFromUi(): Task {
        val task = Task(
            new_task_name.text.toString(),
            new_task_desc.text.toString(),
            if (taskType != 0) taskType else throw IllegalArgumentException("Parameter taskType ($taskType) must be one or two"),
            subjectIdClickedFromList ?: (subject?.subjectId ?: -1L),
            new_task_due_date_btn.text.toString(),
            new_task_reminder_btn.text.toString()
        )
        task.taskId = this.task?.taskId ?: 0
        return task
    }

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

    private fun showSubjectsPopUp(view: View) {
        // Avoiding problems with smart cast
        val listOfSubjects = listOfSubjects
        if (listOfSubjects != null) {
            val popupMenu = PopupMenu(activity!!, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.empty_menu, popupMenu.menu)

            // Adding the subjects to the list if it is not null
            for (subject in listOfSubjects) {
                popupMenu.menu.add(subject.name).setOnMenuItemClickListener {
                    new_task_subject_btn.text = subject.name
                    subjectIdClickedFromList = subject.subjectId
                    true
                }
            }
            popupMenu.show()
        }
    }
}