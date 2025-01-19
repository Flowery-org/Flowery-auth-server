package org.flowery.controller

import jakarta.servlet.http.HttpServletRequest
import org.flowery.dto.*
import org.flowery.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/auth")
class AuthenticationController(
    private val authService: AuthService
) {

    @PostMapping("/sign-up")
    fun signUp(@RequestBody @Validated signUpRequestDto: SignUpRequestDto): Mono<ResponseEntity<SignUpResponseDto>> {
        return authService.signUp(signUpRequestDto)
            .map { response ->
                ResponseEntity.ok(response)
            }
            .onErrorResume { e ->
                when (e) {
                    is IllegalArgumentException -> Mono.just(ResponseEntity.badRequest().build())
                    is IllegalStateException -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build())
                    else -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                }
            }
    }

    /**
     * 로그인 엔드포인트
     *
     * @param loginRequestDto 로그인 요청 DTO
     * @return 로그인 응답 DTO를 포함한 ResponseEntity
     */
    @PostMapping("/user")
    fun login(@RequestBody @Validated loginRequestDto: LoginRequestDto): Mono<ResponseEntity<LoginResponseDto>> {
        return authService.login(loginRequestDto)
            .map { response ->
                ResponseEntity.status(HttpStatus.OK).body(response)
            }
            .onErrorResume { e ->
                when (e) {
                    is IllegalArgumentException -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
                    else -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                }
            }
    }

    /**
    * SMTP 이메일 전송
    *
    * @param emailSendDto 이메일 인증 코드 요청 DTO
    * @return 이메일 인증 코드 전송 결과 응답
    */
    @PostMapping("/email")
    @ResponseBody
    fun endEmail(@RequestBody emailSendDto: EmailSendDto): String {
        return authService.sendEmailMessage(emailSendDto)
    }

    /**
        이메일 인증

        @param emailVerificationDto 인증 코드 검증 DTO
        @return 인증 코드 검증 결과 응답
    */
    @PostMapping("/verification")
    fun verifyCode(@RequestBody emailVerificationDto: EmailVerificationDto): String {
        return authService.verificationEmail(emailVerificationDto)
    }
}
