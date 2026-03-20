package com.progweb.recipify

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.progweb.recipify.databinding.ActivityRegisterBinding
import com.progweb.recipify.viewmodel.RegisterViewModel

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
            val usuario = binding.etUsuario.text.toString().trim()
            val nombre = binding.etName.text.toString().trim()
            val apellido = binding.etLastname.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confPassword = binding.etConfPassword.text.toString()
            
            viewModel.register(usuario, nombre, apellido, password, confPassword)
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.registerState.observe(this) { result ->
            binding.tilUsuario.error = result.errorUsuario
            binding.tilName.error = result.errorNombre
            binding.tilLastname.error = result.errorApellido
            binding.tilPassword.error = result.errorPassword
            binding.confPassword.error = result.errorConfPassword

            if (result.success) {
                Toast.makeText(
                    this,
                    "Usuario ${result.usuario} registrado correctamente",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(this, HomePage::class.java)
                intent.putExtra("usuario", result.usuario)
                startActivity(intent)
                finish()
            }
        }
    }
}
