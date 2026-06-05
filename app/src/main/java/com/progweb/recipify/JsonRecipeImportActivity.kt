package com.progweb.recipify

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.progweb.recipify.databinding.ActivityJsonRecipeImportBinding

class JsonRecipeImportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJsonRecipeImportBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityJsonRecipeImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnImportJson.setOnClickListener {
            importRecipes()
        }
    }

    private fun importRecipes() {
        val jsonText = binding.etJsonInput.text.toString().trim()
        if (jsonText.isEmpty()) {
            Toast.makeText(this, "Por favor pegue algún JSON primero", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Debe iniciar sesión para importar recetas", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val gson = Gson()
            val recipeList = mutableListOf<Map<String, Any>>()

            if (jsonText.startsWith("[")) {
                // Parse as List of Recipes
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val parsedList: List<Map<String, Any>> = gson.fromJson(jsonText, type)
                recipeList.addAll(parsedList)
            } else {
                // Parse as single Recipe
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val parsedObj: Map<String, Any> = gson.fromJson(jsonText, type)
                recipeList.add(parsedObj)
            }

            if (recipeList.isEmpty()) {
                Toast.makeText(this, "No se encontraron recetas para importar", Toast.LENGTH_SHORT).show()
                return
            }

            binding.btnImportJson.isEnabled = false
            val batch = db.batch()

            recipeList.forEach { rawRecipe ->
                val recipeData = HashMap(rawRecipe)
                
                // Link recipe to current authenticated user
                recipeData["userId"] = currentUser.uid
                
                // Add standard timestamps
                if (!recipeData.containsKey("createdAt")) {
                    recipeData["createdAt"] = Timestamp.now()
                }
                recipeData["updatedAt"] = Timestamp.now()

                val newDocRef = db.collection("recipe").document()
                batch.set(newDocRef, recipeData)
            }

            batch.commit()
                .addOnSuccessListener {
                    Toast.makeText(this, "Se importaron ${recipeList.size} receta(s) con éxito", Toast.LENGTH_LONG).show()
                    binding.etJsonInput.text?.clear()
                    binding.btnImportJson.isEnabled = true
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.btnImportJson.isEnabled = true
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al decodificar JSON: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
