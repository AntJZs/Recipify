package com.progweb.recipify
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.progweb.recipify.databinding.ActivityMainpageBinding
import com.progweb.recipify.R
class MainPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainpageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainpageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnIngresar.setOnClickListener {
            val usuario  = binding.etUsuario.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            // Limpiar errores previos
            binding.tilUsuario.isErrorEnabled  = false
            binding.tilPassword.isErrorEnabled = false
            when {
                usuario.isEmpty() -> {
                    binding.tilUsuario.error = "El usuario no puede estar vacío"
                }
                password.isEmpty() -> {
                    binding.tilPassword.error = "La contraseña no puede estar vacía"
                }
                usuario == "admin" && password == "1234" -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("usuario", usuario)
                    startActivity(intent)
                }
                else -> {
                    binding.tilUsuario.error  = " "   // espacio para activar estado de error
                    binding.tilPassword.error = "Usuario o contraseña incorrectos"
                    binding.etPassword.text?.clear()
                }
            }
        }
    }
}