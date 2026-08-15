package org.postpi.auth

data class LoginRequest(
    val email: String,
    val password: String
)
