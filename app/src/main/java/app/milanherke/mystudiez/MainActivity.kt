package app.milanherke.mystudiez

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomappbar.BottomAppBar

import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        this.deleteDatabase("MyStudiez.db")

        val appDatabase = AppDatabase.getInstance(this)
        val db = appDatabase.readableDatabase


        testInsert()
        testInsert()
        testInsert()
        testInsert()
        testInsert()
        testInsert()
        testInsert()
        testInsert()
        testInsert()
        testInsert()
        testInsert()
        testUpdate()
        testDelete()

        val cursor = db.rawQuery("SELECT * FROM Subjects", null)
        Log.i(TAG, "*************************")
        cursor.use {
            while (it.moveToNext()) {
                with(cursor) {
                    val id = getLong(0)
                    val name = getString(1)
                    val teacher = getString(2)
                    val colorcode = getString(3)
                    val result = "Id: $id. Name: $name. Teacher: $teacher. Color code: $colorcode"
                    Log.i(TAG, "onCreate: reading data: $result")
                }
            }
        }
        Log.i(TAG, "*************************")

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val bottomBar = findViewById<BottomAppBar>(R.id.bar)
        bottomBar.replaceMenu(R.menu.bottomappbar_menu)
        bottomBar.setNavigationOnClickListener {
            Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show()
        }
    }

    private fun testInsert() {
        val values = ContentValues().apply {
            put(SubjectsContract.Columns.SUBJECT_NAME, "Subject 1")
            put(SubjectsContract.Columns.SUBJECT_TEACHER, "Teacher 1")
            put(SubjectsContract.Columns.SUBJECT_COLORCODE, "Color code 1")
        }

        val uri = contentResolver.insert(SubjectsContract.CONTENT_URI, values)
        Log.i(TAG, "New row id (in uri) is $uri")
        Log.i(TAG, "id (in uri) is ${TasksContract.getId(uri)}")
    }

    private fun testUpdate() {
        val values = ContentValues().apply {
            put(SubjectsContract.Columns.SUBJECT_NAME, "Subject 11111")
            put(SubjectsContract.Columns.SUBJECT_TEACHER, "Teacher 121")
            put(SubjectsContract.Columns.SUBJECT_COLORCODE, "Color code 132")
        }
        val subjectUri = SubjectsContract.buildUriFromId(5)
        val uri = contentResolver.update(subjectUri, values, null, null)
        Log.i(TAG, "Row with the id ${SubjectsContract.buildUriFromId(5)} is updated.")
    }

    private fun testDelete() {
        val subjectUri = SubjectsContract.buildUriFromId(6)
        val uri = contentResolver.delete(subjectUri, null, null)
        Log.i(TAG, "Row with the id 6 is deleted")
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
        return when(item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
