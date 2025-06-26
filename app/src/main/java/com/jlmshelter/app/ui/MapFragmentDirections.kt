package com.jlmshelter.app.ui

import androidx.navigation.NavDirections
import com.jlmshelter.app.R

/**
 * Navigation directions for MapFragment
 */
class MapFragmentDirections private constructor() {
    companion object {
        fun actionMapFragmentToShelterListFragment(
            userLatitude: Float,
            userLongitude: Float
        ): NavDirections {
            return object : NavDirections {
                override val actionId: Int = R.id.action_mapFragment_to_shelterListFragment
                
                override val arguments = ShelterListFragmentArgs(
                    userLatitude = userLatitude,
                    userLongitude = userLongitude
                ).toBundle()
            }
        }
    }
}
