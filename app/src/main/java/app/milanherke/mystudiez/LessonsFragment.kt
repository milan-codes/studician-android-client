package app.milanherke.mystudiez

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "LessonsFragment"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LessonsFragment.OnLessonClick] interface
 * to handle interaction events.
 * Use the [LessonsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LessonsFragment : Fragment() {
    private var listener: OnLessonClick? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.toolbar.setTitle(R.string.lessons_title)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lessons, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLessonClick) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnLessonClick")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
    interface OnLessonClick {
        fun onLessonTap(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment LessonsFragment.
         */
        @JvmStatic
        fun newInstance() =
            LessonsFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}
