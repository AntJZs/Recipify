package com.progweb.recipify

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.progweb.recipify.viewmodel.LoadingViewModel

class LoadingActivity : AppCompatActivity() {

    private val viewModel: LoadingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        viewModel.navigationTarget.observe(this) { targetActivity ->
            if (targetActivity == HomePage::class.java) {
                val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
                val isLogged = prefs.getBoolean("isLogged", false)
                if (!isLogged) {
                    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    prefs.edit()
                        .putBoolean("isLogged", true)
                        .putString("usuario", currentUser?.displayName ?: currentUser?.email)
                        .apply()
                }
            }
            val intent = Intent(this, targetActivity)
            startActivity(intent)
            finish()
        }
    }
}
