package org.postpi.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}") secretkeyString : String,
    @Value("\${app.jwt.expiration-ms}") private val expirationMs : Long
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secretkeyString.toByteArray())

    fun generateToken(userId: UUID) :String{
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    fun extractUserId(token: String): UUID{
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return UUID.fromString(claims.subject)
    }
}