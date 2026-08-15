package org.postpi.data

data class Pagination(
    val limit: Int,
    val offset: Int,
    val orderBy: String?
){
    companion object{
        private const val DEFAULT_LIMIT = 50
        private const val MAX_LIMIT = 200

        fun from(params: Map<String, String>): Pagination{
            val limit = params["limit"]?.toIntOrNull()?.coerceIn(1, MAX_LIMIT) ?: DEFAULT_LIMIT
            val offset = params["offset"]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
            val orderBy = params["orderBy"]

            return Pagination(limit, offset, orderBy)
        }
    }
}
