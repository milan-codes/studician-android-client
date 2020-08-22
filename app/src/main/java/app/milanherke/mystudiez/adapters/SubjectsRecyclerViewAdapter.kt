package app.milanherke.mystudiez.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import app.milanherke.mystudiez.*
import app.milanherke.mystudiez.adapters.SubjectsRecyclerViewAdapter.OnSubjectClickListener
import app.milanherke.mystudiez.models.Lesson
import app.milanherke.mystudiez.models.Subject
import kotlinx.android.synthetic.main.subject_list_items.view.*

/**
 * A [BaseAdapter] subclass.
 * This class serves as an adapter for RecyclerViews
 * which were created to display subjects.
 *
 * @property subjects An [ArrayList] containing all [Subject] objects to display
 * @property lessons An [ArrayList] containing lessons of subjects
 * @property listener Fragments that use this class must implement [OnSubjectClickListener]
 */
class SubjectsRecyclerViewAdapter(
    private val listener: OnSubjectClickListener,
    private var subjects: ArrayList<Subject> = arrayListOf(),
    private var lessons: ArrayList<Lesson> = arrayListOf()
) : BaseAdapter<Subject>(subjects) {

    /**
     * This interface must be implemented by activities/fragments that contain
     * this RecyclerViewAdapter to allow an interaction in this class to be
     * communicated to the activity/fragment.
     */
    interface OnSubjectClickListener {
        fun onSubjectClick(subject: Subject)
    }

    override fun setViewHolder(parent: ViewGroup): BaseViewHolder<Subject> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.subject_list_items, parent, false)
        return SubjectViewHolder(view)
    }

    /**
     * Changes the data set. Calls [BaseAdapter.swapDataList].
     * Swaps in a new [ArrayList] that contains [Subject] objects.
     *
     * @param subjects An [ArrayList] that contains subjects
     * @return Returns the previously used list, or null if there wasn't one
     */
    fun swapSubjects(subjects: ArrayList<Subject>) {
        super.swapDataList(subjects)
    }

    /**
     * Swaps in a new [ArrayList], that contains [Lesson] objects,
     * which are needed in [SubjectViewHolder].
     *
     * @param lessons New list containing lessons
     * @return Returns the previously set list, or null if there wasn't one
     */
    fun swapLessonsList(lessons: ArrayList<Lesson>) {
        // Secondary data list, which contains no useful information for the BaseAdapter, it is only needed when binding a lesson
        this.lessons = lessons
        notifyDataSetChanged()
    }

    private inner class SubjectViewHolder(override val containerView: View) :
        BaseViewHolder<Subject>(containerView) {

        override fun bind(data: Subject) {
            // Setting day-indicators back to default
            setViewToDefault()

            // Getting binded subject's lessons
            val days = arrayListOf<Int>()
            for (lesson in lessons) {
                if (lesson.subjectId == data.id) {
                    days.add(lesson.day)
                }
            }

            containerView.sli_name.text = data.name
            for (day in days) {
                when (day) {
                    1 -> {
                        containerView.sli_sunday.setBackgroundResource(R.drawable.has_lesson_on_day)
                        containerView.sli_sunday.setTextColor(
                            ContextCompat.getColor(
                                containerView.context,
                                R.color.colorBackgroundPrimary
                            )
                        )
                    }
                    2 -> {
                        containerView.sli_monday.setBackgroundResource(R.drawable.has_lesson_on_day)
                        containerView.sli_monday.setTextColor(
                            ContextCompat.getColor(
                                containerView.context,
                                R.color.colorBackgroundPrimary
                            )
                        )
                    }
                    3 -> {
                        containerView.sli_tuesday.setBackgroundResource(R.drawable.has_lesson_on_day)
                        containerView.sli_tuesday.setTextColor(
                            ContextCompat.getColor(
                                containerView.context,
                                R.color.colorBackgroundPrimary
                            )
                        )
                    }
                    4 -> {
                        containerView.sli_wednesday.setBackgroundResource(R.drawable.has_lesson_on_day)
                        containerView.sli_wednesday.setTextColor(
                            ContextCompat.getColor(
                                containerView.context,
                                R.color.colorBackgroundPrimary
                            )
                        )
                    }
                    5 -> {
                        containerView.sli_thursday.setBackgroundResource(R.drawable.has_lesson_on_day)
                        containerView.sli_thursday.setTextColor(
                            ContextCompat.getColor(
                                containerView.context,
                                R.color.colorBackgroundPrimary
                            )
                        )
                    }
                    6 -> {
                        containerView.sli_friday.setBackgroundResource(R.drawable.has_lesson_on_day)
                        containerView.sli_friday.setTextColor(
                            ContextCompat.getColor(
                                containerView.context,
                                R.color.colorBackgroundPrimary
                            )
                        )
                    }
                    7 -> {
                        containerView.sli_saturday.setBackgroundResource(R.drawable.has_lesson_on_day)
                        containerView.sli_saturday.setTextColor(
                            ContextCompat.getColor(
                                containerView.context,
                                R.color.colorBackgroundPrimary
                            )
                        )
                    }
                }
            }

            containerView.sli_linearlayout.setOnClickListener {
                listener.onSubjectClick(data)
            }

        }

        private fun setViewToDefault() {
            containerView.sli_sunday.setBackgroundResource(R.drawable.has_no_lesson_on_day)
            containerView.sli_sunday.setTextColor(
                ContextCompat.getColor(
                    containerView.context,
                    R.color.colorTextPrimary
                )
            )
            containerView.sli_monday.setBackgroundResource(R.drawable.has_no_lesson_on_day)
            containerView.sli_monday.setTextColor(
                ContextCompat.getColor(
                    containerView.context,
                    R.color.colorTextPrimary
                )
            )
            containerView.sli_tuesday.setBackgroundResource(R.drawable.has_no_lesson_on_day)
            containerView.sli_tuesday.setTextColor(
                ContextCompat.getColor(
                    containerView.context,
                    R.color.colorTextPrimary
                )
            )
            containerView.sli_wednesday.setBackgroundResource(R.drawable.has_no_lesson_on_day)
            containerView.sli_wednesday.setTextColor(
                ContextCompat.getColor(
                    containerView.context,
                    R.color.colorTextPrimary
                )
            )
            containerView.sli_thursday.setBackgroundResource(R.drawable.has_no_lesson_on_day)
            containerView.sli_thursday.setTextColor(
                ContextCompat.getColor(
                    containerView.context,
                    R.color.colorTextPrimary
                )
            )
            containerView.sli_friday.setBackgroundResource(R.drawable.has_no_lesson_on_day)
            containerView.sli_friday.setTextColor(
                ContextCompat.getColor(
                    containerView.context,
                    R.color.colorTextPrimary
                )
            )
            containerView.sli_saturday.setBackgroundResource(R.drawable.has_no_lesson_on_day)
            containerView.sli_saturday.setTextColor(
                ContextCompat.getColor(
                    containerView.context,
                    R.color.colorTextPrimary
                )
            )
        }

    }
}