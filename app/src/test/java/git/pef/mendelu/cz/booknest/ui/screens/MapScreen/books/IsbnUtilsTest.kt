package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.books

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IsbnUtilsTest {
    @Test
    fun isValidIsbn13_acceptsValid() {
        assertTrue(isValidIsbn13("9780306406157"))
    }

    @Test
    fun isValidIsbn13_rejectsInvalid() {
        assertFalse(isValidIsbn13("9780306406158"))
        assertFalse(isValidIsbn13("97803064X6157"))
    }

    @Test
    fun isValidIsbn10_acceptsValid() {
        assertTrue(isValidIsbn10("0306406152"))
        assertTrue(isValidIsbn10("030640615X"))
    }

    @Test
    fun isValidIsbn10_rejectsInvalid() {
        assertFalse(isValidIsbn10("0306406153"))
        assertFalse(isValidIsbn10("03064061XX"))
    }

    @Test
    fun extractIsbnFromText_prefers13Digit() {
        val text = "ISBN 978-0-306-40615-7 and 0-306-40615-2"
        assertEquals("9780306406157", extractIsbnFromText(text))
    }

    @Test
    fun extractIsbnFromText_returnsNullWhenMissing() {
        assertNull(extractIsbnFromText("No isbn here"))
    }
}
