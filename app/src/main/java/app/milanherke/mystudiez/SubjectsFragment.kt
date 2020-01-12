package app.milanherke.mystudiez

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subjects.*

private const val TAG = "SubjectsFragment"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SubjectsFragment.OnSubjectClick] interface
 * to handle interaction events.
 * Use the [SubjectsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubjectsFragment : Fragment() {

    private var listener: OnSubjectClick? = null
    private val viewModel by lazy { ViewModelProviders.of(activity!!).get(MyStudiezViewModel::class.java) }
    private val mAdapter = SubjectsRecyclerViewAdapter(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cursor.observe(this, Observer { cursor ->  mAdapter.swapCursor(cursor)?.close() })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.toolbar.setTitle(R.string.subjects_title)

        subject_list.layoutManager = LinearLayoutManager(context)
        subject_list.adapter = mAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subjects, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSubjectClick) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSubjectClick")
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
    interface OnSubjectClick {
        fun OnTap(uri: Uri)
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
}
