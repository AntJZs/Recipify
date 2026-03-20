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

        viewModel.navigateToNext.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, Destacados::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
