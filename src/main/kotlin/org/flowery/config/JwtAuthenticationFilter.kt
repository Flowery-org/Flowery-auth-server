package org.flowery.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.flowery.jwt.JwtProvider
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 토큰을 검증하고 인증 정보를 SecurityContext에 설정하는 필터
 *
 * @param jwtProvider JWT 토큰 처리를 위한 유틸리티 클래스
 */
@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {

    /**
     * 모든 요청에 대해 JWT 토큰을 검증하고 인증 처리를 수행합니다
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // Authorization 헤더에서 JWT 토큰 추출
            val token = jwtProvider.resolveToken(request)

            // 토큰이 유효하고 블랙리스트에 없는 경우 인증 처리
            if (token != null && jwtProvider.validateToken(token) && !jwtProvider.isTokenBlacklisted(token)) {
                val authentication = jwtProvider.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            // 토큰 처리 중 오류 발생 시 인증 컨텍스트 초기화
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}