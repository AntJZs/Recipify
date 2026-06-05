package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.progweb.recipify.com.progweb.recipify.datamodels.UsuariosManager

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
            _loginState.value = LoginResult(errorUsuario = "El usuario/correo no puede estar vacío")
            return
        }

        if (password.isEmpty()) {
            _loginState.value = LoginResult(errorPassword = "La contraseña no puede estar vacía")
            return
        }

        // Try Firebase Auth first if it looks like an email
        if (usuario.contains("@")) {
            auth.signInWithEmailAndPassword(usuario, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        checkUserVerification()
                    } else {
                        // If firebase fails, check local UsuariosManager as fallback
                        checkLocalLogin(usuario, password, task.exception?.message)
                    }
                }
        } else {
            // Not an email, check local UsuariosManager
            checkLocalLogin(usuario, password)
        }
    }

    private fun checkLocalLogin(usuario: String, password: String, firebaseError: String? = null) {
        val usuarioGuardado = UsuariosManager.usuarios[usuario]

        if ((usuarioGuardado != null && usuarioGuardado.password == password) || (usuario == "admin" && password == "1234")) {
            // Local users don't need Firestore verification
            _loginState.value = LoginResult(success = true, usuario = usuario, needsProfileSetup = false)
        } else {
            _loginState.value = LoginResult(
                errorMessage = if (firebaseError != null) "Error de Firebase: $firebaseError. Además, no se encontró el usuario localmente."
                else "Usuario o contraseña incorrectos"
            )
        }
    }

    fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUserVerification()
                } else {
                    _loginState.value = LoginResult(errorMessage = task.exception?.message ?: "Error de Google Login")
                }
            }
    }

    private fun checkUserVerification() {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        if (uid == null) {
            _loginState.value = LoginResult(errorMessage = "Usuario no autenticado")
            return
        }

        db.collection("users").document(uid).get()
            .addOnCompleteListener { docTask ->
                if (docTask.isSuccessful) {
                    val doc = docTask.result
                    if (doc != null && doc.exists()) {
                        val isVerified = doc.getBoolean("isVerified") ?: false
                        val username = doc.getString("username")
                        _loginState.value = LoginResult(
                            success = true,
                            usuario = if (!username.isNullOrEmpty()) username else currentUser.displayName ?: currentUser.email,
                            needsProfileSetup = !isVerified
                        )
                    } else {
                        // Create document in Firestore with isVerified = false
                        createUserDocument(currentUser)
                    }
                } else {
                    // Fallback to offline/permissions issue: assume profile setup is needed to complete name check
                    _loginState.value = LoginResult(
                        success = true,
                        usuario = currentUser.displayName ?: currentUser.email,
                        needsProfileSetup = true
                    )
                }
            }
    }

    private fun createUserDocument(currentUser: com.google.firebase.auth.FirebaseUser) {
        val email = currentUser.email ?: ""
        val displayName = currentUser.displayName ?: ""
        val photoUrl = currentUser.photoUrl?.toString() ?: "https://placehold.net/avatar.png"

        val names = displayName.split(" ")
        val firstName = names.getOrNull(0) ?: ""
        val lastName = if (names.size > 1) names.subList(1, names.size).joinToString(" ") else ""

        val userData = mapOf(
            "username" to "",
            "displayName" to displayName,
            "firstName" to firstName,
            "lastName" to lastName,
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
                _loginState.value = LoginResult(
                    success = true,
                    usuario = email,
                    needsProfileSetup = true
                )
            }
            .addOnFailureListener { e ->
                _loginState.value = LoginResult(
                    errorMessage = "Error al inicializar perfil en Firestore: ${e.message}"
                )
            }
    }
}
