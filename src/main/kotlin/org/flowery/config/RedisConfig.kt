package org.flowery.config

import com.google.gson.Gson
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.nio.charset.Charset


@Configuration
class RedisConfig {

    /**
     * Redis 서버 연결을 위한 ConnectionFactory Bean
     *
     * @return LettuceConnectionFactory - Redis 연결을 관리하는 Factory 객체
     */
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        // localhost:6379로 Redis 서버 연결 설정
        return LettuceConnectionFactory("localhost", 6379)
    }

    /**
     * Gson을 사용하여 객체를 JSON으로 직렬화/역직렬화하는 RedisSerializer Bean
     *
     * @return RedisSerializer<Any> - Gson 기반의 커스텀 Redis 직렬화
     */
    @Bean
    fun gsonRedisSerializer(): RedisSerializer<Any> {
        return object : RedisSerializer<Any> {
            private val gson = Gson()

            /**
             * 객체를 JSON 문자열로 변환 후 바이트 배열로 직렬화
             *
             * @param source 직렬화할 객체
             * @return ByteArray 직렬화된 바이트 배열
             */
            override fun serialize(source: Any?): ByteArray {
                return if (source == null) {
                    // null인 경우 빈 바이트 배열 반환
                    ByteArray(0)
                } else {
                    // 객체를 JSON 문자열로 변환 후 UTF-8 인코딩된 바이트 배열로 변환
                    gson.toJson(source).toByteArray(Charset.forName("UTF-8"))
                }
            }

            /**
             * 바이트 배열을 JSON 문자열로 변환 후 객체로 역직렬화
             *
             * @param bytes 역직렬화할 바이트 배열
             * @return Any? 역직렬화된 객체
             */
            override fun deserialize(bytes: ByteArray?): Any? {
                return if (bytes == null || bytes.isEmpty()) {
                    // 바이트 배열이 null이거나 비어있으면 null 반환
                    null
                } else {
                    // 바이트 배열을 UTF-8 문자열로 변환 후 객체로 역직렬화
                    val str = String(bytes, Charset.forName("UTF-8"))
                    gson.fromJson(str, Any::class.java)
                }
            }
        }
    }

    /**
     * Redis 작업을 위한 RedisTemplate Bean
     *
     * @param connectionFactory Redis 연결 Factory
     * @return RedisTemplate<String, Any> 설정이 완료된 RedisTemplate
     */
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        // Redis 연결 설정
        redisTemplate.connectionFactory = connectionFactory

        // 일반 Key-Value 작업을 위한 직렬화 설정
        redisTemplate.keySerializer = StringRedisSerializer()  // 키는 일반 문자열로 직렬화
        redisTemplate.valueSerializer = gsonRedisSerializer() // 값은 JSON으로 직렬화

        // Hash 작업을 위한 직렬화 설정
        redisTemplate.hashKeySerializer = StringRedisSerializer()  // Hash의 키는 일반 문자열로 직렬화
        redisTemplate.hashValueSerializer = gsonRedisSerializer() // Hash의 값은 JSON으로 직렬화

        return redisTemplate
    }
}