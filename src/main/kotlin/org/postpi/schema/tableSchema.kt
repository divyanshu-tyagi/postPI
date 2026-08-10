package org.postpi.schema

data class TableSchema(
    val tableName: String,
    val columns: List<ColumnInfo>
)
