package org.postpi.audit

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AuditLogRepository (
    private val jdbcTemplate: NamedParameterJdbcTemplate
){
    fun insert(
        tableName: String,
        operation: String,
        principalType: String,
        principalId: String,
        rowId: String?
    ) {
        val sql = """
            INSERT INTO audit_log (table_name, operation, principal_type, principal_id, row_id)
            VALUES (:tableName, :operation, :principalType, :principalId, :rowId)
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("tableName", tableName)
            .addValue("operation", operation)
            .addValue("principalType", principalType)
            .addValue("principalId", principalId)
            .addValue("rowId", rowId)

        jdbcTemplate.update(sql, params)
    }
}