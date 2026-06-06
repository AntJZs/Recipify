package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _loginState = MutableLiveData<LoginResult>()
    val loginState: LiveData<LoginResult> = _loginState

    data class LoginResult(
        val success: Boolean = false,
        val usuario: String? = null,
        val needsProfileSetup: Boolean = false,
        val errorUsuario: String? = null,
        val errorPassword: String? = null,
        val errorMessage: String? = null
    )

    fun login(usuario: String, password: String) {
        if (usuario.isEmpty()) {
            _loginState.value = LoginResult(errorUsuario = "El correo no puede estar vacío")
            return
        }
        if (password.isEmpty()) {
            _loginState.value = LoginResult(errorPassword = "La contraseña no puede estar vacía")
            return
        }
        if (!usuario.contains("@")) {
            _loginState.value = LoginResult(errorUsuario = "Ingresa un correo electrónico válido")
            return
        }

        auth.signInWithEmailAndPassword(usuario, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUserVerification()
                } else {
                    _loginState.value = LoginResult(
                        errorMessage = task.exception?.message ?: "Credenciales incorrectas"
                    )
                }
            }
    }

    fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUserVerification()
                } else {
                    _loginState.value = LoginResult(
                        errorMessage = task.exception?.message ?: "Error al iniciar con Google"
                    )
                }
            }
    }

    private fun checkUserVerification() {
        val currentUser = auth.currentUser ?: run {
            _loginState.value = LoginResult(errorMessage = "Usuario no autenticado")
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnCompleteListener { docTask ->
                if (docTask.isSuccessful) {
                    val doc = docTask.result
                    if (doc != null && doc.exists()) {
                        val isVerified = doc.getBoolean("isVerified") ?: false
                        val username = doc.getString("username")
                        _loginState.value = LoginResult(
                            success = true,
                            usuario = if (!username.isNullOrEmpty()) username else currentUser.email,
                            needsProfileSetup = !isVerified
                        )
                    } else {
                        createUserDocument(currentUser)
                    }
                } else {
                    _loginState.value = LoginResult(
                        success = true,
                        usuario = currentUser.email,
                        needsProfileSetup = true
                    )
                }
            }
    }

    private fun createUserDocument(currentUser: com.google.firebase.auth.FirebaseUser) {
        val email = currentUser.email ?: ""
        val displayName = currentUser.displayName ?: ""
        val photoUrl = currentUser.photoUrl?.toString() ?: ""
        val names = displayName.split(" ")

        val userData = mapOf(
            "username" to "",
            "displayName" to displayName,
            "firstName" to (names.getOrNull(0) ?: ""),
            "lastName" to (if (names.size > 1) names.subList(1, names.size).joinToString(" ") else ""),
            "email" to email,
            "photoURL" to photoUrl,
            "bio" to "",
            "location" to mapOf("country" to "Colombia", "city" to ""),
            "isVerified" to false,
            "role" to "user",
            "bookmarksCount" to 0,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )

        db.collection("users").document(currentUser.uid).set(userData)
            .addOnSuccessListener {
                _loginState.value = LoginResult(success = true, usuario = email, needsProfileSetup = true)
            }
            .addOnFailureListener { e ->
                FirebaseCrashlytics.getInstance().recordException(e)
                _loginState.value = LoginResult(errorMessage = "Error al crear perfil: ${e.message}")
            }
    }
}
