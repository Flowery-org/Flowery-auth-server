package org.flowery.service

import org.flowery.dto.*
import reactor.core.publisher.Mono

interface AuthService {
    fun login(loginRequestDto: LoginRequestDto): Mono<LoginResponseDto>
    fun sendEmailMessage(data: EmailSendDto): String
    fun verificationEmail(data: EmailVerificationDto): String
    fun signUp(signUpRequestDto: SignUpRequestDto): Mono<SignUpResponseDto>
}
