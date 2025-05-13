package com.example.eparking

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class LoginActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var loginButton: Button
    private var isPasswordVisible = false

    companion object {
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_REMEMBER_ME = "rememberMe"
        private const val KEY_SAVED_USERNAME = "savedUsername"
        private const val KEY_SAVED_PASSWORD = "savedPassword"
        private const val ENCRYPTION_KEY = "YourSecretKey123" // In production, use a more secure key
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
        setupPasswordVisibilityToggle()
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
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox)
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle)
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
        usernameInput.error = null
        passwordInput.error = null

        // Validate fields
        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
        }

        // Update button state
        loginButton.isEnabled = ValidationUtils.isLoginFormValid(username, password)
    }

    private fun setupPasswordVisibilityToggle() {
        passwordVisibilityToggle.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        
        // Toggle password visibility
        val inputType = if (isPasswordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        
        // Save cursor position
        val cursorPosition = passwordInput.selectionStart
        
        // Update input type
        passwordInput.inputType = inputType
        
        // Restore cursor position
        passwordInput.setSelection(cursorPosition)
        
        // Animate the icon
        val fadeOut = AlphaAnimation(1f, 0.3f)
        fadeOut.duration = 100
        val fadeIn = AlphaAnimation(0.3f, 1f)
        fadeIn.duration = 100
        
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                passwordVisibilityToggle.startAnimation(fadeIn)
            }
        })
        
        passwordVisibilityToggle.startAnimation(fadeOut)
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