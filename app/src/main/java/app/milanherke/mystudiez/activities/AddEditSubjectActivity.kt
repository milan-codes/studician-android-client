package app.milanherke.mystudiez.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProviders
import app.milanherke.mystudiez.*
import app.milanherke.mystudiez.ActivityUtils.Companion.ACTIVITY_NAME_BUNDLE_ID
import app.milanherke.mystudiez.ActivityUtils.Companion.FRAGMENT_TO_LOAD_BUNDLE_ID
import app.milanherke.mystudiez.ActivityUtils.Companion.SUBJECT_PARAM_BUNDLE_ID
import app.milanherke.mystudiez.FragmentBackStack
import app.milanherke.mystudiez.fragments.SubjectDetailsFragment
import app.milanherke.mystudiez.fragments.SubjectsFragment
import app.milanherke.mystudiez.fragments.UnsavedChangesDialogFragment
import app.milanherke.mystudiez.models.Subject
import app.milanherke.mystudiez.viewmodels.activities.AddEditSubjectViewModel
import kotlinx.android.synthetic.main.activity_add_edit_subject.*
import kotlinx.android.synthetic.main.content_add_edit_subject.*

/**
 * A simple [AppCompatActivity] subclass.
 * The purpose of this activity is to add or edit subjects.
 */
class AddEditSubjectActivity : AppCompatActivity(), UnsavedChangesDialogFragment.DialogInteractions {

    private var subject: Subject? = null
    private var selectedSubjectColor: Int = -1
    private var subjectIsBeingEdited: Boolean = false
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(AddEditSubjectViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_subject)

        // Getting intent and extras
        val intent = intent
        subject = intent.getParcelableExtra(SUBJECT_PARAM_BUNDLE_ID)

        // Creating a clone of the subject's color indicator circle
        // because we do not want to affect all of the drawable's instances
        // and setting default color
        createAndSetCloneColorIndicator()

        // Avoiding problems with smart cast
        val subject = subject

        if (subject == null) {
            // User wants to create a new subject
            activity_subject_toolbar.setTitle(R.string.add_new_subject_title)
            setSupportActionBar(activity_subject_toolbar)
            subjectIsBeingEdited = false
            selectedColor.visibility = View.INVISIBLE
        } else {
            // User wants to edit an existing subject
            activity_subject_toolbar.title =
                resources.getString(R.string.edit_subject_title, subject.name)
            setSupportActionBar(activity_subject_toolbar)
            subjectIsBeingEdited = true
            new_subject_name_value.setText(subject.name)
            new_subject_teacher_value.setText(subject.teacher)
            selectedColor.drawable.setColor(subject.colorCode, this)
            new_subject_color_btn.text = getColorName(subject.colorCode)
            selectedSubjectColor = subject.colorCode

        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // User must select a color for the subject
        new_subject_color_btn.setOnClickListener {
            showColorsPopUp(it)
        }

        new_subject_save_btn.setOnClickListener {
            saveSubject()
        }
    }
    override fun onStop() {
        super.onStop()
        FragmentBackStack.getInstance(this).pop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                pressedUpBtn()
            } else -> super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        pressedUpBtn()
    }

    override fun onPositiveBtnPressed() {
        openActivity()
    }

    override fun onNegativeBtnPressed() {
        // Dialog automatically gets dismissed in UnsavedChangesDialogFragment
    }

    companion object {
        const val TAG = "AddEditSubject"
    }

    private fun onSavePressed() {
        openActivity()
    }

    /**
     * [AddEditSubjectActivity] can return only to [SubjectsFragment] and [SubjectDetailsFragment].
     * It can be called only by the following fragments: [SubjectsFragment] (when adding new) and [SubjectDetailsFragment] (when editing an existing one).
     */
    private fun pressedUpBtn() {
        val dialog = UnsavedChangesDialogFragment(this)

        // If [subject] is not null, the user is editing an existing one
        if (subject != null) {
            if (requiredFieldsAreFilled()) {
                val newSubject = subjectFromUi()
                if (newSubject != subject) dialog.show(this.supportFragmentManager,
                    TAG
                ) else openActivity()
            } else openActivity()
        } else openActivity()
    }


    private fun openActivity() {
        when (val fragmentCalledFrom = FragmentBackStack.getInstance(this).peek()) {
            Fragments.SUBJECTS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID,
                    TAG
                )
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, SubjectsFragment.TAG)
                startActivity(intent)
            }
            Fragments.SUBJECT_DETAILS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ACTIVITY_NAME_BUNDLE_ID,
                    TAG
                )
                intent.putExtra(FRAGMENT_TO_LOAD_BUNDLE_ID, SubjectDetailsFragment.TAG)
                intent.putExtra(SUBJECT_PARAM_BUNDLE_ID, subject)
                startActivity(intent)
            }
            else -> throw IllegalStateException("onSaveSubjectClicked tries to load unrecognised fragment $fragmentCalledFrom")
        }
    }

    /**
     * Creates a new [Subject] object with the details to be saved, then
     * calls the [viewModel]'s saveSubject function to save it.
     */
    private fun saveSubject() {
        if (requiredFieldsAreFilled()) {
            val newSubject = subjectFromUi()
            if (newSubject != subject) {
                subject = viewModel.saveSubject(newSubject)
                onSavePressed()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.did_not_change),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.required_fields_are_not_filled),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * If we're creating a new [Subject], we set its ID to an empty string.
     * If we're updating a [Subject], we're not changing its ID.
     *
     * @return A [Subject] object created from UI
     */
    private fun subjectFromUi(): Subject {
        val subject = Subject(
            new_subject_name_value.text.toString(),
            new_subject_teacher_value.text.toString(),
            selectedSubjectColor
        )
        subject.id = this.subject?.id ?: ""
        return subject
    }

    /**
     * Simple function to check whether the required fields are filled.
     *
     * @return True if required fields are filled, otherwise false
     */
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

    /**
     * This function allows the user to choose a color from a pop up menu,
     * if a new subject is created.
     */
    private fun showColorsPopUp(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.color_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.color_option_red -> {
                    setColor(
                        R.color.subjectColorRed, this,
                        R.string.colorRedTitle
                    )
                }
                R.id.color_option_pink -> {
                    setColor(
                        R.color.subjectColorPink, this,
                        R.string.colorPinkTitle
                    )
                }
                R.id.color_option_purple -> {
                    setColor(
                        R.color.subjectColorPurple, this,
                        R.string.colorPurpleTitle
                    )
                }
                R.id.color_option_deep_purple -> {
                    setColor(
                        R.color.subjectColorDeepPurple,
                        this,
                        R.string.colorDeepPurpleTitle
                    )
                }
                R.id.color_option_indigo -> {
                    setColor(
                        R.color.subjectColorIndigo, this,
                        R.string.colorIndigoTitle
                    )
                }
                R.id.color_option_blue -> {
                    setColor(
                        R.color.subjectColorBlue, this,
                        R.string.colorBlueTitle
                    )
                }
                R.id.color_option_light_blue -> {
                    setColor(
                        R.color.subjectColorLightBlue,
                        this,
                        R.string.colorLightBlueTitle
                    )
                }
                R.id.color_option_cyan -> {
                    setColor(
                        R.color.subjectColorCyan, this,
                        R.string.colorCyanTitle
                    )
                }
                R.id.color_option_teal -> {
                    setColor(
                        R.color.subjectColorTeal, this,
                        R.string.colorTealTitle
                    )
                }
                R.id.color_option_green -> {
                    setColor(
                        R.color.subjectColorGreen, this,
                        R.string.colorGreenTitle
                    )
                }
                R.id.color_option_light_green -> {
                    setColor(
                        R.color.subjectColorLightGreen,
                        this,
                        R.string.colorLightGreenTitle
                    )
                }
                R.id.color_option_lime -> {
                    setColor(
                        R.color.subjectColorLime, this,
                        R.string.colorLimeTitle
                    )
                }
                R.id.color_option_yellow -> {
                    setColor(
                        R.color.subjectColorYellow, this,
                        R.string.colorYellowTitle
                    )
                }
                R.id.color_option_amber -> {
                    setColor(
                        R.color.subjectColorAmber, this,
                        R.string.colorAmberTitle
                    )
                }
                R.id.color_option_orange -> {
                    setColor(
                        R.color.subjectColorOrange,this,
                        R.string.colorOrangeTitle
                    )
                }
                R.id.color_option_deep_orange -> {
                    setColor(
                        R.color.subjectColorDeepOrange,
                        this,
                        R.string.colorDeepOrangeTitle
                    )
                }
                R.id.color_option_blue_gray -> {
                    setColor(
                        R.color.subjectColorBlueGray, this,
                        R.string.colorBlueGrayTitle
                    )
                }
            }
            true
        }
    }

    /**
     * This function takes in a color resource id as a parameter
     * and gives back its name.
     *
     * @param colorId Color resource id
     * @return The color's name
     */
    private fun getColorName(@ColorRes colorId: Int): String {
        return when (colorId) {
            R.color.subjectColorRed -> getString(
                R.string.colorRedTitle
            )
            R.color.subjectColorPink -> getString(
                R.string.colorPinkTitle
            )
            R.color.subjectColorPurple -> getString(
                R.string.colorPurpleTitle
            )
            R.color.subjectColorDeepPurple -> getString(
                R.string.colorDeepPurpleTitle
            )
            R.color.subjectColorIndigo -> getString(
                R.string.colorIndigoTitle
            )
            R.color.subjectColorBlue -> getString(
                R.string.colorBlueTitle
            )
            R.color.subjectColorLightBlue -> getString(
                R.string.colorLightBlueTitle
            )
            R.color.subjectColorCyan -> getString(
                R.string.colorCyanTitle
            )
            R.color.subjectColorTeal -> getString(
                R.string.colorTealTitle
            )
            R.color.subjectColorGreen -> getString(
                R.string.colorGreenTitle
            )
            R.color.subjectColorLightGreen -> getString(
                R.string.colorLightGreenTitle
            )
            R.color.subjectColorLime -> getString(
                R.string.colorLimeTitle
            )
            R.color.subjectColorYellow -> getString(
                R.string.colorYellowTitle
            )
            R.color.subjectColorAmber -> getString(
                R.string.colorAmberTitle
            )
            R.color.subjectColorOrange -> getString(
                R.string.colorOrangeTitle
            )
            R.color.subjectColorDeepOrange -> getString(
                R.string.colorDeepOrangeTitle
            )
            R.color.subjectColorBlueGray -> getString(
                R.string.colorBlueGrayTitle
            )
            else -> throw IllegalArgumentException("Subject has unrecognised color id")
        }
    }

    /**
     * This function creates a clone of [R.drawable.subject_indicator_circle]
     * because we do not want to affect all of the drawable's instances.
     */
    private fun createAndSetCloneColorIndicator() {
        val clone = selectedColor.drawable.mutatedClone()
        selectedColor.setImageDrawable(clone)
    }

    /**
     *
     * @param colorId Color resource id
     * @param context Context
     * @param colorName String resource id, the name of the color
     */
    private fun setColor(@ColorRes colorId: Int, context: Context, @StringRes colorName: Int) {
        selectedColor.drawable.setColor(colorId, context)
        new_subject_color_btn.setText(colorName)
        selectedSubjectColor = colorId
        if (selectedColor.visibility == View.INVISIBLE) selectedColor.visibility = View.VISIBLE
    }

}
