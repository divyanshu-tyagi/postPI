package org.postpi.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

class EmailAlreadyExistsException(email: String) :
    RuntimeException("An account with email '$email' already exists")

class InvalidCredentialsException :
    RuntimeException("Invalid Email or Password")

@Service
class AuthService (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
){
    fun register(email: String, rawPassword: String): User {
        if (userRepository.existByEmail(email)) {
            throw EmailAlreadyExistsException(email)
        }

        val hash = passwordEncoder.encode(rawPassword)
        return userRepository.insert(email, hash)
    }

    fun login(email: String, rawPassword: String) : String {
        val user = userRepository.findByEmail(email)
            ?:throw InvalidCredentialsException()

        if(!passwordEncoder.matches(rawPassword, user.passwordHash)){
            throw InvalidCredentialsException()
        }
        return jwtService.generateToken((user.id))
    }
}