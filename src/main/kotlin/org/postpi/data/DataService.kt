package org.postpi.data

import org.postpi.schema.SchemaIntrospector
import org.postpi.schema.TableSchema
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

class TableNotFoundException(tableName: String) :
    RuntimeException("Table '$tableName' does not exist or not accessible.")

@Service
class DataService (
    private val schemaIntrospector: SchemaIntrospector,
    private val jdbcTemplate: NamedParameterJdbcTemplate
){
    fun findAll(tableName: String): List<Map<String , Any?>>{
        val schema = schemaIntrospector.introspect()
        val table = schema.find { it.tableName == tableName }
            ?: throw TableNotFoundException(tableName)

        val sql = "SELECT * FROM \"${table.tableName}\""
        return jdbcTemplate.queryForList(sql , emptyMap<String, Any>())
    }
}