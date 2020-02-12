package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

const val SUBJECTS_STATE = "SubjectsState"
var APP_STATE = SUBJECTS_STATE


class MainActivity : AppCompatActivity(),
    AddEditSubjectFragment.AddEditSubjectInteractions,
    AddEditLessonFragment.OnSaveLessonClick,
    AddEditTaskFragment.AddEditTaskInteractions,
    AddEditExamFragment.AddEditExamInteractions,
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

        replaceFragment(loadCorrectFragment(APP_STATE), R.id.fragment_container)

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

    private fun loadCorrectFragment(frag: String): Fragment {
        return when (frag) {
            SUBJECTS_STATE -> SubjectsFragment.newInstance()
            else -> throw IllegalArgumentException("Unknown fragment passed")
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

    // FAB BUTTON FUNCTIONS
    // These functions are used to determine what should be done when the fab button is pressed in specific fragments

    /**
     * When the FAB button is pressed in [SubjectsFragment], it should launch [AddEditSubjectFragment]
     * Because the user wants to add a new [Subject]
     */
    private fun fabBtnInSubjectsFragment() {
        replaceFragment(AddEditSubjectFragment.newInstance(), R.id.fragment_container)
    }

    /**
     * When the FAB button is pressed in [TasksFragment], it should launch [AddEditTaskFragment]
     * Because the user wants to add a new [Task]
     */
    private fun fabBtnInTasksFragment() {
        replaceFragment(AddEditTaskFragment.newInstance(), R.id.fragment_container)
    }

    /**
     * When the FAB button is pressed in [ExamsFragment], it should launch [AddEditExamFragment]
     * Because the user wants to add a new [Exam]
     */
    private fun fabBtnInExamsFragment() {
        replaceFragment(AddEditExamFragment.newInstance(), R.id.fragment_container)
    }


    // UP BUTTON FUNCTIONS
    // These functions are used to determine what should be loaded when the up button is pressed in specific fragments.

    /**
     * [SubjectDetailsFragment] can return only to [SubjectsFragment]
     * It can be called only by the following fragments: [SubjectsFragment], [LessonDetailsFragment], [TaskDetailsFragment] and [ExamDetailsFragment].
     */
    private fun upBtnInSubjectDetailsFragment() {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECTS, Fragments.LESSON_DETAILS, Fragments.TASK_DETAILS, Fragments.EXAM_DETAILS -> {
                replaceFragment(SubjectsFragment.newInstance(), R.id.fragment_container)
            }
            else -> throw IllegalStateException("SubjectDetailsFragment was called by unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * [LessonDetailsFragment] can return only to [SubjectDetailsFragment] and [OverviewFragment].
     * It can be called only by the following fragments: [SubjectDetailsFragment] and [OverviewFragment].
     */
    private fun upBtnInLessonDetailsFragment() {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                replaceFragment(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.OVERVIEW -> {
                replaceFragment(
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
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                replaceFragment(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.TASKS -> {
                replaceFragment(
                    TasksFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.OVERVIEW -> {
                replaceFragment(
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
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                replaceFragment(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.EXAMS -> {
                replaceFragment(
                    ExamsFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.OVERVIEW -> {
                replaceFragment(
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
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECTS -> {
                replaceFragment(
                    SubjectsFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.SUBJECT_DETAILS -> {
                // We can use the subject variable because the fragment was called from SubjectDetails, therefore an object has already been assigned to it.
                replaceFragment(
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
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                replaceFragment(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.LESSON_DETAILS -> {
                replaceFragment(
                    LessonDetailsFragment.newInstance(lesson!!),
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
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.TASKS -> {
                replaceFragment(
                    TasksFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.SUBJECT_DETAILS -> {
                replaceFragment(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.TASK_DETAILS -> replaceFragment(
                TaskDetailsFragment.newInstance(
                    task!!
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
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.EXAMS -> {
                replaceFragment(
                    ExamsFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.SUBJECT_DETAILS -> {
                replaceFragment(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            Fragments.EXAM_DETAILS -> replaceFragment(
                ExamDetailsFragment.newInstance(
                    exam!!
                ), R.id.fragment_container
            )
            else -> {
                throw IllegalStateException("AddEditExamFragment was called by unrecognised fragment $fragmentCalledFrom")
            }
        }
    }

    // INTERACTION INTERFACES
    // Implementing the interfaces which are required to communicate with a fragment.

    /**
     * Interaction interface(s) from [AddEditSubjectFragment]
     */

    override fun onSaveSubjectClick(subject: Subject) {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> replaceFragment(
                SubjectDetailsFragment.newInstance(subject),
                R.id.fragment_container
            )
            Fragments.SUBJECTS -> replaceFragment(
                SubjectsFragment.newInstance(),
                R.id.fragment_container
            )
            else -> throw IllegalStateException("onSaveSubjectClicked tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }


    /**
     * Interaction interface(s) from [AddEditLessonFragment]
     */

    override fun onSaveLessonClick(lesson: Lesson) {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> replaceFragment(
                SubjectDetailsFragment.newInstance(subject!!),
                R.id.fragment_container
            )
            Fragments.LESSON_DETAILS -> replaceFragment(
                LessonDetailsFragment.newInstance(lesson),
                R.id.fragment_container
            )
            else -> throw IllegalStateException("onSaveLessonClick tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }


    /**
     * Interaction interface(s) from [AddEditTaskFragment]
     */

    override fun onSaveTaskClicked(task: Task) {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.TASKS -> replaceFragment(
                TasksFragment.newInstance(),
                R.id.fragment_container
            )
            Fragments.SUBJECT_DETAILS -> replaceFragment(
                SubjectDetailsFragment.newInstance(subject!!),
                R.id.fragment_container
            )
            Fragments.TASK_DETAILS -> replaceFragment(
                TaskDetailsFragment.newInstance(task),
                R.id.fragment_container
            )
            else -> throw IllegalStateException("onSaveTaskClicked tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }


    /**
     * Interaction interface(s) from [AddEditExamFragment]
     */

    override fun onSaveExamClicked(exam: Exam) {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.EXAMS -> replaceFragment(
                ExamsFragment.newInstance(),
                R.id.fragment_container
            )
            Fragments.SUBJECT_DETAILS -> replaceFragment(
                SubjectDetailsFragment.newInstance(subject!!),
                R.id.fragment_container
            )
            Fragments.EXAM_DETAILS -> replaceFragment(
                ExamDetailsFragment.newInstance(exam),
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
     * Interaction interface(s) from [LessonDetailsFragment]
     */

    override fun onDeleteLessonClick(subject: Subject) {
        replaceFragment(SubjectDetailsFragment.newInstance(subject), R.id.fragment_container)
    }

    override fun onEditLessonClick(lesson: Lesson) {
        replaceFragment(
            AddEditLessonFragment.newInstance(lesson, subject!!),
            R.id.fragment_container
        )
    }

    override fun lessonIsLoaded(lesson: Lesson) {
        this.lesson = lesson
    }


    /**
     * Interaction interface(s) from [TaskDetailsFragment]
     */

    override fun onDeleteTaskClick(subject: Subject) {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> replaceFragment(
                SubjectDetailsFragment.newInstance(subject),
                R.id.fragment_container
            )
            Fragments.TASKS -> {
                replaceFragment(
                    TasksFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            else -> throw IllegalStateException("onDeleteTaskClick tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }

    override fun onEditTaskClick(task: Task) {
        replaceFragment(AddEditTaskFragment.newInstance(task, sharedViewModel.subjectFromId(task.subjectId)), R.id.fragment_container)
    }

    override fun taskIsLoaded(task: Task) {
        this.task = task
    }


    /**
     * Interaction interface(s) from [ExamDetailsFragment]
     */

    override fun onDeleteExamClick(subject: Subject) {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> replaceFragment(
                SubjectDetailsFragment.newInstance(subject),
                R.id.fragment_container
            )
            Fragments.EXAMS -> {
                replaceFragment(
                    ExamsFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            else -> throw IllegalStateException("onDeleteExamClick tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }

    override fun onEditExamClick(exam: Exam) {
        replaceFragment(AddEditExamFragment.newInstance(exam, sharedViewModel.subjectFromId(exam.subjectId)), R.id.fragment_container)
    }

    override fun examIsLoaded(exam: Exam) {
        this.exam = exam
    }
}
