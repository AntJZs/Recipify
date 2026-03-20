package com.progweb.recipify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textview.MaterialTextView
import com.progweb.recipify.databinding.ActivityLoginBinding
import com.progweb.recipify.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.main)

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
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { result ->
            binding.tilUsuario.error = result.errorUsuario
            binding.tilPassword.error = result.errorPassword

            if (result.success) {
                val intent = Intent(this, HomePage::class.java)
                intent.putExtra("usuario", result.usuario)
                startActivity(intent)
                finish()
            }
        }
    }
}
