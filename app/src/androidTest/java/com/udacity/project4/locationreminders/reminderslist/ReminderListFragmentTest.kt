package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.ErrorMessage
import com.udacity.project4.R
import com.udacity.project4.di.AppModule
import com.udacity.project4.locationreminders.data.FakeRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {

    private lateinit var repository: FakeRepository
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun initRepository() {
        repository = FakeRepository()
        AppModule.reminderRepository = repository
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun resetRepository() {
        AppModule.resetRepository()
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun assertReminderIsOnListByClick_assertErrorSnackbarAppears(): Unit = runBlocking {
        wrapEspressoIdlingResource {
            repository.saveReminder(
                ReminderDTO(
                    title = "title 2",
                    description = "description 2",
                    location = "location 2",
                    latitude = 0.0,
                    longitude = 0.0
                )
            )
            val navController = Mockito.mock(NavController::class.java)
            val scenario =
                launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

            scenario.onFragment {
                Navigation.setViewNavController(it.requireView(), navController)
                dataBindingIdlingResource.monitorFragment(it)
            }

            onView(withId(R.id.reminderssRecyclerView)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("title 2")), click()
                )
            )

            repository.setReturnError(true)

            scenario.recreate()

            scenario.onFragment {
                dataBindingIdlingResource.monitorFragment(it)
            }

            onView(withId(R.id.snackbar_text)).check(matches(withText(ErrorMessage)))
        }
    }

    @Test
    fun testingNavigationToSaveReminder() = wrapEspressoIdlingResource {
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            dataBindingIdlingResource.monitorFragment(it)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}