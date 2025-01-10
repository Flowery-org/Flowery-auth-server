package org.flowery.service

import org.flowery.dto.EmailSendDto
import org.flowery.dto.EmailVerificationDto
import org.flowery.dto.LoginRequestDto
import org.flowery.dto.LoginResponseDto
import org.flowery.jwt.JwtProvider
import org.flowery.model.User
import org.flowery.repository.AuthRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuthServiceImpl(
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val emailSender: JavaMailSender,
    private val authRepository: AuthRepository,
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

    /*
    * SMTP 이메일 전송
    *
    * 랜덤 6자리 숫자 인증 코드 생성 후 인증 코드 저장
    *
    * 유저가 작성한 이메일로 인증코드 전송
    *
    * */
    override fun sendEmailMessage(data: EmailSendDto): String {
        return try {
            val randNum = (100000..999999).random().toString()

            // 인증 코드 저장 ( TTL : 5분 )
            authRepository.saveAuthCode(data.userEmail, randNum, 5)

            val message = SimpleMailMessage().apply {
                setFrom("hyu.flowery@gmail.com")      // 호스트 이메일
                setTo(data.userEmail)                 // 클라이언트 이메일
                setSubject("[Flowery] 이메일 인증 코드")  // 메일 주제
                setText("[인증코드] ${randNum}")      // 메일 메시지
            }
            emailSender.send(message)
            "Email sent successfully!"

        } catch (e: Exception) {
            "Email sent failed!"
        }
    }

    /*
    * 이메일 인증코드 인증 및 인증 코드 삭제
    *
    * email을 사용해 redis에 저장된 서버에서 보낸 인증 코드와 유저가 입력한 인증 코드의 비교
    *
    * */
    override fun verificationEmail(data: EmailVerificationDto): String {
        val savedCode = authRepository.getAuthCode(data.userEmail) // redis에서 인증코드 조회

        return if (savedCode == null){
            "Verification failed: No code found for this email."
        } else if (data.userCode == savedCode){
            authRepository.deleteAuthCode(data.userEmail) // 인증 성공 후 인증 코드 삭제
            "Verification successful!"
        }
        else {
            "Verification failed: Invalid code."
        }

    }

}
