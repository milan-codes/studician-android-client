package app.milanherke.mystudiez

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.milanherke.mystudiez.SubjectsRecyclerViewAdapter.OnSubjectClickListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.subject_list_items.view.*

// Each constant value represents a view type
private const val VIEW_TYPE_NOT_EMPTY = 0
private const val VIEW_TYPE_EMPTY = 1

/**
 * A [RecyclerView.Adapter] subclass.
 * This class serves as an adapter for RecyclerViews
 * which were created to display lessons.
 *
 * @property subjectsList An [ArrayList] containing all [Subject] objects to display
 * @property lessonsList An [ArrayList] containing lessons of subjects
 * @property listener Fragments that use this class must implement [OnSubjectClickListener]
 */
class SubjectsRecyclerViewAdapter(
    private var subjectsList: ArrayList<Subject>?,
    private var lessonsList: ArrayList<Lesson>?,
    private val listener: OnSubjectClickListener
) : RecyclerView.Adapter<SubjectsRecyclerViewAdapter.ViewHolder>() {

    /**
     * This interface must be implemented by activities/fragments that contain
     * this RecyclerViewAdapter to allow an interaction in this class to be
     * communicated to the activity/fragment.
     */
    interface OnSubjectClickListener {
        fun onSubjectClick(subject: Subject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_subject_list_item, parent, false)
                EmptySubjectViewHolder(view)
            }
            VIEW_TYPE_NOT_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.subject_list_items, parent, false)
                SubjectViewHolder(view)
            }
            else -> throw IllegalStateException("Couldn't recognise the view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = subjectsList
        val lessonsList = lessonsList
        when (getItemViewType(position)) {
            VIEW_TYPE_EMPTY -> {
                // We are not putting any data into the empty view, therefore we do not need to do anything here
            }
            VIEW_TYPE_NOT_EMPTY -> {
                if (list != null && lessonsList != null) {
                    val subject = list[position]
                    val days = arrayListOf<Int>()
                    for (lesson in lessonsList) {
                        if (lesson.subjectId == subject.id) {
                            days.add(lesson.day)
                        }
                    }
                    holder.bind(subject, days, listener)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val list = subjectsList
        return if (list == null || list.size == 0) 1 else list.size
    }

    override fun getItemViewType(position: Int): Int {
        val list = subjectsList
        return if (list == null || list.size == 0) VIEW_TYPE_EMPTY else VIEW_TYPE_NOT_EMPTY
    }

    /**
     * Swap in a new [ArrayList], containing [Subject] objects
     *
     * @param newList New list containing subjects
     * @return Returns the previously used list, or null if there wasn't one
     */
    fun swapSubjectsList(newList: ArrayList<Subject>?): ArrayList<Subject>? {
        if (newList === subjectsList) {
            return null
        }
        val numItems = itemCount
        val oldList = subjectsList
        subjectsList = newList
        if (newList != null) {
            //notify the observers
            notifyDataSetChanged()
        } else {
            //notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldList
    }

    /**
     * Swap in a new [ArrayList], containing [Lesson] objects
     *
     * @param newList New list containing lessons
     * @return Returns the previously set list, or null if there wasn't one
     */
    fun swapLessonsList(newList: ArrayList<Lesson>?): ArrayList<Lesson>? {
        if (newList === lessonsList) {
            return null
        }
        val list = lessonsList
        val numItems = if (list == null || list.size == 0) 0 else list.size
        val oldList = lessonsList
        lessonsList = newList
        if (newList != null) {
            notifyDataSetChanged()
        } else {
            //notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldList
    }

    open class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        open fun bind(
            subject: Subject,
            days: ArrayList<Int>,
            listener: OnSubjectClickListener
        ) {
        }
    }

    private class SubjectViewHolder(override val containerView: View) : ViewHolder(containerView) {

        override fun bind(
            subject: Subject,
            days: ArrayList<Int>,
            listener: OnSubjectClickListener
        ) {
            containerView.sli_name.text = subject.name
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
                listener.onSubjectClick(subject)
            }
        }

    }

    // We do not need to override the bind method since we're not putting any data into the empty view
    private class EmptySubjectViewHolder(override val containerView: View) :
        ViewHolder(containerView)
}