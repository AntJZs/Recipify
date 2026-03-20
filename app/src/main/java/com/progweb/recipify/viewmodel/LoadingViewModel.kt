package com.progweb.recipify.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingViewModel : ViewModel() {

    private val _navigateToNext = MutableLiveData<Boolean>()
    val navigateToNext: LiveData<Boolean> = _navigateToNext

    init {
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            delay(1000)
            _navigateToNext.value = true
        }
    }
}
