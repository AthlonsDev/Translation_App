import org.junit.Assert.*
import org.junit.Test

class StringUtilTest {
    @Test
    fun testReverseString() {
        val input = "hello"
        val expected = "olleh"

        val stringUtils = StringUtil()
        val actualUtil = stringutils.reverseString(input)

        assertEquals(expected, actualUtil)
    }
}