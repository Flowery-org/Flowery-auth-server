package org.flowery.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequestDto(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val ident: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)
