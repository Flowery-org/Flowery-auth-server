package org.flowery.dto

data class LoginResponseDto(
    val token: String,
    val ident: String,
    val roles: Set<String> // user, admin 구분
)