package org.postpi.schema

import jdk.internal.org.jline.utils.InfoCmp
import org.flywaydb.core.internal.database.base.Table
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import tools.jackson.core.util.Named

@Component
class SchemaIntrospector(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) {
    fun introspect() : List<TableSchema>{

        val sql = """
            SELECT table_name, column_name, data_type, is_nullable
            FROM information_schema.columns
            WHERE table_schema = 'public'
            ORDER BY table_name, ordinal_position
        """.trimIndent()

        val rows = jdbcTemplate.queryForList(sql, emptyMap<String, Any>())
        return rows
            .groupBy { it["table_name"] as String }
            .map { (tableName , ColumnRows) ->
                TableSchema(
                    tableName = tableName,
                    columns = ColumnRows.map { row ->
                        ColumnInfo(
                            columnName = row["column_name"] as String,
                            dataType = row["data_type"] as String,
                            isNullable = (row["is_nullable"] as String) == "YES"
                        )
                    }
                )
            }
    }
}