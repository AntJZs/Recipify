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
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.progweb.recipify.R
import com.progweb.recipify.databinding.ActivityAddRecipeBinding
import com.progweb.recipify.viewmodel.AddRecipeViewModel
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

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
    loadDraft()
}

    private fun setupListeners() {
        binding.btnGuardar.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val tiempo = binding.etTiempo.text.toString()
            //viewModel.saveRecipe(nombre, tiempo)
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }

        binding.btnBold.setOnClickListener { applyFormat("**", "**") }
        binding.btnItalic.setOnClickListener { applyFormat("*", "*") }
        binding.btnH1.setOnClickListener { applyLineFormat("## ") }
        binding.btnH2.setOnClickListener { applyLineFormat("### ") }
        binding.btnList.setOnClickListener { applyLineFormat("- ") }
        binding.btnNumberedList.setOnClickListener { applyNumberedList() }
        binding.btnImage.setOnClickListener { showImageDialog() }
    }

    private fun applyFormat(prefix: String, suffix: String) {
        val editText = binding.etBody
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start != end) {
            val text = editText.text.toString()
            val selectedText = text.substring(start, end)
            val newText = text.substring(0, start) + prefix + selectedText + suffix + text.substring(end)
            editText.setText(newText)
            editText.setSelection(start + prefix.length, end + prefix.length)
        }
    }

    private fun applyLineFormat(prefix: String) {
        val editText = binding.etBody
        val selectionStart = editText.selectionStart
        val text = editText.text.toString()
        
        // Find start of current line
        var lineStart = text.lastIndexOf('\n', selectionStart - 1) + 1
        
        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        editText.setText(newText)
        editText.setSelection(selectionStart + prefix.length)
    }

    private fun applyNumberedList() {
        val editText = binding.etBody
        val selectionStart = editText.selectionStart
        val text = editText.text.toString()
        
        var lineStart = text.lastIndexOf('\n', selectionStart - 1) + 1
        val newText = text.substring(0, lineStart) + "1. " + text.substring(lineStart)
        editText.setText(newText)
        editText.setSelection(selectionStart + 3)
    }

    private fun showImageDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Insertar Imagen")
        val input = android.widget.EditText(this)
        input.hint = "URL de la imagen"
        builder.setView(input)
        builder.setPositiveButton("Insertar") { _, _ ->
            val url = input.text.toString()
            if (url.isNotEmpty()) {
                val markdown = "![Imagen]($url)"
                val editText = binding.etBody
                val start = editText.selectionStart
                editText.text?.insert(start, markdown)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
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
        binding.tilBody.error = estado.errorBody

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
    private fun loadDraft() {
        // 1. Get current signed‑in user UID
        val uid = Firebase.auth.currentUser?.uid ?: return

        // 2. Access SharedPreferences where drafts are stored
        val prefs = getSharedPreferences("drafts", MODE_PRIVATE)

        // 3. Retrieve JSON string for this user, e.g. "draft_recipe_<uid>"
        val json = prefs.getString("draft_recipe_$uid", null) ?: return

        // 4. Parse JSON into fields (name, time, categories, body)
        try {
            val obj = JSONObject(json)
            binding.etNombre.setText(obj.optString("name"))
            binding.etTiempo.setText(obj.optString("time"))
            binding.etCategoria.setText(obj.optString("categories"))
            binding.etBody.setText(obj.optString("body"))
        } catch (e: JSONException) {
            // Corrupt draft – clean it out
            prefs.edit().remove("draft_recipe_$uid").apply()
        }
    }
}

