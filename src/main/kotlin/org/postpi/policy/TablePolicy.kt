package org.postpi.policy

data class TablePolicy(
    val tableName: String,
    val operation: String,
    val columnName: String
)
