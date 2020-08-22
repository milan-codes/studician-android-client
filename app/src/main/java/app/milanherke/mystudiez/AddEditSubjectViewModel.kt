package app.milanherke.mystudiez

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.milanherke.mystudiez.models.Subject
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [AndroidViewModel] subclass.
 * This ViewModel was created to save/update [Subject] objects in the database
 * And belongs to [AddEditSubjectActivity]
 */
class AddEditSubjectViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Use this function to save/update a [Subject].
     *
     * @param subject Subject that the user wants to save
     * @return Saved subject
     */
    fun saveSubject(subject: Subject): Subject {
        GlobalScope.launch {
            val database = Firebase.database
            // If the subject's ID is empty, we're creating a new subject, otherwise, we're updating an existing one
            if (subject.id.isEmpty()) {
                val key = database.getReference("subjects/${FirebaseUtils.getUserId()}").push().key
                if (key != null) {
                    subject.id = key
                    database.getReference("subjects/${FirebaseUtils.getUserId()}/$key")
                        .setValue(subject)
                }
            } else {
                database.getReference("subjects/${FirebaseUtils.getUserId()}/${subject.id}")
                    .setValue(subject)
            }
        }
        return subject
    }

}