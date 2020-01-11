package app.milanherke.mystudiez

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    // Test if the database is created correctly when it wasn't already
    @Test
    fun onCreate() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("MyStudiez.db")
        val db = AppDatabase.getInstance(context).readableDatabase
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table';", null)

        assertEquals(cursor.count, 5)
    }
}