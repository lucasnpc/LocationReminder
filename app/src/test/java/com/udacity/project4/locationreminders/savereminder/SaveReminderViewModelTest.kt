package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random


@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    private lateinit var viewModel: SaveReminderViewModel

    private lateinit var repository: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        repository = FakeDataSource()
        viewModel = SaveReminderViewModel(repository)
    }

    @Test
    fun `Should save reminder`() {
        val reminder = ReminderDataItem(
            title = "title",
            description = "desc",
            location = "location",
            latitude = Random.nextDouble(20.00000, 110.00000),
            longitude = Random.nextDouble(40.00000, 90.00000)
        )

        viewModel.validateAndSaveReminder(reminder)

        assertThat(viewModel.showToast.getOrAwaitValue(), `is`(R.string.reminder_saved))
    }

    @Test
    fun `Should present missing title error`() {
        val reminder = ReminderDataItem(
            title = null,
            description = "desc",
            location = "location",
            latitude = Random.nextDouble(20.00000, 110.00000),
            longitude = Random.nextDouble(40.00000, 90.00000)
        )

        viewModel.validateAndSaveReminder(reminder)

        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun `Shoud present missing location error`() {
        val reminder = ReminderDataItem(
            title = "title",
            description = "desc",
            location = null,
            latitude = Random.nextDouble(20.00000, 110.00000),
            longitude = Random.nextDouble(40.00000, 90.00000)
        )

        viewModel.validateAndSaveReminder(reminder)

        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

}