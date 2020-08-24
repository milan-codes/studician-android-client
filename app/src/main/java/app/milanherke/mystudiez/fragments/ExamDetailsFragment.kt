package app.milanherke.mystudiez.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import app.milanherke.mystudiez.*
import app.milanherke.mystudiez.FragmentBackStack
import app.milanherke.mystudiez.activities.AddEditExamActivity
import app.milanherke.mystudiez.models.Exam
import app.milanherke.mystudiez.models.Subject
import app.milanherke.mystudiez.viewmodels.fragments.ExamDetailsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_exam_details.*

// Fragment initialization parameters
private const val ARG_EXAM = "exam"
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * The purpose if this fragment is to display the details of an [Exam].
 * The user can delete an exam from this fragment
 * or launch a new fragment ([AddEditExamActivity]) to edit it.
 * Use the [ExamDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExamDetailsFragment : Fragment() {

    private var exam: Exam? = null
    private var subject: Subject? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(ExamDetailsViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exam = arguments?.getParcelable(ARG_EXAM)
        subject = arguments?.getParcelable(ARG_SUBJECT)
        val exam = exam
        if (exam != null) {
            sharedViewModel.swapExam(exam)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exam_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Avoiding problems with smart-cast
        val exam = exam
        val examReminder = exam?.reminder
        val subject = subject

        if (exam != null && subject != null) {
            activity!!.toolbar.setTitle(R.string.exam_details_title)
            exam_details_name_value.text = exam.name
            exam_details_desc_value.text = exam.description
            exam_details_subject_value.text = subject.name
            exam_details_date_value.text =
                CalendarUtils.dateToString(
                    exam.date,
                    false
                )
            exam_details_reminder_value.text = if (examReminder == null) getString(
                R.string.add_edit_lesson_btn
            ) else CalendarUtils.dateToString(
                examReminder,
                true
            )
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        // Hiding bottom navigation bar and fab button
        activity!!.bar.visibility = View.GONE
        activity!!.fab.visibility = View.GONE

        // Passing the subject to MainActivity
        // MainActivity always needs to have the currently used subject
        val subject = subject
        if (subject != null) {
            sharedViewModel.swapSubject(subject)
        }

        exam_details_del_exam_btn.setOnClickListener {
            deleteExam()
        }

        exam_details_edit_exam_btn.setOnClickListener {
            editExam()
        }
    }

    override fun onDetach() {
        super.onDetach()
        FragmentBackStack.getInstance(context!!).push(
            Fragments.EXAM_DETAILS
        )
    }


    companion object {

        const val TAG = "ExamDetails"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param exam The exam, whose details are displayed
         * @return A new instance of fragment ExamDetailsFragment
         */
        @JvmStatic
        fun newInstance(exam: Exam, subject: Subject? = null) =
            ExamDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_EXAM, exam)
                    putParcelable(ARG_SUBJECT, subject)
                }
            }
    }

    /**
     * Allows the user to delete an [Exam] object from the database.
     * First, it calls the [viewModel]'s deleteExam function
     * and then it opens a new fragment based on [FragmentBackStack].
     */
    private fun deleteExam() {
        // Deleting exam
        viewModel.deleteExam(exam!!)

        // Opening the previous fragment
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(activity!!).peek()) {
            Fragments.SUBJECT_DETAILS -> activity!!.replaceFragmentWithTransition(
                SubjectDetailsFragment.newInstance(
                    subject!!
                ),
                R.id.fragment_container
            )
            Fragments.EXAMS -> {
                activity!!.replaceFragmentWithTransition(
                    ExamsFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            Fragments.OVERVIEW -> {
                activity!!.replaceFragmentWithTransition(
                    OverviewFragment.newInstance(),
                    R.id.fragment_container
                )
            }
            else -> throw IllegalStateException("onDeleteExamClick tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * Allows the user to edit an [Exam] object
     * by opening [AddEditExamActivity].
     */
    private fun editExam() {
        FragmentBackStack.getInstance(activity!!).push(
            Fragments.EXAM_DETAILS
        )
        val intent = Intent(activity, AddEditExamActivity::class.java)
        intent.putExtra(ActivityUtils.EXAM_PARAM_BUNDLE_ID, exam)
        intent.putExtra(ActivityUtils.SUBJECT_PARAM_BUNDLE_ID, subject)
        startActivity(intent)
    }
}
