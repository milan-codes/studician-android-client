package app.milanherke.mystudiez.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import app.milanherke.mystudiez.*
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.ACTIVITY_NAME_BUNDLE_ID
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.EXAM_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.FRAGMENT_TO_LOAD_BUNDLE_ID
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.LESSON_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.SUBJECT_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.TASK_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.FragmentBackStack
import app.milanherke.mystudiez.fragments.*
import app.milanherke.mystudiez.models.Exam
import app.milanherke.mystudiez.models.Lesson
import app.milanherke.mystudiez.models.Subject
import app.milanherke.mystudiez.models.Task
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.createNotification
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.scheduleNotification
import app.milanherke.mystudiez.utils.FirebaseUtils
import app.milanherke.mystudiez.utils.NotificationUtils
import app.milanherke.mystudiez.viewmodels.SharedViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
    OverviewFragment.OverviewInteractions,
    SubjectsFragment.SubjectsInteractions,
    TasksFragment.TasksInteractions,
    ExamsFragment.ExamsInteractions,
    AddEditLessonFragment.LessonSaved,
    AddEditExamFragment.ExamSaved{

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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.setTitle(R.string.overview_title)
        setSupportActionBar(toolbar)

        // Getting intent if there was any
        val intent = intent
        if (intent.hasExtra(ACTIVITY_NAME_BUNDLE_ID)) {
            when (intent.getStringExtra(FRAGMENT_TO_LOAD_BUNDLE_ID)) {
                SubjectsFragment.TAG -> {
                    replaceFragment(
                        SubjectsFragment.newInstance(),
                        R.id.fragment_container
                    )
                }
                SubjectDetailsFragment.TAG -> {
                    val subject = intent.getParcelableExtra<Subject>(SUBJECT_PARAM_BUNDLE_ID)
                    replaceFragment(
                        SubjectDetailsFragment.newInstance(subject),
                        R.id.fragment_container
                    )
                }
                LessonDetailsFragment.TAG -> {
                    val lesson = intent.getParcelableExtra<Lesson>(LESSON_PARAM_BUNDLE_ID)
                    val subject = intent.getParcelableExtra<Subject>(SUBJECT_PARAM_BUNDLE_ID)
                    replaceFragment(
                        LessonDetailsFragment.newInstance(lesson, subject),
                        R.id.fragment_container
                    )
                }
                TasksFragment.TAG -> {
                    replaceFragment(TasksFragment.newInstance(),
                        R.id.fragment_container
                    )
                }
                TaskDetailsFragment.TAG -> {
                    val task = intent.getParcelableExtra<Task>(TASK_PARAM_BUNDLE_ID)
                    val subject = intent.getParcelableExtra<Subject>(SUBJECT_PARAM_BUNDLE_ID)
                    replaceFragment(
                        TaskDetailsFragment.newInstance(task, subject),
                        R.id.fragment_container
                    )
                }
                ExamsFragment.TAG -> {
                    replaceFragment(
                        ExamsFragment.newInstance(),
                        R.id.fragment_container
                    )
                }
                ExamDetailsFragment.TAG -> {
                    val exam = intent.getParcelableExtra<Exam>(EXAM_PARAM_BUNDLE_ID)
                    val subject = intent.getParcelableExtra<Subject>(SUBJECT_PARAM_BUNDLE_ID)
                    replaceFragment(
                        ExamDetailsFragment.newInstance(exam, subject),
                        R.id.fragment_container
                    )
                }
            }
        } else {
            // First we check if there's internet connection, then we check if the user is logged in
            if (FirebaseUtils.userIsLoggedIn()) {
                // Setting the home screen:
                // OverviewFragment is the first fragment seen by the user, just after the app has been opened
                replaceFragment(OverviewFragment.newInstance(),
                    R.id.fragment_container
                )
                Toast.makeText(this, getString(R.string.firebase_user_logged_in), Toast.LENGTH_SHORT)
                    .show()
            } else {
                replaceFragment(SettingsFragment.newInstance(),
                    R.id.fragment_container
                )
                Toast.makeText(this, getString(R.string.firebase_user_logged_out), Toast.LENGTH_SHORT)
                    .show()
            }
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
        // automatically handle clicks on the Home/Up button, as long
        // as you specify a parent activity in AndroidManifest.xml.
        val fragment = findFragmentById(R.id.fragment_container)
        when (item.itemId) {
            android.R.id.home -> {
                when (fragment) {
                    is SubjectDetailsFragment -> upBtnInSubjectDetailsFragment()
                    is LessonDetailsFragment -> upBtnInLessonDetailsFragment()
                    is TaskDetailsFragment -> upBtnInTaskDetailsFragment()
                    is ExamDetailsFragment -> upBtnInExamDetailsFragment()
                    is AddEditLessonFragment -> upInAddEditLessonFragment()
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
            is AddEditLessonFragment -> upInAddEditLessonFragment()
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

    // FAB BUTTON FUNCTIONS
    // These functions are used to determine what should be done when the fab button is pressed in specific fragments

    /**
     * When the FAB button is pressed in [SubjectsFragment], it should launch [AddEditSubjectActivity]
     * Because the user wants to add a new [Subject]
     */
    private fun fabBtnInSubjectsFragment() {
        FragmentBackStack.getInstance(this).push(
            Fragments.SUBJECTS
        )
        val intent = Intent(this, AddEditSubjectActivity::class.java)
        startActivity(intent)
    }

    /**
     * When the FAB button is pressed in [TasksFragment], it should launch [AddEditTaskActivity]
     * Because the user wants to add a new [Task]
     */
    private fun fabBtnInTasksFragment() {
        FragmentBackStack.getInstance(this).push(
            Fragments.TASKS
        )
        val intent = Intent(this, AddEditTaskActivity::class.java)
        startActivity(intent)
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

    override fun onSaveExamClickListener(exam: Exam, subject: Subject) {
        val reminder = exam.reminder
        if (reminder != null) {
            val notification =
                createNotification(this, getString(R.string.notification_exam_reminder_title), exam.name)
            val delay = reminder.time.minus(System.currentTimeMillis())
            scheduleNotification(this, notification, delay, null, exam)
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
     * Registers the necessary observers
     * to provide [MainActivity] with the latest
     * [Subject], [Lesson], [Task] and [Exam].
     */
    private fun registerObservers() {
        sharedViewModel.subjectLiveData.observe(
            this,
            Observer { subject -> this.subject = subject }
        )
        sharedViewModel.lessonLiveData.observe(
            this,
            Observer { lesson -> this.lesson = lesson }
        )
        sharedViewModel.taskLiveData.observe(
            this,
            Observer { task -> this.task = task }
        )
        sharedViewModel.examLiveData.observe(
            this,
            Observer { exam -> this.exam = exam }
        )
    }
}
