package app.milanherke.mystudiez.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import app.milanherke.mystudiez.*
import app.milanherke.mystudiez.Fragments.*
import app.milanherke.mystudiez.adapters.LessonsRecyclerViewAdapter.OnLessonClickListener
import kotlinx.android.synthetic.main.details_list_item.view.*
import kotlin.collections.ArrayList

/**
 * A [BaseAdapter] subclass.
 * This class serves as an adapter for RecyclerViews
 * which were created to display lessons.
 *
 * @property lessons [ArrayList] that contains the [Lesson] objects to display
 * @property listener Classes that use this adapter must implement [OnLessonClickListener]
 * @property usedIn Class where an object of this adapter has been created
 * @property subjects A [MutableMap], whose key is a SubjectID and value is a [Subject] object
 */
class LessonsRecyclerViewAdapter(
    private val usedIn: Fragments,
    private val listener: OnLessonClickListener,
    private var lessons: ArrayList<Lesson> = arrayListOf(),
    private var subjects: MutableMap<String, Subject> = mutableMapOf()
) : BaseAdapter<Lesson>(lessons) {

    /**
     * This interface must be implemented by activities/fragments that contain
     * [LessonsRecyclerViewAdapter] to allow an interaction in this class to be
     * communicated to the activity/fragment.
     */
    interface OnLessonClickListener {
        fun onLessonClick(lesson: Lesson)
    }

    override fun setViewHolder(parent: ViewGroup): BaseViewHolder<Lesson> {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.details_list_item, parent, false)
        return LessonsViewHolder(view)
    }

    /**
     * Changes the data set. Calls [BaseAdapter.swapDataList].
     * Swaps in a new [ArrayList] that contains [Lesson] objects.
     *
     * @param lessons An [ArrayList] that contains [Lesson] objects
     */
    fun swapLessons(lessons: ArrayList<Lesson>) {
        super.swapDataList(lessons)
    }

    /**
     * Swaps in a new [MutableMap], that contains [Subject] objects,
     * which are needed in [LessonsViewHolder].
     *
     * @param subjects A new map that contains subjects
     */
    fun swapSubjects(subjects: MutableMap<String, Subject>) {
        // Secondary data list, which contains no useful information for the BaseAdapter, it is only when binding a lesson
        this.subjects = subjects
        notifyDataSetChanged()
    }

    private inner class LessonsViewHolder(override val containerView: View) :
        BaseViewHolder<Lesson>(containerView) {

        override fun bind(data: Lesson) {
            when (usedIn) {
                SUBJECT_DETAILS -> {
                    // If adapter is used in SubjectDetailsFragment, we do not need to display information about the subject
                    containerView.details_list_title.text =
                        CalendarUtils.getDayFromNumberOfDay(
                            data.day,
                            containerView.context
                        )
                    containerView.details_list_header1.text = containerView.resources.getString(
                        R.string.details_subject_item_time,
                        data.starts,
                        data.ends
                    )
                    containerView.details_list_header2.text = data.location
                    containerView.details_list_subject_indicator.visibility = View.GONE

                    // Setting new constraints because subjectIndicator's visibility is set to gone
                    val params =
                        containerView.details_list_title.layoutParams as ConstraintLayout.LayoutParams
                    params.topToBottom =
                        R.id.details_list_header1
                    params.leftToLeft =
                        R.id.details_list_constraint
                    params.bottomToBottom =
                        R.id.details_list_constraint
                    params.marginStart = super.setMargin(16F)
                    params.bottomMargin = super.setMargin(16F)
                    containerView.details_list_title.layoutParams = params
                }
                else -> {
                    // If adapter is used anywhere else, we need to display subject details
                    val subject: Subject? = subjects[data.subjectId]
                    if (subject != null) {
                        containerView.details_list_title.text = subject.name
                        containerView.details_list_header1.text = containerView.resources.getString(
                            R.string.details_subject_item_time,
                            data.starts,
                            data.ends
                        )
                        containerView.details_list_header2.text = data.location

                        //Creating a clone drawable to avoid affecting other instances of the original drawable
                        val clone =
                            containerView.resources.getDrawable(R.drawable.placeholder_circle, null)
                                .mutatedClone()
                        clone.setColor(subject.colorCode, containerView.context)
                        containerView.details_list_subject_indicator.setImageDrawable(clone)
                    }


                }
            }
            containerView.details_list_container.setOnClickListener {
                listener.onLessonClick(data)
            }
        }
    }
}