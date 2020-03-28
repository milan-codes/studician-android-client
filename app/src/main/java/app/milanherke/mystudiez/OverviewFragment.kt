package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
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
import app.milanherke.mystudiez.CalendarUtils.Companion.DateSet
import app.milanherke.mystudiez.Fragments.OVERVIEW
import app.milanherke.mystudiez.OverviewViewModel.DataFetching
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_overview.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * The main purpose of this fragment is to give the user a brief overview of their day.
 * This includes displaying their lessons, tasks and exams for that specified date.
 * Activities that contain this fragment must implement the
 * [OverviewFragment.OverviewInteractions] interface
 * to handle interaction events.
 * Use the [OverviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OverviewFragment : Fragment(),
    LessonsRecyclerViewAdapter.OnLessonClickListener,
    TasksRecyclerViewAdapter.OnTaskClickListener,
    ExamsRecyclerViewAdapter.OnExamClickListener {
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(OverviewViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }
    private val lessonsAdapter = LessonsRecyclerViewAdapter(null, this, OVERVIEW)
    private val tasksAdapter = TasksRecyclerViewAdapter(null, this, OVERVIEW)
    private val examsAdapter = ExamsRecyclerViewAdapter(null, this, OVERVIEW)
    private var subjectsList: MutableMap<String, Subject>? = null
    private var listener: OverviewInteractions? = null
    private var progressBarHandler: ProgressBarHandler? = null

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OverviewInteractions {
        fun setDoubleBackToDefault()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OverviewInteractions) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OverviewInteractions")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Listener will never be null
        // because the program crashes in onAttach if the interface is not implemented
        listener!!.setDoubleBackToDefault()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_overview, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.toolbar.setTitle(R.string.overview_title)
        // Getting all subjects from the database
        sharedViewModel.getAllSubjects(object : SharedViewModel.RetrievingData {
            override fun onLoad() {
                val activity = activity
                if (activity != null) {
                    progressBarHandler = ProgressBarHandler(activity)
                    progressBarHandler!!.showProgressBar()
                }
            }

            override fun onSuccess(subjects: MutableMap<String, Subject>) {
                lessonsAdapter.swapSubjectMap(subjects)
                tasksAdapter.swapSubjectsMap(subjects)
                examsAdapter.swapSubjectsMap(subjects)
                subjectsList = subjects

                progressBarHandler!!.hideProgressBar()
            }

            override fun onFailure(e: DatabaseError) {
                Toast.makeText(
                    context,
                    getString(R.string.firebase_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        overview_schedule_list.layoutManager = LinearLayoutManager(context)
        overview_schedule_list.adapter = lessonsAdapter

        overview_task_list.layoutManager = LinearLayoutManager(context)
        overview_task_list.adapter = tasksAdapter

        overview_exam_list.layoutManager = LinearLayoutManager(context)
        overview_exam_list.adapter = examsAdapter

    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        // Hiding bottom navigation bar and fab button
        activity!!.bar.visibility = View.VISIBLE
        activity!!.fab.visibility = View.GONE

        // Showing a progress bar while data is being fetched
        progressBarHandler = ProgressBarHandler(activity!!)
        val dataFetchingListener: DataFetching = object : DataFetching {
            override fun onLoad() {
                progressBarHandler!!.showProgressBar()
            }

            override fun onSuccess() {
                progressBarHandler!!.hideProgressBar()
            }

            override fun onFailure(e: DatabaseError) {
                Toast.makeText(
                    context,
                    getString(R.string.firebase_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        //Registering observers
        viewModel.lessonsListLiveData.observe(
            this,
            Observer { list ->
                lessonsAdapter.swapLessonsList(list)
                if (overview_schedule_list != null && list.size != 0) Animations.runLayoutAnimation(
                    overview_schedule_list
                )
            }
        )
        viewModel.tasksListLiveData.observe(
            this,
            Observer { list ->
                tasksAdapter.swapTasksList(list)
                if (overview_task_list != null && list.size != 0) Animations.runLayoutAnimation(
                    overview_task_list
                )
            }
        )
        viewModel.examsListLiveData.observe(
            this,
            Observer { list ->
                examsAdapter.swapExamsList(list)
                if (overview_exam_list != null && list.size != 0) Animations.runLayoutAnimation(
                    overview_exam_list
                )
            }
        )
        viewModel.loadAllDetails(Date(), dataFetchingListener)

        // Setting the default date
        overview_date_button.text =
            SimpleDateFormat("dd'/'MM'/'yyyy", Locale.getDefault()).format(Date())


        overview_date_button.setOnClickListener {
            val cal = Calendar.getInstance()
            val listener = CalendarUtils.getDateSetListener(
                activity!!,
                R.id.overview_date_button,
                cal,
                object : DateSet {
                    override fun onSuccess(date: Date) {
                        viewModel.loadAllDetails(date, dataFetchingListener)
                    }
                })
            DatePickerDialog(
                context!!, listener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    override fun onDetach() {
        super.onDetach()
        FragmentBackStack.getInstance(context!!).push(OVERVIEW)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment OverviewFragment.
         */
        @JvmStatic
        fun newInstance() =
            OverviewFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    override fun onLessonClick(lesson: Lesson) {
        val subjects = subjectsList
        if (subjects != null) {
            activity!!.replaceFragmentWithTransition(
                LessonDetailsFragment.newInstance(lesson, subjects[lesson.subjectId]),
                R.id.fragment_container
            )
        }
    }

    override fun onTaskClickListener(task: Task) {
        val subjects = subjectsList
        if (subjects != null) {
            activity!!.replaceFragmentWithTransition(
                TaskDetailsFragment.newInstance(task, subjects[task.subjectId]),
                R.id.fragment_container
            )
        }
    }

    override fun onExamClickListener(exam: Exam) {
        val subjects = subjectsList
        if (subjects != null) {
            activity!!.replaceFragmentWithTransition(
                ExamDetailsFragment.newInstance(exam, subjects[exam.subjectId]),
                R.id.fragment_container
            )
        }
    }
}