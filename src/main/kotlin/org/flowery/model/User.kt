package org.flowery.model

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(), // 고유 식별자
    val ident: String, // 로그인에 사용되는 사용자 식별자 (예: 이메일)
    val passwordHash: String, // 암호화된 비밀번호
    val name: String, // 사용자 이름
    val roles: Set<String> = setOf("ROLE_USER"), // 사용자 역할
    val createdAt: LocalDateTime = LocalDateTime.now(), // 계정 생성 일시
    val updatedAt: LocalDateTime = LocalDateTime.now(), // 마지막 업데이트 일시
)
