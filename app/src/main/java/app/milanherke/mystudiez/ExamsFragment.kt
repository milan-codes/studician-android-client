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
import app.milanherke.mystudiez.ExamsViewModel.DataFetching
import app.milanherke.mystudiez.Fragments.EXAMS
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_exams.*

/**
 * A simple [Fragment] subclass.
 * The main purpose of this fragment is to display all [Exam] objects from the database.
 * Activities that contain this fragment must implement the
 * [ExamsFragment.ExamsInteractions] interface
 * to handle interaction events.
 * Use the [ExamsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExamsFragment : Fragment(), ExamsRecyclerViewAdapter.OnExamClickListener {

    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(ExamsViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }
    private val examsAdapter = ExamsRecyclerViewAdapter(null, this, EXAMS)
    private var subjectsList: MutableMap<String, Subject>? = null
    private var listener: ExamsInteractions? = null
    private var progressBarHandler: ProgressBarHandler? = null

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface ExamsInteractions {
        fun onCreateCalled()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ExamsInteractions) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ExamsInteractions")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Listener will never be null
        // because the program crashes in onAttach if the interface is not implemented
        listener!!.onCreateCalled()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.toolbar.setTitle(R.string.exams_title)

        // Getting all subjects from the database,
        // examsAdapter must have a map of subjects in order to display
        // details about an exam's subject
        sharedViewModel.getAllSubjects(object : SharedViewModel.RetrievingData {
            override fun onLoad() {
                val activity = activity
                if (activity != null) {
                    progressBarHandler = ProgressBarHandler(activity)
                    progressBarHandler!!.showProgressBar()
                }
            }

            override fun onSuccess(subjects: MutableMap<String, Subject>) {
                exam_list.layoutManager = LinearLayoutManager(context)
                exam_list.adapter = examsAdapter
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
        // Swapping the exams list in ExamsRecyclerViewAdapter
        // and running layout animation
        viewModel.examsListLiveData.observe(
            this,
            Observer { list ->
                val sortedList = ArrayList(list.sortedWith(compareBy(Exam::date, Exam::name)))
                examsAdapter.swapExamsList(sortedList)
                if (exam_list != null && list.size != 0) Animations.runLayoutAnimation(exam_list)
            }
        )
        viewModel.loadExams(dataFetchingListener)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        FragmentBackStack.getInstance(context!!).push(EXAMS)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ExamsFragment.
         */
        @JvmStatic
        fun newInstance() =
            ExamsFragment().apply {
                arguments = Bundle().apply {}
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