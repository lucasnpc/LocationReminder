package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.domain.model.CustomLocation
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.LOCATION_SELECTED_KEY
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

@RequiresApi(Build.VERSION_CODES.M)
@SuppressLint("MissingPermission")
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

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }
}
