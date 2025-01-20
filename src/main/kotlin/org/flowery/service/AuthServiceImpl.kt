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

@Service
class AuthServiceImpl(
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val emailSender: JavaMailSender,
    private val authRepository: AuthRepository,
) : AuthService {

    /**
     * 회원가입 요청을 처리하여 사용자 정보를 저장하고 반환합니다.
     *
     * @param signUpRequestDto 회원가입 요청 DTO
     * @return 회원가입 응답 DTO를 포함한 Mono
     */
    override fun signUp(signUpRequestDto: SignUpRequestDto): Mono<SignUpResponseDto> {
        // 패스워드 유효성 검사
        validatePassword(signUpRequestDto.password)

        // 패스워드 해싱
        val passwordHash = passwordEncoder.encode(signUpRequestDto.password)

        // 사용자 정보 저장
        val user = User(
            ident = signUpRequestDto.ident,
            passwordHash = passwordHash,
            name = signUpRequestDto.userName,
            roles = setOf("ROLE_USER")
        )
        redisTemplate.opsForHash<String, Any>().put("users", user.ident, user)

        return Mono.just(SignUpResponseDto(ident = user.ident, userName = user.name))
    }

    /**
     * 로그인 요청을 처리하여 JWT 토큰을 생성하고 반환합니다.
     *
     * @param loginRequestDto 로그인 요청 DTO
     * @return 로그인 응답 DTO를 포함한 Mono
     */
    override fun login(loginRequestDto: LoginRequestDto): Mono<LoginResponseDto> {
        // Redis에서 사용자 정보 조회
        val user = redisTemplate.opsForHash<String, Any>().get("users", loginRequestDto.ident) as? User

        return if (user != null &&
            passwordEncoder.matches(loginRequestDto.password, user.passwordHash)
        ) {
            val token = jwtProvider.createToken(user.ident, user.roles)
            Mono.just(LoginResponseDto(token = token, ident = user.ident, roles = user.roles))
        } else {
            Mono.error(IllegalArgumentException("Invalid ident or password"))
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
            authRepository.saveAuthCode(data.ident, randNum, 5)

            val message = SimpleMailMessage().apply {
                setFrom("hyu.flowery@gmail.com")      // 호스트 이메일
                setTo(data.ident)                 // 클라이언트 이메일
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
        val savedCode = authRepository.getAuthCode(data.ident) // redis에서 인증코드 조회

        return if (savedCode == null){
            "Verification failed: No code found for this email."
        } else if (data.userCode == savedCode){
            authRepository.deleteAuthCode(data.ident) // 인증 성공 후 인증 코드 삭제
            "Verification successful!"
        }
        else {
            "Verification failed: Invalid code."
        }

    }

    /*
    * 로그아웃 시 토큰을 블랙리스트에 올리기
    *
    * (JWTProvider에서 블랙리스트에 토큰을 추가)
    * */
    fun logout(token: String){
        val remainingMills = 3600000L // 1시간 후 만료
        jwtProvider.addToBlacklist(token, remainingMills)
    }

    /**
     * 패스워드 유효성을 검사합니다.
     * @param password 검사할 패스워드
     * @throws IllegalArgumentException 패스워드가 유효하지 않을 경우
     */
    private fun validatePassword(password: String) {
        val validationResult = PasswordValidator.validate(password)
        if (!validationResult.isValid) {
            throw IllegalArgumentException(validationResult.errors.joinToString("\n"))
        }
    }

}
