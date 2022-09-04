package com.udacity.project4.locationreminders

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.login.AuthenticationViewModel
import com.udacity.project4.utils.AuthenticationState

class RemindersActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val binding: ActivityRemindersBinding by lazy {
        ActivityRemindersBinding.inflate(layoutInflater)
    }
    private val authViewModel: AuthenticationViewModel by viewModels()
    private val navController: NavController by lazy {
        binding.navHostFragment.findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        observeAuthenticationState()
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
}
