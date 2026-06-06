package com.progweb.recipify

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class RecipifyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        configureFirestoreOffline()
    }

    private fun configureFirestoreOffline() {
        val cacheSettings = PersistentCacheSettings.newBuilder()
            .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(cacheSettings)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}
