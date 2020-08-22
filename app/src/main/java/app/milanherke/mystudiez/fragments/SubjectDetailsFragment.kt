package app.milanherke.mystudiez.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import app.milanherke.mystudiez.*
import app.milanherke.mystudiez.ActivityUtils.Companion.SUBJECT_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.FragmentBackStack
import app.milanherke.mystudiez.Fragments.SUBJECT_DETAILS
import app.milanherke.mystudiez.viewmodels.fragments.SubjectDetailsViewModel.DataFetching
import app.milanherke.mystudiez.adapters.ExamsRecyclerViewAdapter
import app.milanherke.mystudiez.adapters.LessonsRecyclerViewAdapter
import app.milanherke.mystudiez.adapters.TasksRecyclerViewAdapter
import app.milanherke.mystudiez.models.Exam
import app.milanherke.mystudiez.models.Lesson
import app.milanherke.mystudiez.models.Subject
import app.milanherke.mystudiez.models.Task
import app.milanherke.mystudiez.viewmodels.fragments.SubjectDetailsViewModel
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subject_details.*

// Fragment initialization parameters
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * The purpose of this fragment is to display the details of a [Subject]
 * The user can delete a subject from this fragment
 * or launch a new fragment ([AddEditSubjectActivity]) to edit it.
 * Use the [SubjectDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubjectDetailsFragment : Fragment(),
    LessonsRecyclerViewAdapter.OnLessonClickListener,
    TasksRecyclerViewAdapter.OnTaskClickListener,
    ExamsRecyclerViewAdapter.OnExamClickListener {

    // Subject, whose details are displayed
    private var subject: Subject? = null
    private val lessonsAdapter = LessonsRecyclerViewAdapter(SUBJECT_DETAILS, this)
    private val tasksAdapter = TasksRecyclerViewAdapter(SUBJECT_DETAILS, this)
    private val examsAdapter = ExamsRecyclerViewAdapter(SUBJECT_DETAILS, this)
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(SubjectDetailsViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subject = arguments?.getParcelable(ARG_SUBJECT)
        val subject = subject
        if (subject != null) {
            sharedViewModel.swapSubject(subject)
        }
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
            FragmentBackStack.getInstance(activity!!).push(SUBJECT_DETAILS)
            val intent = Intent(activity, AddEditSubjectActivity::class.java)
            intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
            startActivity(intent)
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
        // the application takes them to AddEditLessonActivity
        subject_details_add_new_lesson_btn.setOnClickListener {
            FragmentBackStack.getInstance(activity!!).push(SUBJECT_DETAILS)
            val intent = Intent(activity, AddEditLessonActivity::class.java)
            intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
            startActivity(intent)
        }

        // If users want to add a new task to the subject
        // the application takes them to AddEditTaskActivity
        subject_details_add_new_task_btn.setOnClickListener {
            FragmentBackStack.getInstance(activity!!).push(SUBJECT_DETAILS)
            val intent = Intent(activity, AddEditTaskActivity::class.java)
            intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
            startActivity(intent)
        }

        // If users want to add a new exam to the subject
        // the application takes them to AddEditExamActivity
        subject_details_add_new_exam_btn.setOnClickListener {
            FragmentBackStack.getInstance(activity!!).push(SUBJECT_DETAILS)
            val intent = Intent(activity, AddEditExamActivity::class.java)
            intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
            startActivity(intent)
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

        // Showing a progress bar while data is being fetched
        val progressBar = ProgressBarHandler(activity!!)
        val dataFetchingListener: DataFetching = object : DataFetching {
            override fun onLoad() {
                progressBar.showProgressBar()
            }

            override fun onSuccess() {
                progressBar.hideProgressBar()
            }

            override fun onFailure(e: DatabaseError) {
                Toast.makeText(
                    context,
                    getString(R.string.firebase_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Passing the subject
        // MainActivity always needs to have the currently used subject
        val subject = subject
        if (subject != null) {
            sharedViewModel.swapSubject(subject)
        }

        // Registering observers
        viewModel.selectedLessonsLiveData.observe(
            this,
            Observer { list ->
                val sortedList = ArrayList(list.sortedWith(compareBy(Lesson::day, Lesson::starts)))
                lessonsAdapter.swapLessons(sortedList)
            }
        )
        viewModel.selectedTasksLiveData.observe(
            this,
            Observer { list ->
                val sortedList = ArrayList(list.sortedWith(compareBy(Task::dueDate, Task::name)))
                tasksAdapter.swapTasks(sortedList)
            }
        )
        viewModel.selectedExamsLiveData.observe(
            this,
            Observer { list ->
                val sortedList = ArrayList(list.sortedWith(compareBy(Exam::date, Exam::name)))
                examsAdapter.swapExams(sortedList)
            }
        )
        viewModel.loadAllDetails(subject!!.id, dataFetchingListener)
    }

    override fun onDetach() {
        super.onDetach()
        FragmentBackStack.getInstance(context!!).push(SUBJECT_DETAILS)
    }

    companion object {

        const val TAG = "SubjectDetails"

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
                LessonDetailsFragment.newInstance(
                    lesson,
                    subject
                ),
                R.id.fragment_container
            )
        }
    }

    override fun onTaskClickListener(task: Task) {
        val subject = subject
        if (subject != null) {
            activity!!.replaceFragmentWithTransition(
                TaskDetailsFragment.newInstance(
                    task,
                    subject
                ), R.id.fragment_container
            )
        }
    }

    override fun onExamClickListener(exam: Exam) {
        val subject = subject
        if (subject != null) {
            activity!!.replaceFragmentWithTransition(
                ExamDetailsFragment.newInstance(exam, subject),
                R.id.fragment_container
            )
        }
    }

}
