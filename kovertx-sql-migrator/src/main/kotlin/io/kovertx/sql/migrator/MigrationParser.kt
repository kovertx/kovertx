package io.kovertx.sql.migrator

object MigrationParser {
    private val stepHeaderRegex = Regex(
        "^--+\\h*migration:\\h*([^\\n\\r]*)$",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

    fun splitIntoSteps(content: String): List<MigrationStepPlan> {
        val headers = stepHeaderRegex.findAll(content).map { match ->
            val title = match.groups[1]?.value ?: "(untitled)"
            Pair(title, match.range)
        }.toList()

        if (headers.isEmpty()) {
            return listOf(MigrationStepPlan("(untitled)", content))
        }

        return headers.mapIndexed { idx, (title, headerRange) ->
            val nextHeader = headers.getOrNull(idx + 1)
            val contentStart = headerRange.last + 1
            val contentEnd = nextHeader?.second?.start ?: content.length
            MigrationStepPlan(title, content.substring(contentStart, contentEnd))
        }
    }
}