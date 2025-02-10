package org.flowery.utils

import org.flowery.dto.ValidationResult

class PasswordValidator {
    companion object {
        private val LENGTH_PATTERN = Regex(".{8,20}")  // 8-20자
        private val UPPERCASE_PATTERN = Regex(".*[A-Z].*")  // 대문자 포함
        private val LOWERCASE_PATTERN = Regex(".*[a-z].*")  // 소문자 포함
        private val NUMBER_PATTERN = Regex(".*\\d.*")  // 숫자 포함
        private val SPECIAL_CHAR_PATTERN = Regex(".*[!@#\$%^&*(),.?\":{}|<>].*")  // 특수문자 포함

        fun validate(password: String): ValidationResult {
            val errors = mutableListOf<String>()

            // 1) 비밀번호 길이 확인
            if (!password.matches(LENGTH_PATTERN)) {
                errors.add("비밀번호는 8자 이상 20자 이하이어야 합니다.")
            }

            // 2) 각 패턴별로 매칭되는 개수 확인
            val matchCount = listOf(
                UPPERCASE_PATTERN,
                LOWERCASE_PATTERN,
                NUMBER_PATTERN,
                SPECIAL_CHAR_PATTERN
            ).count { regex -> password.matches(regex) }

            // 3) 대문자/소문자/숫자/특수문자 중 2종류 이상 만족하는지 확인
            if (matchCount < 2) {
                errors.add("비밀번호는 대문자, 소문자, 숫자, 특수문자 중 최소 2종류 이상 포함해야 합니다.")
            }

            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors
            )
        }
    }
}
