package com.example.eparking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var passwordStrengthMeter: ProgressBar
    private lateinit var passwordStrengthText: TextView
    private lateinit var registerButton: Button

    companion object {
        private const val MAX_USERNAME_LENGTH = 40
        private const val MAX_PASSWORD_LENGTH = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authManager = AuthManager(this)
        initializeViews()
        setupTextWatchers()

        registerButton.setOnClickListener {
            if (!registerButton.isEnabled) {
                Toast.makeText(this, "Please fill in all required fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            when (authManager.register(username, password)) {
                AuthManager.RegistrationResult.SUCCESS -> {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                AuthManager.RegistrationResult.USERNAME_EXISTS -> {
                    usernameLayout.error = "Username already exists"
                }
                else -> {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initializeViews() {
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        usernameLayout = findViewById(R.id.usernameLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)
        passwordStrengthMeter = findViewById(R.id.passwordStrengthMeter)
        passwordStrengthText = findViewById(R.id.passwordStrengthText)
        registerButton = findViewById(R.id.registerButton)
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateRegisterForm()
            }
        }

        usernameInput.addTextChangedListener(textWatcher)
        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePasswordStrength(s.toString())
                validateRegisterForm()
            }
        })
        confirmPasswordInput.addTextChangedListener(textWatcher)
    }

    private fun validateRegisterForm() {
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        // Clear previous errors
        usernameLayout.error = null
        passwordLayout.error = null
        confirmPasswordLayout.error = null

        // Validate username length
        if (username.length > MAX_USERNAME_LENGTH) {
            usernameLayout.error = "Username must be at most $MAX_USERNAME_LENGTH characters"
        }

        // Validate password length
        if (password.length > MAX_PASSWORD_LENGTH) {
            passwordLayout.error = "Password must be at most $MAX_PASSWORD_LENGTH characters"
        }

        // Validate confirm password length
        if (confirmPassword.length > MAX_PASSWORD_LENGTH) {
            confirmPasswordLayout.error = "Password must be at most $MAX_PASSWORD_LENGTH characters"
        }

        // Validate username
        if (!ValidationUtils.isValidUsername(username)) {
            usernameLayout.error = "Username must be at least 3 characters and contain only letters and numbers"
        }

        // Validate password
        if (!ValidationUtils.isValidPassword(password)) {
            passwordLayout.error = "Password must be at least 6 characters and contain uppercase, digit, and special character"
        }

        // Validate confirm password
        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            confirmPasswordLayout.error = "Passwords do not match"
        }

        // Update button state
        registerButton.isEnabled = ValidationUtils.isRegisterFormValid(username, password, confirmPassword) &&
                username.length <= MAX_USERNAME_LENGTH &&
                password.length <= MAX_PASSWORD_LENGTH &&
                confirmPassword.length <= MAX_PASSWORD_LENGTH
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