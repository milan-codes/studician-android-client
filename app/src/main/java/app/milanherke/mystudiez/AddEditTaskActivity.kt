package app.milanherke.mystudiez

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import app.milanherke.mystudiez.ActivityUtils.Companion.ACTIVITY_NAME_BUNDLE_ID
import app.milanherke.mystudiez.ActivityUtils.Companion.FRAGMENT_TO_LOAD_BUNDLE_ID
import app.milanherke.mystudiez.ActivityUtils.Companion.SUBJECT_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.ActivityUtils.Companion.TASK_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.ActivityUtils.Companion.createNotification
import app.milanherke.mystudiez.ActivityUtils.Companion.scheduleNotification
import app.milanherke.mystudiez.CalendarUtils.Companion.CalendarInteractions
import app.milanherke.mystudiez.Fragments.*
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_add_edit_task.*
import kotlinx.android.synthetic.main.content_add_edit_task.*
import java.util.*

/**
 * A simple [AppCompatActivity] subclass.
 * The purpose of this activity is to add or edit tasks.
 */
class AddEditTaskActivity : AppCompatActivity() {
    private var task: Task? = null
    private var subject: Subject? = null
    private var dueDate: Date? = null
    private var reminder: Date? = null
    private var listOfSubjects: MutableMap<String, Subject>? = null
    private var selectedSubjectId: String? = null
    private var taskType: Int = 0
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(AddEditTaskViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(this).get(SharedViewModel::class.java)
    }
    private var progressBarHandler: ProgressBarHandler? = null

    /**
     * If a new [Task] is created, we need to get all [Subject] objects
     * from Firebase to let the user choose the task's subject.
     * New task is created when the fragment was called
     * from any other fragment than [TaskDetailsFragment].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_task)

        val intent = intent
        task = intent.getParcelableExtra(TASK_PARAM_BUNDLE_ID)
        subject = intent.getParcelableExtra(SUBJECT_PARAM_BUNDLE_ID)

        // Avoiding problems with smart-cast
        val task = task
        if (task != null) {
            dueDate = task.dueDate
            reminder = task.reminder
        }

        // Avoiding problems with smart cast
        val taskReminder = task?.reminder
        val subject = subject
        val listOfSubjects = listOfSubjects

        if (task == null && subject == null) {
            // Fragment was called from TasksFragment
            // User wants to create a new task
            activity_task_toolbar.setTitle(R.string.add_new_task_title)
            setSupportActionBar(activity_task_toolbar)
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
            activity_task_toolbar.title = resources.getString(R.string.edit_subject_title, subject.name)
            setSupportActionBar(activity_task_toolbar)
            new_task_name.setText(task.name)
            new_task_desc.setText(task.description)
            new_task_type_btn.text = TaskUtils.getTaskType(task.type, this)
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
            activity_task_toolbar.title = resources.getString(R.string.add_new_task_title)
            setSupportActionBar(activity_task_toolbar)
            new_task_subject_btn.text = subject.name
            new_task_subject_btn.isEnabled = false
            new_task_subject_btn.background =
                resources.getDrawable(R.drawable.circular_disabled_button, null)
            new_task_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // If the last fragment in the backStack is not TaskDetails
        // then we need to fetch all of the subjects to let the users choose one
        // for the exam they are about to create
        if (FragmentBackStack.getInstance(this).peek() != TASK_DETAILS) {
            sharedViewModel.getAllSubjects(object : SharedViewModel.RetrievingData {
                override fun onLoad() {
                        progressBarHandler = ProgressBarHandler(this@AddEditTaskActivity)
                        progressBarHandler!!.showProgressBar()
                }

                override fun onSuccess(subjects: MutableMap<String, Subject>) {
                    this@AddEditTaskActivity.listOfSubjects = subjects
                    progressBarHandler!!.hideProgressBar()
                }

                override fun onFailure(e: DatabaseError) {
                    Toast.makeText(
                        this@AddEditTaskActivity,
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
               this,
                CalendarUtils.getDateSetListener(this, R.id.new_task_due_date_btn, cal, calendarListener),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // User can set a reminder
        new_task_reminder_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
             this,
                CalendarUtils.getTimeSetListener(this, R.id.new_task_reminder_btn, cal, true, calendarListener),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
            DatePickerDialog(
               this,
                CalendarUtils.getDateSetListener(this, R.id.new_task_reminder_btn, cal),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        new_task_save_btn.setOnClickListener {
            saveTask()
        }
    }

    override fun onStop() {
        super.onStop()
        FragmentBackStack.getInstance(this).pop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onUpBtnPressed()
            } else -> super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        onUpBtnPressed()
    }

    companion object {
        const val TAG = "AddEditTask"
    }

    /**
     * [AddEditTaskActivity] can return only to [TasksFragment] [SubjectDetailsFragment] and [TaskDetailsFragment].
     * It can be called only by the following fragments:
     *  [TasksFragment]: When pressing the FAB button and creating a new one
     *  [SubjectDetailsFragment]: When adding a new one from [SubjectDetailsFragment]
     *  [TaskDetailsFragment]: When editing an existing one
     */
    private fun onUpBtnPressed() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            TASKS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID, TAG)
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, TasksFragment.TAG)
                startActivity(intent)
            }
            SUBJECT_DETAILS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID, TAG)
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, SubjectDetailsFragment.TAG)
                intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
                startActivity(intent)
            }
            TASK_DETAILS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID, TAG)
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, TaskDetailsFragment.TAG)
                intent.putExtra(TASK_PARAM_BUNDLE_ID, task)
                intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
                startActivity(intent)
            }
            else -> {
                throw IllegalStateException("AddEditTaskActivity was called by unrecognised fragment $fragmentCalledFrom")
            }
        }
    }

    private fun onSaveBtnPressed(task: Task, subject: Subject) {
        val reminder = task.reminder
        if (reminder != null) {
            val notification =
                createNotification(this, getString(R.string.notification_task_reminder_title), task.name)
            val delay = reminder.time.minus(System.currentTimeMillis())
            scheduleNotification(this, notification, delay, task, null)
        }

        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            TASKS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID, TAG)
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, TasksFragment.TAG)
                startActivity(intent)
            }
            SUBJECT_DETAILS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID, TAG)
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, SubjectDetailsFragment.TAG)
                intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
                startActivity(intent)
            }
            TASK_DETAILS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID, TAG)
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, TaskDetailsFragment.TAG)
                intent.putExtra(TASK_PARAM_BUNDLE_ID, task)
                intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
                startActivity(intent)
            }
            else -> throw IllegalStateException("onSaveTaskClicked tries to load unrecognised fragment $fragmentCalledFrom")
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
                onSaveBtnPressed(task!!, subject!!)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.did_not_change),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                this,
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
        val popupMenu = PopupMenu(this, view)
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
            val popupMenu = PopupMenu(this, view)
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