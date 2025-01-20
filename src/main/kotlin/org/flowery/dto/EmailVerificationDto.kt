package org.flowery.dto

data class EmailVerificationDto(
    val ident: String,
    val userCode: String
)