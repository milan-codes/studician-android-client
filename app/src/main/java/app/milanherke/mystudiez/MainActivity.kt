package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import app.milanherke.mystudiez.NotificationUtils.Companion.CHANNEL_ID
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MainActivity"
private const val TASK_NOTIFICATION_PRE_CODE = 100
private const val EXAM_NOTIFICATION_PRE_CODE = 200

class MainActivity : AppCompatActivity(),
    OverviewFragment.OverviewInteractions,
    SubjectsFragment.SubjectsInteractions,
    TasksFragment.TasksInteractions,
    ExamsFragment.ExamsInteractions,
    AddEditSubjectFragment.AddEditSubjectInteractions,
    AddEditLessonFragment.LessonSaved,
    AddEditTaskFragment.TaskSaved,
    AddEditExamFragment.ExamSaved,
    SubjectDetailsFragment.SubjectDetailsInteractions,
    LessonDetailsFragment.LessonDetailsInteraction,
    TaskDetailsFragment.TaskDetailsInteraction,
    ExamDetailsFragment.ExamDetailsInteraction {

    // The subject, whose details are displayed when SubjectDetailsFragment is called
    private var subject: Subject? = null

    // The lesson, whose details are displayed when LessonDetailsFragment is called
    private var lesson: Lesson? = null

    // The task, whose details are displayed when TaskDetailsFragment is called
    private var task: Task? = null

    // The exam, whose details are displayed when ExamDetailsFragment is called
    private var exam: Exam? = null

    // This variable is used to check whether the back button has already been pressed once
    private var doubleBackToExit: Boolean = false

    private val sharedViewModel by lazy {
        ViewModelProviders.of(this).get(SharedViewModel::class.java)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.setTitle(R.string.overview_title)
        setSupportActionBar(toolbar)

        // First we check if there's internet connection, then we check if the user is logged in
        if (FirebaseUtils.userIsLoggedIn()) {
            // Setting the home screen:
            // OverviewFragment is the first fragment seen by the user, just after the app has been opened
            replaceFragment(OverviewFragment.newInstance(), R.id.fragment_container)
            Toast.makeText(this, "User logged in", Toast.LENGTH_SHORT).show()
        } else {
            replaceFragment(SettingsFragment.newInstance(), R.id.fragment_container)
            Toast.makeText(this, "User is not logged in, please log in", Toast.LENGTH_SHORT).show()
        }

        // Creating channel to support android O and higher (for notifications)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils(this)
        }

        registerObservers()

        // Showing the bottom navigation bar
        val bottomBar = findViewById<BottomAppBar>(R.id.bar)
        bottomBar.setNavigationOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
            bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
        }

        fab.setOnClickListener {
            when (val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)) {
                is SubjectsFragment -> fabBtnInSubjectsFragment()
                is TasksFragment -> fabBtnInTasksFragment()
                is ExamsFragment -> fabBtnInExamsFragment()
                else -> {
                    throw IllegalStateException("FAB Button pressed in unrecognised fragment $fragment")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val fragment = findFragmentById(R.id.fragment_container)
        when (item.itemId) {
            android.R.id.home -> {
                when (fragment) {
                    is SubjectDetailsFragment -> upBtnInSubjectDetailsFragment()
                    is LessonDetailsFragment -> upBtnInLessonDetailsFragment()
                    is TaskDetailsFragment -> upBtnInTaskDetailsFragment()
                    is ExamDetailsFragment -> upBtnInExamDetailsFragment()
                    is AddEditSubjectFragment -> upBtnInAddEditSubjectFragment()
                    is AddEditLessonFragment -> upInAddEditLessonFragment()
                    is AddEditTaskFragment -> upInAddEditTaskFragment()
                    is AddEditExamFragment -> upInAddEditExamFragment()
                    else -> throw IllegalArgumentException("Up button used by unrecognised fragment $fragment")
                }
            }
            else -> super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when (val fragment = findFragmentById(R.id.fragment_container)) {
            is SubjectDetailsFragment -> upBtnInSubjectDetailsFragment()
            is LessonDetailsFragment -> upBtnInLessonDetailsFragment()
            is TaskDetailsFragment -> upBtnInTaskDetailsFragment()
            is ExamDetailsFragment -> upBtnInExamDetailsFragment()
            is AddEditSubjectFragment -> upBtnInAddEditSubjectFragment()
            is AddEditLessonFragment -> upInAddEditLessonFragment()
            is AddEditTaskFragment -> upInAddEditTaskFragment()
            is AddEditExamFragment -> upInAddEditExamFragment()
            is OverviewFragment, is SubjectsFragment, is TasksFragment, is ExamsFragment -> {
                if (doubleBackToExit) {
                    super.onBackPressed()
                    return
                }
                doubleBackToExit = true
                Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT)
                    .show()
            }
            else -> throw IllegalArgumentException("Back button pressed in unrecognised fragment $fragment")
        }
    }

    private fun scheduleNotification(
        notification: Notification,
        delay: Long,
        task: Task? = null,
        exam: Exam? = null
    ) {
        val requestCode = when {
            task != null -> {
                Integer.parseInt("${TASK_NOTIFICATION_PRE_CODE}${task.id}".filter { it.isDigit() })
            }
            exam != null -> {
                Integer.parseInt("${EXAM_NOTIFICATION_PRE_CODE}${exam.id}".filter { it.isDigit() })
            }
            else -> {
                throw IllegalStateException("Unrecognised notification type")
            }
        }
        val notificationIntent = Intent(this, NotificationPublisher::class.java)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, requestCode)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val futureInMillis = SystemClock.elapsedRealtime() + delay
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotification(contentTitle: String, contentText: String): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.reminder_icon)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        return builder.build()
    }

    // FAB BUTTON FUNCTIONS
    // These functions are used to determine what should be done when the fab button is pressed in specific fragments

    /**
     * When the FAB button is pressed in [SubjectsFragment], it should launch [AddEditSubjectFragment]
     * Because the user wants to add a new [Subject]
     */
    private fun fabBtnInSubjectsFragment() {
        replaceFragmentWithTransition(AddEditSubjectFragment.newInstance(), R.id.fragment_container)
    }

    /**
     * When the FAB button is pressed in [TasksFragment], it should launch [AddEditTaskFragment]
     * Because the user wants to add a new [Task]
     */
    private fun fabBtnInTasksFragment() {
        replaceFragmentWithTransition(AddEditTaskFragment.newInstance(), R.id.fragment_container)
    }

    /**
     * When the FAB button is pressed in [ExamsFragment], it should launch [AddEditExamFragment]
     * Because the user wants to add a new [Exam]
     */
    private fun fabBtnInExamsFragment() {
        replaceFragmentWithTransition(AddEditExamFragment.newInstance(), R.id.fragment_container)
    }


    // UP BUTTON FUNCTIONS
    // These functions are used to determine what should be loaded when the up button is pressed in specific fragments.

    /**
     * [SubjectDetailsFragment] can return only to [SubjectsFragment]
     * It can be called only by the following fragments: [SubjectsFragment], [LessonDetailsFragment], [TaskDetailsFragment] and [ExamDetailsFragment].
     */
    private fun upBtnInSubjectDetailsFragment() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.SUBJECTS, Fragments.LESSON_DETAILS, Fragments.TASK_DETAILS, Fragments.EXAM_DETAILS -> {
                replaceFragmentWithTransition(
                    SubjectsFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            else -> throw IllegalStateException("SubjectDetailsFragment was called by unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * [LessonDetailsFragment] can return only to [SubjectDetailsFragment] and [OverviewFragment].
     * It can be called only by the following fragments: [SubjectDetailsFragment] and [OverviewFragment].
     */
    private fun upBtnInLessonDetailsFragment() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                replaceFragmentWithTransition(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.OVERVIEW -> {
                replaceFragmentWithTransition(
                    OverviewFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            else -> throw IllegalStateException("LessonDetailsFragment was called by unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * [TaskDetailsFragment] can return only to [SubjectDetailsFragment], [TasksFragment] and [OverviewFragment].
     * It can be called only by the following fragments: [SubjectDetailsFragment], [TasksFragment] and [OverviewFragment].
     */
    private fun upBtnInTaskDetailsFragment() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                replaceFragmentWithTransition(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.TASKS -> {
                replaceFragmentWithTransition(
                    TasksFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.OVERVIEW -> {
                replaceFragmentWithTransition(
                    OverviewFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            else -> throw IllegalStateException("TaskDetailsFragment was called by unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * [ExamDetailsFragment] can return only to [SubjectDetailsFragment], [ExamsFragment] and [OverviewFragment].
     * It can be called only by the following fragments: [SubjectDetailsFragment], [ExamsFragment] and [OverviewFragment].
     */
    private fun upBtnInExamDetailsFragment() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                replaceFragmentWithTransition(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.EXAMS -> {
                replaceFragmentWithTransition(
                    ExamsFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.OVERVIEW -> {
                replaceFragmentWithTransition(
                    OverviewFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            else -> throw IllegalStateException("ExamDetailsFragment was called by unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * [AddEditSubjectFragment] can return only to [SubjectsFragment] and [SubjectDetailsFragment].
     * It can be called only by the following fragments: [SubjectsFragment] (when adding new) and [SubjectDetailsFragment] (when editing an existing one).
     */
    private fun upBtnInAddEditSubjectFragment() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.SUBJECTS -> {
                replaceFragmentWithTransition(
                    SubjectsFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.SUBJECT_DETAILS -> {
                // We can use the subject variable because the fragment was called from SubjectDetails, therefore an object has already been assigned to it.
                replaceFragmentWithTransition(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            else -> {
                throw IllegalStateException("AddEditSubjectFragment was called by unrecognised fragment $fragmentCalledFrom")
            }
        }
    }

    /**
     * [AddEditLessonFragment] can return only to [SubjectDetailsFragment] and [LessonDetailsFragment].
     * It can be called only by the following fragments: [SubjectDetailsFragment] (when adding new) and [LessonDetailsFragment] (when editing an existing one).
     */
    private fun upInAddEditLessonFragment() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                replaceFragmentWithTransition(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.LESSON_DETAILS -> {
                replaceFragmentWithTransition(
                    LessonDetailsFragment.newInstance(lesson!!, subject!!),
                    R.id.fragment_container
                )
            }
            else -> {
                throw IllegalStateException("AddEditLessonFragment was called by unrecognised fragment $fragmentCalledFrom")
            }
        }
    }

    /**
     * [AddEditTaskFragment] can return only to [TasksFragment] [SubjectDetailsFragment] and [TaskDetailsFragment].
     * It can be called only by the following fragments:
     *  [TasksFragment]: When pressing the FAB button and creating a new one
     *  [SubjectDetailsFragment]: When adding a new one from [SubjectDetailsFragment]
     *  [TaskDetailsFragment]: When editing an existing one
     */
    private fun upInAddEditTaskFragment() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.TASKS -> {
                replaceFragmentWithTransition(
                    TasksFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.SUBJECT_DETAILS -> {
                replaceFragmentWithTransition(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.TASK_DETAILS -> replaceFragmentWithTransition(
                TaskDetailsFragment.newInstance(
                    task!!, subject!!
                ), R.id.fragment_container
            )
            else -> {
                throw IllegalStateException("AddEditTaskFragment was called by unrecognised fragment $fragmentCalledFrom")
            }
        }
    }

    /**
     * [AddEditExamFragment] can return only to [ExamsFragment] [SubjectDetailsFragment] and [ExamDetailsFragment].
     * It can be called only by the following fragments:
     *  [ExamsFragment]: When pressing the FAB button and creating a new one
     *  [SubjectDetailsFragment]: When adding new
     *  [ExamDetailsFragment]: When editing an existing one
     */
    private fun upInAddEditExamFragment() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.EXAMS -> {
                replaceFragmentWithTransition(
                    ExamsFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.SUBJECT_DETAILS -> {
                replaceFragmentWithTransition(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.EXAM_DETAILS -> replaceFragmentWithTransition(
                ExamDetailsFragment.newInstance(
                    exam!!, subject!!
                ), R.id.fragment_container
            )
            else -> {
                throw IllegalStateException("AddEditExamFragment was called by unrecognised fragment $fragmentCalledFrom")
            }
        }
    }

    // INTERACTION INTERFACES
    // Implementing the interfaces which are required to communicate with the fragments.


    /**
     * We need to set [doubleBackToExit]'s value to false,
     * because we must prevent the app from closing after the back button has only been pressed just once
     */
    private fun setDoubleBackToFalse() {
        doubleBackToExit = false
    }


    /**
     * Interaction interface(s) from [OverviewFragment]
     */
    override fun setDoubleBackToDefault() {
        setDoubleBackToFalse()
    }


    /**
     * Interaction interface(s) from [SubjectsFragment]
     */
    override fun subjectsFragmentIsBeingCreated() {
        setDoubleBackToFalse()
    }


    /**
     * Interaction interface(s) from [TasksFragment]
     */
    override fun tasksFragmentIsBeingCreated() {
        setDoubleBackToFalse()
    }


    /**
     * Interaction interface(s) from [ExamsFragment]
     */
    override fun onCreateCalled() {
        setDoubleBackToFalse()
    }


    /**
     * Interaction interface(s) from [AddEditSubjectFragment]
     */

    override fun onSaveSubjectClick(subject: Subject) {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> replaceFragmentWithTransition(
                SubjectDetailsFragment.newInstance(subject),
                R.id.fragment_container
            )
            Fragments.SUBJECTS -> replaceFragmentWithTransition(
                SubjectsFragment.newInstance(),
                R.id.fragment_container
            )
            else -> throw IllegalStateException("onSaveSubjectClicked tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }


    /**
     * Interaction interface(s) from [AddEditLessonFragment]
     */

    override fun onSaveLessonClickListener(lesson: Lesson, subject: Subject) {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> replaceFragmentWithTransition(
                SubjectDetailsFragment.newInstance(subject),
                R.id.fragment_container
            )
            Fragments.LESSON_DETAILS -> replaceFragmentWithTransition(
                LessonDetailsFragment.newInstance(lesson, subject),
                R.id.fragment_container
            )
            else -> throw IllegalStateException("onSaveLessonClick tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }


    /**
     * Interaction interface(s) from [AddEditTaskFragment]
     */

    override fun onSaveTaskClickListener(task: Task, subject: Subject) {
        if (task.reminder.isNotEmpty()) {
            val notification =
                createNotification(getString(R.string.notification_task_reminder_title), task.name)
            val reminder = SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss",
                Locale.getDefault()
            ).parse(task.reminder + ":00")
            val delay = reminder.time.minus(System.currentTimeMillis())
            scheduleNotification(notification, delay, task, null)
        }

        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.TASKS -> replaceFragmentWithTransition(
                TasksFragment.newInstance(),
                R.id.fragment_container
            )
            Fragments.SUBJECT_DETAILS -> replaceFragmentWithTransition(
                SubjectDetailsFragment.newInstance(subject),
                R.id.fragment_container
            )
            Fragments.TASK_DETAILS -> replaceFragmentWithTransition(
                TaskDetailsFragment.newInstance(task, subject),
                R.id.fragment_container
            )
            else -> throw IllegalStateException("onSaveTaskClicked tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }


    /**
     * Interaction interface(s) from [AddEditExamFragment]
     */

    override fun onSaveExamClickListener(exam: Exam, subject: Subject) {
        if (exam.reminder.isNotEmpty() && exam.reminder != getString(R.string.add_edit_lesson_btn)) {
            val notification =
                createNotification(getString(R.string.notification_exam_reminder_title), exam.name)
            val reminder = SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss",
                Locale.getDefault()
            ).parse(exam.reminder + ":00")
            val delay = reminder.time.minus(System.currentTimeMillis())
            scheduleNotification(notification, delay, null, exam)
        }
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.EXAMS -> replaceFragmentWithTransition(
                ExamsFragment.newInstance(),
                R.id.fragment_container
            )
            Fragments.SUBJECT_DETAILS -> replaceFragmentWithTransition(
                SubjectDetailsFragment.newInstance(subject),
                R.id.fragment_container
            )
            Fragments.EXAM_DETAILS -> replaceFragmentWithTransition(
                ExamDetailsFragment.newInstance(exam, subject),
                R.id.fragment_container
            )
            else -> throw IllegalStateException("onSaveExamClicked tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }


    /**
     * Interaction interface(s) from [SubjectDetailsFragment]
     */

    override fun subjectIsLoaded(subject: Subject) {
        this.subject = subject
    }

    /**
     * Interaction interface(s) from [SubjectDetailsFragment]
     */

    override fun swapLesson(lesson: Lesson) {
        this.lesson = lesson
    }


    /**
     * Interaction interface(s) from [TaskDetailsFragment]
     */

    override fun taskIsLoaded(task: Task) {
        this.task = task
    }


    /**
     * Interaction interface(s) from [ExamDetailsFragment]
     */

    override fun swapExam(exam: Exam) {
        this.exam = exam
    }

    override fun swapSubject(subject: Subject) {
        this.subject = subject
    }

    private fun registerObservers() {
        sharedViewModel.subjectLiveData.observe(
            this,
            Observer { subject -> this.subject = subject }
        )
    }
}
