package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
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
import java.lang.IllegalStateException
import java.util.*

private const val TAG = "MainActivity"

const val SUBJECTS_STATE = "SubjectsState"
var APP_STATE = SUBJECTS_STATE


class MainActivity : AppCompatActivity(),
    SubjectDetailsFragment.OnLessonClick, AddEditSubjectFragment.OnSaveSubjectClick {

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

        fab.setOnClickListener { view ->
            when(val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)) {
                is SubjectsFragment -> {
                    replaceFragment(AddEditSubjectFragment.newInstance(), R.id.fragment_container)
                    bar.visibility = View.INVISIBLE
                    fab.visibility = View.INVISIBLE

                } else -> {
                    Snackbar.make(view, "You are in the SubjectDetailsFragment", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
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
                    is AddEditSubjectFragment -> {
                        replaceFragment(SubjectsFragment.newInstance(), R.id.fragment_container)
                    } else -> throw IllegalArgumentException("Home button used by unrecognised fragment $fragment")
                }
            }
            else -> super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }



    // Fragment interfaces
    override fun onLessonTap(uri: Uri) {
        Toast.makeText(this, "RecyclerView tapped", Toast.LENGTH_SHORT).show()
    }

    override fun onSaveSubjectClick() {
        replaceFragment(SubjectsFragment.newInstance(), R.id.fragment_container)
    }
}
