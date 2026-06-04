package com.progweb.recipify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Placeholder
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.progweb.recipify.databinding.ActivityHomePageBinding
import com.progweb.recipify.home.HomeFragment
import com.progweb.recipify.home.ProfileFragment
import com.progweb.recipify.home.SavedFragment
import com.progweb.recipify.home.SearchFragment

class HomePage : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLogged = prefs.getBoolean("isLogged", false)
        android.util.Log.d("SESSION", "isLogged en HomePage: $isLogged")

        if (!isLogged) {
            startActivity(Intent(this, Destacados::class.java))
            finish()
            return
        }

        // Firebase Auth Verification Gate
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val isVerified = doc.getBoolean("isVerified") ?: false
                        if (!isVerified) {
                            val intent = Intent(this, ProfileSetupActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        // Document doesn't exist yet, force setup
                        val intent = Intent(this, ProfileSetupActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
        }

        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // No padding bottom for BottomNav
            insets
        }

        // Load HomeFragment by default
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_buscar -> {
                    replaceFragment(SearchFragment())
                    true
                }
                R.id.nav_guardados -> {
                    replaceFragment(SavedFragment())
                    true
                }
                R.id.nav_perfil -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
