package com.udacity.project4.locationreminders.selectreminderlocation


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.location.LocationListenerCompat
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.domain.model.CustomLocation
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.LOCATION_SELECTED_KEY
import com.udacity.project4.utils.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.udacity.project4.utils.permissionDenied
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

@SuppressLint("MissingPermission")
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, LocationListenerCompat {

    override val _viewModel: SaveReminderViewModel by inject()
    private val binding: FragmentSelectLocationBinding by lazy {
        FragmentSelectLocationBinding.inflate(layoutInflater)
    }
    private lateinit var map: GoogleMap
    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private val location by lazy {
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }
    private var customLocation: CustomLocation? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (permissionDenied) {
            newInstance(finishActivity = false).show(parentFragmentManager, "dialog")
            findNavController().popBackStack()
        }

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        requireActivity().addMenuProvider(selectedReminderMenu, viewLifecycleOwner)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0f, this)

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
        map.isMyLocationEnabled = true
        location?.let {
            showUserCurrentPosition(it)
        }
        map.run {
            setMapStyle()
            setPoiClick()
            setMapLongClick()
            setMapMarkerClick()
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
            )
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

    override fun onLocationChanged(location: Location) {}

    private fun showUserCurrentPosition(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, ZOOM_LEVEL))
    }

    private companion object {
        const val ZOOM_LEVEL = 15f
    }
}
