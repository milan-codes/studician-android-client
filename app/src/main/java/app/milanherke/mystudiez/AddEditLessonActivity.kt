package app.milanherke.mystudiez

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
import app.milanherke.mystudiez.ActivityUtils.Companion.LESSON_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.ActivityUtils.Companion.SUBJECT_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.fragments.LessonDetailsFragment
import app.milanherke.mystudiez.fragments.SubjectDetailsFragment
import app.milanherke.mystudiez.fragments.UnsavedChangesDialogFragment
import app.milanherke.mystudiez.models.Lesson
import app.milanherke.mystudiez.models.Subject
import app.milanherke.mystudiez.viewmodels.activities.AddEditLessonViewModel
import kotlinx.android.synthetic.main.activity_add_edit_lesson.*
import kotlinx.android.synthetic.main.content_add_edit_lesson.*
import java.util.*

/**
 * A simple [AppCompatActivity] subclass.
 * The purpose of this activity is to add or edit lessons.
 */
class AddEditLessonActivity : AppCompatActivity(), UnsavedChangesDialogFragment.DialogInteractions {

    private var lesson: Lesson? = null
    private var subject: Subject? = null
    private var selectedDay: Int = 0
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(AddEditLessonViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_lesson)

        val intent = intent
        lesson = intent.getParcelableExtra(LESSON_PARAM_BUNDLE_ID)
        subject = intent.getParcelableExtra(SUBJECT_PARAM_BUNDLE_ID)

        // Avoiding problems with smart-cast
        val lesson = lesson
        val subject = subject

        if (lesson == null) {
            // User wants to create a new lesson
            activity_lesson_toolbar.setTitle(R.string.add_new_lesson_title)
            setSupportActionBar(activity_lesson_toolbar)
        } else {
            // User wants to edit an existing lesson
            activity_lesson_toolbar.title = resources.getString(R.string.edit_lesson_title, subject!!.name)
            setSupportActionBar(activity_lesson_toolbar)
            new_lesson_day_btn.text = CalendarUtils.getDayFromNumberOfDay(lesson.day, this)
            selectedDay = lesson.day
            new_lesson_starts_at_btn.text = lesson.starts
            new_lesson_ends_at_btn.text = lesson.ends
            new_lesson_location.setText(lesson.location)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // User must select the day of the lesson
        new_lesson_day_btn.setOnClickListener {
            showDaysPopUp(it)
        }

        // User must set the time when the lesson starts
        new_lesson_starts_at_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                this,
                CalendarUtils.getTimeSetListener(
                    this,
                    R.id.new_lesson_starts_at_btn,
                    cal,
                    false
                ),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        // User must set the time when the lesson ends
        new_lesson_ends_at_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                this,
                CalendarUtils.getTimeSetListener(
                    this,
                    R.id.new_lesson_ends_at_btn,
                    cal,
                    false
                ),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        new_lesson_save_btn.setOnClickListener {
            saveLesson()
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

    override fun onPositiveBtnPressed() {
        openActivity()
    }

    override fun onNegativeBtnPressed() {
        // Dialog automatically gets dismissed in UnsavedChangesDialogFragment
    }

    companion object {
        const val TAG = "AddEditLesson"
    }

    /**
     * [AddEditLessonActivity] can return only to [SubjectDetailsFragment] and [LessonDetailsFragment].
     * It can be called only by the following fragments: [SubjectDetailsFragment] (when adding new) and [LessonDetailsFragment] (when editing an existing one).
     */
    private fun onUpBtnPressed() {
        val dialog = UnsavedChangesDialogFragment(this)

        // If [lesson] is not null, the user is editing an existing one
        if (lesson != null) {
            if (requiredFieldsAreFilled()) {
                val newLesson = lessonFromUi()
                if (newLesson != lesson) dialog.show(this.supportFragmentManager, TAG) else openActivity()
            } else openActivity()
        } else openActivity()
    }

    private fun onSaveBtnPressed() {
        openActivity()
    }

    private fun openActivity() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.SUBJECT_DETAILS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID, TAG)
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, SubjectDetailsFragment.TAG)
                intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
                startActivity(intent)
            }
            Fragments.LESSON_DETAILS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID, TAG)
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, LessonDetailsFragment.TAG)
                intent.putExtra(LESSON_PARAM_BUNDLE_ID, lesson)
                intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
                startActivity(intent)
            }
            else -> {
                throw IllegalStateException("AddEditLessonActivity was called by unrecognised fragment $fragmentCalledFrom")
            }
        }
    }

    /**
     * Creates a new [Lesson] object with the details to be saved, then
     * calls the [viewModel]'s saveLesson function to save it.
     */
    private fun saveLesson() {
        if (requiredFieldsAreFilled()) {
            val newLesson = lessonFromUi()
            if (newLesson != lesson) {
                lesson = viewModel.saveLesson(newLesson)
                onSaveBtnPressed()
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
     * We set [Lesson]'s SubjectID to [subject]'s ID.
     *
     * @return A [Lesson] object created from UI
     */
    private fun lessonFromUi(): Lesson {
        val lesson = Lesson(
            subject!!.id,
            "A",
            if (selectedDay != 0) selectedDay else throw IllegalArgumentException("Parameter numOfSelectedDay $selectedDay must be between one and seven"),
            new_lesson_starts_at_btn.text.toString(),
            new_lesson_ends_at_btn.text.toString(),
            new_lesson_location.text.toString()
        )
        lesson.id = this.lesson?.id ?: ""
        return lesson
    }

    /**
     * Simple function to check whether the required fields are filled.
     *
     * @return True if required fields are filled, otherwise false
     */
    private fun requiredFieldsAreFilled(): Boolean {
        if (new_lesson_day_btn.text.isNotEmpty()
            && new_lesson_day_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_lesson_starts_at_btn.text.isNotEmpty()
            && new_lesson_starts_at_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_lesson_ends_at_btn.text.isNotEmpty()
            && new_lesson_ends_at_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_lesson_location.text.isNotEmpty()
        ) {
            return true
        }
        return false
    }

    /**
     * This function allows the user to choose a day
     * for the lesson from a pop up menu.
     * Days are saved as integers (Sunday: 1 - Saturday: 7)
     * This way they can be displayed in different languages.
     */
    private fun showDaysPopUp(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.day_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.day_option_sunday -> {
                    selectedDay = 1
                    new_lesson_day_btn.setText(R.string.dayOptionSunday)
                }
                R.id.day_option_monday -> {
                    selectedDay = 2
                    new_lesson_day_btn.setText(R.string.dayOptionMonday)
                }
                R.id.day_option_tuesday -> {
                    selectedDay = 3
                    new_lesson_day_btn.setText(R.string.dayOptionTuesday)
                }
                R.id.day_option_wednesday -> {
                    selectedDay = 4
                    new_lesson_day_btn.setText(R.string.dayOptionWednesday)
                }
                R.id.day_option_thursday -> {
                    selectedDay = 5
                    new_lesson_day_btn.setText(R.string.dayOptionThursday)
                }
                R.id.day_option_friday -> {
                    selectedDay = 6
                    new_lesson_day_btn.setText(R.string.dayOptionFriday)
                }
                R.id.day_option_saturday -> {
                    selectedDay = 7
                    new_lesson_day_btn.setText(R.string.dayOptionSaturday)
                }
            }
            true
        }
    }

}