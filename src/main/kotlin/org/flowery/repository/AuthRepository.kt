package org.flowery.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class AuthRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    /*
    * 인증 코드 저장
    *
    *  이메일 & 전송된 랜덤 인증코드 & TTL 설정
    * */

    fun saveAuthCode(email: String, code: String, ttlMinutes: Long){
        val ops = redisTemplate.opsForValue()
        println("Saving to Redis: email=$email, code=$code, ttl=$ttlMinutes")
        ops.set(email, code, ttlMinutes * 60, TimeUnit.SECONDS) // TTL 설정
    }

    // (redis에서) 인증 코드 가져오기
    fun getAuthCode(email: String): Any? {
        val ops = redisTemplate.opsForValue()
        println("Getting to Redis: email=$email")
        return ops.get(email)
    }

    // 인증 코드 삭제하기
    fun deleteAuthCode(email: String){
        println("Delete to Redis: email=$email")
        redisTemplate.delete(email)
    }
}