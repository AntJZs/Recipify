package com.progweb.recipify

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textview.MaterialTextView
import com.progweb.recipify.databinding.ActivityLoginBinding
import com.progweb.recipify.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.main)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<MaterialTextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupObservers()

        binding.btnIngresar.setOnClickListener {
            val usuario = binding.etUsuario.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(usuario, password)
        }

        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { viewModel.loginWithGoogle(it) }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { result ->
            binding.tilUsuario.error = result.errorUsuario
            binding.tilPassword.error = result.errorPassword

            result.errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }

            if (result.success) {
                if (result.needsProfileSetup) {
                    val intent = Intent(this, ProfileSetupActivity::class.java)
                    intent.putExtra("usuario", result.usuario)
                    startActivity(intent)
                    finish()
                } else {
                    val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean("isLogged", true)
                        .putString("usuario", result.usuario)
                        .apply()
                    val intent = Intent(this, HomePage::class.java)
                    intent.putExtra("usuario", result.usuario)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
