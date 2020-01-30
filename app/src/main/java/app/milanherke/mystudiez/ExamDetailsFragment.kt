package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_exam_details.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_EXAM = "exam"

/**
 * A simple [Fragment] subclass.
 * This fragment was created to list the details of an exam.
 * The user can delete an exam from this fragment
 * or launch a new fragment ([AddEditExamFragment]) to edit it.
 * This fragment can return only to [SubjectDetailsFragment].
 * This fragment can be called only by the following fragments: [SubjectDetailsFragment].
 * Activities that contain this fragment must implement the
 * [ExamDetailsFragment.ExamDetailsInteraction] interface
 * to handle interaction events.
 * Use the [ExamDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExamDetailsFragment : Fragment() {

    private var exam: Exam? = null
    private var subject: Subject? = null
    private var listener: ExamDetailsInteraction? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(ExamDetailsViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface ExamDetailsInteraction {
        fun onDeleteExamClick(subject: Subject)
        fun onEditExamClick(exam: Exam)
        fun examIsLoaded(exam: Exam)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ExamDetailsInteraction) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ExamDetailsInteraction")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exam = arguments?.getParcelable(ARG_EXAM)
        listener?.examIsLoaded(exam!!)
        subject = sharedViewModel.subjectFromId(exam!!.subjectId)

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
        val subject = subject

        if (exam != null && subject != null) {
            activity!!.toolbar.setTitle(R.string.exam_details_title)
            exam_details_name_value.text = exam.name
            exam_details_desc_value.text = exam.description
            exam_details_subject_value.text = subject.name
            exam_details_date_value.text = exam.date
            exam_details_reminder_value.text = exam.reminder
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        activity!!.bar.visibility = View.GONE
        activity!!.fab.visibility = View.GONE

        exam_details_del_exam_btn.setOnClickListener {
            viewModel.deleteExam(exam!!.examId)
            listener?.onDeleteExamClick(subject!!)
        }

        exam_details_edit_exam_btn.setOnClickListener {
            listener?.onEditExamClick(exam!!)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        FragmentsStack.getInstance(context!!).push(Fragments.EXAM_DETAILS)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param exam The exam, whose details are displayed
         * @return A new instance of fragment ExamDetailsFragment.
         */
        @JvmStatic
        fun newInstance(exam: Exam) =
            ExamDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_EXAM, exam)
                }
            }
    }
}
