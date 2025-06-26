package com.jlmshelter.app.ui

import android.os.Bundle
import androidx.navigation.NavArgs

/**
 * Arguments for ShelterListFragment
 */
data class ShelterListFragmentArgs(
    val userLatitude: Float,
    val userLongitude: Float
) : NavArgs {
    
    fun toBundle(): Bundle {
        val result = Bundle()
        result.putFloat("userLatitude", this.userLatitude)
        result.putFloat("userLongitude", this.userLongitude)
        return result
    }
    
    companion object {
        @JvmStatic
        fun fromBundle(bundle: Bundle): ShelterListFragmentArgs {
            bundle.classLoader = ShelterListFragmentArgs::class.java.classLoader
            return ShelterListFragmentArgs(
                bundle.getFloat("userLatitude"),
                bundle.getFloat("userLongitude")
            )
        }
    }
}
