package io.kovertx.sql.migrator.common

interface SqlVariantParserBuilder {
    fun variant(parser: SqlParser)
}

class SqlVariantParser(fn: SqlVariantParserBuilder.() -> Unit) {

    private val variants: List<SqlParser>
    private val preferredVariant: Int get() = variants.size - 1
    private val preferredParser: SqlParser get() = variants[preferredVariant]

    init {
        val initVariants = mutableListOf<SqlParser>()
        fn(object : SqlVariantParserBuilder {
            override fun variant(parser: SqlParser) {
                initVariants.add(parser)
            }
        })
        if (initVariants.isEmpty()) throw IllegalArgumentException("No variants registered")
        variants = initVariants
    }

    private fun parseReferenceVariant(hash: String): Int {
        val parts = hash.split(':')
        if (parts.size < 2) throw IllegalArgumentException("Unparsable variant hash")
        val variant = Integer.parseInt(parts[1])
        return variant
    }

    fun hash(content: String, reference: String? = null): SqlValidateVariantResult {
        val currentVariant = if (reference != null)
            parseReferenceVariant(reference) else preferredVariant

        if (currentVariant >= variants.size) {
            throw IllegalStateException("Migration log contains newer hash variant, are you using an outdated migration driver?")
        }
        val impl = variants[currentVariant]
        val hash = impl.hash(content) + ":" + currentVariant

        if (currentVariant == preferredVariant) {
            return SqlValidateVariantResult(hash, hash)
        } else {
            val preferredHash = preferredParser.hash(content) + ":" + preferredVariant
            return SqlValidateVariantResult(hash, preferredHash)
        }
    }

    fun split(content: String): List<String> {
        val impl = variants.last()
        return impl.split(content)
    }
}

data class SqlValidateVariantResult(
    val hash: String,
    val preferHash: String
)