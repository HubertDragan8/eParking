package com.example.eparking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authManager = AuthManager(this)

        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)

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
} 