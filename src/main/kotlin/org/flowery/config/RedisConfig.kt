package org.flowery.config

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.nio.charset.Charset
import java.time.LocalDateTime


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
     * LocalDateTime을 위한 커스텀 TypeAdapter
     */
    private class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
        override fun write(out: JsonWriter, value: LocalDateTime?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value.toString())
            }
        }

        override fun read(input: JsonReader): LocalDateTime? {
            val value = input.nextString()
            return if (value == null) null else LocalDateTime.parse(value)
        }
    }

    @Bean
    fun gsonRedisSerializer(): RedisSerializer<Any> {
        return object : RedisSerializer<Any> {
            private val gson = GsonBuilder()
                .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
                .create()

            override fun serialize(source: Any?): ByteArray {
                return if (source == null) {
                    ByteArray(0)
                } else {
                    gson.toJson(source).toByteArray(Charset.forName("UTF-8"))
                }
            }

            override fun deserialize(bytes: ByteArray?): Any? {
                return if (bytes == null || bytes.isEmpty()) {
                    null
                } else {
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