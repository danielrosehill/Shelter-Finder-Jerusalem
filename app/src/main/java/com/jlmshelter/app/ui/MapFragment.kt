package com.jlmshelter.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jlmshelter.app.R
import com.jlmshelter.app.data.Shelter
import com.jlmshelter.app.data.ShelterRepository
import com.jlmshelter.app.databinding.FragmentMapBinding
import com.jlmshelter.app.utils.LocationUtils
import com.jlmshelter.app.utils.OSMUtils
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shelterRepository: ShelterRepository
    
    private var userLocation: Location? = null
    private var shelters: List<Shelter> = emptyList()
    private var radiusOverlay: Polygon? = null
    
    private val RADIUS_METERS = 200.0 // 200 meter radius
    private val DISPLAY_RADIUS = 300.0 // Display radius (meters) for zoom level calculation
    
    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions.entries.any { 
            it.key in listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) && it.value 
        }
        
        if (locationGranted) {
            enableMyLocation()
            loadShelters()
        } else {
            showLocationPermissionDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize OSM configuration
        OSMUtils.initialize(requireContext())
        
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        // Initialize repository
        shelterRepository = ShelterRepository(requireContext())
        
        // Set up map
        mapView = binding.map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true) // Explicitly enable multi-touch for zooming
        mapView.controller.setZoom(16.0) // Higher zoom level for better detail
        mapView.minZoomLevel = 4.0
        mapView.maxZoomLevel = 19.0
        
        // Set up location overlay
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView)
        myLocationOverlay.enableMyLocation()
        mapView.overlays.add(myLocationOverlay)
        
        // Immediately request location permission if not granted
        if (!LocationUtils.hasLocationPermissions(requireContext())) {
            requestLocationPermission()
        } else {
            enableMyLocation()
        }
        
        // Load shelters
        loadShelters()
        
        // Set up My Location button
        binding.myLocationButton.setOnClickListener {
            if (LocationUtils.hasLocationPermissions(requireContext())) {
                zoomToUserLocation()
            } else {
                requestLocationPermission()
            }
        }
        
        // Set up find shelters button
        binding.findSheltersButton.setOnClickListener {
            if (userLocation != null) {
                findNavController().navigate(
                    MapFragmentDirections.actionMapFragmentToShelterListFragment(
                        userLocation!!.latitude.toFloat(),
                        userLocation!!.longitude.toFloat()
                    )
                )
            } else {
                checkLocationPermission()
            }
        }
    }


    
    private fun checkLocationPermission() {
        when {
            LocationUtils.hasLocationPermissions(requireContext()) -> {
                enableMyLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showLocationPermissionDialog()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }
    
    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    private fun showLocationPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.location_permission_required)
            .setMessage(R.string.location_permission_required)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                LocationUtils.openAppSettings(requireContext())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (LocationUtils.hasLocationPermissions(requireContext())) {
            myLocationOverlay.enableMyLocation()
            
            // Get last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = it
                    // Automatically zoom to user location when permissions are granted
                    zoomToUserLocation()
                }
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun zoomToUserLocation() {
        if (LocationUtils.hasLocationPermissions(requireContext())) {
            // Get current location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = it
                    val userPoint = GeoPoint(it.latitude, it.longitude)
                    
                    // Calculate appropriate zoom level to show the display radius
                    val zoomLevel = calculateZoomLevelForRadius(DISPLAY_RADIUS)
                    
                    // Animate to user location with calculated zoom level
                    mapView.controller.animateTo(userPoint)
                    mapView.controller.setZoom(zoomLevel)
                    
                    // Draw 200m radius circle
                    drawRadiusCircle(userPoint, RADIUS_METERS)
                }
            }
        }
    }
    
    /**
     * Calculate the appropriate zoom level to display a given radius in meters
     * This is an approximation based on the fact that at the equator:
     * zoom level 1 ~ 78,271,484 meters per pixel
     * Each zoom level divides this by 2
     */
    private fun calculateZoomLevelForRadius(radiusInMeters: Double): Double {
        // Get the screen width in pixels
        val screenWidth = resources.displayMetrics.widthPixels
        
        // We want the radius to take up about 1/3 of the screen width
        val targetScreenWidth = screenWidth / 3
        
        // Calculate the ground resolution at the equator for zoom level 1
        val earthCircumference = 40075016.686 // in meters
        val metersPerPixelAtZoom1 = earthCircumference / 256
        
        // Calculate the required zoom level
        val zoomLevel = Math.log(metersPerPixelAtZoom1 / (radiusInMeters / targetScreenWidth)) / Math.log(2.0)
        
        // Ensure zoom level is within valid range
        return Math.min(Math.max(zoomLevel, mapView.minZoomLevel), mapView.maxZoomLevel)
    }
    
    private fun drawRadiusCircle(center: GeoPoint, radiusMeters: Double) {
        // Remove existing radius overlay if any
        if (radiusOverlay != null) {
            mapView.overlays.remove(radiusOverlay)
        }
        
        // Create a circle of points around the center
        val circlePoints = ArrayList<GeoPoint>()
        for (i in 0 until 360 step 5) {
            val radian = Math.toRadians(i.toDouble())
            // Convert radius from meters to degrees (approximate)
            val radiusDegrees = radiusMeters / 111000 // 1 degree is approximately 111km
            
            val lat = center.latitude + radiusDegrees * Math.sin(radian)
            val lon = center.longitude + radiusDegrees * Math.cos(radian) / Math.cos(Math.toRadians(center.latitude))
            
            circlePoints.add(GeoPoint(lat, lon))
        }
        
        // Create polygon for the circle
        radiusOverlay = Polygon().apply {
            points = circlePoints
            fillColor = 0x3000FF00 // Semi-transparent green
            strokeColor = 0xFF00FF00.toInt() // Green border
            strokeWidth = 3f
        }
        
        // Add to map
        mapView.overlays.add(radiusOverlay)
        mapView.invalidate()
    }
    
    private fun loadShelters() {
        viewLifecycleOwner.lifecycleScope.launch {
            shelters = shelterRepository.loadShelters()
            displaySheltersOnMap(shelters)
        }
    }
    
    private fun displaySheltersOnMap(shelters: List<Shelter>) {
        // Remove existing markers but keep location overlay
        val overlays = mapView.overlays.filter { it is MyLocationNewOverlay }
        mapView.overlays.clear()
        mapView.overlays.addAll(overlays)
        
        // Add markers for each shelter
        shelters.forEach { shelter ->
            val marker = OSMUtils.createShelterMarker(
                requireContext(),
                mapView,
                shelter.latitude,
                shelter.longitude,
                shelter.name,
                shelter.address,
                shelter.categoryNumber
            )
            
            // Add click listener to show shelter details dialog
            marker.setOnMarkerClickListener { clickedMarker, _ ->
                showShelterDetailsDialog(shelter)
                true // Consume the event
            }
            
            mapView.overlays.add(marker)
        }
        
        // If no user location, center on Jerusalem
        if (userLocation == null) {
            val jerusalemCenter = GeoPoint(31.7683, 35.2137)
            mapView.controller.animateTo(jerusalemCenter)
            mapView.controller.setZoom(12.0)
        }
        
        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
        _binding = null
    }
    
    /**
     * Show a dialog with shelter details
     */
    private fun showShelterDetailsDialog(shelter: Shelter) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_shelter_details, null)
        
        // Set shelter details in the dialog
        dialogView.findViewById<TextView>(R.id.shelter_name).text = shelter.name
        dialogView.findViewById<TextView>(R.id.shelter_address).text = shelter.address
        
        // Set category text based on category number with color coding
        val categoryTextView = dialogView.findViewById<TextView>(R.id.shelter_category)
        val categoryText = when(shelter.categoryNumber) {
            1 -> "Protected Underground Parking Lot"
            2 -> "Public Shelter"
            3 -> "Public Shelter at School"
            else -> "Shelter"
        }
        categoryTextView.text = getString(R.string.shelter_category, categoryText)
        
        // Apply color coding based on shelter category
        val categoryColor = when(shelter.categoryNumber) {
            1 -> Color.RED
            2 -> Color.BLUE
            3 -> Color.GREEN
            else -> Color.GRAY
        }
        categoryTextView.setTextColor(categoryColor)
        
        // Create and show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.shelter_details)
            .setView(dialogView)
            .create()
        
        // Set close button click listener
        dialogView.findViewById<Button>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
}
