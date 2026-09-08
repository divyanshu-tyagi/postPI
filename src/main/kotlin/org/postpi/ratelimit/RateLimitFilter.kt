package org.postpi.ratelimit

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.postpi.apiKey.ApiKeyPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class RateLimitFilter(
    private val rateLimiter: RateLimiter
): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!request.requestURI.startsWith("/api/")) {
            filterChain.doFilter(request, response)
            return
        }

        val principal = SecurityContextHolder.getContext().authentication?.principal
        val identifier = when (principal) {
            is ApiKeyPrincipal -> "apikey:${principal.apiKeyId}"
            is UUID -> "user:$principal"
            else -> "anonymous"
        }

        if (!rateLimiter.isAllowed(identifier)) {
            response.status = 429
            response.contentType = "application/json"
            response.writer.write("""{"error":"Rate limit exceeded. Try again later."}""")
            return
        }

        filterChain.doFilter(request, response)
    }
}