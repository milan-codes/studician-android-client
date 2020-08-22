package app.milanherke.mystudiez.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.milanherke.mystudiez.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottomsheet.*

/**
 * A simple [BottomSheetDialogFragment] subclass.
 * Here, we inflate our bottom navigation drawer
 * and we define what each menu item does.
 */
class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {

    private lateinit var appContext: Context

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigation_view.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottomnav_overview -> {
                    activity!!.replaceFragment(
                        OverviewFragment.newInstance(),
                        R.id.fragment_container
                    )
                    activity!!.removeFragment(this)
                }
                R.id.bottomnav_subjects -> {
                    activity!!.replaceFragment(
                        SubjectsFragment.newInstance(),
                        R.id.fragment_container
                    )
                    activity!!.removeFragment(this)
                }
                R.id.bottomnav_tasks -> {
                    activity!!.replaceFragment(
                        TasksFragment.newInstance(),
                        R.id.fragment_container
                    )
                    activity!!.removeFragment(this)
                }
                R.id.bottomnav_exams -> {
                    activity!!.replaceFragment(
                        ExamsFragment.newInstance(),
                        R.id.fragment_container
                    )
                    activity!!.removeFragment(this)
                }
                R.id.bottomnav_settings -> {
                    activity!!.replaceFragment(
                        SettingsFragment.newInstance(),
                        R.id.fragment_container
                    )
                    activity!!.removeFragment(this)
                }
                else -> throw IllegalArgumentException("Unknown menuItem ${menuItem.itemId}")
            }
            true
        }
    }
}
