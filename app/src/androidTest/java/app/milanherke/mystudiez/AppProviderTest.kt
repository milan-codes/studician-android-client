package app.milanherke.mystudiez

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.test.ProviderTestCase2
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppProviderTest : ProviderTestCase2<AppProvider>(AppProvider::class.java, CONTENT_AUTHORITY) {

    @Before
    fun settingUp() {
        setUp()
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun shouldGetSubjectItemMIMEType() {
        assertEquals(
            SubjectsContract.CONTENT_ITEM_TYPE,
            context.contentResolver.getType(Uri.parse("content://$CONTENT_AUTHORITY/${SubjectsContract.TABLE_NAME}/5"))
        )
    }

    @Test
    fun shouldInsertQueryUpdateAndDelete() {
        // Insert
        val valuesToInsert = ContentValues().apply {
            put(SubjectsContract.Columns.SUBJECT_NAME, "Subject added from test")
            put(SubjectsContract.Columns.SUBJECT_TEACHER, "Teacher 1")
            put(SubjectsContract.Columns.SUBJECT_COLORCODE, "Color code 1")
        }
        val insertedUri =
            context.contentResolver.insert(SubjectsContract.CONTENT_URI, valuesToInsert)
        val resultingId = SubjectsContract.getId(insertedUri!!)
        assertTrue(resultingId > 0L)

        // Query
        val cursor = context.contentResolver.query(
            SubjectsContract.CONTENT_URI,
            null,
            "${SubjectsContract.Columns.SUBJECT_NAME} = ?",
            arrayOf("Subject added from test"),
            null
        )
        assertNotNull(cursor)

        // Update
        val valuesToUpdate = ContentValues().apply {
            put(SubjectsContract.Columns.SUBJECT_NAME, "Updated, you can delete me now")
            put(SubjectsContract.Columns.SUBJECT_TEACHER, "Teacher 1")
            put(SubjectsContract.Columns.SUBJECT_COLORCODE, "Color code 1")
        }
        val updatedUri = context.contentResolver.update(
            SubjectsContract.CONTENT_URI,
            valuesToUpdate,
            "${SubjectsContract.Columns.SUBJECT_NAME} = ?",
            arrayOf("Subject added from test")
        )
        assertEquals(1, updatedUri)

        // Delete
        val uriToDelete = SubjectsContract.buildUriFromId(resultingId)
        val deletedUri = context.contentResolver.delete(uriToDelete, null, null)
        assertEquals(1, deletedUri)
    }
}