package org.postpi.data

data class QueryFilter(
    val column : String,
    val value: String
){
    companion object {
        private val FILTER_PARAM_REGEX = Regex("""filter\[(.+)]""")

        fun parseAll(params: Map<String, String>): List<QueryFilter> {
            return params.mapNotNull { (key, rawValue) ->
                val match = FILTER_PARAM_REGEX.matchEntire(key) ?: return@mapNotNull null
                val column = match.groupValues[1]

                // Expect format "eq.<value>" — extract the value after "eq."
                if (!rawValue.startsWith("eq.")) return@mapNotNull null
                val value = rawValue.removePrefix("eq.")

                QueryFilter(column = column, value = value)
            }
        }
    }
}
