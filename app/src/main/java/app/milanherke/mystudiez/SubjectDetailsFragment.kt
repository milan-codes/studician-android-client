package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import app.milanherke.mystudiez.Fragments.SUBJECT_DETAILS
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subject_details.*

// Fragment initialization parameters
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * The purpose of this fragment is to display the details of a [Subject]
 * The user can delete a subject from this fragment
 * or launch a new fragment ([AddEditSubjectFragment]) to edit it.
 * Activities that contain this fragment must implement the
 * [SubjectDetailsFragment.SubjectDetailsInteractions] interface
 * to handle interaction events.
 * Use the [SubjectDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubjectDetailsFragment : Fragment(),
    LessonsRecyclerViewAdapter.OnLessonClickListener,
    TasksRecyclerViewAdapter.OnTaskClickListener,
    ExamsRecyclerViewAdapter.OnExamClickListener {

    private var subject: Subject? = null
    private var listener: SubjectDetailsInteractions? = null
    private val lessonsAdapter = LessonsRecyclerViewAdapter(null, this, SUBJECT_DETAILS)
    private val tasksAdapter = TasksRecyclerViewAdapter(null, this, SUBJECT_DETAILS)
    private val examsAdapter = ExamsRecyclerViewAdapter(null, this, SUBJECT_DETAILS)
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(SubjectDetailsViewModel::class.java)
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

        // Avoiding problems with smart-cast
        val subject = subject
        if (subject != null) {
            subject_details_name_value.text = subject.name
            subject_details_teacher_value.text = subject.teacher

            //Creating a clone drawable because we do not want to affect other instances of the original one
            val clone = subject_details_color_value.drawable.mutatedClone()
            clone.setColor(subject.colorCode, context!!)
            subject_details_color_value.setImageDrawable(clone)
        }

        subject_details_lessons_recycler.layoutManager = LinearLayoutManager(context)
        subject_details_lessons_recycler.adapter = lessonsAdapter

        subject_details_tasks_recycler.layoutManager = LinearLayoutManager(context)
        subject_details_tasks_recycler.adapter = tasksAdapter

        subject_details_exams_recycler.layoutManager = LinearLayoutManager(context)
        subject_details_exams_recycler.adapter = examsAdapter

        button.setOnClickListener {
            activity!!.replaceFragmentWithTransition(
                AddEditSubjectFragment.newInstance(subject!!),
                R.id.fragment_container
            )
        }

        // After deleting a subject
        // the application returns to SubjectsFragment
        del_subject_button.setOnClickListener {
            viewModel.deleteSubject(subject!!.id)
            activity!!.replaceFragmentWithTransition(
                SubjectsFragment.newInstance(),
                R.id.fragment_container
            )
        }

        // If users want to add a new lesson to the subject
        // the application takes them to AddEditLessonFragment
        subject_details_add_new_lesson_btn.setOnClickListener {
            activity!!.replaceFragmentWithTransition(
                AddEditLessonFragment.newInstance(null, subject!!),
                R.id.fragment_container
            )
        }

        // If users want to add a new task to the subject
        // the application takes them to AddEditTaskFragment
        subject_details_add_new_task_btn.setOnClickListener {
            activity!!.replaceFragmentWithTransition(
                AddEditTaskFragment.newInstance(null, subject!!),
                R.id.fragment_container
            )
        }

        // If users want to add a new exam to the subject
        // the application takes them to AddEditExamFragment
        subject_details_add_new_exam_btn.setOnClickListener {
            activity!!.replaceFragmentWithTransition(
                AddEditExamFragment.newInstance(null, subject!!),
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

        // Hiding bottom navigation bar and fab button
        activity!!.bar.visibility = View.GONE
        activity!!.fab.visibility = View.GONE

        // Passing the subject
        // MainActivity always needs to have the currently used subject
        val subject = subject
        if (subject != null) {
            sharedViewModel.swapSubject(subject)
        }

        // Registering observers
        viewModel.selectedLessonsLiveData.observe(
            this,
            Observer { list -> lessonsAdapter.swapLessonsList(list) })
        viewModel.selectedTasksLiveData.observe(
            this,
            Observer { list -> tasksAdapter.swapTasksList(list) })
        viewModel.selectedExamsLiveData.observe(
            this,
            Observer { list -> examsAdapter.swapExamsList(list) })
        viewModel.loadAllDetails(subject!!.id)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        FragmentBackStack.getInstance(context!!).push(SUBJECT_DETAILS)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param subject The subject, whose details are displayed
         * @return A new instance of fragment SubjectDetailsFragment
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
        val subject = subject
        if (subject != null) {
            activity!!.replaceFragmentWithTransition(
                LessonDetailsFragment.newInstance(lesson, subject),
                R.id.fragment_container
            )
        }
    }

    override fun onTaskClickListener(task: Task) {
        val subject = subject
        if (subject != null) {
            activity!!.replaceFragmentWithTransition(
                TaskDetailsFragment.newInstance(task, subject), R.id.fragment_container
            )
        }
    }

    override fun onExamClickListener(exam: Exam) {
        val subject = subject
        if (subject != null) {
            activity!!.replaceFragmentWithTransition(
                ExamDetailsFragment.newInstance(exam, subject), R.id.fragment_container
            )
        }
    }

}
