package org.postpi.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

class EmailAlreadyExistsException(email: String) :
    RuntimeException("An account with email '$email' already exists")

@Service
class AuthService (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
){
    fun register(email: String, rawPassword: String): User {
        if (userRepository.existByEmail(email)) {
            throw EmailAlreadyExistsException(email)
        }

        val hash = passwordEncoder.encode(rawPassword)
        return userRepository.insert(email, hash)
    }
}