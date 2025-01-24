package org.flowery.dto

data class SignUpRequestDto(
    val ident: String,
    val userEmail: String,
    val password: String,
    val userName: String
)