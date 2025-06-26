package com.jlmshelter.app.ui

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.jlmshelter.app.data.Shelter
import com.jlmshelter.app.data.ShelterRepository
import com.jlmshelter.app.databinding.FragmentShelterListBinding
import kotlinx.coroutines.launch

class ShelterListFragment : Fragment() {

    private var _binding: FragmentShelterListBinding? = null
    private val binding get() = _binding!!
    
    private val args: ShelterListFragmentArgs by navArgs()
    private lateinit var shelterRepository: ShelterRepository
    private lateinit var shelterAdapter: ShelterAdapter
    
    private var userLocation: Location? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShelterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize repository
        shelterRepository = ShelterRepository(requireContext())
        
        // Set up user location from arguments
        userLocation = Location("").apply {
            latitude = args.userLatitude.toDouble()
            longitude = args.userLongitude.toDouble()
        }
        
        // Set up RecyclerView
        shelterAdapter = ShelterAdapter(userLocation)
        binding.sheltersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shelterAdapter
        }
        
        // Set up back button
        binding.backToMapButton.setOnClickListener {
            findNavController().navigate(
                ShelterListFragmentDirections.actionShelterListFragmentToMapFragment()
            )
        }
        
        // Load nearest shelters
        loadNearestShelters()
    }
    
    private fun loadNearestShelters() {
        binding.progressBar.visibility = View.VISIBLE
        
        viewLifecycleOwner.lifecycleScope.launch {
            userLocation?.let { location ->
                val nearestShelters = shelterRepository.findNearestShelters(location)
                shelterAdapter.submitList(nearestShelters)
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
