package com.example.eparking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var confirmPasswordVisibilityToggle: ImageView
    private lateinit var registerButton: Button
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authManager = AuthManager(this)
        initializeViews()
        setupPasswordVisibilityToggles()
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
                    usernameInput.error = "Username already exists"
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
        passwordStrengthMeter = findViewById(R.id.passwordStrengthMeter)
        passwordStrengthText = findViewById(R.id.passwordStrengthText)
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle)
        confirmPasswordVisibilityToggle = findViewById(R.id.confirmPasswordVisibilityToggle)
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
        usernameInput.error = null
        passwordInput.error = null
        confirmPasswordInput.error = null

        // Validate username
        if (!ValidationUtils.isValidUsername(username)) {
            usernameInput.error = "Username must be at least 3 characters and contain only letters and numbers"
        }

        // Validate password
        if (!ValidationUtils.isValidPassword(password)) {
            passwordInput.error = "Password must be at least 6 characters and contain uppercase, digit, and special character"
        }

        // Validate confirm password
        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            confirmPasswordInput.error = "Passwords do not match"
        }

        // Update button state
        registerButton.isEnabled = ValidationUtils.isRegisterFormValid(username, password, confirmPassword)
    }

    private fun setupPasswordVisibilityToggles() {
        passwordVisibilityToggle.setOnClickListener {
            togglePasswordVisibility(passwordInput, passwordVisibilityToggle, isPasswordVisible) { visible ->
                isPasswordVisible = visible
            }
        }

        confirmPasswordVisibilityToggle.setOnClickListener {
            togglePasswordVisibility(confirmPasswordInput, confirmPasswordVisibilityToggle, isConfirmPasswordVisible) { visible ->
                isConfirmPasswordVisible = visible
            }
        }
    }

    private fun togglePasswordVisibility(
        editText: EditText,
        toggle: ImageView,
        isVisible: Boolean,
        onVisibilityChanged: (Boolean) -> Unit
    ) {
        val newVisibility = !isVisible
        
        // Toggle password visibility
        val inputType = if (newVisibility) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        
        // Save cursor position
        val cursorPosition = editText.selectionStart
        
        // Update input type
        editText.inputType = inputType
        
        // Restore cursor position
        editText.setSelection(cursorPosition)
        
        // Animate the icon
        val fadeOut = AlphaAnimation(1f, 0.3f)
        fadeOut.duration = 100
        val fadeIn = AlphaAnimation(0.3f, 1f)
        fadeIn.duration = 100
        
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                toggle.startAnimation(fadeIn)
            }
        })
        
        toggle.startAnimation(fadeOut)
        onVisibilityChanged(newVisibility)
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