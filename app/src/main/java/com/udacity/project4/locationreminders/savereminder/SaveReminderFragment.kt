package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.domain.model.CustomLocation
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.LOCATION_SELECTED_KEY
import com.udacity.project4.utils.foregroundAndBackgroundLocationPermissionApproved
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

@RequiresApi(Build.VERSION_CODES.M)
class SaveReminderFragment : BaseFragment() {
    override val _viewModel: SaveReminderViewModel by inject()
    private val binding: FragmentSaveReminderBinding by lazy {
        FragmentSaveReminderBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<CustomLocation>(
            LOCATION_SELECTED_KEY
        )?.observe(viewLifecycleOwner) {
            _viewModel.apply {
                reminderSelectedLocationStr.value = it.name
                latitude.value = it.position.latitude
                longitude.value = it.position.longitude
            }
        }

        binding.saveReminder.setOnClickListener {
            if (requireActivity().foregroundAndBackgroundLocationPermissionApproved()) {
                checkDeviceLocationSettingsAndStartGeofence()
                return@setOnClickListener
            }

            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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

    private val backgroundLocationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted)
                checkDeviceLocationSettingsAndStartGeofence()
            else {
                Toast.makeText(
                    requireContext(),
                    R.string.permission_denied_explanation,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val result =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK)
                saveDataAndGeofence()
            else
                _viewModel.showToast.value = R.string.error_adding_geofence
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
                        "SaveReminderFragment",
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
                saveDataAndGeofence()
            }
        }
    }

    private fun saveDataAndGeofence() {
        with(_viewModel) {
            validateAndSaveReminder(
                ReminderDataItem(
                    title = reminderTitle.value,
                    description = reminderDescription.value,
                    location = reminderSelectedLocationStr.value,
                    latitude = latitude.value,
                    longitude = longitude.value
                )
            )
        }
    }
}
