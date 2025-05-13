package com.example.eparking

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class LoginActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var loginButton: Button

    companion object {
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_REMEMBER_ME = "rememberMe"
        private const val KEY_SAVED_USERNAME = "savedUsername"
        private const val KEY_SAVED_PASSWORD = "savedPassword"
        private const val ENCRYPTION_KEY = "YourSecretKey123" // In production, use a more secure key
        private const val MAX_USERNAME_LENGTH = 40
        private const val MAX_PASSWORD_LENGTH = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authManager = AuthManager(this)

        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        initializeViews()
        setupTextWatchers()
        checkSavedCredentials()

        loginButton.setOnClickListener {
            if (!loginButton.isEnabled) {
                Toast.makeText(this, "Please fill in all required fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            if (authManager.login(username, password)) {
                handleRememberMe(username, password)
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.registerButton).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun initializeViews() {
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        usernameLayout = findViewById(R.id.usernameLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox)
        loginButton = findViewById(R.id.loginButton)
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateLoginForm()
            }
        }

        usernameInput.addTextChangedListener(textWatcher)
        passwordInput.addTextChangedListener(textWatcher)
    }

    private fun validateLoginForm() {
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()

        // Clear previous errors
        usernameLayout.error = null
        passwordLayout.error = null

        // Validate username length
        if (username.length > MAX_USERNAME_LENGTH) {
            usernameLayout.error = "Username must be at most $MAX_USERNAME_LENGTH characters"
        }

        // Validate password length
        if (password.length > MAX_PASSWORD_LENGTH) {
            passwordLayout.error = "Password must be at most $MAX_PASSWORD_LENGTH characters"
        }

        // Validate required fields
        if (username.isEmpty()) {
            usernameLayout.error = "Username is required"
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Password is required"
        }

        // Update button state
        loginButton.isEnabled = ValidationUtils.isLoginFormValid(username, password) &&
                username.length <= MAX_USERNAME_LENGTH &&
                password.length <= MAX_PASSWORD_LENGTH
    }

    private fun checkSavedCredentials() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)

        if (rememberMe) {
            val savedUsername = prefs.getString(KEY_SAVED_USERNAME, null)
            val savedPassword = prefs.getString(KEY_SAVED_PASSWORD, null)

            if (savedUsername != null && savedPassword != null) {
                usernameInput.setText(savedUsername)
                passwordInput.setText(decryptPassword(savedPassword))
                rememberMeCheckbox.isChecked = true
            }
        }
    }

    private fun handleRememberMe(username: String, password: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()

        if (rememberMeCheckbox.isChecked) {
            editor.putBoolean(KEY_REMEMBER_ME, true)
            editor.putString(KEY_SAVED_USERNAME, username)
            editor.putString(KEY_SAVED_PASSWORD, encryptPassword(password))
        } else {
            editor.clear()
        }
        editor.apply()
    }

    private fun encryptPassword(password: String): String {
        val key = generateKey()
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(password.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun decryptPassword(encryptedPassword: String): String {
        val key = generateKey()
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedPassword, Base64.DEFAULT))
        return String(decryptedBytes)
    }

    private fun generateKey(): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(ENCRYPTION_KEY.toCharArray(), "salt".toByteArray(), 65536, 128)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    override fun onBackPressed() {
        finishAffinity()
    }
} 