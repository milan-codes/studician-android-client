package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.content.Context
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
import kotlinx.android.synthetic.main.fragment_subject_details.*

// the fragment initialization parameters, e.g. ARG_SUBJECT
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SubjectDetailsFragment.SubjectDetailsInteractions] interface
 * to handle interaction events.
 * Use the [SubjectDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubjectDetailsFragment : Fragment(), LessonsRecyclerViewAdapter.OnLessonClickListener {

    private var subject: Subject? = null
    private var listener: SubjectDetailsInteractions? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(SubjectDetailsViewModel::class.java)
    }
    private val lessonsAdapter = LessonsRecyclerViewAdapter(null, null, this)
    private val tasksAdapter = TasksRecyclerViewAdapter(null, null)
    private val examsAdapter = ExamsRecyclerViewAdapter(null, null)

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     * We need to pass a Subject object to the main activity
     * because it needs to know which subject's details should be loaded after tapping the up button.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface SubjectDetailsInteractions {
        fun subjectIsLoaded(subject: Subject)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SubjectDetailsInteractions) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SubjectDetailsInteractions")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subject = arguments?.getParcelable(ARG_SUBJECT)
        listener?.subjectIsLoaded(subject!!)
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
            activity!!.replaceFragment(
                AddEditSubjectFragment.newInstance(subject!!),
                R.id.fragment_container
            )
        }

        del_subject_button.setOnClickListener {
            viewModel.deleteSubject(subject!!.subjectId)
            activity!!.replaceFragment(SubjectsFragment.newInstance(), R.id.fragment_container)
        }

        subject_details_add_new_lesson_btn.setOnClickListener {
            activity!!.replaceFragment(
                AddEditLessonFragment.newInstance(null, subject!!),
                R.id.fragment_container
            )
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

    override fun onLessonClick(lesson: Lesson) {
        activity!!.replaceFragment(
            LessonDetailsFragment.newInstance(lesson),
            R.id.fragment_container
        )
    }
}
