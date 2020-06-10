package app.milanherke.mystudiez

import android.annotation.SuppressLint
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
import app.milanherke.mystudiez.Fragments.TASKS
import app.milanherke.mystudiez.TasksViewModel.DataFetching
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tasks.*

/**
 * A simple [Fragment] subclass.
 * The main purpose of this fragment is to display all [Task] objects from the database.
 * Activities that contain this fragment must implement the
 * [TasksFragment.TasksInteractions] interface
 * to handle interaction events.
 * Use the [TasksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TasksFragment : Fragment(), TasksRecyclerViewAdapter.OnTaskClickListener {

    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(TasksViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }
    private val tasksAdapter = TasksRecyclerViewAdapter(null, this, TASKS)
    private var subjectsList: MutableMap<String, Subject>? = null
    private var listener: TasksInteractions? = null
    private var progressBarHandler: ProgressBarHandler? = null

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface TasksInteractions {
        fun tasksFragmentIsBeingCreated()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TasksInteractions) {
            listener = context
        } else {
            throw RuntimeException("$context must implement TaskInteractions")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Listener will never be null since the program crashes in onAttach if the interface is not implemented
        listener!!.tasksFragmentIsBeingCreated()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.toolbar.setTitle(R.string.tasks_title)

        // Getting all subjects from the database,
        // tasksAdapter must have a map of subjects in order to display
        // details about a task's subject
        sharedViewModel.getAllSubjects(object : SharedViewModel.RetrievingData {
            override fun onLoad() {
                val activity = activity
                if (activity != null) {
                    progressBarHandler = ProgressBarHandler(activity)
                    progressBarHandler!!.showProgressBar()
                }
            }

            override fun onSuccess(subjects: MutableMap<String, Subject>) {
                tasksAdapter.swapSubjectsMap(subjects)
                subjectsList = subjects
            }

            override fun onFailure(e: DatabaseError) {
                Toast.makeText(
                    context,
                    getString(R.string.firebase_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        task_list.layoutManager = LinearLayoutManager(context)
        task_list.adapter = tasksAdapter
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        // Hiding bottom navigation bar and fab button
        activity!!.bar.visibility = View.VISIBLE
        activity!!.fab.visibility = View.VISIBLE

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

        // Registering observer
        viewModel.tasksListLiveData.observe(
            this,
            Observer { list ->
                val sortedList = ArrayList(list.sortedWith(compareBy(Task::dueDate, Task::name)))
                tasksAdapter.swapTasksList(sortedList)
                if (task_list != null && list.size != 0) Animations.runLayoutAnimation(task_list)
            }
        )
        viewModel.loadTasks(dataFetchingListener)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        FragmentBackStack.getInstance(context!!).push(TASKS)
    }

    companion object {

        const val TAG = "Tasks"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment TasksFragment.
         */
        @JvmStatic
        fun newInstance() =
            TasksFragment().apply {
                arguments = Bundle().apply {}
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
}
