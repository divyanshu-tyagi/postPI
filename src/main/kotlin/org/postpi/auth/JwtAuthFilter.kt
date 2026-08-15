package org.postpi.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter (
    private val jwtService: JwtService
): OncePerRequestFilter(){

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            val token = authHeader.removePrefix("Bearer ").trim()

            try {
                val userId = jwtService.extractUserId(token)
                val authentication = UsernamePasswordAuthenticationToken(userId,null ,emptyList())

                SecurityContextHolder.getContext().authentication = authentication
            }catch (ex: Exception){
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }
}