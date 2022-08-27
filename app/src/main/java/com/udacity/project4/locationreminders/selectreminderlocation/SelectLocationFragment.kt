package com.udacity.project4.locationreminders.selectreminderlocation


import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.udacity.project4.utils.permissionDenied
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private val binding: FragmentSelectLocationBinding by lazy {
        FragmentSelectLocationBinding.inflate(layoutInflater)
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

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected

        parentFragmentManager.findFragmentById(R.id.map)?.apply {
            this as SupportMapFragment
            this.getMapAsync(this@SelectLocationFragment)
        }

//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        if (permissionDenied) {
            newInstance(finishActivity = false).show(parentFragmentManager, "dialog")
        }
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
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
}
