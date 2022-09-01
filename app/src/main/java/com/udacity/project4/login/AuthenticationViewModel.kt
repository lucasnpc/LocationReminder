package com.udacity.project4.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.utils.AuthenticationState
import com.udacity.project4.utils.FirebaseUserLiveData

class AuthenticationViewModel : ViewModel() {
    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}