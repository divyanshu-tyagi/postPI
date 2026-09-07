package org.postpi.apiKey

import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

data class GeneratedApiKey(
    val rawKey: String,
    val id: UUID,
    val name: String
)


@Service
class ApiKeyService(
    private val apiKeyRepository: ApiKeyRepository
) {
    private val secureRandom = SecureRandom()

    fun generate(name: String): GeneratedApiKey{
        val randomBytes = ByteArray(32)
        secureRandom.nextBytes(randomBytes)
        val randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
        val rawKey = "pk_live_$randomPart"
        val keyHash = sha256(rawKey)
        val stored = apiKeyRepository.insert(keyHash, name)
        return GeneratedApiKey(rawKey = rawKey, id = stored.id, name = stored.name)
    }

    fun validate(rawKey: String): ApiKey?{
        val keyHash = sha256(rawKey)
        return apiKeyRepository.findByKeyHash(keyHash)
    }

    private fun sha256(input: String): String{
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}