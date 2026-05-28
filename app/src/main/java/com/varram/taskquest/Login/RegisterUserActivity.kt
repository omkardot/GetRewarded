package com.varram.taskquest.Login

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.varram.taskquest.R

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.varram.taskquest.Helpers.RewaredSharedPref
import com.varram.taskquest.MainActivity

class RegisterUserActivity : AppCompatActivity() {

    // ── Views ──────────────────────────────────────────────────────────────
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var btnSignUp: Button
    private lateinit var btnGoogle: Button
    private lateinit var btnApple: Button
    private lateinit var tvLogin: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: RewaredSharedPref
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isPasswordVisible = false

    private var RC_SIGN_IN: Int = 1001
    // ── Lifecycle ──────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        FirebaseApp.initializeApp(LoginActivity())
        auth = FirebaseAuth.getInstance()
        prefs = RewaredSharedPref(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        initViews()
        styleLoginText()
        setClickListeners()
    }

    // ── Init ───────────────────────────────────────────────────────────────
    private fun initViews() {
        etFullName       = findViewById(R.id.etFullName)
        etEmail          = findViewById(R.id.etEmail)
        etPassword       = findViewById(R.id.etPassword)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        btnSignUp        = findViewById(R.id.btnSignUp)
        btnGoogle        = findViewById(R.id.btnGoogle)
        btnApple         = findViewById(R.id.btnApple)
        tvLogin          = findViewById(R.id.tvLogin)
    }

    /**
     * "Already part of the team? " grey  |  "Login here" red
     */
    private fun styleLoginText() {
        val full      = "Already part of the team? Login here"
        val spannable = SpannableString(full)
        val start     = full.indexOf("Login here")
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#E53935")),
            start, full.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvLogin.text = spannable
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
//                showError("Google sign-in failed: ${e.message}")
                Log.e(TAG, "Google sign-in failed", e)
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveUserToFirestore(auth.currentUser, "google")
//                insertDataToDB(auth.currentUser,"google")
            } else {
                Toast.makeText(this,"Google login failed: ${task.exception?.message}",Toast.LENGTH_SHORT).show()
            }
        }
    }
    // ── Click Listeners ────────────────────────────────────────────────────
    private fun setClickListeners() {

        // Password visibility toggle
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.transformationMethod = if (isPasswordVisible)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()
//            ivTogglePassword.setImageResource(
////                if (isPasswordVisible) R.drawable.ic_visibility_off
////                else R.drawable.ic_visibility
//            )
            etPassword.setSelection(etPassword.text.length)
        }

        // Sign-up button
        btnSignUp.setOnClickListener {
            val name     = etFullName.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (!validateInputs(name, email, password)) return@setOnClickListener

            performRegistration(name, email, password)
        }

        // Google sign-up
        btnGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }

        // Apple sign-up
        btnApple.setOnClickListener {
            // TODO: Integrate Apple Sign-In
            Toast.makeText(this, "Apple sign-up coming soon", Toast.LENGTH_SHORT).show()
        }

        // Navigate back to Login
        tvLogin.setOnClickListener {
            finish()   // Pops RegisterActivity → returns to LoginActivity
            // Or: startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    // ── Validation ─────────────────────────────────────────────────────────
    private fun validateInputs(name: String, email: String, password: String): Boolean {
        // Full name
        if (name.isEmpty()) {
            etFullName.error = "Full name is required"
            etFullName.requestFocus()
            return false
        }
        if (name.length < 2) {
            etFullName.error = "Enter a valid full name"
            etFullName.requestFocus()
            return false
        }

        // Email
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

        // Password
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return false
        }
        if (password.length < 8) {
            etPassword.error = "Password must be at least 8 characters"
            etPassword.requestFocus()
            return false
        }
        if (!password.any { it.isUpperCase() }) {
            etPassword.error = "Password must contain at least one uppercase letter"
            etPassword.requestFocus()
            return false
        }
        if (!password.any { it.isDigit() }) {
            etPassword.error = "Password must contain at least one number"
            etPassword.requestFocus()
            return false
        }

        return true
    }

    // ── Registration Logic ─────────────────────────────────────────────────
    private fun performRegistration(name: String, email: String, password: String) {
        // Show loading state
        btnSignUp.isEnabled = false
        btnSignUp.text = "Creating account..."
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update profile with name
                    val user = auth.currentUser
                    saveUserToFirestore(user, "email", name)
                } else {
                    Toast.makeText(this,"Sign up failed: ${task.exception?.message}",Toast.LENGTH_SHORT).show()
                }
            }
        btnSignUp.postDelayed({
            btnSignUp.isEnabled = true
            btnSignUp.text = "SIGN UP NOW"

            // TODO: On success → navigate to HomeActivity or email verification screen
            // startActivity(Intent(this, HomeActivity::class.java))
            // finish()

            Toast.makeText(
                this,
                "Welcome, $name! Account created successfully.",
                Toast.LENGTH_SHORT
            ).show()
        }, 1500)
    }

    private fun saveUserToFirestore(user: FirebaseUser?, provider: String, displayName: String? = null) {
        if (user == null) return

        val userRef = firestore.collection("login_details").document(user.uid)

        userRef.get().addOnSuccessListener { document ->
            val now = FieldValue.serverTimestamp()
            if (document.exists()) {
                // Update last login
                userRef.update("lastLogin", now).addOnSuccessListener {
                    navigateToHome()
                }
            } else {
                // Create new record
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "name" to (displayName ?: user.displayName),
                    "email" to user.email,
                    "provider" to provider,
                    "createdAt" to now,
                    "lastLogin" to now,
                    "profilePicUrl" to user.photoUrl?.toString()
                )
                userRef.set(userData).addOnSuccessListener {
                    navigateToHome()
                }
            }
        }.addOnFailureListener { e ->
            Log.e("ERROR",e.message.toString())
            Toast.makeText(this,"Failed ",Toast.LENGTH_SHORT).show()
        }
    }
    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        prefs.saveBoolean("isLoggedIn", true)
    }
}