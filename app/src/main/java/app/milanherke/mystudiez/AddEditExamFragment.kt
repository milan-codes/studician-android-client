package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import app.milanherke.mystudiez.SharedViewModel.RetrievingData
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_edit_exam.*
import java.util.*

// Fragment initialization parameters
private const val ARG_EXAM = "exam"
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * This fragment was created to add or edit exams.
 * Activities that contain this fragment must implement the
 * [AddEditExamFragment.ExamSaved] interface
 * to handle interaction events.
 * Use the [AddEditExamFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditExamFragment : Fragment() {

    private var exam: Exam? = null
    private var subject: Subject? = null
    private var listener: ExamSaved? = null
    private var listOfSubjects: MutableMap<String, Subject>? = null
    private var selectedSubjectId: String? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(AddEditExamViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface ExamSaved {
        fun onSaveExamClickListener(exam: Exam, subject: Subject)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ExamSaved) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ExamSaved")
        }
    }

    /**
     * If a new [Exam] is created, we need to get all [Subject] objects
     * from Firebase to let the user choose the exam's subject
     * New exam is created when the fragment was called
     * from any other fragment than [ExamDetailsFragment].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exam = arguments?.getParcelable(ARG_EXAM)
        subject = arguments?.getParcelable(ARG_SUBJECT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit_exam, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Avoiding problems with smart cast
        val exam = exam
        val subject = subject
        val listOfSubjects = listOfSubjects

        if (exam == null && subject == null) {
            // Fragment called from ExamsFragment
            // User wants to create a new exam
            activity!!.toolbar.setTitle(R.string.add_new_exam_title)
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
            activity!!.toolbar.title =
                resources.getString(R.string.edit_subject_title, exam.name)
            new_exam_name.setText(exam.name)
            new_exam_desc.setText(exam.description)
            new_exam_subject_btn.text = subject.name
            new_exam_subject_btn.background =
                resources.getDrawable(R.drawable.circular_disabled_button, null)
            new_exam_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
            new_exam_subject_btn.isEnabled = false
            new_exam_date_btn.text = exam.date
            new_exam_reminder_btn.text = exam.reminder

        } else if (exam == null && subject != null) {
            // Fragment called from SubjectDetailsFragment
            // User wants to create a new exam
            activity!!.toolbar.setTitle(R.string.add_new_exam_title)
            new_exam_subject_btn.text = subject.name
            new_exam_subject_btn.background =
                resources.getDrawable(R.drawable.circular_disabled_button, null)
            new_exam_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
            new_exam_subject_btn.isEnabled = false
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        // If the last fragment in the backStack is not TaskDetails
        // then we need to fetch all of the subjects to let the users choose one
        // for the exam they are about to create
        if (FragmentBackStack.getInstance(context!!).peek() != Fragments.EXAM_DETAILS) {
            val progressBar = ProgressBarHandler(activity!!)
            sharedViewModel.getAllSubjects(object : RetrievingData {
                override fun onLoad() {
                    progressBar.showProgressBar()
                }

                override fun onSuccess(subjects: MutableMap<String, Subject>) {
                    listOfSubjects = subjects
                    progressBar.hideProgressBar()
                }

                override fun onFailure(e: DatabaseError) {
                    Toast.makeText(
                        context,
                        getString(R.string.firebase_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // Hiding bottom navigation bar and fab button
        activity!!.bar.visibility = View.GONE
        activity!!.fab.visibility = View.GONE

        // User must set a subject if a new exam is created
        if (new_exam_subject_btn.isEnabled) {
            new_exam_subject_btn.setOnClickListener {
                showSubjectsPopUp(it)
            }
        }

        // User must set the date for the exam
        new_exam_date_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                context!!,
                CalendarUtils.getDateSetListener(activity!!, R.id.new_exam_date_btn, cal),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // User can set a reminder
        new_exam_reminder_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                context,
                CalendarUtils.getTimeSetListener(activity!!, R.id.new_exam_reminder_btn, cal, true),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
            DatePickerDialog(
                context!!,
                CalendarUtils.getDateSetListener(activity!!, R.id.new_exam_reminder_btn, cal),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        new_exam_save_btn.setOnClickListener {
            saveExam()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        FragmentBackStack.getInstance(context!!).pop()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param exam The exam to be edited, or null when creating a new one.
         * @param subject Subject associated with the exam. Null if fragment was called from [ExamsFragment]
         * @return A new instance of fragment AddEditExamFragment.
         */
        @JvmStatic
        fun newInstance(exam: Exam? = null, subject: Subject? = null) =
            AddEditExamFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_EXAM, exam)
                    putParcelable(ARG_SUBJECT, subject)
                }
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
                listener?.onSaveExamClickListener(exam!!, subject!!)
            } else {
                Toast.makeText(
                    context!!,
                    getString(R.string.did_not_change),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context!!,
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
            new_exam_date_btn.text.toString(),
            if (new_exam_reminder_btn.text.toString() != getString(R.string.add_edit_lesson_btn)) new_exam_reminder_btn.text.toString() else ""
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
            val popupMenu = PopupMenu(activity!!, view)
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