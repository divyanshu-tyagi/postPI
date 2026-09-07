package org.postpi.apiKey

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class CreateApiKeyRequest(val name: String)

@RestController
class ApiKeyController (
    private val apiKeyService: ApiKeyService
){
    @PostMapping("/auth/api-keys")
    fun create(@RequestBody request: CreateApiKeyRequest) : ResponseEntity<Map<String, String>> {
        val generated = apiKeyService.generate(request.name)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "id" to generated.id.toString(),
                "name" to generated.name,
                "key" to generated.rawKey
            )
        )
    }
}