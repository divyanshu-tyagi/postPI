package org.postpi.schema

data class ColumnInfo(
    val columnName: String,
    val dataType: String,
    val isNullable: Boolean
)
