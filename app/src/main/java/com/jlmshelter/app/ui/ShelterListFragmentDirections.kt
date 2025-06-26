package com.jlmshelter.app.ui

import androidx.navigation.NavDirections
import com.jlmshelter.app.R

/**
 * Navigation directions for ShelterListFragment
 */
class ShelterListFragmentDirections private constructor() {
    companion object {
        fun actionShelterListFragmentToMapFragment(): NavDirections {
            return object : NavDirections {
                override val actionId: Int = R.id.action_shelterListFragment_to_mapFragment
                override val arguments = androidx.core.os.bundleOf()
            }
        }
    }
}
