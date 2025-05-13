package com.example.eparking

import android.util.Patterns
import android.widget.EditText

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidUsername(username: String): Boolean {
        return username.length >= 3 && username.all { it.isLetterOrDigit() }
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }
    }

    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun isLoginFormValid(username: String, password: String): Boolean {
        return username.isNotEmpty() && password.isNotEmpty()
    }

    fun isRegisterFormValid(
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return isValidUsername(username) &&
                isValidPassword(password) &&
                doPasswordsMatch(password, confirmPassword)
    }

    fun EditText.setErrorIfInvalid(errorMessage: String?) {
        error = errorMessage
    }
} 