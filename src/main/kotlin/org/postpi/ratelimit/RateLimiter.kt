package org.postpi.ratelimit

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RateLimiter (
    private val redisTemplate: StringRedisTemplate,
    @Value("\${app.rate-limit.max-requests}") private val maxRequests: Long,
    @Value("\${app.rate-limit.window-seconds}") private val windowSeconds: Long
){
    fun isAllowed(identifier: String): Boolean {
        val key = "rate_limit:$identifier"
        val currentCount = redisTemplate.opsForValue().increment(key) ?: 1L

        if (currentCount == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds))
        }

        return currentCount <= maxRequests
    }
}