package com.progweb.recipify
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textview.MaterialTextView
import com.progweb.recipify.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.main)
        val tvRegister = findViewById<MaterialTextView>(R.id.tvRegister)
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnIngresar.setOnClickListener {

            val usuario = binding.etUsuario.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            binding.tilUsuario.error = null
            binding.tilPassword.error = null

            when {
                usuario.isEmpty() -> {
                    binding.tilUsuario.error = "El usuario no puede estar vacío"
                }

                password.isEmpty() -> {
                    binding.tilPassword.error = "La contraseña no puede estar vacía"
                }

                else -> {
                    val usuarioGuardado = UsuariosManager.usuarios[usuario]

                    if (usuarioGuardado != null && usuarioGuardado.password == password) {

                        val intent = Intent(this, HomePage::class.java)
                        intent.putExtra("usuario", usuario)
                        startActivity(intent)

                    } else {
                        binding.tilPassword.error = "Usuario o contraseña incorrectos"
                        binding.etPassword.text?.clear()
                    }
                }
            }
        }
    }
}