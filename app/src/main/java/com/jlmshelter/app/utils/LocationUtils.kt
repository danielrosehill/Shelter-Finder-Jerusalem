package com.jlmshelter.app.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat

import com.jlmshelter.app.data.Shelter

/**
 * Utility functions for location-related operations
 */
object LocationUtils {

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Open application settings to enable location permissions
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    /**
     * Open Google Maps for navigation to a shelter
     */
    fun navigateToShelter(context: Context, shelter: Shelter, userLocation: Location?) {
        val uri = if (userLocation != null) {
            // If we have user location, use it as the starting point
            Uri.parse("https://www.google.com/maps/dir/?api=1" +
                    "&origin=${userLocation.latitude},${userLocation.longitude}" +
                    "&destination=${shelter.latitude},${shelter.longitude}" +
                    "&travelmode=walking")
        } else {
            // Otherwise just navigate to the shelter
            Uri.parse("https://www.google.com/maps/search/?api=1" +
                    "&query=${shelter.latitude},${shelter.longitude}")
        }
        
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        
        // If Google Maps is not installed, open in browser
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Open in browser if Maps app is not available
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }



    /**
     * Format distance for display
     */
    fun formatDistance(distanceInMeters: Float): String {
        return if (distanceInMeters < 1000) {
            "${distanceInMeters.toInt()} m"
        } else {
            String.format("%.1f km", distanceInMeters / 1000)
        }
    }
}
