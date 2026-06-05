package com.progweb.recipify

import android.app.Application
import com.google.firebase.FirebaseApp

class RecipifyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
