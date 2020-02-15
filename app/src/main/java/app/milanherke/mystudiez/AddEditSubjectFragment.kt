package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_edit_subject.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * This fragment was created to add or edit subjects.
 * Activities that contain this fragment must implement the
 * [AddEditSubjectFragment.AddEditSubjectInteractions] interface
 * to handle interaction events.
 * Use the [AddEditSubjectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditSubjectFragment : Fragment() {

    private var subject: Subject? = null
    private var listener: AddEditSubjectInteractions? = null
    private var selectedSubjectColor: Int = -1
    private var subjectIsBeingEdited: Boolean = false
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(AddEditSubjectViewModel::class.java)
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
    interface AddEditSubjectInteractions {
        fun onSaveSubjectClick(subject: Subject)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEditSubjectInteractions) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AddEditSubjectInteractions")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subject = arguments?.getParcelable(ARG_SUBJECT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit_subject, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Creating a clone of the subject indicator because we do not want to affect all of the drawable's instances
        // And setting default color
        createAndSetCloneColorIndicator()

        // Avoiding problems with smart cast
        val subject = subject

        if (subject == null) {
            activity!!.toolbar.setTitle(R.string.add_new_subject_title)
            subjectIsBeingEdited = false
            selectedColor.visibility = View.INVISIBLE
        } else {
            activity!!.toolbar.title =
                resources.getString(R.string.edit_subject_title, subject.name)
            subjectIsBeingEdited = true
            new_subject_name_value.setText(subject.name)
            new_subject_teacher_value.setText(subject.teacher)
            selectedColor.drawable.displayColor(subject.colorCode, activity!!)
            new_subject_color_btn.text = getColorName(subject.colorCode)
            selectedSubjectColor = subject.colorCode

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

        new_subject_color_btn.setOnClickListener {
            showPopup(it)
        }
        new_subject_save_btn.setOnClickListener {
            saveSubject()
        }

    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        FragmentsStack.getInstance(context!!).pop()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param subject The subject to be edited or null if creating a new one
         * @return A new instance of fragment AddEditSubjectFragment.
         */
        @JvmStatic
        fun newInstance(subject: Subject? = null) =
            AddEditSubjectFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SUBJECT, subject)
                }
            }
    }

    /**
     * Creates a newSubject object with the details to be saved, then
     * call the viewModel's saveSubject function to save it
     * Subject is not a data class, so we can compare the new details with the original subject
     * and only save if they are different
     */
    private fun saveSubject() {
        val newSubject = subjectFromUi()
        if (newSubject != subject && requiredFieldsAreFilled()) {
            subject = viewModel.saveSubject(newSubject)
            listener?.onSaveSubjectClick(subject!!)
        } else {
            Toast.makeText(
                context!!,
                getString(R.string.required_fields_are_not_filled),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun subjectFromUi(): Subject {
        val subject = Subject(
            new_subject_name_value.text.toString(),
            new_subject_teacher_value.text.toString(),
            selectedSubjectColor
        )
        subject.subjectId = this.subject?.subjectId ?: 0
        return subject
    }

    private fun requiredFieldsAreFilled(): Boolean {
        if (new_subject_name_value.text.isNotEmpty()
            && new_subject_teacher_value.text.isNotEmpty()
            && new_subject_color_btn.text != getString(R.string.new_subject_color_button)
            && new_subject_color_btn.text.isNotEmpty()
        ) {
            return true
        }
        return false
    }

    private fun showPopup(view: View) {
        val popupMenu = PopupMenu(activity!!, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.color_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.color_option_red -> {
                    setColor(R.color.subjectColorRed, activity!!, R.string.colorRedTitle)
                }
                R.id.color_option_pink -> {
                    setColor(R.color.subjectColorPink, activity!!, R.string.colorPinkTitle)
                }
                R.id.color_option_purple -> {
                    setColor(R.color.subjectColorPurple, activity!!, R.string.colorPurpleTitle)
                }
                R.id.color_option_deep_purple -> {
                    setColor(
                        R.color.subjectColorDeepPurple,
                        activity!!,
                        R.string.colorDeepPurpleTitle
                    )
                }
                R.id.color_option_indigo -> {
                    setColor(R.color.subjectColorIndigo, activity!!, R.string.colorIndigoTitle)
                }
                R.id.color_option_blue -> {
                    setColor(R.color.subjectColorBlue, activity!!, R.string.colorBlueTitle)
                }
                R.id.color_option_light_blue -> {
                    setColor(
                        R.color.subjectColorLightBlue,
                        activity!!,
                        R.string.colorLightBlueTitle
                    )
                }
                R.id.color_option_cyan -> {
                    setColor(R.color.subjectColorCyan, activity!!, R.string.colorCyanTitle)
                }
                R.id.color_option_teal -> {
                    setColor(R.color.subjectColorTeal, activity!!, R.string.colorTealTitle)
                }
                R.id.color_option_green -> {
                    setColor(R.color.subjectColorGreen, activity!!, R.string.colorGreenTitle)
                }
                R.id.color_option_light_green -> {
                    setColor(
                        R.color.subjectColorLightGreen,
                        activity!!,
                        R.string.colorLightGreenTitle
                    )
                }
                R.id.color_option_lime -> {
                    setColor(R.color.subjectColorLime, activity!!, R.string.colorLimeTitle)
                }
                R.id.color_option_yellow -> {
                    setColor(R.color.subjectColorYellow, activity!!, R.string.colorYellowTitle)
                }
                R.id.color_option_amber -> {
                    setColor(R.color.subjectColorAmber, activity!!, R.string.colorAmberTitle)
                }
                R.id.color_option_orange -> {
                    setColor(R.color.subjectColorOrange, activity!!, R.string.colorOrangeTitle)
                }
                R.id.color_option_deep_orange -> {
                    setColor(
                        R.color.subjectColorDeepOrange,
                        activity!!,
                        R.string.colorDeepOrangeTitle
                    )
                }
                R.id.color_option_blue_gray -> {
                    setColor(R.color.subjectColorBlueGray, activity!!, R.string.colorBlueGrayTitle)
                }
            }
            true
        }
    }

    private fun getColorName(@ColorRes colorId: Int): String {
        return when (colorId) {
            R.color.subjectColorRed -> getString(R.string.colorRedTitle)
            R.color.subjectColorPink -> getString(R.string.colorPinkTitle)
            R.color.subjectColorPurple -> getString(R.string.colorPurpleTitle)
            R.color.subjectColorDeepPurple -> getString(R.string.colorDeepPurpleTitle)
            R.color.subjectColorIndigo -> getString(R.string.colorIndigoTitle)
            R.color.subjectColorBlue -> getString(R.string.colorBlueTitle)
            R.color.subjectColorLightBlue -> getString(R.string.colorLightBlueTitle)
            R.color.subjectColorCyan -> getString(R.string.colorCyanTitle)
            R.color.subjectColorTeal -> getString(R.string.colorTealTitle)
            R.color.subjectColorGreen -> getString(R.string.colorGreenTitle)
            R.color.subjectColorLightGreen -> getString(R.string.colorLightGreenTitle)
            R.color.subjectColorLime -> getString(R.string.colorLimeTitle)
            R.color.subjectColorYellow -> getString(R.string.colorYellowTitle)
            R.color.subjectColorAmber -> getString(R.string.colorAmberTitle)
            R.color.subjectColorOrange -> getString(R.string.colorOrangeTitle)
            R.color.subjectColorDeepOrange -> getString(R.string.colorDeepOrangeTitle)
            R.color.subjectColorBlueGray -> getString(R.string.colorBlueGrayTitle)
            else -> throw IllegalArgumentException("Subject has unrecognised color id")
        }
    }

    private fun createAndSetCloneColorIndicator() {
        val clone = selectedColor.drawable.mutatedClone()
        selectedColor.setImageDrawable(clone)
    }

    private fun setColor(@ColorRes colorId: Int, context: Context, @StringRes colorName: Int) {
        selectedColor.drawable.displayColor(colorId, context)
        new_subject_color_btn.setText(colorName)
        selectedSubjectColor = colorId
        if (selectedColor.visibility == View.INVISIBLE) selectedColor.visibility = View.VISIBLE
    }


}
