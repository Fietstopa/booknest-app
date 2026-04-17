package git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.utils

import java.util.Calendar
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HistoryDateTest {
    private var originalLocale: Locale? = null

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        originalLocale?.let(Locale::setDefault)
    }

    @Test
    fun formatHistoryDate_null_returnsUnknown() {
        assertEquals("Unknown", formatHistoryDate(null, "Unknown"))
    }

    @Test
    fun formatHistoryDate_formatsDate() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            set(2024, Calendar.JULY, 12, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        assertEquals("12.7.2024", formatHistoryDate(calendar.timeInMillis, "Unknown"))
    }
}
