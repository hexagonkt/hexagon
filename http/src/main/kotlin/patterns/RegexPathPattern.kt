package com.hexagonkt.http.patterns

data class RegexPathPattern(val regex: Regex) : PathPattern {

    override val pattern: String = regex.pattern
    override val prefix: Boolean = !regex.pattern.endsWith("$")

    internal companion object {
        val PARAMETER_REGEX = Regex("""\(\?<\w+>""")
    }

    init {
        checkPathPatternPrefix(pattern, listOf("", "$"))
    }

    val parameters: List<String> =
        PARAMETER_REGEX.findAll(pattern)
            .map { it.value.removePrefix("(?<").removeSuffix(">") }
            .toList()

    override fun addPrefix(prefix: String?): PathPattern =
        if (prefix == null) this
        else copy(regex = Regex(prefix + pattern))

    @OptIn(ExperimentalStdlibApi::class)
    override fun matches(requestUrl: String): Boolean =
        if (prefix) regex.matchesAt(requestUrl, 0)
        else regex.matches(requestUrl)

    override fun extractParameters(requestUrl: String): Map<String, String> {
        val result = regex.matchEntire(requestUrl)
        require(result != null) { "URL '$requestUrl' does not match path" }

        val allValues = result.groupValues
            .drop(1)
            .mapIndexed { ii, v -> ii.toString() to v }
            .toMap()

        return if (parameters.isEmpty()) allValues
        else parameters.associateWith { result.groups[it]?.value ?: "" } + allValues
    }
}
