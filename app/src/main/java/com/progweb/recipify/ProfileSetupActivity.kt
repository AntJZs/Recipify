package com.progweb.recipify

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.progweb.recipify.databinding.ActivityProfileSetupBinding
import com.progweb.recipify.viewmodel.ProfileSetupViewModel

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding
    private val viewModel: ProfileSetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupObservers()
        setupListeners()

        // Load initial profile data
        viewModel.loadUserProfile()
    }

    private fun setupListeners() {
        binding.btnSaveProfile.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val displayName = binding.etDisplayName.text.toString()
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val bio = binding.etBio.text.toString()
            val city = binding.etCity.text.toString()

            viewModel.saveProfile(username, displayName, firstName, lastName, bio, city)
        }
    }

    private fun setupObservers() {
        viewModel.profileState.observe(this) { state ->
            if (state.errorMessage != null) {
                Toast.makeText(this, "Error: ${state.errorMessage}", Toast.LENGTH_LONG).show()
            }

            // Populate fields if they are currently empty in UI
            if (binding.etUsername.text.isNullOrEmpty() && state.username.isNotEmpty()) {
                binding.etUsername.setText(state.username)
            }
            if (binding.etDisplayName.text.isNullOrEmpty() && state.displayName.isNotEmpty()) {
                binding.etDisplayName.setText(state.displayName)
            }
            if (binding.etFirstName.text.isNullOrEmpty() && state.firstName.isNotEmpty()) {
                binding.etFirstName.setText(state.firstName)
            }
            if (binding.etLastName.text.isNullOrEmpty() && state.lastName.isNotEmpty()) {
                binding.etLastName.setText(state.lastName)
            }
            if (binding.etBio.text.isNullOrEmpty() && state.bio.isNotEmpty()) {
                binding.etBio.setText(state.bio)
            }
            if (binding.etCity.text.isNullOrEmpty() && state.city.isNotEmpty()) {
                binding.etCity.setText(state.city)
            }

            // Load photo using Glide
            if (state.photoURL.isNotEmpty()) {
                Glide.with(this)
                    .load(state.photoURL)
                    .circleCrop()
                    .placeholder(R.drawable.input_page_01)
                    .error(R.drawable.input_page_01)
                    .into(binding.ivSetupAvatar)
            }
        }

        viewModel.saveResult.observe(this) { result ->
            // Reset errors
            binding.tilUsername.error = null
            binding.tilDisplayName.error = null
            binding.tilFirstName.error = null
            binding.tilLastName.error = null

            result.errorUsername?.let { binding.tilUsername.error = it }
            result.errorDisplayName?.let { binding.tilDisplayName.error = it }
            result.errorFirstName?.let { binding.tilFirstName.error = it }
            result.errorLastName?.let { binding.tilLastName.error = it }

            result.errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }

            if (result.success) {
                Toast.makeText(this, "Perfil guardado con éxito", Toast.LENGTH_SHORT).show()

                // Save user logged state in SharedPreferences
                val username = binding.etUsername.text.toString().trim()
                val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("isLogged", true)
                    .putString("usuario", username)
                    .apply()

                // Navigate to HomePage
                val intent = Intent(this, HomePage::class.java)
                intent.putExtra("usuario", username)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
