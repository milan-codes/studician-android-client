package app.milanherke.mystudiez.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import app.milanherke.mystudiez.*
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.ACTIVITY_NAME_BUNDLE_ID
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.EXAM_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.FRAGMENT_TO_LOAD_BUNDLE_ID
import app.milanherke.mystudiez.utils.ActivityUtils.Companion.SUBJECT_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.utils.CalendarUtils.Companion.CalendarInteractions
import app.milanherke.mystudiez.FragmentBackStack
import app.milanherke.mystudiez.Fragments.*
import app.milanherke.mystudiez.viewmodels.SharedViewModel.RetrievingData
import app.milanherke.mystudiez.fragments.ExamDetailsFragment
import app.milanherke.mystudiez.fragments.ExamsFragment
import app.milanherke.mystudiez.fragments.SubjectDetailsFragment
import app.milanherke.mystudiez.fragments.UnsavedChangesDialogFragment
import app.milanherke.mystudiez.models.Exam
import app.milanherke.mystudiez.models.Subject
import app.milanherke.mystudiez.utils.ActivityUtils
import app.milanherke.mystudiez.utils.CalendarUtils
import app.milanherke.mystudiez.viewmodels.SharedViewModel
import app.milanherke.mystudiez.viewmodels.activities.AddEditExamViewModel
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_add_edit_exam.*
import kotlinx.android.synthetic.main.content_add_edit_exam.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * The purpose of this activity is to add or edit exams.
 */
class AddEditExamActivity : AppCompatActivity(), UnsavedChangesDialogFragment.DialogInteractions {

    private var exam: Exam? = null
    private var subject: Subject? = null
    private var examDate: Date? = null
    private var reminder: Date? = null
    private var listOfSubjects: MutableMap<String, Subject>? = null
    private var selectedSubjectId: String? = null
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(AddEditExamViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(this).get(SharedViewModel::class.java)
    }

    /**
     * If a new [Exam] is created, we need to get all [Subject] objects
     * from Firebase to let the user choose the exam's subject
     * New exam is created when the fragment was called
     * from any other fragment than [ExamDetailsFragment].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_exam)

        val intent = intent
        exam = intent.getParcelableExtra(EXAM_PARAM_BUNDLE_ID)
        subject = intent.getParcelableExtra(SUBJECT_PARAM_BUNDLE_ID)

        // Avoiding problems with smart-cast
        val exam = exam
        if (exam != null) {
            examDate = exam.date
            reminder = exam.reminder
        }
        val examReminder = exam?.reminder
        val subject = subject
        val listOfSubjects = listOfSubjects

        if (exam == null && subject == null) {
            // Fragment called from ExamsFragment
            // User wants to create a new exam
            activity_exam_toolbar.setTitle(R.string.add_new_exam_title)
            setSupportActionBar(activity_exam_toolbar)
            if (listOfSubjects != null) {
                // We need to disable the subject btn if listOfSubjects is empty
                // Because the user could not choose from any subjects
                if (listOfSubjects.isEmpty()) {
                    new_exam_subject_btn.text = getString(R.string.no_subjects_to_select_from)
                    new_exam_subject_btn.background =
                        resources.getDrawable(R.drawable.circular_disabled_button, null)
                    new_exam_subject_btn.setTextColor(
                        resources.getColor(
                            R.color.colorTextSecondary,
                            null
                        )
                    )
                    new_exam_subject_btn.isEnabled = false
                }
            }
        } else if (exam != null && subject != null) {
            // Fragment called from ExamDetailsFragment
            // User wants to edit an existing exam
            activity_exam_toolbar.title =resources.getString(R.string.edit_subject_title, exam.name)
            setSupportActionBar(activity_exam_toolbar)
            new_exam_name.setText(exam.name)
            new_exam_desc.setText(exam.description)
            new_exam_subject_btn.text = subject.name
            new_exam_subject_btn.background =
                resources.getDrawable(R.drawable.circular_disabled_button, null)
            new_exam_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
            new_exam_subject_btn.isEnabled = false
            new_exam_date_btn.text =
                CalendarUtils.dateToString(
                    exam.date,
                    false
                )
            new_exam_reminder_btn.text = if (examReminder == null) getString(R.string.add_edit_lesson_btn) else CalendarUtils.dateToString(
                examReminder,
                true
            )
        } else if (exam == null && subject != null) {
            // Fragment called from SubjectDetailsFragment
            // User wants to create a new exam
            activity_exam_toolbar.setTitle(R.string.add_new_exam_title)
            setSupportActionBar(activity_exam_toolbar)
            new_exam_subject_btn.text = subject.name
            new_exam_subject_btn.background =
                resources.getDrawable(R.drawable.circular_disabled_button, null)
            new_exam_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
            new_exam_subject_btn.isEnabled = false
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // If the last fragment in the backStack is not TaskDetails
        // then we need to fetch all of the subjects to let the users choose one
        // for the exam they are about to create
        if (FragmentBackStack.getInstance(this).peek() != EXAM_DETAILS) {
            val progressBar = ProgressBarHandler(this)
            sharedViewModel.getAllSubjects(object : RetrievingData {
                override fun onLoad() {
                    progressBar.showProgressBar()
                }

                override fun onSuccess(subjects: MutableMap<String, Subject>) {
                    this@AddEditExamActivity.listOfSubjects = subjects
                    progressBar.hideProgressBar()
                }

                override fun onFailure(e: DatabaseError) {
                    Toast.makeText(
                        this@AddEditExamActivity,
                        getString(R.string.firebase_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // User must set a subject if a new exam is created
        if (new_exam_subject_btn.isEnabled) {
            new_exam_subject_btn.setOnClickListener {
                showSubjectsPopUp(it)
            }
        }

        val calendarListener : CalendarInteractions = object: CalendarInteractions {
            override fun onDateSet(date: Date) {
                examDate = date
            }

            override fun onTimeSet(date: Date) {
                reminder = date
            }
        }

        // User must set the date for the exam
        new_exam_date_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
               this,
                CalendarUtils.getDateSetListener(
                    this,
                    R.id.new_exam_date_btn,
                    cal,
                    calendarListener
                ),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // User can set a reminder
        new_exam_reminder_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
             this,
                CalendarUtils.getTimeSetListener(
                    this,
                    R.id.new_exam_reminder_btn,
                    cal,
                    true,
                    calendarListener
                ),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
            DatePickerDialog(
               this,
                CalendarUtils.getDateSetListener(
                    this,
                    R.id.new_exam_reminder_btn,
                    cal
                ),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        new_exam_save_btn.setOnClickListener {
            saveExam()
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
        const val TAG = "AddEditExam"
    }

    /**
     * [AddEditExamActivity] can return only to [ExamsFragment] [SubjectDetailsFragment] and [ExamDetailsFragment].
     * It can be called only by the following fragments:
     *  [ExamsFragment]: When pressing the FAB button and creating a new one
     *  [SubjectDetailsFragment]: When adding new
     *  [ExamDetailsFragment]: When editing an existing one
     */
    private fun onUpBtnPressed() {
        val dialog = UnsavedChangesDialogFragment(this)

        // If [exam] is not null, the user is editing an existing one
        if (exam != null) {
            if (requiredFieldsAreFilled()) {
                val newExam = examFromUi()
                if (newExam != exam) dialog.show(this.supportFragmentManager,
                    TAG
                ) else openActivity()
            } else openActivity()
        } else openActivity()
    }

    private fun onSaveBtnPressed(exam: Exam) {
        val reminder = exam.reminder
        if (reminder != null) {
            val notification =
                ActivityUtils.createNotification(
                    this,
                    getString(R.string.notification_exam_reminder_title),
                    exam.name
                )
            val delay = reminder.time.minus(System.currentTimeMillis())
            ActivityUtils.scheduleNotification(
                this,
                notification,
                delay,
                null,
                exam
            )
        }

        openActivity()
    }

    private fun openActivity() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            EXAMS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID,
                    TAG
                )
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, ExamsFragment.TAG)
                startActivity(intent)
            }
            SUBJECT_DETAILS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID,
                    TAG
                )
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, SubjectDetailsFragment.TAG)
                intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
                startActivity(intent)
            }
            EXAM_DETAILS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID,
                    TAG
                )
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, ExamDetailsFragment.TAG)
                intent.putExtra(EXAM_PARAM_BUNDLE_ID, exam)
                intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
                startActivity(intent)
            }
            else -> throw IllegalStateException("onSaveExamClicked tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * Creates a new [Exam] object with the details to be saved, then
     * calls the [viewModel]'s saveExam function to save it.
     */
    private fun saveExam() {
        if (requiredFieldsAreFilled()) {
            val newExam = examFromUi()
            if (newExam != exam) {
                exam = viewModel.saveExam(newExam)
                onSaveBtnPressed(exam!!)
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
     * If we're creating a new [Exam], we set its SubjectID to [selectedSubjectId],
     * which holds the selected subject's ID, chosen by the user from [listOfSubjects].
     * Furthermore, we set the exam's ID to an empty string.
     * If we're updating an [Exam], we're not changing its SubjectID, nor its own ID
     *
     * @return An [Exam] object created from UI
     */
    private fun examFromUi(): Exam {
        val exam = Exam(
            new_exam_name.text.toString(),
            new_exam_desc.text.toString(),
            selectedSubjectId ?: (subject?.id ?: ""),
            examDate!!,
            reminder
        )
        exam.id = this.exam?.id ?: ""
        return exam
    }

    /**
     * Simple function to check whether the required fields are filled.
     *
     * @return True if required fields are filled, otherwise false
     */
    private fun requiredFieldsAreFilled(): Boolean {
        if (new_exam_name.text.isNotEmpty()
            && new_exam_subject_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_exam_subject_btn.text.isNotEmpty()
            && new_exam_date_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_exam_date_btn.text.isNotEmpty()
        ) {
            return true
        }
        return false
    }

    /**
     * Allows the user to choose a subject from a pop up menu,
     * if a new exam is created.
     */
    private fun showSubjectsPopUp(view: View) {
        // Avoiding problems with smart cast
        val listOfSubjects = listOfSubjects
        if (listOfSubjects != null) {
            val popupMenu = PopupMenu(this, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.empty_menu, popupMenu.menu)

            // Adding the subjects to the PopUp Menu
            for (subject in listOfSubjects) {
                popupMenu.menu.add(subject.value.name).setOnMenuItemClickListener {
                    new_exam_subject_btn.text = subject.value.name
                    selectedSubjectId = subject.value.id
                    this.subject = subject.value
                    true
                }
            }
            popupMenu.show()
        }
    }

}