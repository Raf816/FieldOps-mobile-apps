package com.raf.fieldops.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InputSanitiserTest {

    @Test
    fun sanitiseShort_normalText_returnsUnchanged() {
        assertEquals("Fix broadband", InputSanitiser.sanitiseShort("Fix broadband"))
    }

    @Test
    fun sanitiseShort_leadingTrailingSpaces_trimmed() {
        assertEquals("Fix broadband", InputSanitiser.sanitiseShort("  Fix broadband  "))
    }

    @Test
    fun sanitiseShort_multipleSpaces_collapsedToOne() {
        assertEquals("Fix the broadband", InputSanitiser.sanitiseShort("Fix  the   broadband"))
    }

    @Test
    fun sanitiseShort_htmlTags_stripped() {
        assertEquals("alert('xss')", InputSanitiser.sanitiseShort("<script>alert('xss')</script>"))
    }

    @Test
    fun sanitiseShort_htmlImgTag_stripped() {
        assertEquals("Hello world", InputSanitiser.sanitiseShort("Hello <img src=x onerror=alert(1)> world"))
    }

    @Test
    fun sanitiseShort_htmlBoldTag_stripped() {
        assertEquals("Important text", InputSanitiser.sanitiseShort("<b>Important</b> text"))
    }

    @Test
    fun sanitiseShort_controlChars_removed() {
        assertEquals("Hello world", InputSanitiser.sanitiseShort("Hello\u0000 world"))
    }

    @Test
    fun sanitiseShort_exceedsMaxLength_truncated() {
        val longString = "a".repeat(300)
        val result = InputSanitiser.sanitiseShort(longString)
        assertEquals(200, result.length)
    }

    @Test
    fun sanitiseShort_emptyString_returnsEmpty() {
        assertEquals("", InputSanitiser.sanitiseShort(""))
    }

    @Test
    fun sanitiseShort_onlySpaces_returnsEmpty() {
        assertEquals("", InputSanitiser.sanitiseShort("     "))
    }

    @Test
    fun sanitiseLong_preservesNewlines() {
        val input = "First line\nSecond line"
        assertEquals("First line\nSecond line", InputSanitiser.sanitiseLong(input))
    }

    @Test
    fun sanitiseLong_collapsesExcessiveNewlines() {
        val input = "First\n\n\n\n\nSecond"
        assertEquals("First\n\nSecond", InputSanitiser.sanitiseLong(input))
    }

    @Test
    fun sanitiseLong_htmlTags_stripped() {
        assertEquals("Hello world", InputSanitiser.sanitiseLong("<div>Hello</div> world"))
    }

    @Test
    fun sanitiseLong_exceedsMaxLength_truncated() {
        val longString = "a".repeat(2500)
        val result = InputSanitiser.sanitiseLong(longString)
        assertEquals(2000, result.length)
    }

    @Test
    fun sanitiseLong_normalMultilineText_unchanged() {
        val input = "Arrived on site.\nCustomer not home.\nLeft card."
        assertEquals(input, InputSanitiser.sanitiseLong(input))
    }

    @Test
    fun stripHtmlTags_scriptTag_removed() {
        assertEquals("alert(1)", InputSanitiser.stripHtmlTags("<script>alert(1)</script>"))
    }

    @Test
    fun stripHtmlTags_nestedTags_allRemoved() {
        assertEquals("Hello world", InputSanitiser.stripHtmlTags("<div><span>Hello</span> world</div>"))
    }

    @Test
    fun stripHtmlTags_noTags_unchanged() {
        assertEquals("Normal text", InputSanitiser.stripHtmlTags("Normal text"))
    }

    @Test
    fun stripHtmlTags_angleBracketsInMath_stripped() {

        assertEquals("5  10", InputSanitiser.stripHtmlTags("5 <is less than> 10"))
    }

    @Test
    fun removeControlChars_nullByte_removed() {
        assertEquals("ab", InputSanitiser.removeControlChars("a\u0000b"))
    }

    @Test
    fun removeControlChars_preservesNewline() {
        assertEquals("a\nb", InputSanitiser.removeControlChars("a\nb"))
    }

    @Test
    fun removeControlChars_preservesTab() {
        assertEquals("a\tb", InputSanitiser.removeControlChars("a\tb"))
    }

    @Test
    fun removeControlChars_bellChar_removed() {
        assertEquals("ab", InputSanitiser.removeControlChars("a\u0007b"))
    }
}
