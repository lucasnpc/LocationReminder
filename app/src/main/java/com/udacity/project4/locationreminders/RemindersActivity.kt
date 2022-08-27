package com.udacity.project4.locationreminders

import android.Manifest
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.utils.AuthenticationState
import com.udacity.project4.utils.LOCATION_PERMISSION_REQUEST_CODE
import com.udacity.project4.utils.PermissionUtils.isPermissionGranted
import com.udacity.project4.utils.enableMyLocation
import com.udacity.project4.utils.permissionDenied
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemindersActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val binding: ActivityRemindersBinding by lazy {
        ActivityRemindersBinding.inflate(layoutInflater)
    }
    private val _viewModel: RemindersListViewModel by viewModel()
    private val navController: NavController by lazy {
        binding.navHostFragment.findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        observeAuthenticationState()
        enableMyLocation()
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
        _viewModel.authenticationState.observe(this) { state ->
            when (state) {
                AuthenticationState.AUTHENTICATED -> {
                    navController.navigate(R.id.reminderListFragment)
                }
                AuthenticationState.UNAUTHENTICATED -> {
                    navController.navigate(R.id.loginFragment)
                }
                else -> {
                    println("else authenticated")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            enableMyLocation()
        } else {
            permissionDenied = true
        }
    }
}
