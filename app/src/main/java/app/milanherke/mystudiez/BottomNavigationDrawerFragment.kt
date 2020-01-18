package app.milanherke.mystudiez

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.lang.IllegalArgumentException
import kotlinx.android.synthetic.main.fragment_bottomsheet.*

class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {

    private lateinit var appContext: Context

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigation_view.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottomnav_subjects -> {
                    activity!!.replaceFragment(
                        SubjectsFragment.newInstance(),
                        R.id.fragment_container
                    )
                    activity!!.removeFragment(this)
                }
                R.id.bottomnav_tasks -> {
                    activity!!.replaceFragment(
                        SubjectsFragment.newInstance(),
                        R.id.fragment_container
                    )
                    activity!!.removeFragment(this)
                }
                R.id.bottomnav_exams -> {
                    activity!!.replaceFragment(
                        SubjectsFragment.newInstance(),
                        R.id.fragment_container
                    )
                    activity!!.removeFragment(this)
                }
                else -> throw IllegalArgumentException("Unknown menuItem passed")
            }
            true
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appContext = context.applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottomsheet, container, false)
    }
}