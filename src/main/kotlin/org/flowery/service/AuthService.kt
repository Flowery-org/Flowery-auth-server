package org.flowery.service

import org.flowery.dto.EmailSendDto
import org.flowery.dto.EmailVerificationDto
import org.flowery.dto.LoginRequestDto
import org.flowery.dto.LoginResponseDto
import reactor.core.publisher.Mono

interface AuthService {
    fun login(loginRequestDto: LoginRequestDto): Mono<LoginResponseDto>
    fun sendEmailMessage(data: EmailSendDto): String
    fun verificationEmail(data: EmailVerificationDto): String
}
