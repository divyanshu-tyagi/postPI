package org.postpi.policy

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class PolicyRepository (
    private val jdbcTemplate: NamedParameterJdbcTemplate
){
    fun findPolicy(tableName: String , operation: String): TablePolicy?{
        val sql = """
            SELECT table_name, operation, column_name
            FROM table_policies WHERE table_name = :tableName AND operation = :operation
            LIMIT 1
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("tableName", tableName)
            .addValue("operation", operation)

        return jdbcTemplate.query(sql, params) { rs, _ ->
            TablePolicy(
                tableName = rs.getString("table_name"),
                operation = rs.getString("operation"),
                columnName = rs.getString("column_name")
            )
        }.firstOrNull()
    }
}