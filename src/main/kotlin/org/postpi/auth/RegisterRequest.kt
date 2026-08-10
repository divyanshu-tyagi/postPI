package org.postpi.auth

data class RegisterRequest (
    val email: String,
    val password: String
)