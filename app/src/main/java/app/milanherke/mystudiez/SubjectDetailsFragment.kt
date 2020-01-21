package app.milanherke.mystudiez

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_edit_subject.*
import kotlinx.android.synthetic.main.fragment_subject_details.*

// the fragment initialization parameters, e.g. ARG_SUBJECT
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SubjectDetailsFragment.SubjectDetailsInteraction] interface
 * to handle interaction events.
 * Use the [SubjectDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubjectDetailsFragment : Fragment() {

    private var listener: SubjectDetailsInteraction? = null
    private var subject: Subject? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(SubjectDetailsViewModel::class.java)
    }
    private val lessonsAdapter = LessonsRecyclerViewAdapter(null, null)
    private val tasksAdapter = TasksRecyclerViewAdapter(null, null)
    private val examsAdapter = ExamsRecyclerViewAdapter(null, null)

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
    interface SubjectDetailsInteraction {
        fun onSubjectEditButtonClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SubjectDetailsInteraction) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SubjectDetailsInteraction")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subject = arguments?.getParcelable(ARG_SUBJECT)
        viewModel.cursorSelectedLessons.observe(
            this,
            Observer { cursor -> lessonsAdapter.swapLessonsCursor(cursor)?.close() })
        viewModel.cursorSelectedTasks.observe(
            this,
            Observer { cursor -> tasksAdapter.swapTasksCursor(cursor)?.close() })
        viewModel.cursorSelectedExams.observe(
            this,
            Observer { cursor -> examsAdapter.swapExamsCursor(cursor)?.close() })
        viewModel.loadAllDetails(subject!!.subjectId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subject_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.toolbar.setTitle(R.string.lessons_title)
        if (savedInstanceState == null) {
            val subject = subject
            if (subject != null) {
                // Loading the data into the subject details section
                subject_details_name_value.text = subject.name
                subject_details_teacher_value.text = subject.teacher

                //Creating a clone drawable because we do not want to affect other instances of the original drawable
                val clone = subject_details_color_value.drawable.mutatedClone()
                clone.displayColor(subject.colorCode, context!!)
                subject_details_color_value.setImageDrawable(clone)


                // Passing the drawable to the adapters
                lessonsAdapter.swapDrawable(clone)
                tasksAdapter.swapDrawable(clone)
                examsAdapter.swapDrawable(clone)
            }
        }

        subject_details_lessons_recycler.layoutManager = LinearLayoutManager(context)
        subject_details_lessons_recycler.adapter = lessonsAdapter

        subject_details_tasks_recycler.layoutManager = LinearLayoutManager(context)
        subject_details_tasks_recycler.adapter = tasksAdapter

        subject_details_exams_recycler.layoutManager = LinearLayoutManager(context)
        subject_details_exams_recycler.adapter = examsAdapter

        button.setOnClickListener {
            activity!!.replaceFragment(AddEditSubjectFragment.newInstance(subject!!), R.id.fragment_container)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        viewModel.subjectFilter.postValue(null)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param subject The subject to be edited.
         * @return A new instance of fragment SubjectDetailsFragment.
         */
        @JvmStatic
        fun newInstance(subject: Subject) =
            SubjectDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SUBJECT, subject)
                }
            }
    }
}
