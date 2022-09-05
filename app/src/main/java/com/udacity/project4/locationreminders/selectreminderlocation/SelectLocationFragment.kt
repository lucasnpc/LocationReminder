package com.udacity.project4.locationreminders.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.domain.model.CustomLocation
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.LOCATION_SELECTED_KEY
import com.udacity.project4.utils.foregroundAndBackgroundLocationPermissionApproved
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

@SuppressLint("MissingPermission")
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    override val _viewModel: SaveReminderViewModel by inject()
    private val binding: FragmentSelectLocationBinding by lazy {
        FragmentSelectLocationBinding.inflate(layoutInflater)
    }
    private lateinit var map: GoogleMap
    private var customLocation: CustomLocation? = null
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        requireActivity().addMenuProvider(selectedReminderMenu, viewLifecycleOwner)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveLocation.setOnClickListener {
            customLocation?.let { customLocation ->
                findNavController().apply {
                    previousBackStackEntry?.savedStateHandle?.set(
                        LOCATION_SELECTED_KEY,
                        customLocation
                    )
                    popBackStack()
                }
            } ?: Toast.makeText(requireContext(), getString(R.string.ask_poi), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun onLocationSelected(location: CustomLocation) {
        customLocation = location
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        configureMap()
        if (requireActivity().foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
            return
        }
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }

    private fun configureMap() {
        map.run {
            setMapStyle()
            setPoiClick()
            setMapLongClick()
            setMapMarkerClick()
        }
    }

    private fun enableMapLocation() {
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener {
            it?.let {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.latitude,
                            it.longitude
                        ), ZOOM_LEVEL
                    )
                )
            }
        }
    }

    private fun GoogleMap.setMapMarkerClick() {
        this.setOnMarkerClickListener { marker ->
            onLocationSelected(
                CustomLocation(
                    name = marker.title ?: "",
                    position = marker.position
                )
            )
            false
        }
    }

    private fun GoogleMap.setMapStyle() {
        try {
            this.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun GoogleMap.setPoiClick() {
        this.setOnPoiClickListener { poi ->
            this.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
            )?.showInfoWindow()
            onLocationSelected(CustomLocation(poi.name, poi.latLng))
        }
    }

    private fun GoogleMap.setMapLongClick() {
        this.setOnMapLongClickListener { latlng ->
            this.addMarker(
                MarkerOptions().position(latlng).title(getString(R.string.dropped_pin))
            )?.also {
                onLocationSelected(CustomLocation(it.title.toString(), it.position))
            }
        }
    }

    private val selectedReminderMenu = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.map_options, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if (!::map.isInitialized) return false
            when (menuItem.itemId) {
                R.id.normal_map -> {
                    map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    return true
                }
                R.id.hybrid_map -> {
                    map.mapType = GoogleMap.MAP_TYPE_HYBRID
                    return true
                }
                R.id.satellite_map -> {
                    map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    return true
                }
                R.id.terrain_map -> {
                    map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    return true
                }
                else -> return false
            }
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    enableMapLocation()
                    checkDeviceLocationSettingsAndStartGeofence()
                }
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    enableMapLocation()
                    checkDeviceLocationSettingsAndStartGeofence()
                }
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    R.string.permission_denied_explanation,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private val result =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK)
                checkDeviceLocationSettingsAndStartGeofence()
        }

    private fun checkDeviceLocationSettingsAndStartGeofence() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    result.launch(
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        "SelectLocationFragment",
                        "Error geting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                enableMapLocation()
            }
        }
    }

    private companion object {
        const val ZOOM_LEVEL = 15f
    }
}
