package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileSetupViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState

    private val _saveResult = MutableLiveData<SaveResult>()
    val saveResult: LiveData<SaveResult> = _saveResult

    data class ProfileState(
        val email: String = "",
        val username: String = "",
        val displayName: String = "",
        val firstName: String = "",
        val lastName: String = "",
        val bio: String = "",
        val city: String = "",
        val photoURL: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    data class SaveResult(
        val success: Boolean = false,
        val errorMessage: String? = null,
        val errorUsername: String? = null,
        val errorDisplayName: String? = null,
        val errorFirstName: String? = null,
        val errorLastName: String? = null
    )

    fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        _profileState.value = ProfileState(isLoading = true)

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val email = document.getString("email") ?: ""
                    val username = document.getString("username") ?: ""
                    val displayName = document.getString("displayName") ?: ""
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val bio = document.getString("bio") ?: ""
                    val photoURL = document.getString("photoURL") ?: ""
                    
                    val locationMap = document.get("location") as? Map<*, *>
                    val city = locationMap?.get("city") as? String ?: ""

                    _profileState.value = ProfileState(
                        email = email,
                        username = username,
                        displayName = displayName,
                        firstName = firstName,
                        lastName = lastName,
                        bio = bio,
                        city = city,
                        photoURL = photoURL,
                        isLoading = false
                    )
                } else {
                    // Fallback to Firebase Auth info
                    val currentUser = auth.currentUser
                    val displayName = currentUser?.displayName ?: ""
                    val names = displayName.split(" ")
                    val firstName = names.getOrNull(0) ?: ""
                    val lastName = if (names.size > 1) names.subList(1, names.size).joinToString(" ") else ""
                    
                    _profileState.value = ProfileState(
                        email = currentUser?.email ?: "",
                        displayName = displayName,
                        firstName = firstName,
                        lastName = lastName,
                        photoURL = currentUser?.photoUrl?.toString() ?: "",
                        isLoading = false
                    )
                }
            }
            .addOnFailureListener { e ->
                _profileState.value = ProfileState(errorMessage = e.message, isLoading = false)
            }
    }

    fun saveProfile(
        usernameInput: String,
        displayNameInput: String,
        firstNameInput: String,
        lastNameInput: String,
        bioInput: String,
        cityInput: String
    ) {
        val username = usernameInput.trim()
        val displayName = displayNameInput.trim()
        val firstName = firstNameInput.trim()
        val lastName = lastNameInput.trim()
        val bio = bioInput.trim()
        val city = cityInput.trim()

        var hasError = false
        var errorUsername: String? = null
        var errorDisplayName: String? = null
        var errorFirstName: String? = null
        var errorLastName: String? = null

        if (username.isEmpty()) {
            errorUsername = "El nombre de usuario no puede estar vacío"
            hasError = true
        } else if (username.length < 3) {
            errorUsername = "Debe tener al menos 3 caracteres"
            hasError = true
        } else if (!username.matches(Regex("^[a-zA-Z0-9_.]+$"))) {
            errorUsername = "Solo se permiten letras, números, puntos y guiones bajos"
            hasError = true
        }

        if (displayName.isEmpty()) {
            errorDisplayName = "El nombre a mostrar no puede estar vacío"
            hasError = true
        }

        if (firstName.isEmpty()) {
            errorFirstName = "El nombre no puede estar vacío"
            hasError = true
        }

        if (lastName.isEmpty()) {
            errorLastName = "El apellido no puede estar vacío"
            hasError = true
        }

        if (hasError) {
            _saveResult.value = SaveResult(
                success = false,
                errorUsername = errorUsername,
                errorDisplayName = errorDisplayName,
                errorFirstName = errorFirstName,
                errorLastName = errorLastName
            )
            return
        }

        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _saveResult.value = SaveResult(success = false, errorMessage = "Error: Usuario no autenticado")
                return@launch
            }

            try {
                // Check if username is already taken by another user
                val usernameQuery = db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .await()

                val isTaken = usernameQuery.documents.any { doc -> doc.id != uid }
                if (isTaken) {
                    _saveResult.value = SaveResult(
                        success = false,
                        errorUsername = "El nombre de usuario ya está en uso"
                    )
                    return@launch
                }

                // Update user details in Firestore
                val updateData = hashMapOf(
                    "username" to username,
                    "displayName" to displayName,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "bio" to bio,
                    "location" to mapOf("country" to "Colombia", "city" to city),
                    "isVerified" to true,
                    "updatedAt" to Timestamp.now()
                )

                db.collection("users").document(uid)
                    .set(updateData, SetOptions.merge())
                    .await()

                _saveResult.value = SaveResult(success = true)
            } catch (e: Exception) {
                _saveResult.value = SaveResult(success = false, errorMessage = e.message ?: "Error al guardar el perfil")
            }
        }
    }
}
