package org.flowery.service

import org.flowery.dto.*
import org.flowery.jwt.JwtProvider
import org.flowery.model.User
import org.flowery.repository.AuthRepository
import org.flowery.utils.PasswordValidator
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import org.slf4j.LoggerFactory

@Service
class AuthServiceImpl(
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val emailSender: JavaMailSender,
    private val authRepository: AuthRepository,
) : AuthService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun signUp(signUpRequestDto: SignUpRequestDto): Mono<SignUpResponseDto> {
        log.info("회원가입 프로세스 시작: ident={}, email={}", signUpRequestDto.ident, signUpRequestDto.userEmail)

        return try {
            validatePassword(signUpRequestDto.password)
            log.debug("비밀번호 유효성 검사 완료: ident={}", signUpRequestDto.ident)

            val passwordHash = passwordEncoder.encode(signUpRequestDto.password)
            val user = User(
                ident = signUpRequestDto.ident,
                passwordHash = passwordHash,
                name = signUpRequestDto.userName,
                userEmail = signUpRequestDto.userEmail,
                roles = setOf("ROLE_USER")
            )

            redisTemplate.opsForHash<String, Any>().put("users", user.ident, user)
            log.info("사용자 정보 Redis 저장 완료: ident={}", user.ident)

            Mono.just(SignUpResponseDto(ident = user.ident, userName = user.name))
        } catch (e: Exception) {
            log.error("회원가입 실패: ident={}, error={}", signUpRequestDto.ident, e.message)
            Mono.error(e)
        }
    }

    override fun login(loginRequestDto: LoginRequestDto): Mono<LoginResponseDto> {
        log.info("로그인 시도: ident={}", loginRequestDto.ident)

        val user = redisTemplate.opsForHash<String, Any>().get("users", loginRequestDto.ident) as? User

        return if (user != null && passwordEncoder.matches(loginRequestDto.password, user.passwordHash)) {
            val token = jwtProvider.createToken(user.ident, user.roles)
            log.info("로그인 성공: ident={}", user.ident)
            Mono.just(LoginResponseDto(token = token, ident = user.ident, roles = user.roles))
        } else {
            log.warn("로그인 실패: ident={}, reason=invalid_credentials", loginRequestDto.ident)
            Mono.error(IllegalArgumentException("Invalid ident or password"))
        }
    }

    override fun sendEmailMessage(data: EmailSendDto): String {
        log.info("이메일 인증 코드 전송 시작: email={}", data.userEmail)

        return try {
            val randNum = (100000..999999).random().toString()
            authRepository.saveAuthCode(data.userEmail, randNum, 5)
            log.debug("인증 코드 생성 및 저장 완료: email={}", data.userEmail)

            val message = SimpleMailMessage().apply {
                setFrom("hyu.flowery@gmail.com")
                setTo(data.userEmail)
                setSubject("[Flowery] 이메일 인증 코드")
                setText("[인증코드] ${randNum}")
            }
            emailSender.send(message)
            log.info("이메일 전송 성공: email={}", data.userEmail)
            "Email sent successfully!"
        } catch (e: Exception) {
            log.error("이메일 전송 실패: email={}, error={}", data.userEmail, e.message)
            "Email sent failed!"
        }
    }

    override fun verificationEmail(data: EmailVerificationDto): String {
        log.info("이메일 인증 코드 검증 시작: email={}", data.userEmail)

        val savedCode = authRepository.getAuthCode(data.userEmail)

        return when {
            savedCode == null -> {
                log.warn("인증 코드 없음: email={}", data.userEmail)
                "Verification failed: No code found for this email."
            }
            data.userCode == savedCode -> {
                authRepository.deleteAuthCode(data.userEmail)
                log.info("인증 성공: email={}", data.userEmail)
                "Verification successful!"
            }
            else -> {
                log.warn("잘못된 인증 코드: email={}", data.userEmail)
                "Verification failed: Invalid code."
            }
        }
    }

    fun logout(token: String) {
        log.info("로그아웃 처리 시작")
        val remainingMills = 3600000L
        jwtProvider.addToBlacklist(token, remainingMills)
        log.info("토큰 블랙리스트 추가 완료")
    }

    private fun validatePassword(password: String) {
        val validationResult = PasswordValidator.validate(password)
        if (!validationResult.isValid) {
            log.warn("비밀번호 유효성 검사 실패: {}", validationResult.errors.joinToString("\n"))
            throw IllegalArgumentException(validationResult.errors.joinToString("\n"))
        }
    }
}