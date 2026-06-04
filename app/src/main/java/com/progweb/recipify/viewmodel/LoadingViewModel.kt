package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.progweb.recipify.Destacados
import com.progweb.recipify.HomePage
import com.progweb.recipify.ProfileSetupActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingViewModel : ViewModel() {

    private val _navigationTarget = MutableLiveData<Class<*>>()
    val navigationTarget: LiveData<Class<*>> = _navigationTarget

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(currentUser.uid).get()
                    .addOnCompleteListener { task ->
                        val targetActivity = if (task.isSuccessful) {
                            val doc = task.result
                            val isVerified = doc?.getBoolean("isVerified") ?: false
                            if (isVerified) {
                                HomePage::class.java
                            } else {
                                ProfileSetupActivity::class.java
                            }
                        } else {
                            // Offline or network error: if currentUser exists, redirect to verification/setup
                            ProfileSetupActivity::class.java
                        }

                        val elapsed = System.currentTimeMillis() - startTime
                        viewModelScope.launch {
                            if (elapsed < 1000) {
                                delay(1000 - elapsed)
                            }
                            _navigationTarget.value = targetActivity
                        }
                    }
            } else {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < 1000) {
                    delay(1000 - elapsed)
                }
                _navigationTarget.value = Destacados::class.java
            }
        }
    }
}
