package org.postpi.apiKey

import java.util.UUID

data class ApiKey(
    val id : UUID,
    val keyHash: String,
    val name : String
)
