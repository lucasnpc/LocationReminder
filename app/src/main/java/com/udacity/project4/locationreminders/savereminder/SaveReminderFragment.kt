package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.domain.model.CustomLocation
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject

@RequiresApi(Build.VERSION_CODES.M)
@SuppressLint("MissingPermission")
class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private val binding: FragmentSaveReminderBinding by lazy {
        FragmentSaveReminderBinding.inflate(layoutInflater)
    }
    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(requireContext())
    }
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
                val geofence = Geofence.Builder()
                    .setRequestId(reminderSelectedLocationStr.value ?: "")
                    .setCircularRegion(
                        latitude.value ?: 0f.toDouble(),
                        longitude.value ?: 0f.toDouble(),
                        GEOFENCE_RADIUS_IN_METERS
                    )
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setExpirationDuration(GEOFENCE_EXPIRATION)
                    .build()

                geofencingClient.addGeofences(getGeofencingRequest(geofence), geofencePendingIntent)
                    .run {
                        addOnSuccessListener {
                            println("success")
                        }
                        addOnFailureListener {
                            println("failed $it")
                        }
                    }
            }
        }
    }

    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }


    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }
}
