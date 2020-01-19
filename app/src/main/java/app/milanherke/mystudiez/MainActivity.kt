package app.milanherke.mystudiez

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomappbar.BottomAppBar

import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.*

private const val TAG = "MainActivity"

const val SUBJECTS_STATE = "SubjectsState"
var APP_STATE = SUBJECTS_STATE


class MainActivity : AppCompatActivity(),
    SubjectDetailsFragment.OnLessonClick {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.setTitle(R.string.overview_title)
        setSupportActionBar(toolbar)

        replaceFragment(loadCorrectFragment(APP_STATE), R.id.fragment_container)

        fab.setOnClickListener { view ->
            when(val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)) {
                is SubjectsFragment -> {
                    val intent = Intent(this, NewSubjectActivity::class.java)
                    this.startActivity(intent)
                } else -> {
                    Snackbar.make(view, "You are in the SubjectDetailsFragment", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                    throw IllegalStateException("Unrecognised fragment $fragment")
                }
            }
        }

        // Showing the bottom navigation bar
        val bottomBar = findViewById<BottomAppBar>(R.id.bar)
        bottomBar.setNavigationOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
            bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
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
        when (item.itemId) {
            android.R.id.home -> {
                when (findFragmentById(R.id.fragment_container)) {
                    is SubjectDetailsFragment -> {
                        replaceFragment(SubjectsFragment.newInstance(), R.id.fragment_container)
                    }
                }
            }
            else -> super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun testInsertMatek() {
        var values = ContentValues().apply {
            put(SubjectsContract.Columns.SUBJECT_NAME, "Matematika")
            put(SubjectsContract.Columns.SUBJECT_TEACHER, "Sheldon Cooper")
            put(SubjectsContract.Columns.SUBJECT_COLORCODE, R.color.subjectColorAmber)
        }

        val uri = contentResolver.insert(SubjectsContract.CONTENT_URI, values)
        val id = SubjectsContract.getId(uri!!)

        values = ContentValues().apply {
            put(LessonsContract.Columns.LESSON_SUBJECT, id)
            put(LessonsContract.Columns.LESSON_WEEK, "A")
            put(LessonsContract.Columns.LESSON_DAY, "Monday")
            put(LessonsContract.Columns.LESSON_STARTS, "8:15")
            put(LessonsContract.Columns.LESSON_ENDS, "9:30")
            put(LessonsContract.Columns.LESSON_LOCATION, "32-es terem")
        }

        contentResolver.insert(LessonsContract.CONTENT_URI, values)

        values = ContentValues().apply {
            put(LessonsContract.Columns.LESSON_SUBJECT, id)
            put(LessonsContract.Columns.LESSON_WEEK, "A")
            put(LessonsContract.Columns.LESSON_DAY, "Thursday")
            put(LessonsContract.Columns.LESSON_STARTS, "10:15")
            put(LessonsContract.Columns.LESSON_ENDS, "11:30")
            put(LessonsContract.Columns.LESSON_LOCATION, "38-as terem")
        }

        contentResolver.insert(LessonsContract.CONTENT_URI, values)

        values = ContentValues().apply {
            put(LessonsContract.Columns.LESSON_SUBJECT, id)
            put(LessonsContract.Columns.LESSON_WEEK, "A")
            put(LessonsContract.Columns.LESSON_DAY, "Friday")
            put(LessonsContract.Columns.LESSON_STARTS, "8:30")
            put(LessonsContract.Columns.LESSON_ENDS, "10:00")
            put(LessonsContract.Columns.LESSON_LOCATION, "16-os terem")
        }

        contentResolver.insert(LessonsContract.CONTENT_URI, values)

        values = ContentValues().apply {
            put(TasksContract.Columns.TASK_NAME, "16. oldal, 3. feladat")
            put(TasksContract.Columns.TASK_DESCRIPTION, "Összes feladat")
            put(TasksContract.Columns.TASK_TYPE, "Assignment")
            put(TasksContract.Columns.TASK_SUBJECT, id)
            put(TasksContract.Columns.TASK_DUEDATE, Date().toString())
            put(TasksContract.Columns.TASK_REMINDER, Date().toString())
        }

        contentResolver.insert(TasksContract.CONTENT_URI, values)

        values = ContentValues().apply {
            put(TasksContract.Columns.TASK_NAME, "19. oldal, 1. feladat")
            put(TasksContract.Columns.TASK_DESCRIPTION, "Összes feladat")
            put(TasksContract.Columns.TASK_TYPE, "Assignment")
            put(TasksContract.Columns.TASK_SUBJECT, id)
            put(TasksContract.Columns.TASK_DUEDATE, Date().toString())
            put(TasksContract.Columns.TASK_REMINDER, Date().toString())
        }

        contentResolver.insert(TasksContract.CONTENT_URI, values)

        values = ContentValues().apply {
            put(ExamsContract.Columns.EXAM_NAME, "Térgeometria")
            put(ExamsContract.Columns.EXAM_DESCRIPTION, "Minden térgeometriával kapcsolatos feladat")
            put(ExamsContract.Columns.EXAM_SUBJECT, id)
            put(ExamsContract.Columns.EXAM_DATE, Date().toString())
            put(ExamsContract.Columns.EXAM_REMINDER, Date().toString())
        }

        contentResolver.insert(ExamsContract.CONTENT_URI, values)
    }

    // Fragment interfaces
    override fun onLessonTap(uri: Uri) {
        Toast.makeText(this, "RecyclerView tapped", Toast.LENGTH_SHORT).show()
    }

}
