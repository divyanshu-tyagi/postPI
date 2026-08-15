package org.postpi.auth

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/auth/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<Map<String, String>> {
        val user = authService.register(request.email, request.password)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapOf("id" to user.id.toString(), "email" to user.email))
    }

    @PostMapping("/auth/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val token = authService.login(request.email, request.password)
        return ResponseEntity.ok(LoginResponse(token))
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailExists(ex: EmailAlreadyExistsException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf("error" to (ex.message ?: "Email already exists")))
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("error" to (ex.message ?: "Invalid credentials")))
    }
}