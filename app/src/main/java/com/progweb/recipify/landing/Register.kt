package com.progweb.recipify

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.progweb.recipify.databinding.ActivityRegisterBinding
import com.progweb.recipify.home.HomeFragment
import com.progweb.recipify.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnCrear.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val usuario = binding.etUsuario.text.toString().trim()
            val nombre = binding.etName.text.toString().trim()
            val apellido = binding.etLastname.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confPassword = binding.etConfPassword.text.toString()

            viewModel.register(email, usuario, nombre, apellido, password, confPassword)
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { result ->
                    binding.tilUsuario.error = result.errorUsuario
                    binding.tilEmail.error = result.errorEmail
                    binding.tilName.error = result.errorNombre
                    binding.tilLastname.error = result.errorApellido
                    binding.tilPassword.error = result.errorPassword
                    
                    result.errorMessage?.let {
                        Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_LONG).show()
                    }

                    if (result.success) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Usuario ${result.usuario} registrado correctamente",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(this@RegisterActivity, ProfileSetupActivity::class.java)
                        intent.putExtra("usuario", result.usuario)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}