package org.flowery.service

import org.flowery.dto.LoginRequestDto
import org.flowery.dto.LoginResponseDto
import reactor.core.publisher.Mono

interface AuthService {
    fun login(loginRequestDto: LoginRequestDto): Mono<LoginResponseDto>
}