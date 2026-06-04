package com.progweb.recipify.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.progweb.recipify.Destacados
import com.progweb.recipify.ProfileSetupActivity
import com.progweb.recipify.R
import com.progweb.recipify.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileSetupActivity::class.java)
            startActivity(intent)
        }

        binding.btnProfileLogout.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cargarDatosUsuario() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Local mock user fallback
            val prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val username = prefs.getString("usuario", "Usuario") ?: "Usuario"
            binding.tvProfileDisplayName.text = username
            binding.tvProfileUsername.text = "@$username"
            binding.tvProfileBio.text = "Usuario local sin datos en la nube."
            binding.tvProfileLocation.text = "Colombia"
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists() && _binding != null) {
                    val displayName = doc.getString("displayName") ?: ""
                    val username = doc.getString("username") ?: ""
                    val bio = doc.getString("bio") ?: ""
                    val photoURL = doc.getString("photoURL") ?: ""
                    
                    val locationMap = doc.get("location") as? Map<*, *>
                    val city = locationMap?.get("city") as? String ?: ""
                    val country = locationMap?.get("country") as? String ?: "Colombia"
                    val locationStr = if (city.isNotEmpty()) "$city, $country" else country

                    val followers = doc.getLong("followersCount") ?: 0
                    val following = doc.getLong("followingCount") ?: 0
                    val bookmarks = doc.getLong("bookmarksCount") ?: 0

                    binding.tvProfileDisplayName.text = if (displayName.isNotEmpty()) displayName else currentUser.displayName ?: "Recipifyer"
                    binding.tvProfileUsername.text = if (username.isNotEmpty()) "@$username" else "@usuario"
                    binding.tvProfileBio.text = if (bio.isNotEmpty()) bio else "Este usuario no ha agregado una biografía."
                    binding.tvProfileLocation.text = locationStr

                    binding.tvFollowersCount.text = followers.toString()
                    binding.tvFollowingCount.text = following.toString()
                    binding.tvBookmarksCount.text = bookmarks.toString()

                    if (photoURL.isNotEmpty()) {
                        Glide.with(this)
                            .load(photoURL)
                            .placeholder(R.drawable.input_page_01)
                            .error(R.drawable.input_page_01)
                            .into(binding.ivProfileAvatar)
                    }
                }
            }
            .addOnFailureListener { e ->
                if (_binding != null) {
                    Toast.makeText(requireContext(), "Error al cargar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun cerrarSesion() {
        auth.signOut()

        val prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(requireActivity(), Destacados::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}