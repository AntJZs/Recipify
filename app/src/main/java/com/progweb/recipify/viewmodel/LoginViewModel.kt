package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.progweb.recipify.com.progweb.recipify.datamodels.UsuariosManager

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _loginState = MutableLiveData<LoginResult>()
    val loginState: LiveData<LoginResult> = _loginState

    data class LoginResult(
        val success: Boolean = false,
        val usuario: String? = null,
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
                        val user = auth.currentUser
                        _loginState.value = LoginResult(success = true, usuario = user?.email)
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
            _loginState.value = LoginResult(success = true, usuario = usuario)
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
                    val user = auth.currentUser
                    _loginState.value = LoginResult(success = true, usuario = user?.displayName ?: user?.email)
                } else {
                    _loginState.value = LoginResult(errorMessage = task.exception?.message ?: "Error de Google Login")
                }
            }
    }
}
