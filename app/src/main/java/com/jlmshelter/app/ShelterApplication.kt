package com.jlmshelter.app

import android.app.Application
import org.osmdroid.config.Configuration

class ShelterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize OSM configuration
        Configuration.getInstance().userAgentValue = packageName
    }
}
