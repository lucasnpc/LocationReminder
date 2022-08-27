package com.udacity.project4.locationreminders.selectreminderlocation


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.core.location.LocationListenerCompat
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
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

//        TODO: add style to the map
//        TODO: put a marker to location that the user selected

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0f, this)

//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        location?.let {
            showUserCurrentPosition(it)
        }
    }

    private val selectedReminderMenu = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.map_options, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
            // TODO: Change the map type based on the user's selection.
            R.id.normal_map -> {
                true
            }
            R.id.hybrid_map -> {
                true
            }
            R.id.satellite_map -> {
                true
            }
            R.id.terrain_map -> {
                true
            }
            else -> false
        }
    }

    override fun onLocationChanged(location: Location) {
        if (::map.isInitialized) {
            showUserCurrentPosition(location)
        }
    }

    private fun showUserCurrentPosition(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, ZOOM_LEVEL))
    }

    private companion object {
        const val ZOOM_LEVEL = 15f
    }
}
