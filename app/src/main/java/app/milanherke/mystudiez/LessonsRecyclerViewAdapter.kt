package app.milanherke.mystudiez

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.milanherke.mystudiez.Fragments.OVERVIEW
import app.milanherke.mystudiez.Fragments.SUBJECT_DETAILS
import app.milanherke.mystudiez.LessonsRecyclerViewAdapter.OnLessonClickListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.details_list_item.view.*

// Each constant value represents a view type
private const val ANY_NOT_EMPTY = 0
private const val SUBJECT_DETAILS_EMPTY = 1
private const val PLACEHOLDER = 2
private const val OVERVIEW_EMPTY = 3

/**
 * A [RecyclerView.Adapter] subclass.
 * This class serves as an adapter for RecyclerViews
 * which were created to display lessons.
 *
 * @property lessonsList An [ArrayList] containing all [Lesson] objects to display
 * @property listener Fragments that use this class must implement [OnLessonClickListener]
 * @property usedIn The fragment where an object of this class has been created
 * @property listOfSubjects A [MutableMap], whose key is a SubjectID and value is a [Subject] object
 */
class LessonsRecyclerViewAdapter(
    private var lessonsList: ArrayList<Lesson>?,
    private val listener: OnLessonClickListener,
    private val usedIn: Fragments,
    private var listOfSubjects: MutableMap<String, Subject>? = null
) :
    RecyclerView.Adapter<LessonsRecyclerViewAdapter.ViewHolder>() {

    /**
     * This interface must be implemented by activities/fragments that contain
     * this RecyclerViewAdapter to allow an interaction in this class to be
     * communicated to the activity/fragment.
     */
    interface OnLessonClickListener {
        fun onLessonClick(lesson: Lesson)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            OVERVIEW_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_lessons_overview, parent, false)
                EmptyLessonViewHolder(view)
            }
            SUBJECT_DETAILS_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_lesson_list_item, parent, false)
                EmptyLessonViewHolder(view)
            }
            ANY_NOT_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.details_list_item, parent, false)
                LessonViewHolder(view)
            }
            PLACEHOLDER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_placeholder, parent, false)
                LessonViewHolder(view)
            }
            else -> throw IllegalStateException("Couldn't recognise the view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lessonsList
        when (getItemViewType(position)) {
            PLACEHOLDER, SUBJECT_DETAILS_EMPTY, OVERVIEW_EMPTY -> {
                // We are not putting any data into the empty view, therefore we do not need to do anything here
            }
            ANY_NOT_EMPTY -> {
                if (list != null) {
                    val lesson = list[position]
                    holder.bind(lesson)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val list = lessonsList
        return if (list == null || list.size == 0) 1 else list.size
    }

    override fun getItemViewType(position: Int): Int {
        val list = lessonsList
        return if (list == null) {
            PLACEHOLDER
        } else if (list.size == 0) {
            if (usedIn == OVERVIEW) {
                OVERVIEW_EMPTY
            } else {
                SUBJECT_DETAILS_EMPTY
            }
        } else {
            ANY_NOT_EMPTY
        }
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
        val numItems = itemCount
        val oldList = lessonsList
        lessonsList = newList
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
     * Swaps in a new [MutableMap], containing [Subject] objects
     *
     * @param newMap New map containing subjects
     */
    fun swapSubjectMap(newMap: MutableMap<String, Subject>) {
        listOfSubjects = newMap
    }

    open class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        open fun bind(lesson: Lesson) {}
    }

    private inner class LessonViewHolder(override val containerView: View) :
        ViewHolder(containerView) {

        override fun bind(lesson: Lesson) {
            // If recycler view is used in SubjectDetailsFragment
            // Meaning we do not have to load nor display the subject details
            if (usedIn == SUBJECT_DETAILS) {
                containerView.details_list_title.text =
                    CalendarUtils.getDayFromNumberOfDay(lesson.day, containerView.context)
                containerView.details_list_header1.text = containerView.resources.getString(
                    R.string.details_subject_item_time,
                    lesson.starts,
                    lesson.ends
                )
                containerView.details_list_header2.text = lesson.location
                containerView.details_list_subject_indicator.visibility = View.GONE

                // Setting new constraints because subjectIndicator's visibility is set to gone
                val params =
                    containerView.details_list_title.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = R.id.details_list_header1
                params.leftToLeft = R.id.details_list_constraint
                params.bottomToBottom = R.id.details_list_constraint
                params.marginStart = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16F,
                    containerView.context.resources.displayMetrics
                ).toInt()
                params.bottomMargin = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16F,
                    containerView.context.resources.displayMetrics
                ).toInt()
                containerView.details_list_title.layoutParams = params
            } else {
                val listOfSubjects = listOfSubjects
                if (listOfSubjects != null) {
                    val subject: Subject? = listOfSubjects[lesson.subjectId]
                    if (subject != null) {
                        containerView.details_list_title.text = subject.name
                        containerView.details_list_header1.text = containerView.resources.getString(
                            R.string.details_subject_item_time,
                            lesson.starts,
                            lesson.ends
                        )
                        containerView.details_list_header2.text = lesson.location

                        //Creating a clone drawable because we do not want to affect other instances of the original drawable
                        val clone =
                            containerView.resources.getDrawable(R.drawable.placeholder_circle, null)
                                .mutatedClone()
                        clone.setColor(subject.colorCode, containerView.context)
                        containerView.details_list_subject_indicator.setImageDrawable(clone)
                    }
                }
            }

            containerView.details_list_container.setOnClickListener {
                listener.onLessonClick(lesson)
            }
        }

    }

    // We do not need to override the bind method since we're not putting any data into the empty view
    private class EmptyLessonViewHolder(override val containerView: View) :
        ViewHolder(containerView)
}