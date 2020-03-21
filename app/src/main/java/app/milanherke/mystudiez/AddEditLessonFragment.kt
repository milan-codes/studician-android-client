package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_edit_lesson.*
import java.util.*

// Fragment initialization parameters
private const val ARG_LESSON = "lesson"
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * This fragment was created to add or edit lessons.
 * Activities that contain this fragment must implement the
 * [AddEditLessonFragment.LessonSaved] interface
 * to handle interaction events.
 * Use the [AddEditLessonFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditLessonFragment : Fragment() {

    private var lesson: Lesson? = null
    private var subject: Subject? = null
    private var selectedDay: Int = 0
    private var listener: LessonSaved? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(AddEditLessonViewModel::class.java)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface LessonSaved {
        fun onSaveLessonClickListener(lesson: Lesson, subject: Subject)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LessonSaved) {
            listener = context
        } else {
            throw RuntimeException("$context must implement LessonSaved")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lesson = arguments?.getParcelable(ARG_LESSON)
        subject = arguments?.getParcelable(ARG_SUBJECT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit_lesson, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Avoiding problems with smart cast
        val lesson = lesson

        if (lesson == null) {
            // User wants to create a new lesson
            activity!!.toolbar.setTitle(R.string.add_new_lesson_title)
        } else {
            // User wants to edit an existing lesson
            activity!!.toolbar.title =
                resources.getString(R.string.edit_lesson_title, subject!!.name)
            new_lesson_day_btn.text = CalendarUtils.getDayFromNumberOfDay(lesson.day, context!!)
            selectedDay = lesson.day
            new_lesson_starts_at_btn.text = lesson.starts
            new_lesson_ends_at_btn.text = lesson.ends
            new_lesson_location.setText(lesson.location)
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

        // User must select the day of the lesson
        new_lesson_day_btn.setOnClickListener {
            showDaysPopUp(it)
        }

        // User must set the time when the lesson starts
        new_lesson_starts_at_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                context,
                CalendarUtils.getTimeSetListener(activity!!, R.id.new_lesson_starts_at_btn, cal, false),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        // User must set the time when the lesson ends
        new_lesson_ends_at_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                context,
                CalendarUtils.getTimeSetListener(activity!!, R.id.new_lesson_ends_at_btn, cal, false),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        new_lesson_save_btn.setOnClickListener {
            saveLesson()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        FragmentBackStack.getInstance(context!!).pop()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param lesson Lesson to be edited/created.
         * @param subject Subject of the lesson. Can't be null since we're launching this fragment from [SubjectDetailsFragment].
         * @return A new instance of fragment AddEditLessonFragment.
         */
        @JvmStatic
        fun newInstance(lesson: Lesson? = null, subject: Subject) =
            AddEditLessonFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_LESSON, lesson)
                    putParcelable(ARG_SUBJECT, subject)
                }
            }
    }

    /**
     * Creates a new [Lesson] object with the details to be saved, then
     * calls the [viewModel]'s saveLesson function to save it.
     */
    private fun saveLesson() {
        if (requiredFieldsAreFilled()) {
            val newLesson = lessonFromUi()
            if (newLesson != lesson) {
                lesson = viewModel.saveLesson(newLesson)
                listener?.onSaveLessonClickListener(lesson!!, subject!!)
            } else {
                Toast.makeText(
                    context!!,
                    getString(R.string.did_not_change),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context!!,
                getString(R.string.required_fields_are_not_filled),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * We set [Lesson]'s SubjectID to [subject]'s ID.
     *
     * @return A [Lesson] object created from UI
     */
    private fun lessonFromUi(): Lesson {
        val lesson = Lesson(
            subject!!.id,
            "A",
            if (selectedDay != 0) selectedDay else throw IllegalArgumentException("Parameter numOfSelectedDay $selectedDay must be between one and seven"),
            new_lesson_starts_at_btn.text.toString(),
            new_lesson_ends_at_btn.text.toString(),
            new_lesson_location.text.toString()
        )
        lesson.id = this.lesson?.id ?: ""
        return lesson
    }

    /**
     * Simple function to check whether the required fields are filled.
     *
     * @return True if required fields are filled, otherwise false
     */
    private fun requiredFieldsAreFilled(): Boolean {
        if (new_lesson_day_btn.text.isNotEmpty()
            && new_lesson_day_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_lesson_starts_at_btn.text.isNotEmpty()
            && new_lesson_starts_at_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_lesson_ends_at_btn.text.isNotEmpty()
            && new_lesson_ends_at_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_lesson_location.text.isNotEmpty()
        ) {
            return true
        }
        return false
    }

    /**
     * This function allows the user to choose a day
     * for the lesson from a pop up menu.
     * Days are saved as integers (Sunday: 1 - Saturday: 7)
     * This way they can be displayed in different languages.
     */
    private fun showDaysPopUp(view: View) {
        val popupMenu = PopupMenu(activity!!, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.day_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.day_option_sunday -> {
                    selectedDay = 1
                    new_lesson_day_btn.setText(R.string.dayOptionSunday)
                }
                R.id.day_option_monday -> {
                    selectedDay = 2
                    new_lesson_day_btn.setText(R.string.dayOptionMonday)
                }
                R.id.day_option_tuesday -> {
                    selectedDay = 3
                    new_lesson_day_btn.setText(R.string.dayOptionTuesday)
                }
                R.id.day_option_wednesday -> {
                    selectedDay = 4
                    new_lesson_day_btn.setText(R.string.dayOptionWednesday)
                }
                R.id.day_option_thursday -> {
                    selectedDay = 5
                    new_lesson_day_btn.setText(R.string.dayOptionThursday)
                }
                R.id.day_option_friday -> {
                    selectedDay = 6
                    new_lesson_day_btn.setText(R.string.dayOptionFriday)
                }
                R.id.day_option_saturday -> {
                    selectedDay = 7
                    new_lesson_day_btn.setText(R.string.dayOptionSaturday)
                }
            }
            true
        }
    }

}