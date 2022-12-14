package com.udacity.project4.di

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppModule {
    private val lock = Any()

    @Volatile
    var reminderRepository: ReminderDataSource? = null
        @VisibleForTesting set

    fun getModules(context: Context) = module {
        //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
        viewModel {
            RemindersListViewModel(
                get() as ReminderDataSource
            )
        }
        //Declare singleton definitions to be later injected using by inject()
        single {
            //This view model is declared singleton to be used across multiple fragments
            SaveReminderViewModel(
                get() as ReminderDataSource
            )
        }
        single {
            reminderRepository
                ?: RemindersLocalRepository(get()) as ReminderDataSource
        }
        single { LocalDB.createRemindersDao(context) }
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            reminderRepository = null
        }
    }
}