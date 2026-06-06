package com.progweb.recipify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.progweb.recipify.databinding.ActivityDestacadosBinding
import com.progweb.recipify.home.RecipeAdapter
import com.progweb.recipify.viewmodel.DestacadosViewModel

class Destacados : AppCompatActivity() {
    private lateinit var binding: ActivityDestacadosBinding
    private val viewModel: DestacadosViewModel by viewModels()
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDestacadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter { recipe ->
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.rvDestacados.apply {
            adapter = this@Destacados.adapter
            layoutManager = GridLayoutManager(this@Destacados, 2)
        }
    }

    private fun setupListeners() {
        binding.btnRegistrarse.setOnClickListener { viewModel.onRegisterClicked() }
        binding.btnIniciarSesion.setOnClickListener { viewModel.onLoginClicked() }
    }

    private fun setupObservers() {
        viewModel.navigateToLogin.observe(this) { navigate ->
            if (navigate) {
                startActivity(Intent(this, LoginActivity::class.java))
                viewModel.onNavigationDone()
            }
        }
        viewModel.navigateToRegister.observe(this) { navigate ->
            if (navigate) {
                startActivity(Intent(this, RegisterActivity::class.java))
                viewModel.onNavigationDone()
            }
        }
        viewModel.recetas.observe(this) { recetas ->
            adapter.submitList(recetas)
        }
    }
}
