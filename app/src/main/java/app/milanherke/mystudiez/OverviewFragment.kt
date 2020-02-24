package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_overview.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
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
    private val lessonsAdapter = LessonsRecyclerViewAdapter(null, null, this, true)
    private val tasksAdapter = TasksRecyclerViewAdapter(null, null, this, true)
    private val examsAdapter = ExamsRecyclerViewAdapter(null, null, this, true)
    private var listener: OverviewInteractions? = null

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
    interface OverviewInteractions {
        fun overviewFragmentIsBeingCreated()
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
        viewModel.cursorLessons.observe(
            this,
            Observer { cursor ->
                lessonsAdapter.swapLessonsCursor(cursor)?.close()
                if (overview_schedule_list != null && cursor.count != 0) Animations.runLayoutAnimation(
                    overview_schedule_list
                )
            }
        )
        viewModel.cursorTasks.observe(
            this,
            Observer { cursor ->
                tasksAdapter.swapTasksCursor(cursor)?.close()
                if (overview_task_list != null && cursor.count != 0) Animations.runLayoutAnimation(
                    overview_task_list
                )
            }
        )
        viewModel.cursorExams.observe(
            this,
            Observer { cursor ->
                examsAdapter.swapExamsCursor(cursor)?.close()
                if (overview_exam_list != null && cursor.count != 0) Animations.runLayoutAnimation(
                    overview_exam_list
                )
            }
        )
        viewModel.loadAllDetails(Date())
        // Listener will never be null since the program crashes in onAttach if the interface is not implemented
        listener!!.overviewFragmentIsBeingCreated()
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

        activity!!.bar.visibility = View.VISIBLE
        activity!!.fab.visibility = View.GONE

        overview_date_button.text =
            SimpleDateFormat("dd'/'MM'/'yyyy", Locale.getDefault()).format(Date())

        overview_date_button.setOnClickListener {
            val cal = Calendar.getInstance()
             DatePickerDialog(
                context!!, getDate(cal),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.dateFilter.postValue(null)
        FragmentsStack.getInstance(context!!).push(Fragments.OVERVIEW)
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
        activity!!.replaceFragmentWithTransition(
            LessonDetailsFragment.newInstance(lesson),
            R.id.fragment_container
        )
    }

    override fun loadSubjectFromLesson(id: Long): Subject? {
        return sharedViewModel.subjectFromId(id)
    }

    override fun onTaskClickListener(task: Task) {
        activity!!.replaceFragmentWithTransition(
            TaskDetailsFragment.newInstance(task),
            R.id.fragment_container
        )
    }

    override fun loadSubjectFromTask(id: Long): Subject? {
        return sharedViewModel.subjectFromId(id)
    }

    override fun onExamClickListener(exam: Exam) {
        activity!!.replaceFragmentWithTransition(
            ExamDetailsFragment.newInstance(exam),
            R.id.fragment_container
        )
    }

    override fun loadSubjectFromExam(id: Long): Subject? {
        return sharedViewModel.subjectFromId(id)
    }

    private fun getDate(
        cal: Calendar
    ): DatePickerDialog.OnDateSetListener {

        return DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            viewModel.loadAllDetails(cal.time)

            overview_date_button.text =
                SimpleDateFormat("dd'/'MM'/'yyyy", Locale.getDefault()).format(cal.time)
        }
    }

}
