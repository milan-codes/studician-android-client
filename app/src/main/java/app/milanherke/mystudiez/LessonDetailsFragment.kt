package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_lesson_details.*

// Fragment initialization parameters
private const val ARG_LESSON = "lesson"
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * The purpose of this fragment is to display the details of a [Lesson].
 * The user can delete a lesson from this fragment
 * or launch a new fragment ([AddEditLessonActivity]) to edit it.
 * Use the [LessonDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LessonDetailsFragment : Fragment() {

    private var lesson: Lesson? = null
    private var subject: Subject? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(LessonDetailsViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lesson = arguments?.getParcelable(ARG_LESSON)
        subject = arguments?.getParcelable(ARG_SUBJECT)
        val lesson = lesson
        if (lesson != null) {
            sharedViewModel.swapLesson(lesson)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lesson_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Avoiding problems with smart-casting
        val lesson = lesson

        if (lesson != null) {
            activity!!.toolbar.setTitle(R.string.lesson_details_title)
            lesson_details_name_value.text = subject!!.name
            lesson_details_starts_at_value.text = lesson.starts
            lesson_details_ends_at_value.text = lesson.ends
            lesson_details_location_value.text = lesson.location
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

        // Swapping in the Subject in MainActivity
        // MainActivity always needs to have the currently used Subject
        val subject = subject
        if (subject != null) {
            sharedViewModel.swapSubject(subject)
        }

        lesson_details_del_subject_btn.setOnClickListener {
            viewModel.deleteLesson(lesson!!)
            activity!!.replaceFragmentWithTransition(
                SubjectDetailsFragment.newInstance(subject!!),
                R.id.fragment_container
            )
        }

        lesson_details_edit_subject_btn.setOnClickListener {
            FragmentBackStack.getInstance(activity!!).push(Fragments.LESSON_DETAILS)
            val intent = Intent(activity, AddEditLessonActivity::class.java)
            intent.putExtra(ActivityUtils.LESSON_PARAM_BUNDLE_ID, lesson)
            intent.putExtra(ActivityUtils.SUBJECT_PARAM_BUNDLE_ID, subject)
            startActivity(intent)
        }
    }

    override fun onDetach() {
        super.onDetach()
        FragmentBackStack.getInstance(context!!).push(Fragments.LESSON_DETAILS)
    }

    companion object {

        const val TAG = "LessonDetails"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param lesson The lesson to be displayed.
         * @return A new instance of fragment LessonDetailsFragment.
         */
        @JvmStatic
        fun newInstance(lesson: Lesson, subject: Subject? = null) =
            LessonDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_LESSON, lesson)
                    putParcelable(ARG_SUBJECT, subject)
                }
            }
    }
}
