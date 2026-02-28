package com.progweb.recipify

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.progweb.recipify.databinding.ActivityHomeBinding
import com.progweb.recipify.R
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val usuario = intent.getStringExtra("usuario") ?: "usuario"
        binding.tvUsuarioLogueado.text = "Hola, $usuario. Tu sesión está activa."
        binding.btnCerrarSesion.setOnClickListener {
            finish()
        }
    }
}
