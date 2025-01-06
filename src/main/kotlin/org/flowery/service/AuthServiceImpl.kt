// AuthServiceImpl.kt
package org.flowery.service

import org.flowery.dto.LoginRequestDto
import org.flowery.dto.LoginResponseDto
import org.flowery.jwt.JwtProvider
import org.flowery.model.User
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuthServiceImpl(
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    private val redisTemplate: RedisTemplate<String, Any>
) : AuthService {

    /**
     * 로그인 요청을 처리하여 JWT 토큰을 생성하고 반환합니다.
     *
     * @param loginRequestDto 로그인 요청 DTO
     * @return 로그인 응답 DTO를 포함한 Mono
     */
    override fun login(loginRequestDto: LoginRequestDto): Mono<LoginResponseDto> {
        // Redis에서 사용자 정보 조회
        val user = redisTemplate.opsForHash<String, Any>().get("users", loginRequestDto.username) as? User

        return if (user != null &&
            passwordEncoder.matches(loginRequestDto.password, user.passwordHash)
        ) {
            val token = jwtProvider.createToken(user.username)
            Mono.just(LoginResponseDto(token = token, username = user.username))
        } else {
            Mono.error(IllegalArgumentException("Invalid username or password"))
        }
    }

}
