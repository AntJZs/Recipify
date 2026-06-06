package com.progweb.recipify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _registerState = MutableStateFlow(RegisterResult())
    val registerState: StateFlow<RegisterResult> = _registerState

    data class RegisterResult(
        val success: Boolean = false,
        val usuario: String? = null,
        val errorMessage: String? = null,
        val errorEmail: String? = null,
        val errorUsuario: String? = null,
        val errorNombre: String? = null,
        val errorApellido: String? = null,
        val errorPassword: String? = null,
        val errorConfPassword: String? = null
    )

    fun register(email: String, usuario: String, nombre: String, apellido: String, password: String, confPassword: String) {
        var hasError = false
        var errorEmail: String? = null
        var errorUsuario: String? = null
        var errorNombre: String? = null
        var errorApellido: String? = null
        var errorPassword: String? = null
        var errorConfPassword: String? = null

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmail = "Ingresa un correo válido"
            hasError = true
        }
        if (usuario.isEmpty()) {
            errorUsuario = "Ingresa un usuario"
            hasError = true
        }
        if (nombre.isEmpty()) {
            errorNombre = "Ingresa tu nombre"
            hasError = true
        }
        if (apellido.isEmpty()) {
            errorApellido = "Ingresa tu apellido"
            hasError = true
        }
        if (password.isEmpty()) {
            errorPassword = "Ingresa una contraseña"
            hasError = true
        } else if (password.length < 6) {
            errorPassword = "Mínimo 6 caracteres"
            hasError = true
        }
        if (confPassword.isEmpty()) {
            errorConfPassword = "Confirma la contraseña"
            hasError = true
        } else if (password != confPassword) {
            errorConfPassword = "Las contraseñas no coinciden"
            hasError = true
        }

        if (hasError) {
            _registerState.value = RegisterResult(
                errorEmail = errorEmail,
                errorUsuario = errorUsuario,
                errorNombre = errorNombre,
                errorApellido = errorApellido,
                errorPassword = errorPassword,
                errorConfPassword = errorConfPassword
            )
        } else {
            viewModelScope.launch {
                try {
                    val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                    val uid = authResult.user?.uid ?: throw Exception("Error al obtener ID de usuario")
                    
                    val userData = mapOf(
                        "username" to usuario,
                        "displayName" to "$nombre $apellido",
                        "firstName" to nombre,
                        "lastName" to apellido,
                        "email" to email,
                        "photoURL" to "https://placehold.net/avatar.png",
                        "bio" to "",
                        "location" to mapOf("country" to "Colombia", "city" to ""),
                        "isVerified" to false,
                        "role" to "user",
                        "bookmarksCount" to 0,
                        "createdAt" to Timestamp.now(),
                        "updatedAt" to Timestamp.now()
                    )
                    
                    db.collection("users").document(uid).set(userData).await()
                    _registerState.value = RegisterResult(success = true, usuario = usuario)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    _registerState.value = RegisterResult(errorMessage = "Error en el registro: ${e.message}")
                }
            }
        }
    }
}
