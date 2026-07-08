package com.varram.taskquest.Login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.varram.taskquest.Helpers.RewaredSharedPref
import com.varram.taskquest.MainActivity
import com.varram.taskquest.R
import com.varram.taskquest.TaskQuestSecuredPrefrences

/**
 * SplashActivity
 *
 * Renders the GetRewarded splash screen matching the Stitch design:
 *  - Radial warm-white background
 *  - Animated entry for identity cluster (logo + name + divider)
 *  - Animated entry for footer cluster (tagline + loading dots + CTA)
 *  - Gradient text shader on the app name
 *  - Bouncing loading dots animation
 *  - "START YOUR FLOW" button navigates to MainActivity
 *
 * SETUP NOTES:
 *  1. Add to AndroidManifest.xml:
 *       <activity android:name=".SplashActivity"
 *                 android:theme="@style/Theme.GetRewarded.Splash"
 *                 android:exported="true">
 *           <intent-filter>
 *               <action android:name="android.intent.action.MAIN"/>
 *               <category android:name="android.intent.category.LAUNCHER"/>
 *           </intent-filter>
 *       </activity>
 *
 *  2. Add to build.gradle (app):
 *       implementation "androidx.core:core-splashscreen:1.0.1"
 *       implementation "androidx.cardview:cardview:1.0.0"
 *
 *  3. Place your logo image in res/drawable/ic_app_logo.png (or use Glide/Coil to load from URL)
 *
 *  4. Add fonts to res/font/:
 *       - lexend.ttf  (Lexend from Google Fonts)
 *       - inter.ttf   (Inter from Google Fonts)
 */
@SuppressLint("CustomSplashScreen")
class SplashScreenActivty : AppCompatActivity() {

    // ── Timing constants (ms) ──────────────────────────────────────────────
    private val IDENTITY_ENTRY_DELAY   = 300L   // delay before identity animates in
    private val FOOTER_ENTRY_DELAY     = 800L   // delay before footer animates in
    private val IDENTITY_DURATION      = 700L   // identity cluster animation duration
    private val FOOTER_DURATION        = 600L   // footer cluster animation duration
    private val DOT_BOUNCE_DURATION    = 400L   // single bounce cycle
    private val DOT_1_DELAY            = 100L
    private val DOT_2_DELAY            = 300L
    private val DOT_3_DELAY            = 500L
    private lateinit var prefs: RewaredSharedPref

    // ── Views ──────────────────────────────────────────────────────────────
    private lateinit var identityCluster: LinearLayout
    private lateinit var footerCluster:   LinearLayout
    private lateinit var ivLogo:          ImageView
    private lateinit var tvAppName:       TextView
    private lateinit var tvTagline:       TextView
    private lateinit var tvVersion:       TextView
    private lateinit var btnStartFlow:    Button
    private lateinit var dot1:            View
    private lateinit var dot2:            View
    private lateinit var dot3:            View

    override fun onCreate(savedInstanceState: Bundle?) {
        // ── AndroidX SplashScreen API (shows OS-level splash with your icon) ──
        val splashScreen = installSplashScreen()
        prefs = RewaredSharedPref(this)

        // Keep splash visible until we're ready to animate our custom screen
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen_activty)

        bindViews()
        applyGradientToAppName()
        setupEdgeToEdge()

        // Signal splash is done and our Activity is ready
        isReady = true

        // Kick off entry animations after a short grace period
        window.decorView.postDelayed({ animateIdentityIn() }, IDENTITY_ENTRY_DELAY)
        window.decorView.postDelayed({ animateFooterIn() },   FOOTER_ENTRY_DELAY)
        window.decorView.postDelayed({ startBouncingDots() }, FOOTER_ENTRY_DELAY + FOOTER_DURATION)
        val isLoggedIn = prefs.getBoolean("isLoggedIn")?:false
        if (isLoggedIn){
            window.decorView.postDelayed({

                startActivity(
                    Intent(this, MainActivity::class.java)
                )
                finish()

            }, 2500)
        }
        else{
            window.decorView.postDelayed({

                startActivity(
                    Intent(this, LoginActivity::class.java)
                )
                finish()

            }, 2500)
        }

    }

    // ── View Binding ───────────────────────────────────────────────────────
    private fun bindViews() {
        identityCluster = findViewById(R.id.identity_cluster)
        footerCluster   = findViewById(R.id.footer_cluster)
        ivLogo          = findViewById(R.id.iv_logo)
        tvAppName       = findViewById(R.id.tv_app_name)
        tvTagline       = findViewById(R.id.tv_tagline)
        tvVersion       = findViewById(R.id.tv_version)
        dot1            = findViewById(R.id.dot_1)
        dot2            = findViewById(R.id.dot_2)
        dot3            = findViewById(R.id.dot_3)
    }

    // ── Gradient shader on app name (linear 135° #AA3000 → #BC0100) ───────
    private fun applyGradientToAppName() {
        tvAppName.post {
            val width = tvAppName.width.toFloat()
            if (width > 0f) {
                val gradient = LinearGradient(
                    0f, 0f, width, 0f,
                    intArrayOf(0xFFAA3000.toInt(), 0xFFBC0100.toInt()),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
                tvAppName.paint.shader = gradient
                tvAppName.invalidate()
            }
        }
    }

    // ── Entry animation: identity cluster (logo + name) ───────────────────
    private fun animateIdentityIn() {
        val fadeIn      = ObjectAnimator.ofFloat(identityCluster, View.ALPHA,       0f, 1f)
        val slideUp     = ObjectAnimator.ofFloat(identityCluster, View.TRANSLATION_Y, 32f.dp, 0f)
        val scaleX      = ObjectAnimator.ofFloat(identityCluster, View.SCALE_X,    0.95f, 1f)
        val scaleY      = ObjectAnimator.ofFloat(identityCluster, View.SCALE_Y,    0.95f, 1f)

        AnimatorSet().apply {
            playTogether(fadeIn, slideUp, scaleX, scaleY)
            duration     = IDENTITY_DURATION
            interpolator = OvershootInterpolator(1.2f)
            start()
        }
    }

    // ── Entry animation: footer cluster (tagline + dots + button) ─────────
    private fun animateFooterIn() {
        val fadeIn  = ObjectAnimator.ofFloat(footerCluster, View.ALPHA,         0f, 1f)
        val slideUp = ObjectAnimator.ofFloat(footerCluster, View.TRANSLATION_Y, 32f.dp, 0f)

        AnimatorSet().apply {
            playTogether(fadeIn, slideUp)
            duration     = FOOTER_DURATION
            interpolator = FastOutSlowInInterpolator()
            start()
        }
    }

    // ── Continuous bouncing loading dots ───────────────────────────────────
    private fun startBouncingDots() {
        bounceDot(dot1, DOT_1_DELAY)
        bounceDot(dot2, DOT_2_DELAY)
        bounceDot(dot3, DOT_3_DELAY)
    }

    private fun bounceDot(dot: View, delay: Long) {
        val bounceUp = ObjectAnimator.ofFloat(dot, View.TRANSLATION_Y, 0f, -8f.dp).apply {
            duration     = DOT_BOUNCE_DURATION
            startDelay   = delay
            interpolator = DecelerateInterpolator()
            repeatCount  = android.animation.ValueAnimator.INFINITE
            repeatMode   = android.animation.ValueAnimator.REVERSE
        }
        bounceUp.start()
    }

    // ── Navigation ─────────────────────────────────────────────────────────
    private fun navigateToMain() {
        // Scale-down press feedback
        btnStartFlow.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                btnStartFlow.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }.start()
    }

    // ── Edge-to-edge (status bar + nav bar transparent) ───────────────────
    private fun setupEdgeToEdge() {
        window.statusBarColor           = android.graphics.Color.TRANSPARENT
        window.navigationBarColor       = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    // ── Extension: Float dp → pixels ──────────────────────────────────────
    private val Float.dp: Float
        get() = this * resources.displayMetrics.density
}