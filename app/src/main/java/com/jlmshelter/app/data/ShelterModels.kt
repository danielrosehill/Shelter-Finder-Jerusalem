package com.jlmshelter.app.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * GeoJSON Feature Collection model
 */
data class FeatureCollection(
    @SerializedName("type") val type: String,
    @SerializedName("features") val features: List<Feature>
)

/**
 * GeoJSON Feature model
 */
@Parcelize
data class Feature(
    @SerializedName("type") val type: String,
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("properties") val properties: Properties
) : Parcelable

/**
 * GeoJSON Geometry model
 */
@Parcelize
data class Geometry(
    @SerializedName("type") val type: String,
    @SerializedName("coordinates") val coordinates: List<Double>
) : Parcelable

/**
 * GeoJSON Properties model for shelter data
 */
@Parcelize
data class Properties(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: String,
    @SerializedName("category_number") val categoryNumber: Int,
    @SerializedName("short_address") val shortAddress: String,
    @SerializedName("full_address") val fullAddress: String,
    @SerializedName("original_id") val originalId: Int
) : Parcelable

/**
 * Shelter model with distance calculation
 */
@Parcelize
data class Shelter(
    val id: Int,
    val name: String,
    val address: String,
    val fullAddress: String,
    val category: String,
    val categoryNumber: Int,
    val latitude: Double,
    val longitude: Double,
    var distance: Float = 0f
) : Parcelable {
    companion object {
        fun fromFeature(feature: Feature): Shelter {
            return Shelter(
                id = feature.properties.id,
                name = feature.properties.name,
                address = feature.properties.address,
                fullAddress = feature.properties.fullAddress,
                category = feature.properties.category,
                categoryNumber = feature.properties.categoryNumber,
                // GeoJSON coordinates are in [longitude, latitude] order
                longitude = feature.geometry.coordinates[0],
                latitude = feature.geometry.coordinates[1]
            )
        }
    }
}
