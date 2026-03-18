package com.progweb.recipify

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.content.Intent

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilUsuario: TextInputLayout
    private lateinit var tilName: TextInputLayout
    private lateinit var tilLastname: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfPassword: TextInputLayout

    private lateinit var etUsuario: TextInputEditText
    private lateinit var etName: TextInputEditText
    private lateinit var etLastname: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfPassword: TextInputEditText


    private lateinit var btnCrear: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupListeners()

        val tvLogin = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tvLogin)

        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        tilUsuario = findViewById(R.id.tilUsuario)
        tilName = findViewById(R.id.tilName)
        tilLastname = findViewById(R.id.tilLastname)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfPassword = findViewById(R.id.confPassword)

        etUsuario = findViewById(R.id.etUsuario)
        etName = findViewById(R.id.etName)
        etLastname = findViewById(R.id.etLastname)
        etPassword = findViewById(R.id.etPassword)
        etConfPassword = findViewById(R.id.etConfPassword)

        btnCrear = findViewById(R.id.btnCrear)
    }

    private fun setupListeners() {
        btnCrear.setOnClickListener {
            if (validarCampos()) {
                registrarUsuario()
            }
        }
    }

    private fun validarCampos(): Boolean {
        val usuario = etUsuario.text.toString().trim()
        val nombre = etName.text.toString().trim()
        val apellido = etLastname.text.toString().trim()
        val password = etPassword.text.toString()
        val confPassword = etConfPassword.text.toString()

        var valido = true

        // Limpiar errores
        tilUsuario.error = null
        tilName.error = null
        tilLastname.error = null
        tilPassword.error = null
        tilConfPassword.error = null

        if (usuario.isEmpty()) {
            tilUsuario.error = "Ingresa un usuario"
            valido = false
        }

        if (nombre.isEmpty()) {
            tilName.error = "Ingresa tu nombre"
            valido = false
        }

        if (apellido.isEmpty()) {
            tilLastname.error = "Ingresa tu apellido"
            valido = false
        }

        if (password.isEmpty()) {
            tilPassword.error = "Ingresa una contraseña"
            valido = false
        } else if (password.length < 6) {
            tilPassword.error = "Mínimo 6 caracteres"
            valido = false
        }

        if (confPassword.isEmpty()) {
            tilConfPassword.error = "Confirma la contraseña"
            valido = false
        } else if (password != confPassword) {
            tilConfPassword.error = "Las contraseñas no coinciden"
            valido = false
        }

        return valido
    }

    private fun registrarUsuario() {
        val usuario = etUsuario.text.toString().trim()
        val nombre = etName.text.toString().trim()
        val apellido = etLastname.text.toString().trim()

        // Aquí iría tu lógica real:
        // Firebase, API, base de datos, etc.

        Toast.makeText(
            this,
            "Usuario $usuario registrado correctamente",
            Toast.LENGTH_LONG
        ).show()
    }
}