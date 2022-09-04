package com.udacity.project4.locationreminders

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.login.AuthenticationViewModel
import com.udacity.project4.utils.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.AuthenticationState
import com.udacity.project4.utils.GEOFENCE_EXPIRATION
import com.udacity.project4.utils.GEOFENCE_RADIUS_IN_METERS
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemindersActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val binding: ActivityRemindersBinding by lazy {
        ActivityRemindersBinding.inflate(layoutInflater)
    }
    private val _viewModel: RemindersListViewModel by viewModel()
    private val authViewModel: AuthenticationViewModel by viewModels()
    private val navController: NavController by lazy {
        binding.navHostFragment.findNavController()
    }

    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(this)
    }
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        observeAuthenticationState()
        requestForegroundAndBackgroundLocationPermissions()
    }

    @SuppressLint("MissingPermission")
    private fun observeReminderList() {
        _viewModel.reminderListLiveDataItem.observe(this) {
            val geofences: ArrayList<Geofence> = arrayListOf()
            it.forEach { reminderItem ->
                geofences.add(
                    Geofence.Builder()
                        .setRequestId(reminderItem.id)
                        .setCircularRegion(
                            reminderItem.latitude ?: 0f.toDouble(),
                            reminderItem.longitude ?: 0f.toDouble(),
                            GEOFENCE_RADIUS_IN_METERS
                        )
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .setExpirationDuration(GEOFENCE_EXPIRATION)
                        .build()
                )
            }

            geofencingClient.removeGeofences(geofencePendingIntent).run {
                addOnSuccessListener {
                    geofences.forEach { geofence ->
                        geofencingClient.addGeofences(
                            getGeofencingRequest(geofence),
                            geofencePendingIntent
                        )
                            .run {
                                addOnSuccessListener {
                                    println("success")
                                }
                                addOnFailureListener {
                                    println("failed $geofence")
                                }
                            }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observeAuthenticationState() {
        authViewModel.authenticationState.observe(this) { state ->
            when (state) {
                AuthenticationState.UNAUTHENTICATED -> {
                    startActivity(Intent(this, AuthenticationActivity::class.java))
                    finish()
                }
                else -> Unit
            }
        }
    }

    private fun requestForegroundAndBackgroundLocationPermissions() {
//        if (foregroundAndBackgroundLocationPermissionApproved()) {
//            observeReminderList()
//            return
//        }
//        locationPermissionRequest.launch(
//            arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//            )
//        )
    }
}
