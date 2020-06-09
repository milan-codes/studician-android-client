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
import app.milanherke.mystudiez.SubjectsViewModel.DataFetching
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subjects.*

/**
 * A simple [Fragment] subclass.
 * The main purpose of this fragment is to display all [Subject] objects from the database.
 * Activities that contain this fragment must implement the
 * [SubjectsFragment.SubjectsInteractions] interface
 * to handle interaction events.
 * Use the [SubjectsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubjectsFragment : Fragment(), SubjectsRecyclerViewAdapter.OnSubjectClickListener {

    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(SubjectsViewModel::class.java)
    }
    private val subjectsAdapter = SubjectsRecyclerViewAdapter(null, null, this)
    private var listener: SubjectsInteractions? = null

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface SubjectsInteractions {
        fun subjectsFragmentIsBeingCreated()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SubjectsInteractions) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SubjectInteractions")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Listener will never be null since the program crashes in onAttach if the interface is not implemented
        listener!!.subjectsFragmentIsBeingCreated()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subjects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.toolbar.setTitle(R.string.subjects_title)
        subject_list.layoutManager = LinearLayoutManager(context)
        subject_list.adapter = subjectsAdapter
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

        // Registering observers
        viewModel.subjectsListLiveData.observe(
            this,
            Observer { list ->
                val sortedList = ArrayList(list.sortedWith(compareBy(Subject::name, Subject::teacher)))
                subjectsAdapter.swapSubjectsList(sortedList)
                if (subject_list != null && list.size != 0) Animations.runLayoutAnimation(
                    subject_list
                )
            })
        viewModel.lessonsListLiveData.observe(
            this,
            Observer { list ->
                subjectsAdapter.swapLessonsList(list)
                if (subject_list != null && list.size != 0) Animations.runLayoutAnimation(
                    subject_list
                )
            })
        viewModel.loadSubjects(dataFetchingListener)
        viewModel.loadLessonsForSubjects(dataFetchingListener)
    }

    override fun onDetach() {
        super.onDetach()
        FragmentBackStack.getInstance(context!!).push(Fragments.SUBJECTS)
    }

    companion object {

        const val TAG = "Subjects"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SubjectsFragment.
         */
        @JvmStatic
        fun newInstance() =
            SubjectsFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    override fun onSubjectClick(subject: Subject) {
        activity!!.replaceFragmentWithTransition(
            SubjectDetailsFragment.newInstance(subject),
            R.id.fragment_container
        )
    }
}
