package com.progweb.recipify.addRecipe

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.progweb.recipify.databinding.ActivityAddRecipeBinding
import android.widget.Toast
import com.progweb.recipify.R

class AddRecipe : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configurarBotones()
    }

    private fun configurarBotones() {
        binding.btnGuardar.setOnClickListener {
            if (validarCampos()) guardarReceta()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun validarCampos(): Boolean {
        var valido = true
        binding.tilNombre.error = null
        binding.tilTiempo.error = null

        if (binding.etNombre.text.toString().trim().isEmpty()) {
            binding.tilNombre.error = "Ingresa el nombre del plato"
            valido = false
        }

        val tiempo = binding.etTiempo.text.toString().trim()
        if (tiempo.isEmpty()) {
            binding.tilTiempo.error = "Ingresa el tiempo de preparación"
            valido = false
        } else if (tiempo.toIntOrNull() == null) {
            binding.tilTiempo.error = "Ingresa un número válido"
            valido = false
        }

        return valido
    }

    private fun guardarReceta() {
        // TODO: guardar en Firebase cuando esté lista la conexión

        Toast.makeText(this, getString(R.string.msg_receta_guardada), Toast.LENGTH_SHORT).show()
        finish()
    }
}