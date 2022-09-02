package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuProvider
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private val binding: FragmentRemindersBinding by lazy {
        FragmentRemindersBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.run {
            root.visibility = View.VISIBLE
            viewModel = _viewModel
            lifecycleOwner = this@ReminderListFragment
            refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
            setupRecyclerView()
            addReminderFAB.setOnClickListener {
                navigateToAddReminder()
            }
        }
        requireActivity().addMenuProvider(reminderListMenu, viewLifecycleOwner)
        _viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.refreshLayout.isRefreshing = it
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
    }


    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    private val reminderListMenu = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.main_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.logout -> {
                    AuthUI.getInstance().signOut(requireContext())
                }
            }
            return true
        }
    }
}
