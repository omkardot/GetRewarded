package com.varram.taskquest.Login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.varram.taskquest.R

import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varram.taskquest.Helpers.RewaredSharedPref

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: RewaredSharedPref
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private var RC_SIGN_IN: Int = 1001
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvSignUp: TextView
    private lateinit var tvTitle: TextView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Initialize Firebase Auth
        FirebaseApp.initializeApp(LoginActivity())
        auth = FirebaseAuth.getInstance()
        prefs = RewaredSharedPref(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        initViews()
        styleTitle()
        styleSignUpText()
        setClickListeners()
    }

    private fun initViews() {
        etEmail          = findViewById(R.id.etEmail)
        etPassword       = findViewById(R.id.etPassword)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        btnLogin         = findViewById(R.id.btnLogin)
        btnGoogle        = findViewById(R.id.btnGoogle)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvSignUp         = findViewById(R.id.tvSignUp)
        tvTitle          = findViewById(R.id.tvTitle)
    }

    /**
     * "GET" in black, "REWARDED" in red (#E53935)
     */
    private fun styleTitle() {
        val full = "TASKQUEST"
        val spannable = SpannableString(full)
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#1A1A1A")),
            0, 4,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#E53935")),
            4, full.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvTitle.text = spannable
    }

    /**
     * "New here? " in grey, "Create an account" in red
     */
    private fun styleSignUpText() {
        val full = "New here? Create an account"
        val spannable = SpannableString(full)
        val startIndex = full.indexOf("Create an account")
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#E53935")),
            startIndex, full.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvSignUp.text = spannable
    }

    private fun setClickListeners() {

        // Toggle password visibility
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
//                ivTogglePassword.setImageResource(R.drawable.ic_visibility_off)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
//                ivTogglePassword.setImageResource(R.drawable.ic_visibility)
            }
            // Keep cursor at end
            etPassword.setSelection(etPassword.text.length)
        }

        // Login button
        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (!validateInputs(email, password)) return@setOnClickListener

            // TODO: Replace with your actual auth logic (Firebase, Retrofit, etc.)
            performLogin(email, password)
        }

        // Forgot password
        tvForgotPassword.setOnClickListener {
            // TODO: Navigate to ForgotPasswordActivity
            Toast.makeText(this, "Forgot password tapped", Toast.LENGTH_SHORT).show()
        }

        // Google sign-in
        btnGoogle.setOnClickListener {

        }

        // Create account
        tvSignUp.setOnClickListener {
            // TODO: Navigate to RegisterActivity
             startActivity(Intent(this, RegisterUserActivity::class.java))
            Toast.makeText(this, "Navigate to register screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email address"
            etEmail.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return false
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return false
        }
        return true
    }

    private fun performLogin(email: String, password: String) {
        // Example: show a loading state
        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        // Simulate async (replace with real call)
        btnLogin.postDelayed({
            btnLogin.isEnabled = true
            btnLogin.text = "Login to Flow ⚡"

            // TODO: On success → navigate to HomeActivity
            // startActivity(Intent(this, HomeActivity::class.java))
            // finish()

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        }, 1500)
    }
}