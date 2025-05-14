package com.example.eparking.utils

object ValidationUtils {
    private const val MIN_USERNAME_LENGTH = 3
    private const val MAX_USERNAME_LENGTH = 40
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_PASSWORD_LENGTH = 20

    fun isValidUsername(username: String): Boolean {
        return username.length in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH &&
                username.matches(Regex("^[a-zA-Z0-9._-]+$"))
    }

    fun isValidPassword(password: String): Boolean {
        return password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH &&
                password.any { it.isDigit() } &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) {
            return PasswordStrength(0, "Empty", android.graphics.Color.GRAY)
        }

        var score = 0
        var hasUppercase = false
        var hasLowercase = false
        var hasDigit = false
        var hasSpecial = false

        // Check for character types
        password.forEach { char ->
            when {
                char.isUpperCase() -> hasUppercase = true
                char.isLowerCase() -> hasLowercase = true
                char.isDigit() -> hasDigit = true
                !char.isLetterOrDigit() -> hasSpecial = true
            }
        }

        // Calculate score based on length and character types
        score += password.length * 4 // Base score from length
        if (hasUppercase) score += 10
        if (hasLowercase) score += 10
        if (hasDigit) score += 10
        if (hasSpecial) score += 15

        // Cap the score at 100
        score = score.coerceAtMost(100)

        return when {
            score < 40 -> PasswordStrength(score, "Weak", android.graphics.Color.RED)
            score < 70 -> PasswordStrength(score, "Medium", android.graphics.Color.rgb(255, 165, 0)) // Orange
            else -> PasswordStrength(score, "Strong", android.graphics.Color.GREEN)
        }
    }

    data class PasswordStrength(
        val score: Int,
        val label: String,
        val color: Int
    )
} 