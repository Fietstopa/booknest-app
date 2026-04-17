package git.pef.mendelu.cz.booknest.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPrefsTest {
    @Test
    fun firstRun_defaultsToTrue_thenCanBeSetFalse() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val initial = UserPrefs.isFirstRun(context).first()
        assertTrue(initial)

        UserPrefs.setFirstRunComplete(context)
        val updated = UserPrefs.isFirstRun(context).first()
        assertFalse(updated)
    }
}
