package com.progweb.recipify

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.progweb.recipify.R.id.main
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
            finish()

        }, 60)
    }

}
