package com.udacity.project4.locationreminders

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.utils.AuthenticationState
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemindersActivity : AppCompatActivity() {

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
}
