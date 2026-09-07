package org.postpi.apiKey

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

data class ApiKeyPrincipal(
    val apiKeyId: java.util.UUID,
    val name: String
)


@Component
class ApiKeyAuthFilter (
    private val apiKeyService: ApiKeyService
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKeyHeader = request.getHeader("X-API-KEY")
        if(apiKeyHeader != null){
            val apiKey = apiKeyService.validate(apiKeyHeader)
            if(apiKey != null){
                val principal = ApiKeyPrincipal(apiKey.id, apiKey.name)
                val authentication = UsernamePasswordAuthenticationToken(
                    principal,null,emptyList()
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        }
        filterChain.doFilter(request, response)
    }
}