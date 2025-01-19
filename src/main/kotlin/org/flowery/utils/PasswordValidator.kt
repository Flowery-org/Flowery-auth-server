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

            if (!password.matches(LENGTH_PATTERN)) {
                errors.add("Password must be between 8 and 20 characters")
            }
            if (!password.matches(UPPERCASE_PATTERN)) {
                errors.add("Password must contain at least one uppercase letter")
            }
            if (!password.matches(LOWERCASE_PATTERN)) {
                errors.add("Password must contain at least one lowercase letter")
            }
            if (!password.matches(NUMBER_PATTERN)) {
                errors.add("Password must contain at least one number")
            }
            if (!password.matches(SPECIAL_CHAR_PATTERN)) {
                errors.add("Password must contain at least one special character")
            }

            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors
            )
        }
    }
}
