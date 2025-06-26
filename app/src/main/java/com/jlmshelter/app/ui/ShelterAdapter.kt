package com.jlmshelter.app.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jlmshelter.app.R
import com.jlmshelter.app.data.Shelter
import com.jlmshelter.app.databinding.ItemShelterBinding
import com.jlmshelter.app.utils.LocationUtils

class ShelterAdapter(private val userLocation: Location?) : 
    ListAdapter<Shelter, ShelterAdapter.ShelterViewHolder>(ShelterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelterViewHolder {
        val binding = ItemShelterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShelterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShelterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ShelterViewHolder(private val binding: ItemShelterBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(shelter: Shelter) {
            binding.apply {
                shelterNameTextView.text = shelter.name
                shelterAddressTextView.text = shelter.address
                
                // Set category text and color based on category number
                val categoryText = when(shelter.categoryNumber) {
                    1 -> "Protected Underground Parking Lot"
                    2 -> "Public Shelter"
                    3 -> "Public Shelter at School"
                    else -> "Shelter"
                }
                shelterCategoryChip.text = categoryText
                
                // Apply color coding based on shelter category
                val categoryColor = when(shelter.categoryNumber) {
                    1 -> Color.RED
                    2 -> Color.BLUE
                    3 -> Color.GREEN
                    else -> Color.GRAY
                }
                
                // Set chip background color with alpha for better visibility
                val alphaColor = Color.argb(50, Color.red(categoryColor), 
                                           Color.green(categoryColor), 
                                           Color.blue(categoryColor))
                shelterCategoryChip.chipBackgroundColor = ColorStateList.valueOf(alphaColor)
                
                // Set chip text color
                shelterCategoryChip.setTextColor(categoryColor)
                
                // Format distance
                shelterDistanceTextView.text = root.context.getString(
                    R.string.distance,
                    LocationUtils.formatDistance(shelter.distance)
                )
                
                // Set up navigation button
                takeMeToShelterButton.setOnClickListener {
                    LocationUtils.navigateToShelter(root.context, shelter, userLocation)
                }
            }
        }
    }

    class ShelterDiffCallback : DiffUtil.ItemCallback<Shelter>() {
        override fun areItemsTheSame(oldItem: Shelter, newItem: Shelter): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Shelter, newItem: Shelter): Boolean {
            return oldItem == newItem
        }
    }
}
