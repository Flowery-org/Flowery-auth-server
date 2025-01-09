package org.flowery.dto

data class EmailVerificationDto(
    val useremail: String,
    val usercode: String,
)