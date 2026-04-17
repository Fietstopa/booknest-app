package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.utils

import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MapUtilsTest {
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
    fun formatDistance_underOneKm_formatsMeters() {
        assertEquals("250m", formatDistance(250f))
        assertEquals("999m", formatDistance(999.4f))
    }

    @Test
    fun formatDistance_overOneKm_formatsKilometers() {
        assertEquals("1.0Km", formatDistance(1000f))
        assertEquals("2.5Km", formatDistance(2500f))
    }
}
