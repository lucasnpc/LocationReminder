package com.udacity.project4.login.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.udacity.project4.databinding.FragmentSignupBinding

class SignUpFragment : Fragment() {

    private val binding: FragmentSignupBinding by lazy {
        FragmentSignupBinding.inflate(layoutInflater)
    }
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        println(result.resultCode)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            email.setOnClickListener { openSignInTools() }
            google.setOnClickListener { openSignInTools(google = true) }
        }
    }

    private fun openSignInTools(google: Boolean = false) {
        signInLauncher.launch(AuthUI.getInstance().createSignInIntentBuilder().apply {
            if (google) setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
        }.build())
    }
}