package com.raf.fieldops.util

object InputSanitiser {

    private const val MAX_SHORT_FIELD = 200

    private const val MAX_LONG_FIELD = 2000

    fun sanitiseShort(input: String): String {
        var result = input.trim()
        result = stripHtmlTags(result)
        result = collapseWhitespace(result)
        result = removeControlChars(result)
        return result.take(MAX_SHORT_FIELD)
    }

    fun sanitiseLong(input: String): String {
        var result = input.trim()
        result = stripHtmlTags(result)
        result = collapseInlineWhitespace(result)
        result = removeControlChars(result)
        return result.take(MAX_LONG_FIELD)
    }

    fun stripHtmlTags(input: String): String {
        return input.replace(Regex("<[^>]*>"), "")
    }

    fun removeControlChars(input: String): String {
        return input.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]"), "")
    }

    fun collapseWhitespace(input: String): String {
        return input
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
    }

    fun collapseInlineWhitespace(input: String): String {
        return input
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
    }
}
