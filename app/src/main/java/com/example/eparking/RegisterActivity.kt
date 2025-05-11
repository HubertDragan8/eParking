package com.example.eparking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class RegisterActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var passwordStrengthMeter: ProgressBar
    private lateinit var passwordStrengthText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authManager = AuthManager(this)

        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        passwordStrengthMeter = findViewById(R.id.passwordStrengthMeter)
        passwordStrengthText = findViewById(R.id.passwordStrengthText)

        // Set up password strength monitoring
        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePasswordStrength(s.toString())
            }
        })

        findViewById<Button>(R.id.registerButton).setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            // Clear previous errors
            usernameInput.error = null
            passwordInput.error = null
            confirmPasswordInput.error = null

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordInput.error = "Passwords do not match"
                return@setOnClickListener
            }

            when (authManager.register(username, password)) {
                AuthManager.RegistrationResult.SUCCESS -> {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                AuthManager.RegistrationResult.EMPTY_USERNAME -> {
                    usernameInput.error = "Username cannot be empty"
                }
                AuthManager.RegistrationResult.INVALID_PASSWORD -> {
                    when (authManager.validatePassword(password)) {
                        AuthManager.PasswordValidationResult.EMPTY -> {
                            passwordInput.error = "Password cannot be empty"
                        }
                        AuthManager.PasswordValidationResult.TOO_SHORT -> {
                            passwordInput.error = "Password must be at least 6 characters"
                        }
                        AuthManager.PasswordValidationResult.NO_UPPERCASE -> {
                            passwordInput.error = "Password must contain at least one uppercase letter"
                        }
                        AuthManager.PasswordValidationResult.NO_DIGIT -> {
                            passwordInput.error = "Password must contain at least one digit"
                        }
                        AuthManager.PasswordValidationResult.NO_SPECIAL_CHAR -> {
                            passwordInput.error = "Password must contain at least one special character"
                        }
                        else -> {
                            passwordInput.error = "Invalid password"
                        }
                    }
                }
                AuthManager.RegistrationResult.USERNAME_EXISTS -> {
                    usernameInput.error = "Username already exists"
                }
                AuthManager.RegistrationResult.ERROR -> {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updatePasswordStrength(password: String) {
        val strength = calculatePasswordStrength(password)
        passwordStrengthMeter.progress = strength.score
        passwordStrengthText.text = strength.label
        passwordStrengthText.setTextColor(strength.color)
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) {
            return PasswordStrength(0, "Empty", Color.GRAY)
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
            score < 40 -> PasswordStrength(score, "Weak", Color.RED)
            score < 70 -> PasswordStrength(score, "Medium", Color.rgb(255, 165, 0)) // Orange
            else -> PasswordStrength(score, "Strong", Color.GREEN)
        }
    }

    private data class PasswordStrength(
        val score: Int,
        val label: String,
        val color: Int
    )
} 