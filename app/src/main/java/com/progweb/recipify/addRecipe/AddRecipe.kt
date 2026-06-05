package com.progweb.recipify.addRecipe

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.progweb.recipify.R
import com.progweb.recipify.databinding.ActivityAddRecipeBinding
import com.progweb.recipify.viewmodel.AddRecipeViewModel
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

class AddRecipe : AppCompatActivity() {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val crashlytics = Firebase.crashlytics
    private lateinit var binding: ActivityAddRecipeBinding
    private val viewModel: AddRecipeViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivRecipePreview.setImageURI(it)
        }
    }

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
        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnGuardar.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val tiempo = binding.etTiempo.text.toString()
            val categorias = binding.etCategoria.text.toString()
            val ingredients = binding.etIngredients.text.toString()
            val cuerpo = binding.etBody.text.toString()
            viewModel.saveRecipe(nombre, tiempo, categorias, ingredients, cuerpo)
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

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { estado ->
                    actualizarUI(estado)
                }
            }
        }
    }

    private fun actualizarUI(estado: AddRecipeViewModel.UiState) {
        binding.tilNombre.error = estado.errorNombre
        binding.tilTiempo.error = estado.errorTiempo
        binding.tilIngredients.error = estado.errorIngredients
        binding.tilBody.error = estado.errorBody

        binding.btnGuardar.isEnabled = !estado.guardando

        if (estado.success) {
            uploadImageAndSaveRecipe()
        }
    }

    private fun uploadImageAndSaveRecipe() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: No hay sesión iniciada", Toast.LENGTH_SHORT).show()
            viewModel.resetAfterFailure()
            return
        }

        if (selectedImageUri != null) {
            val imageRef = storage.reference.child("recipes/${UUID.randomUUID()}.jpg")
            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { url ->
                        saveToFirestore(url.toString())
                    }
                }
                .addOnFailureListener { e ->
                    crashlytics.recordException(e)
                    Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                    viewModel.resetAfterFailure()
                }
        } else {
            saveToFirestore("https://placehold.net/recipe.png")
        }
    }

    private fun saveToFirestore(imageURL: String) {
        val user = Firebase.auth.currentUser ?: return
        
        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val authorName = if (doc.exists()) {
                doc.getString("displayName") ?: doc.getString("username") ?: "Usuario"
            } else {
                user.displayName ?: "Usuario"
            }

            val recipeData = hashMapOf(
                "name"             to binding.etNombre.text.toString(),
                "totalTimeMinutes" to binding.etTiempo.text.toString().toLong(),
                "body"             to binding.etBody.text.toString(),
                "category"         to binding.etCategoria.text.toString()
                                          .split(",")
                                          .map { it.trim() }
                                          .filter { it.isNotEmpty() },
                "userId"           to user.uid,
                "authorName"       to authorName,
                "createdAt"        to com.google.firebase.Timestamp.now(),
                "description"      to (binding.etBody.text?.take(100)?.toString() ?: ""),
                "imageURL"         to imageURL,
                "ingredients"      to binding.etIngredients.text.toString()
                                          .split("\n")
                                          .map { it.trim() }
                                          .filter { it.isNotEmpty() },
                "area"             to "Colombia"
            )

            db.collection("recipe").add(recipeData).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.msg_receta_guardada), Toast.LENGTH_SHORT).show()
                clearDraft()
                finish()
            }.addOnFailureListener { e ->
                crashlytics.recordException(e)
                viewModel.resetAfterFailure()
                Toast.makeText(this, getString(R.string.msg_error_guardar_receta), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadDraft() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val prefs = getSharedPreferences("drafts", MODE_PRIVATE)
        val json = prefs.getString("draft_recipe_$uid", null) ?: return

        try {
            val obj = JSONObject(json)
            binding.etNombre.setText(obj.optString("name"))
            binding.etTiempo.setText(obj.optString("time"))
            binding.etCategoria.setText(obj.optString("categories"))
            binding.etIngredients.setText(obj.optString("ingredients"))
            binding.etBody.setText(obj.optString("body"))
            
            val imageUriStr = obj.optString("imageUri")
            if (imageUriStr.isNotEmpty()) {
                selectedImageUri = Uri.parse(imageUriStr)
                Glide.with(this).load(selectedImageUri).into(binding.ivRecipePreview)
            }
        } catch (e: JSONException) {
            prefs.edit().remove("draft_recipe_$uid").apply()
        }
    }

    private fun clearDraft() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        getSharedPreferences("drafts", MODE_PRIVATE).edit().remove("draft_recipe_$uid").apply()
    }

    override fun onPause() {
        super.onPause()
        saveDraft()
    }

    private fun saveDraft() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val obj = JSONObject().apply {
            put("name", binding.etNombre.text.toString())
            put("time", binding.etTiempo.text.toString())
            put("categories", binding.etCategoria.text.toString())
            put("ingredients", binding.etIngredients.text.toString())
            put("body", binding.etBody.text.toString())
            put("imageUri", selectedImageUri?.toString() ?: "")
        }
        getSharedPreferences("drafts", MODE_PRIVATE).edit()
            .putString("draft_recipe_$uid", obj.toString())
            .apply()
    }
}
