package org.flowery.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration}") private val validityInMilliseconds: Long,
    private val redisTemplate: RedisTemplate<String, Any>
) {

    private val key: SecretKey by lazy { Keys.hmacShaKeyFor(secretKey.toByteArray()) }

    /**
     * 주어진 사용자명과 역할 목록으로 새로운 JWT 토큰을 생성합니다
     *
     * @param username 사용자 식별자
     * @param roles 사용자에게 할당된 역할 목록
     * @return 생성된 JWT 토큰 문자열
     */
    fun createToken(username: String, roles: List<String>): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출합니다
     *
     * @param request Authorization 헤더를 포함한 HTTP 요청
     * @return 'Bearer ' 접두사가 제거된 토큰 문자열, 또는 토큰이 없는 경우 null
     */
    fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    /**
     * JWT 토큰의 서명과 만료 여부를 검증합니다
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효한 경우 true, 그렇지 않은 경우 false
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            !claims.payload.expiration.before(Date())
        } catch (e: SecurityException) {
            false
        } catch (e: MalformedJwtException) {
            false
        } catch (e: ExpiredJwtException) {
            false
        } catch (e: UnsupportedJwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /**
     * 유효한 JWT 토큰으로부터 JwtAuthentication 객체를 생성합니다
     *
     * @param token 유효한 JWT 토큰
     * @return 사용자명과 역할 정보를 포함한 JwtAuthentication 객체
     * @throws JwtException 토큰이 유효하지 않거나 파싱할 수 없는 경우
     */
    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        val username = claims.subject
        @Suppress("UNCHECKED_CAST")
        val roles = claims["roles"] as List<String>
        val authorities = roles.map { SimpleGrantedAuthority(it) }

        return UsernamePasswordAuthenticationToken(
            /* principal = */ username,
            /* credentials = */ null,
            /* authorities = */ authorities
        )
    }

    /**
     * 토큰이 블랙리스트에 있는지(로그아웃 되었는지) 확인합니다
     *
     * @param token 확인할 JWT 토큰
     * @return 토큰이 블랙리스트에 있으면 true, 없으면 false
     */
    fun isTokenBlacklisted(token: String): Boolean {
        return redisTemplate.opsForValue().get(getBlacklistKey(token)) != null
    }

    /**
     * 토큰을 블랙리스트에 추가하고 만료 시간을 설정합니다
     *
     * @param token 블랙리스트에 추가할 JWT 토큰
     * @param remainingMills 블랙리스트 항목이 만료될 때까지의 시간(밀리초)
     */
    fun addToBlacklist(token: String, remainingMills: Long) {
        redisTemplate.opsForValue().set(getBlacklistKey(token), true, remainingMills, TimeUnit.MILLISECONDS)
    }

    /**
     * 블랙리스트에 저장된 토큰의 Redis 키를 생성합니다
     *
     * @param token JWT 토큰
     * @return Redis 키 문자열
     */
    private fun getBlacklistKey(token: String) = "blacklist:$token"
}