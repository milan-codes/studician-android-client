package app.milanherke.mystudiez

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.milanherke.mystudiez.ExamsRecyclerViewAdapter.OnExamClickListener
import app.milanherke.mystudiez.Fragments.OVERVIEW
import app.milanherke.mystudiez.Fragments.SUBJECT_DETAILS
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.details_list_item.view.*

// Each constant value represents a view type
private const val ANY_NOT_EMPTY = 0
private const val EXAMS_EMPTY = 1
private const val EXAM_DETAILS_EMPTY = 2
private const val OVERVIEW_EMPTY = 3

/**
 * A [RecyclerView.Adapter] subclass.
 * This class serves as an adapter for RecyclerViews
 * which were created to display exams.
 *
 * @property examsList An [ArrayList] containing all [Exam] objects to display
 * @property listener Fragments that use this class must implement [OnExamClickListener]
 * @property usedIn The fragment where an object of this class has been created
 * @property listOfSubjects A [MutableMap], whose key is a SubjectID and value is a [Subject] object
 */
class ExamsRecyclerViewAdapter(
    private var examsList: ArrayList<Exam>?,
    private val listener: OnExamClickListener,
    private val usedIn: Fragments,
    private var listOfSubjects: MutableMap<String, Subject>? = null
) :
    RecyclerView.Adapter<ExamsRecyclerViewAdapter.ViewHolder>() {

    /**
     * This interface must be implemented by activities/fragments that contain
     * this RecyclerViewAdapter to allow an interaction in this class to be
     * communicated to the activity/fragment.
     */
    interface OnExamClickListener {
        fun onExamClickListener(exam: Exam)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            OVERVIEW_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_exams_overview, parent, false)
                EmptyExamViewHolder(view)
            }
            EXAMS_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_exams_for_any_subject, parent, false)
                EmptyExamViewHolder(view)
            }
            EXAM_DETAILS_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_exam_list_item, parent, false)
                EmptyExamViewHolder(view)
            }
            ANY_NOT_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.details_list_item, parent, false)
                ExamViewHolder(view)
            }
            else -> throw IllegalStateException("Couldn't recognise the view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = examsList
        when (getItemViewType(position)) {
            EXAM_DETAILS_EMPTY, EXAMS_EMPTY, OVERVIEW_EMPTY -> {
                // We are not putting any data into the empty view, therefore we do not need to do anything here
            }
            ANY_NOT_EMPTY -> {
                if (list != null) {
                    val exam = list[position]
                    holder.bind(exam)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val list = examsList
        return if (list == null || list.size == 0) 1 else list.size
    }

    override fun getItemViewType(position: Int): Int {
        val list = examsList
        return if (usedIn != SUBJECT_DETAILS && (list == null || list.size == 0)) {
            if (usedIn == OVERVIEW) {
                OVERVIEW_EMPTY
            } else {
                EXAMS_EMPTY
            }
        } else if (usedIn == SUBJECT_DETAILS && (list == null || list.size == 0)) {
            EXAM_DETAILS_EMPTY
        } else {
            ANY_NOT_EMPTY
        }
    }

    /**
     * Swaps in a new [ArrayList], containing [Exam] objects
     *
     * @param newList New list containing exams
     * @return Returns the previously set list, or null if there wasn't one
     */
    fun swapExamsList(newList: ArrayList<Exam>?): ArrayList<Exam>? {
        if (newList === examsList) {
            return null
        }
        val numItems = itemCount
        val oldList = examsList
        examsList = newList
        if (newList != null) {
            //notifying the observers
            notifyDataSetChanged()
        } else {
            //notifying the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldList
    }

    /**
     * Swaps in a new [MutableMap], containing [Subject] objects
     *
     * @param newMap New map containing subjects
     */
    fun swapSubjectsMap(newMap: MutableMap<String, Subject>) {
        listOfSubjects = newMap
    }

    open class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        open fun bind(exam: Exam) {}
    }

    private inner class ExamViewHolder(override val containerView: View) :
        ViewHolder(containerView) {

        override fun bind(exam: Exam) {
            // If recycler view is used in SubjectDetailsFragment
            // Meaning we do not have to load nor display the subject details
            if (usedIn == SUBJECT_DETAILS) {
                containerView.details_list_title.text = exam.name
                containerView.details_list_header1.text = CalendarUtils.dateToString(exam.date, false)
                containerView.details_list_header2.visibility = View.GONE
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
                    val subject: Subject? = listOfSubjects[exam.subjectId]
                    if (subject != null) {
                        if (usedIn == OVERVIEW) {
                            containerView.details_list_title.text = exam.name
                            containerView.details_list_header1.text = subject.name
                            containerView.details_list_header2.visibility = View.GONE
                        } else {
                            containerView.details_list_title.text = exam.name
                            containerView.details_list_header1.text = subject.name
                            containerView.details_list_header2.text = CalendarUtils.dateToString(exam.date, false)
                        }

                        //We're creating a clone drawable because we do not want to affect other instances of the original drawable
                        val clone = containerView.resources.getDrawable(R.drawable.placeholder_circle, null).mutatedClone()
                        clone.setColor(subject.colorCode, containerView.context)
                        containerView.details_list_subject_indicator.setImageDrawable(clone)
                    }
                }
            }

            containerView.details_list_container.setOnClickListener {
                listener.onExamClickListener(exam)
            }
        }
    }

    // We do not need to override the bind method since we're not putting any data into the empty view
    private class EmptyExamViewHolder(override val containerView: View) : ViewHolder(containerView)

}