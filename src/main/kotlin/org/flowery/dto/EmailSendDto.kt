package org.flowery.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class EmailSendDto(
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val userEmail: String,
    @field:NotBlank(message = "사용자 이름은 필수입니다")
    val userName: String,
)