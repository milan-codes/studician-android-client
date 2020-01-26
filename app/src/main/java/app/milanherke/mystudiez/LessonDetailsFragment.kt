package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_lesson_details.*
import java.lang.RuntimeException

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_LESSON = "lesson"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LessonDetailsFragment.LessonDetailsInteraction] interface
 * to handle interaction events.
 * Use the [LessonDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LessonDetailsFragment : Fragment() {

    private var lesson: Lesson? = null
    private var subject: Subject? = null
    private var listener: LessonDetailsInteraction? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(LessonDetailsViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }

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
    interface LessonDetailsInteraction {
        fun onDeleteLessonClick(subject: Subject)
        fun onEditLessonClick(lesson: Lesson)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LessonDetailsInteraction) {
            listener = context
        } else {
            throw RuntimeException("$context must implement LessonDetailsInteraction")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lesson = arguments?.getParcelable(ARG_LESSON)
        subject = sharedViewModel.subjectFromId(lesson!!.subjectId)
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
            activity!!.toolbar.title =
                resources.getString(R.string.edit_subject_title, subject!!.name)
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

        activity!!.bar.visibility = View.GONE
        activity!!.fab.visibility = View.GONE

        lesson_details_del_subject_btn.setOnClickListener {
            viewModel.deleteLesson(lesson!!.lessonId)
            listener?.onDeleteLessonClick(subject!!)
        }

        lesson_details_edit_subject_btn.setOnClickListener {
            listener?.onEditLessonClick(lesson!!)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param lesson The lesson to be displayed.
         * @return A new instance of fragment LessonDetailsFragment.
         */
        @JvmStatic
        fun newInstance(lesson: Lesson) =
            LessonDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_LESSON, lesson)
                }
            }
    }
}
