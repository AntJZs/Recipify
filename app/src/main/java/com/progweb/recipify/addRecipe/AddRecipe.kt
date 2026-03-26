package com.progweb.recipify.addRecipe

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.progweb.recipify.R
import com.progweb.recipify.databinding.ActivityAddRecipeBinding
import com.progweb.recipify.viewmodel.AddRecipeViewModel
import kotlinx.coroutines.launch

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
        // repeatOnLifecycle es el equivalente correcto de observe() para StateFlow
        // Se cancela automáticamente cuando la Activity va a background (STARTED)
        // y se reanuda cuando vuelve — evita procesar eventos con la UI invisible
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { estado ->
                    actualizarUI(estado)
                }
            }
        }
    }

    private fun actualizarUI(estado: AddRecipeViewModel.UiState) {
        // Muestra errores de validación en los campos
        binding.tilNombre.error = estado.errorNombre
        binding.tilTiempo.error = estado.errorTiempo

        // Deshabilita el botón mientras está guardando — evita doble clic
        binding.btnGuardar.isEnabled = !estado.guardando

        // Cuando termina con éxito
        if (estado.success) {
            db.collection("recipe").add(
                hashMapOf(
                    "name" to binding.etNombre.text.toString(),
                    "totalTimeMinutes" to binding.etTiempo.text.toString().toInt()
                )
            )
            Toast.makeText(this, getString(R.string.msg_receta_guardada), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

