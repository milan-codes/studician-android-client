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
import kotlinx.android.synthetic.main.fragment_lessons.*

// the fragment initialization parameters, e.g. ARG_SUBJECT
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LessonsFragment.OnLessonClick] interface
 * to handle interaction events.
 * Use the [LessonsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LessonsFragment : Fragment() {

    private var listener: OnLessonClick? = null
    private var subject: Subject? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(MyStudiezViewModel::class.java)
    }
    private val lessonsAdapter = LessonsRecyclerViewAdapter(null)
    private val tasksAdapter = TasksRecyclerViewAdapter(null)
    private val examsAdapter = ExamsRecyclerViewAdapter(null)

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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.toolbar.setTitle(R.string.lessons_title)
        if (savedInstanceState == null) {
            val subject = subject
            if (subject != null) {
                subject_details_name_value.text = subject.name
                subject_details_teacher_value.text = subject.teacher
            }
        }
        subject_details_lessons_recycler.layoutManager = LinearLayoutManager(context)
        subject_details_lessons_recycler.adapter = lessonsAdapter

        subject_details_tasks_recycler.layoutManager = LinearLayoutManager(context)
        subject_details_tasks_recycler.adapter = tasksAdapter

        subject_details_exams_recycler.layoutManager = LinearLayoutManager(context)
        subject_details_exams_recycler.adapter = examsAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lessons, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLessonClick) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnLessonClick")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        viewModel.subjectFilter.postValue("")
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
    interface OnLessonClick {
        fun onLessonTap(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param subject The subject to be edited.
         * @return A new instance of fragment LessonsFragment.
         */
        @JvmStatic
        fun newInstance(subject: Subject) =
            LessonsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SUBJECT, subject)
                }
            }
    }
}
