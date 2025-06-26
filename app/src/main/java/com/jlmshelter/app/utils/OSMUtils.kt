package com.jlmshelter.app.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.jlmshelter.app.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

/**
 * Utility functions for OpenStreetMap
 */
object OSMUtils {

    /**
     * Initialize OSM configuration
     */
    fun initialize(context: Context) {
        // Set user agent to prevent getting banned from OSM servers
        Configuration.getInstance().userAgentValue = context.packageName
        
        // Set the tile cache path
        Configuration.getInstance().osmdroidTileCache = File(
            context.cacheDir, "osm"
        )
    }

    /**
     * Configure the map view with default settings
     */
    fun configureMapView(mapView: MapView) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
        mapView.minZoomLevel = 4.0
        mapView.maxZoomLevel = 19.0
        mapView.isTilesScaledToDpi = true
        mapView.isHorizontalMapRepetitionEnabled = false
        mapView.isVerticalMapRepetitionEnabled = false
    }

    /**
     * Create a marker for a shelter
     */
    fun createShelterMarker(
        context: Context,
        mapView: MapView,
        latitude: Double,
        longitude: Double,
        title: String,
        snippet: String,
        categoryNumber: Int
    ): Marker {
        val marker = Marker(mapView)
        marker.position = GeoPoint(latitude, longitude)
        marker.title = title
        marker.snippet = snippet
        marker.icon = getShelterMarkerIcon(context, categoryNumber)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        return marker
    }

    /**
     * Get marker icon based on shelter category
     * 
     * Category 1: Protected underground parking lots (red)
     * Category 2: Public shelters (blue)
     * Category 3: Public shelters at schools (green)
     */
    private fun getShelterMarkerIcon(context: Context, categoryNumber: Int): Drawable? {
        return when (categoryNumber) {
            1 -> ContextCompat.getDrawable(context, R.drawable.ic_shelter_marker_cat1) // Parking lots (red)
            2 -> ContextCompat.getDrawable(context, R.drawable.ic_shelter_marker_cat2) // Public shelters (blue)
            3 -> ContextCompat.getDrawable(context, R.drawable.ic_shelter_marker_cat3) // School shelters (green)
            4 -> ContextCompat.getDrawable(context, R.drawable.ic_shelter_marker_cat4)
            5 -> ContextCompat.getDrawable(context, R.drawable.ic_shelter_marker_cat5)
            else -> ContextCompat.getDrawable(context, R.drawable.ic_shelter_marker) // Default fallback
        }
    }

    /**
     * Open navigation to a location using an external app
     */
    fun navigateToLocation(context: Context, latitude: Double, longitude: Double) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        intent.data = android.net.Uri.parse("geo:0,0?q=$latitude,$longitude")
        context.startActivity(intent)
    }
}
