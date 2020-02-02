package app.milanherke.mystudiez

import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.fragment_subject_details.*
import kotlinx.android.synthetic.main.fragment_tasks.*
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
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
    private val tasksAdapter = TasksRecyclerViewAdapter(null, null, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadTasks()
        viewModel.cursorTasks.observe(
            this,
            Observer { cursor -> tasksAdapter.swapTasksCursor(cursor)?.close() }
        )
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

        task_list.layoutManager = LinearLayoutManager(context)
        task_list.adapter = tasksAdapter

    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        activity!!.bar.visibility = View.VISIBLE
        activity!!.fab.visibility = View.VISIBLE
    }

    override fun onDetach() {
        super.onDetach()
        FragmentsStack.getInstance(context!!).push(Fragments.TASKS)
    }

    companion object {
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
        activity!!.replaceFragment(TaskDetailsFragment.newInstance(task), R.id.fragment_container)
    }

    override fun loadSubjectFromId(id: Long): Subject {
        val subject = sharedViewModel.subjectFromId(id)
        if (subject != null) {
            return subject
        } else {
            throw Exception("Couldn't find subject from id $id")
        }
    }
}
