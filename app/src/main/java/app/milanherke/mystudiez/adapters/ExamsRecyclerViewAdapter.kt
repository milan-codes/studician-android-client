package app.milanherke.mystudiez.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.milanherke.mystudiez.*
import app.milanherke.mystudiez.Fragments.OVERVIEW
import app.milanherke.mystudiez.Fragments.SUBJECT_DETAILS
import app.milanherke.mystudiez.adapters.ExamsRecyclerViewAdapter.OnExamClickListener
import app.milanherke.mystudiez.generic.BaseAdapter
import app.milanherke.mystudiez.generic.BaseViewHolder
import app.milanherke.mystudiez.models.Exam
import app.milanherke.mystudiez.models.Subject
import app.milanherke.mystudiez.utils.CalendarUtils
import kotlinx.android.synthetic.main.details_list_item.view.*

/**
 * A [RecyclerView.Adapter] subclass.
 * This class serves as an adapter for RecyclerViews
 * which were created to display exams.
 *
 * @property usedIn The fragment where an object of this class has been created
 * @property listener Fragments that use this class must implement [OnExamClickListener]
 * @property exams An [ArrayList] containing all [Exam] objects to display
 * @property subjects A [MutableMap], whose key is a SubjectID and value is a [Subject] object
 */
class ExamsRecyclerViewAdapter(
    private val usedIn: Fragments,
    private val listener: OnExamClickListener,
    private var exams: ArrayList<Exam> = arrayListOf(),
    private var subjects: MutableMap<String, Subject> = mutableMapOf()
) :
    BaseAdapter<Exam>(exams) {

    /**
     * This interface must be implemented by activities/fragments that contain
     * this RecyclerViewAdapter to allow an interaction in this class to be
     * communicated to the activity/fragment.
     */
    interface OnExamClickListener {
        fun onExamClickListener(exam: Exam)
    }

    override fun setViewHolder(parent: ViewGroup): BaseViewHolder<Exam> {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.details_list_item, parent, false)
        return ExamViewHolder(view)
    }

    /**
     * Changes the data set. Calls [BaseAdapter.swapDataList].
     * Swaps in a new [ArrayList] that contains [Exam] objects.
     *
     * @param exams An [ArrayList] that contains [Exam] objects
     */
    fun swapExams(exams: ArrayList<Exam>) {
        super.swapDataList(exams)
    }

    /**
     * Swaps in a new [MutableMap], that contains [Subject] objects,
     * which are needed in [ExamViewHolder].
     *
     * @param subjects A [MutableMap]<[String], [Subject]>
     */
    fun swapSubjects(subjects: MutableMap<String, Subject>) {
        // Secondary data list, which contains no useful information for the BaseAdapter, it is only needed when binding an exam
        this.subjects = subjects
        notifyDataSetChanged()
    }

    private inner class ExamViewHolder(override val containerView: View) :
        BaseViewHolder<Exam>(containerView) {

        override fun bind(data: Exam) {
            if (usedIn == SUBJECT_DETAILS) {
                // If adapter is used in SubjectDetailsFragment, we do not need to display information about the subject
                containerView.details_list_title.text = data.name
                containerView.details_list_header1.text =
                    CalendarUtils.dateToString(data.date, false)
                containerView.details_list_header2.visibility = View.GONE
                containerView.details_list_subject_indicator.visibility = View.GONE

                // Setting new constraints because subjectIndicator's visibility is set to gone
                val params =
                    containerView.details_list_title.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = R.id.details_list_header1
                params.leftToLeft = R.id.details_list_constraint
                params.bottomToBottom = R.id.details_list_constraint
                params.marginStart = super.setMargin(16F)
                params.bottomMargin = super.setMargin(16F)
                containerView.details_list_title.layoutParams = params
            } else {
                // If adapter is used anywhere else, we need to display subject details
                val subjects = subjects
                val subject: Subject? = subjects[data.subjectId]
                if (subject != null) {
                    if (usedIn == OVERVIEW) {
                        containerView.details_list_title.text = data.name
                        containerView.details_list_header1.text = subject.name
                        containerView.details_list_header2.visibility = View.GONE
                    } else {
                        containerView.details_list_title.text = data.name
                        containerView.details_list_header1.text = subject.name
                        containerView.details_list_header2.text =
                            CalendarUtils.dateToString(data.date, false)
                    }

                    //We're creating a clone drawable because we do not want to affect other instances of the original drawable
                    val clone =
                        containerView.resources.getDrawable(R.drawable.placeholder_circle, null)
                            .mutatedClone()
                    clone.setColor(subject.colorCode, containerView.context)
                    containerView.details_list_subject_indicator.setImageDrawable(clone)
                }
            }

            containerView.details_list_container.setOnClickListener {
                listener.onExamClickListener(data)
            }
        }
    }
}