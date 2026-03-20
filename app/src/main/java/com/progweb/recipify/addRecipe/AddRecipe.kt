package com.progweb.recipify.addRecipe

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.progweb.recipify.R
import com.progweb.recipify.databinding.ActivityAddRecipeBinding
import com.progweb.recipify.viewmodel.AddRecipeViewModel

class AddRecipe : AppCompatActivity() {
    val db = Firebase.firestore
    private lateinit var binding: ActivityAddRecipeBinding
    private val viewModel: AddRecipeViewModel by viewModels()

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

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnGuardar.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val tiempo = binding.etTiempo.text.toString()
            viewModel.saveRecipe(nombre, tiempo)
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.saveResult.observe(this) { result ->
            binding.tilNombre.error = result.errorNombre
            binding.tilTiempo.error = result.errorTiempo
            db.collection("recipe").add(
                hashMapOf(
                    "name" to binding.etNombre.text.toString(),
                    "totalTimeMinutes" to binding.etTiempo.text.toString().toInt(),
                    ))

            if (result.success) {
                Toast.makeText(this, getString(R.string.msg_receta_guardada), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
