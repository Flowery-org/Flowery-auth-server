package org.flowery.dto

data class EmailVerificationDto(
    val userEmail: String,
    val userCode: String,
)