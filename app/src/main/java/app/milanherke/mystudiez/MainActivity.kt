package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomappbar.BottomAppBar

import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

private const val TAG = "MainActivity"

const val SUBJECTS_STATE = "SubjectsState"
var APP_STATE = SUBJECTS_STATE


class MainActivity : AppCompatActivity(),
    AddEditSubjectFragment.AddEditSubjectInteractions,
    AddEditLessonFragment.OnSaveLessonClick,
    SubjectDetailsFragment.SubjectDetailsInteractions,
    LessonDetailsFragment.LessonDetailsInteraction,
    AddEditTaskFragment.AddEditTaskInteractions,
    TaskDetailsFragment.TaskDetailsInteraction {

    // The subject, whose details are displayed when SubjectDetailsFragment is called
    private var subject: Subject? = null
    // The lesson, whose details are displayed when LessonDetailsFragment is called
    private var lesson: Lesson? = null
    // The task, whose details are displayed when TaskDetailsFragment is called
    private var task: Task? = null

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
                is SubjectsFragment -> {
                    replaceFragment(AddEditSubjectFragment.newInstance(), R.id.fragment_container)
                    bar.visibility = View.GONE
                    fab.visibility = View.GONE
                }
                else -> {
                    throw IllegalStateException("Unrecognised fragment $fragment")
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
                    is AddEditSubjectFragment -> upBtnInAddEditSubjectFragment()
                    is AddEditLessonFragment -> upInAddEditLessonFragment()
                    is AddEditTaskFragment -> upInAddEditTaskFragment()
                    else -> throw IllegalArgumentException("Up button used by unrecognised fragment $fragment")
                }
            }
            else -> super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }

    // UP BUTTON FUNCTIONS
    // These functions are used to determine what should be loaded when the up button is pressed in specific fragments.

    /**
     * [SubjectDetailsFragment] can return only to [SubjectsFragment]
     * It can be called only by the following fragments: [SubjectsFragment], [LessonDetailsFragment] and [TaskDetailsFragment].
     */
    private fun upBtnInSubjectDetailsFragment() {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECTS, Fragments.LESSON_DETAILS, Fragments.TASK_DETAILS -> {
                replaceFragment(SubjectsFragment.newInstance(), R.id.fragment_container)
            }
            else -> throw IllegalStateException("SubjectDetailsFragment was called by unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * [LessonDetailsFragment] can return only to [SubjectDetailsFragment].
     * It can be called only by the following fragments: [SubjectDetailsFragment].
     */
    private fun upBtnInLessonDetailsFragment() {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                replaceFragment(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            else -> throw IllegalStateException("LessonDetailsFragment was called by unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * [TaskDetailsFragment] can return only to [SubjectDetailsFragment].
     * It can be called only by the following fragments: [SubjectDetailsFragment].
     */
    private fun upBtnInTaskDetailsFragment() {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                replaceFragment(
                    SubjectDetailsFragment.newInstance(subject!!),
                    R.id.fragment_container
                )
            }
            else -> throw IllegalStateException("TaskDetailsFragment was called by unrecognised fragment $fragmentCalledFrom")
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
     * [AddEditTaskFragment] can return only to [SubjectDetailsFragment] and [TaskDetailsFragment].
     * It can be called only by the following fragments: [SubjectDetailsFragment] (when adding new) and [TaskDetailsFragment] (when editing an existing one).
     */
    private fun upInAddEditTaskFragment() {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
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
     * Interaction interface(s) from [TaskDetailsFragment]
     */
    override fun onDeleteTaskClick(subject: Subject) {
        when (val fragmentCalledFrom = FragmentsStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> replaceFragment(
                SubjectDetailsFragment.newInstance(subject),
                R.id.fragment_container
            )
            else -> throw IllegalStateException("onDeleteTaskClick tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }

    override fun onEditTaskClick(task: Task) {
        replaceFragment(AddEditTaskFragment.newInstance(task, subject), R.id.fragment_container)
    }

    override fun taskIsLoaded(task: Task) {
        this.task = task
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
}
