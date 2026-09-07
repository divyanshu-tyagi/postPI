package org.postpi.apiKey

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ApiKeyRepository (
    private val jdbcTemplate: NamedParameterJdbcTemplate
){
    fun insert(keyHash: String, name: String): ApiKey {
        val sql = """
            INSERT INTO api_keys (key_hash, name)
            VALUES (:keyHash, :name)
            RETURNING id, key_hash, name
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("keyHash", keyHash)
            .addValue("name", name)

        return jdbcTemplate.queryForObject(sql, params) { rs, _ ->
            ApiKey(
                id = rs.getObject("id", UUID::class.java),
                keyHash = rs.getString("key_hash"),
                name = rs.getString("name")
            )
        }!!
    }

    fun findByKeyHash(keyHash: String): ApiKey? {
        val sql = "SELECT id, key_hash, name FROM api_keys WHERE key_hash = :keyHash"
        val params = MapSqlParameterSource("keyHash", keyHash)

        return jdbcTemplate.query(sql, params) { rs, _ ->
            ApiKey(
                id = rs.getObject("id", UUID::class.java),
                keyHash = rs.getString("key_hash"),
                name = rs.getString("name")
            )
        }.firstOrNull()
    }
}