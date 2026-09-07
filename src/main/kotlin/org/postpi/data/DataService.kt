package org.postpi.data

import org.postpi.apiKey.ApiKeyPrincipal
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

class RowNotFoundException(tableName: String, id: String) :
    RuntimeException("Row with id '$id' not found in table '$tableName'")


@Service
class DataService (
    private val schemaIntrospector: SchemaIntrospector,
    private val policyRepository: PolicyRepository,
    private val jdbcTemplate: NamedParameterJdbcTemplate
){
    fun findAll(tableName: String , params: Map<String, String>): List<Map<String , Any?>>{
        val table = requireTable(tableName)

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
        if(policy != null && !isApiKeyPrincipal()){
            val currentUserId = requireCurrentUserId()
                ?: throw IllegalStateException("Policy requires an authenticated user .")
            whereClauses.add("\"${policy.columnName}\" = :currentUserId")
            sqlParams.addValue("currentUserId", currentUserId)
        }

        val whereSql = buildWhereSql(whereClauses)

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

    fun insert(tableName: String, body: Map<String,Any?>): Map<String, Any?>{
        val table = requireTable(tableName)
        body.keys.forEach { validateColumn(table, it) }

        val insertData = body.toMutableMap()

        val policy = policyRepository.findPolicy(tableName, "INSERT")
        if(policy != null && !isApiKeyPrincipal()){
            val currentUserId = requireCurrentUserId()
            insertData[policy.columnName] = currentUserId
        }
        val sqlParams = MapSqlParameterSource()
        val columns = insertData.keys.toList()
        columns.forEach { col -> sqlParams.addValue(col, insertData[col]) }

        val columnList = columns.joinToString(", ") { "\"$it\"" }
        val valueList = columns.joinToString(", ") { ":$it" }

        val sql = """
            INSERT INTO "${table.tableName}" ($columnList)
            VALUES ($valueList)
            RETURNING *
        """.trimIndent()
        return jdbcTemplate.queryForMap(sql, sqlParams)
    }

    fun update(tableName: String, id: String, body: Map<String, Any?>): Map<String, Any?>{
        val table = requireTable(tableName)
        body.keys.forEach { validateColumn(table, it) }

        val sqlParams = MapSqlParameterSource()
        val setClauses = body.keys.map { col ->
            sqlParams.addValue(col, body[col])
            "\"$col\" = :$col"
        }
        val whereClauses = mutableListOf("\"id\" = :id")
        sqlParams.addValue("id", UUID.fromString(id))

        applyPolicy(table, "UPDATE", whereClauses, sqlParams)
        val sql = """
            UPDATE "${table.tableName}"
            SET ${setClauses.joinToString(", ")}
            WHERE ${whereClauses.joinToString(" AND ")}
            RETURNING *
        """.trimIndent()

        val results = jdbcTemplate.queryForList(sql, sqlParams)
        return results.firstOrNull() ?: throw RowNotFoundException(tableName, id)
    }

    fun delete(tableName: String, id: String) {
        val table = requireTable(tableName)

        val sqlParams = MapSqlParameterSource()
        val whereClauses = mutableListOf("\"id\" = :id")
        sqlParams.addValue("id", UUID.fromString(id))

        applyPolicy(table, "DELETE", whereClauses, sqlParams)

        val sql = """
            DELETE FROM "${table.tableName}"
            WHERE ${whereClauses.joinToString(" AND ")}
        """.trimIndent()

        val rowsAffected = jdbcTemplate.update(sql, sqlParams)
        if (rowsAffected == 0) throw RowNotFoundException(tableName, id)
    }


    private fun validateColumn(table: TableSchema, columnName: String?){
        if(columnName == null) return
        val exists = table.columns.any { it.columnName == columnName }
        if( !exists) throw InvalidColumnException(columnName , table.tableName)
    }
    private fun requireCurrentUserId(): UUID? {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return when(principal){
            is UUID -> principal
            else -> throw IllegalStateException("This Operation requires an authenticated user .")
        }

    }
    private fun requireTable(tableName: String): TableSchema{
        val schema = schemaIntrospector.introspect()
        return  schema.find{ it.tableName == tableName}
            ?: throw TableNotFoundException(tableName)
    }

    private fun applyPolicy(
        table: TableSchema,
        operation: String,
        whereClauses: MutableList<String>,
        sqlParams: MapSqlParameterSource
    ){
        val policy = policyRepository.findPolicy(table.tableName, operation) ?: return
        if(isApiKeyPrincipal()) return
        val currentUserId = requireCurrentUserId()
        whereClauses.add("\"${policy.columnName}\" = :currentUserId")
        sqlParams.addValue("currentUserId", currentUserId)
    }

    private fun isApiKeyPrincipal(): Boolean {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return principal is ApiKeyPrincipal
    }

    private fun buildWhereSql(whereClauses: List<String>): String =
        if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}"
        else ""
}