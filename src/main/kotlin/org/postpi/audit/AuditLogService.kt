package org.postpi.audit

import org.postpi.apiKey.ApiKeyPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuditLogService (
    private val auditLogRepository: AuditLogRepository
){
    fun record(tableName: String, operation: String, rowId: String?) {
        val principal = SecurityContextHolder.getContext().authentication?.principal

        val (principalType, principalId) = when (principal) {
            is ApiKeyPrincipal -> "api_key" to principal.apiKeyId.toString()
            is UUID -> "user" to principal.toString()
            else -> "unknown" to "unknown"
        }

        auditLogRepository.insert(tableName, operation, principalType, principalId, rowId)
    }
}