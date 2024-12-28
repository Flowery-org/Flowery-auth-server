package org.flowery

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [FloweryAuthServerApplication::class])
@TestPropertySource(properties = [
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
])
class FloweryAuthServerApplicationTests {

    @Autowired
    private lateinit var redisTemplate: StringRedisTemplate

    @BeforeEach
    fun setUp() {
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun whenSetKeyValue_thenCanRetrieveIt() {
        // given
        val key = "test:key"
        val value = "test-value"

        // when
        redisTemplate.opsForValue().set(key, value)

        // then
        val retrievedValue = redisTemplate.opsForValue().get(key)
        assertThat(retrievedValue).isEqualTo(value)
    }

    @Test
    fun whenKeyExpires_thenKeyIsRemoved() {
        // given
        val key = "test:expired-key"
        val value = "test-value"

        // when
        redisTemplate.opsForValue().set(key, value)
        redisTemplate.expire(key, java.time.Duration.ofSeconds(1))

        // then
        Thread.sleep(1100) // Wait for key to expire
        val exists = redisTemplate.hasKey(key)
        assertThat(exists).isFalse()
    }

    @Test
    fun whenDeleteKey_thenKeyIsRemoved() {
        // given
        val key = "test:delete-key"
        val value = "test-value"

        // when
        redisTemplate.opsForValue().set(key, value)
        redisTemplate.delete(key)

        // then
        val exists = redisTemplate.hasKey(key)
        assertThat(exists).isFalse()
    }
}