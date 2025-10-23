package com.neverforget

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NeverForgetApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialisation de l'application
        // Les canaux de notification seront créés ici
    }
}