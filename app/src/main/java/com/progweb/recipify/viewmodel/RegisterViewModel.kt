package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progweb.recipify.com.progweb.recipify.datamodels.UsuariosManager

class RegisterViewModel : ViewModel() {

    private val _registerState = MutableLiveData<RegisterResult>()
    val registerState: LiveData<RegisterResult> = _registerState

    data class RegisterResult(
        val success: Boolean = false,
        val usuario: String? = null,
        val errorUsuario: String? = null,
        val errorNombre: String? = null,
        val errorApellido: String? = null,
        val errorPassword: String? = null,
        val errorConfPassword: String? = null
    )

    fun register(usuario: String, nombre: String, apellido: String, password: String, confPassword: String) {
        var hasError = false
        var errorUsuario: String? = null
        var errorNombre: String? = null
        var errorApellido: String? = null
        var errorPassword: String? = null
        var errorConfPassword: String? = null

        if (usuario.isEmpty()) {
            errorUsuario = "Ingresa un usuario"
            hasError = true
        } else if (UsuariosManager.usuarios.containsKey(usuario)) {
            errorUsuario = "El usuario ya existe"
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
                errorUsuario = errorUsuario,
                errorNombre = errorNombre,
                errorApellido = errorApellido,
                errorPassword = errorPassword,
                errorConfPassword = errorConfPassword
            )
        } else {
            val nuevoUsuario = UsuariosManager.Usuario(usuario, nombre, apellido, password)
            UsuariosManager.usuarios[usuario] = nuevoUsuario
            _registerState.value = RegisterResult(success = true, usuario = usuario)
        }
    }
}
