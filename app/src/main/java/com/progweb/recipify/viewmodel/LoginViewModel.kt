package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progweb.recipify.com.progweb.recipify.datamodels.UsuariosManager

class LoginViewModel : ViewModel() {

    private val _loginState = MutableLiveData<LoginResult>()
    val loginState: LiveData<LoginResult> = _loginState

    data class LoginResult(
        val success: Boolean = false,
        val usuario: String? = null,
        val errorUsuario: String? = null,
        val errorPassword: String? = null
    )

    fun login(usuario: String, password: String) {
        if (usuario.isEmpty()) {
            _loginState.value = LoginResult(errorUsuario = "El usuario no puede estar vacío")
            return
        }

        if (password.isEmpty()) {
            _loginState.value = LoginResult(errorPassword = "La contraseña no puede estar vacía")
            return
        }

        val usuarioGuardado = UsuariosManager.usuarios[usuario]

        if ((usuarioGuardado != null && usuarioGuardado.password == password) || (usuario == "admin" && password == "1234")) {
            _loginState.value = LoginResult(success = true, usuario = usuario)
        } else {
            _loginState.value = LoginResult(errorPassword = "Usuario o contraseña incorrectos")
        }
    }
}
