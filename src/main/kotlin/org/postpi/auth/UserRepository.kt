package org.postpi.auth

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String
)

@Repository
class UserRepository (
    private val jdbcTemplate: NamedParameterJdbcTemplate
){
    fun existByEmail(email: String): Boolean {
        val sql = "SELECT COUNT(*) FROM users WHERE email = :email"
        val count = jdbcTemplate.queryForObject(
            sql,
            MapSqlParameterSource("email", email),
            Int::class.java
        )
        return (count ?: 0) > 0
    }

    fun insert(email: String, passwordHash: String?): User {
        val sql = """
            INSERT INTO users (email, password_hash)
            VALUES (:email, :passwordHash)
            RETURNING id, email, password_hash
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("email", email)
            .addValue("passwordHash", passwordHash)

        return jdbcTemplate.queryForObject(sql, params) { rs, _ ->
            User(
                id = rs.getObject("id", UUID::class.java),
                email = rs.getString("email"),
                passwordHash = rs.getString("password_hash")
            )
        }!!
    }

    fun findByEmail(email: String): User? {
        val sql = "SELECT id, email, password_hash FROM users WHERE email = :email"
        val params = MapSqlParameterSource("email", email)

        return jdbcTemplate.query(sql, params) { rs, _ ->
            User(
                id = rs.getObject("id", UUID::class.java),
                email = rs.getString("email"),
                passwordHash = rs.getString("password_hash")
            )
        }.firstOrNull()
    }
}