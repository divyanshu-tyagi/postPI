package org.postpi.data

import org.postpi.policy.PolicyRepository
import org.postpi.schema.SchemaIntrospector
import org.postpi.schema.TableSchema
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.UUID

class TableNotFoundException(tableName: String) :
    RuntimeException("Table '$tableName' does not exist or not accessible.")

class InvalidColumnException(columnName: String, tableName: String) :
    RuntimeException("Column '$columnName' does not exist in table '$tableName'")

@Service
class DataService (
    private val schemaIntrospector: SchemaIntrospector,
    private val policyRepository: PolicyRepository,
    private val jdbcTemplate: NamedParameterJdbcTemplate
){
    fun findAll(tableName: String , params: Map<String, String>): List<Map<String , Any?>>{
        val schema = schemaIntrospector.introspect()
        val table = schema.find { it.tableName == tableName }
            ?: throw TableNotFoundException(tableName)

        val pagination = Pagination.from(params)
        val filters = QueryFilter.parseAll(params)

        validateColumn(table, pagination.orderBy)
        filters.forEach { validateColumn(table, it.column) }

        val sqlParams = MapSqlParameterSource()
        val whereClauses = mutableListOf<String>()

        filters.forEachIndexed { index, filter ->
            val paramName = "filterValue$index"
            whereClauses.add("\"${filter.column}\" = :$paramName")
            sqlParams.addValue(paramName, filter.value)
        }

        val policy = policyRepository.findPolicy(tableName, "SELECT")
        if(policy != null){
            val currentUserId = getCurrentUserId()
                ?: throw IllegalStateException("Policy requires an authenticated user .")
            whereClauses.add("\"${policy.columnName}\" = :currentUserId")
            sqlParams.addValue("currentUserId", currentUserId)
        }

        val whereSql = if (whereClauses.isNotEmpty())
            "WHERE ${whereClauses.joinToString(" AND ")}"
        else ""

        val orderBySql = pagination.orderBy
            ?.let { "ORDER BY \"$it\"" }
            ?: ""

        sqlParams.addValue("limit", pagination.limit)
        sqlParams.addValue("offset", pagination.offset)

        val sql = """
            SELECT * FROM "${table.tableName}"
            $whereSql
            $orderBySql
            LIMIT :limit OFFSET :offset
        """.trimIndent()

        return jdbcTemplate.queryForList(sql, sqlParams)
    }
    private fun validateColumn(table: TableSchema, columnName: String?){
        if(columnName == null) return
        val exists = table.columns.any { it.columnName == columnName }
        if( !exists) throw InvalidColumnException(columnName , table.tableName)
    }
    private fun getCurrentUserId(): UUID? {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return principal as? UUID
    }
}