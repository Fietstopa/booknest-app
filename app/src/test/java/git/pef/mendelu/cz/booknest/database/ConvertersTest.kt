package git.pef.mendelu.cz.booknest.database

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun fromStringList_null_returnsNull() {
        assertEquals(null, converters.fromStringList(null))
    }

    @Test
    fun fromStringList_serializesList() {
        val result = converters.fromStringList(listOf("A", "B", "C"))
        assertEquals("A|B|C", result)
    }

    @Test
    fun toStringList_null_returnsEmptyList() {
        assertEquals(emptyList<String>(), converters.toStringList(null))
    }

    @Test
    fun toStringList_deserializesList() {
        val result = converters.toStringList("A,B,C")
        assertEquals(listOf("A,B,C"), result)
    }
}
