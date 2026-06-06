package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DestacadosViewModel : ViewModel() {

    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    private val _navigateToRegister = MutableLiveData<Boolean>()
    val navigateToRegister: LiveData<Boolean> = _navigateToRegister

    fun onLoginClicked() {
        _navigateToLogin.value = true
    }

    fun onRegisterClicked() {
        _navigateToRegister.value = true
    }

    fun onNavigationDone() {
        _navigateToLogin.value = false
        _navigateToRegister.value = false
    }
}
