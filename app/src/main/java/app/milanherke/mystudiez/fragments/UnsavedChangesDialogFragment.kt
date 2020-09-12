package app.milanherke.mystudiez.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import app.milanherke.mystudiez.R

class UnsavedChangesDialogFragment(private val listener: DialogInteractions) : DialogFragment() {

    interface DialogInteractions {
        fun onPositiveBtnPressed()
        fun onNegativeBtnPressed()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder
                .setMessage(R.string.changes_dialog_title)
                .setPositiveButton(R.string.changes_dialog_continue) { _, _ ->
                    listener.onPositiveBtnPressed()
                }
                .setNegativeButton(R.string.changes_dialog_back) { _, _ ->
                    listener.onNegativeBtnPressed()
                    dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}