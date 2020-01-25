package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_edit_lesson.*
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_LESSON = "lesson"
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AddEditLessonFragment.OnSaveLessonClick] interface
 * to handle interaction events.
 * Use the [AddEditLessonFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditLessonFragment : Fragment() {

    private var lesson: Lesson? = null
    private var subject: Subject? = null
    private var listener: OnSaveLessonClick? = null
    private val viewModel by lazy { ViewModelProviders.of(activity!!).get(AddEditLessonViewModel::class.java) }

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
    interface OnSaveLessonClick {
        fun onSaveLessonClick(subject: Subject)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSaveLessonClick) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSaveLessonClick")
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
           activity!!.toolbar.setTitle(R.string.add_new_lesson_title)
        } else {
           activity!!.toolbar.title = resources.getString(R.string.edit_lesson_title, subject!!.name)
            new_lesson_day_btn.text = lesson.day
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

        activity!!.bar.visibility = View.GONE
        activity!!.fab.visibility = View.GONE

        new_lesson_day_btn.setOnClickListener {
            showDaysPopUp(it)
        }

        new_lesson_starts_at_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(context, getTime(R.id.new_lesson_starts_at_btn, cal), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        new_lesson_ends_at_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(context, getTime(R.id.new_lesson_ends_at_btn, cal), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        new_lesson_save_btn.setOnClickListener {
            saveLesson()
            listener?.onSaveLessonClick(subject!!)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
     * Creates a newLesson object with the details to be saved, then
     * call the viewModel's saveLesson function to save it
     * Lesson is not a data class, so we can compare the new details with the original lesson
     * and only save if they are different
     */
    private fun saveLesson() {
        val newLesson = subjectFromUi()
        if(newLesson != lesson) {
            lesson = viewModel.saveLesson(newLesson)
        }
    }

    private fun subjectFromUi(): Lesson {
        val lesson = Lesson(subject!!.subjectId, "A", new_lesson_day_btn.text.toString(), new_lesson_starts_at_btn.text.toString(), new_lesson_ends_at_btn.text.toString(), new_lesson_location.text.toString())
        lesson.lessonId = this.lesson?.lessonId ?: 0
        return lesson
    }

    private fun showDaysPopUp(view: View) {
        val popupMenu = PopupMenu(activity!!, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.day_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.day_option_monday -> new_lesson_day_btn.setText(R.string.dayOptionMonday)
                R.id.day_option_tuesday -> new_lesson_day_btn.setText(R.string.dayOptionTuesday)
                R.id.day_option_wednesday -> new_lesson_day_btn.setText(R.string.dayOptionWednesday)
                R.id.day_option_thursday -> new_lesson_day_btn.setText(R.string.dayOptionThursday)
                R.id.day_option_friday -> new_lesson_day_btn.setText(R.string.dayOptionFriday)
                R.id.day_option_saturday -> new_lesson_day_btn.setText(R.string.dayOptionSaturday)
                R.id.day_option_sunday -> new_lesson_day_btn.setText(R.string.dayOptionSunday)
            }
            true
        }
    }

    private fun getTime(@IdRes buttonId: Int, cal: Calendar) : TimePickerDialog.OnTimeSetListener {
        val button = activity!!.findViewById<Button>(buttonId)

        return TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

            button.text = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(cal.time)
        }
    }

}
