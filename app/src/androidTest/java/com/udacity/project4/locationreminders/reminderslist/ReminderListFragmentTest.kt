package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.di.AppModule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {

    private lateinit var repository: ReminderDataSource

    @Before
    fun initRepository() {
        repository = FakeDataSource()
        AppModule.reminderRepository = repository
    }

    @Test
    fun testingNoDataState() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {

        }

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun testingReminderDateState() = runBlockingTest {
        ('A'..'Z').forEach {
            repository.saveReminder(
                ReminderDTO(
                    title = it.toString(),
                    description = it.toString(),
                    location = it.toString(),
                    longitude = 0.0,
                    latitude = 0.0
                )
            )
        }

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        ('A'..'Z').forEach {
            onView(withId(R.id.reminderssRecyclerView)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(it.toString())), click()
                )
            )
        }
    }

    @Test
    fun testingNavigationToSaveReminder() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
//    TODO: add testing for the error messages.
}