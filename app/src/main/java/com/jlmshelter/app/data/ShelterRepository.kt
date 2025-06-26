package com.jlmshelter.app.data

import android.content.Context
import android.location.Location
import com.google.gson.Gson
import com.jlmshelter.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

/**
 * Repository for accessing shelter data
 */
class ShelterRepository(private val context: Context) {

    /**
     * Load shelters from GeoJSON file in assets
     */
    suspend fun loadShelters(): List<Shelter> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.resources.openRawResource(
                R.raw.public_shelters_jerusalem_190625
            )
            
            val reader = InputStreamReader(inputStream)
            val featureCollection = Gson().fromJson(reader, FeatureCollection::class.java)
            
            return@withContext featureCollection.features.map { Shelter.fromFeature(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    /**
     * Find nearest shelters to a given location
     * @param userLocation The user's current location
     * @param limit The maximum number of shelters to return
     * @return List of shelters sorted by distance
     */
    suspend fun findNearestShelters(
        userLocation: Location,
        limit: Int = 10
    ): List<Shelter> = withContext(Dispatchers.Default) {
        val shelters = loadShelters()
        
        // Calculate distance for each shelter
        shelters.forEach { shelter ->
            val shelterLocation = Location("").apply {
                latitude = shelter.latitude
                longitude = shelter.longitude
            }
            shelter.distance = userLocation.distanceTo(shelterLocation)
        }
        
        // Sort by distance and take the specified limit
        return@withContext shelters.sortedBy { it.distance }.take(limit)
    }
}
