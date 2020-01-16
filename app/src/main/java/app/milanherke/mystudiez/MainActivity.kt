package app.milanherke.mystudiez

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomappbar.BottomAppBar

import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException

private const val TAG = "MainActivity"

const val SUBJECTS_STATE = "SubjectsState"
const val LESSONS_STATE = "LessonsState"
const val TASKS_STATE = "TasksState"
const val EXAMS_STATE = "ExamsState"
var APP_STATE = SUBJECTS_STATE


class MainActivity : AppCompatActivity(), SubjectsFragment.OnSubjectClick,
    LessonsFragment.OnLessonClick {


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.setTitle(R.string.overview_title)
        setSupportActionBar(toolbar)

        replaceFragment(loadCorrectFragment(APP_STATE), R.id.fragment_container)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
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
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Fragment interfaces
    override fun OnTap(uri: Uri) {
        Toast.makeText(this, "RecyclerView tapped", Toast.LENGTH_SHORT).show()
    }

    override fun onLessonTap(uri: Uri) {
        Toast.makeText(this, "RecyclerView tapped", Toast.LENGTH_SHORT).show()
    }
}
