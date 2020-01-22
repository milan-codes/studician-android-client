package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subjects.*

/**
 * Use the [SubjectsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubjectsFragment : Fragment(), SubjectsRecyclerViewAdapter.OnSubjectClickListener {

    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(SubjectsViewModel::class.java)
    }
    private val subjectsAdapter = SubjectsRecyclerViewAdapter(null, null, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cursorSubjects.observe(
            this,
            Observer { cursor -> subjectsAdapter.swapSubjectsCursor(cursor)?.close() })
        viewModel.cursorLessons.observe(
            this,
            Observer { cursor -> subjectsAdapter.swapLessonsCursor(cursor)?.close() })

        // Loading subjects and selected lessons
        viewModel.loadSubjects()
        viewModel.loadLessonsForSubjects()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.toolbar.setTitle(R.string.subjects_title)

        subject_list.layoutManager = LinearLayoutManager(context)
        subject_list.adapter = subjectsAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subjects, container, false)
    }

    companion object {
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
        activity!!.replaceFragment(SubjectDetailsFragment.newInstance(subject), R.id.fragment_container)
    }
}
