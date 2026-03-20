package com.progweb.recipify.com.progweb.recipify.datamodels

object UsuariosManager {

    data class Usuario(
        val usuario: String,
        val nombre: String,
        val apellido: String,
        val password: String
    )

    val usuarios = mutableMapOf<String, Usuario>()
}