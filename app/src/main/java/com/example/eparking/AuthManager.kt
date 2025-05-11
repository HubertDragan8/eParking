package com.example.eparking

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "AuthPrefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val MIN_PASSWORD_LENGTH = 6
    }

    fun validatePassword(password: String): PasswordValidationResult {
        if (password.isEmpty()) {
            return PasswordValidationResult.EMPTY
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return PasswordValidationResult.TOO_SHORT
        }
        if (!password.any { it.isUpperCase() }) {
            return PasswordValidationResult.NO_UPPERCASE
        }
        if (!password.any { it.isDigit() }) {
            return PasswordValidationResult.NO_DIGIT
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            return PasswordValidationResult.NO_SPECIAL_CHAR
        }
        return PasswordValidationResult.VALID
    }

    fun register(username: String, password: String): RegistrationResult {
        if (username.isEmpty()) {
            return RegistrationResult.EMPTY_USERNAME
        }

        val passwordValidation = validatePassword(password)
        if (passwordValidation != PasswordValidationResult.VALID) {
            return RegistrationResult.INVALID_PASSWORD
        }

        // Check if username already exists
        if (sharedPreferences.getString(KEY_USERNAME, "") == username) {
            return RegistrationResult.USERNAME_EXISTS
        }

        val success = sharedPreferences.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }.commit()

        return if (success) RegistrationResult.SUCCESS else RegistrationResult.ERROR
    }

    fun login(username: String, password: String): Boolean {
        if (username.isEmpty() || password.isEmpty()) {
            return false
        }

        val savedUsername = sharedPreferences.getString(KEY_USERNAME, "")
        val savedPassword = sharedPreferences.getString(KEY_PASSWORD, "")

        return if (username == savedUsername && password == savedPassword) {
            sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
            true
        } else {
            false
        }
    }

    fun logout() {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getCurrentUsername(): String {
        return sharedPreferences.getString(KEY_USERNAME, "") ?: ""
    }

    enum class PasswordValidationResult {
        VALID,
        EMPTY,
        TOO_SHORT,
        NO_UPPERCASE,
        NO_DIGIT,
        NO_SPECIAL_CHAR
    }

    enum class RegistrationResult {
        SUCCESS,
        EMPTY_USERNAME,
        INVALID_PASSWORD,
        USERNAME_EXISTS,
        ERROR
    }
} 