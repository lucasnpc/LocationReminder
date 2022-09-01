package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.udacity.project4.locationreminders.ErrorMessage
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var viewModel: RemindersListViewModel

    private lateinit var repository: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() = runBlocking {
        repository = FakeDataSource()
        viewModel = RemindersListViewModel(repository)
    }

    @Test
    fun `Should return error`() {
        repository.setReturnError(true)

        viewModel.loadReminders()

        assertThat(viewModel.showErrorMessage.getOrAwaitValue(), `is`(ErrorMessage))
    }

    @Test
    fun `Should return no data`() {
        repository.setReturnNoData(true)

        viewModel.loadReminders()

        assertThat(viewModel.remindersList.getOrAwaitValue(), `is`(emptyList()))
    }

    @Test
    fun `Testing show loading liveData when loading reminders`() {
        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}