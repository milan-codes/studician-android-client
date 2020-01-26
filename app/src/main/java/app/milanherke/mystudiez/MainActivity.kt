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
    AddEditSubjectFragment.AddEditSubjectInteractions, AddEditLessonFragment.OnSaveLessonClick,
    SubjectDetailsFragment.SubjectDetailsInteractions,
    LessonDetailsFragment.LessonDetailsInteraction {

    private var subject: Subject? = null
    private var fragmentCalledFrom: Fragments? = null

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
                    is SubjectDetailsFragment -> {
                        replaceFragment(SubjectsFragment.newInstance(), R.id.fragment_container)
                    }
                    is LessonDetailsFragment -> {
                        replaceFragment(
                            SubjectDetailsFragment.newInstance(subject!!),
                            R.id.fragment_container
                        )
                    }
                    is AddEditSubjectFragment -> {
                        when (fragmentCalledFrom) {
                            Fragments.SUBJECT -> {
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
                                throw IllegalStateException("AddEditSubjectFragment called from unrecognised fragment $fragmentCalledFrom")
                            }
                        }
                    }
                    is AddEditLessonFragment -> {
                        replaceFragment(
                            SubjectDetailsFragment.newInstance(subject!!),
                            R.id.fragment_container
                        )
                    }
                    else -> throw IllegalArgumentException("Home button used by unrecognised fragment $fragment")
                }
            }
            else -> super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * Fragment interfaces from [AddEditSubjectFragment]
     */
    override fun addEditSubjectCreated(fragment: Fragments) {
        fragmentCalledFrom = fragment
    }

    override fun onSaveSubjectClick(subject: Subject) {
        if (fragmentCalledFrom == Fragments.SUBJECT_DETAILS) {
            replaceFragment(SubjectDetailsFragment.newInstance(subject), R.id.fragment_container)
        } else {
            replaceFragment(SubjectsFragment.newInstance(), R.id.fragment_container)
        }
    }

    /**
     * Fragment interfaces from [AddEditLessonFragment]
     */
    override fun onSaveLessonClick(subject: Subject) {
        replaceFragment(SubjectDetailsFragment.newInstance(subject), R.id.fragment_container)
    }

    /**
     * Fragment interfaces from [SubjectDetailsFragment]
     */
    override fun subjectIsLoaded(subject: Subject) {
        this.subject = subject
    }

    /**
     * Fragment interfaces from [LessonDetailsFragment]
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
}
