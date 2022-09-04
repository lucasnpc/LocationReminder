package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        localRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
        runBlocking {
            localRepository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun saveReminder_getSavedReminder() = runBlocking {
        val reminder = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 0.0,
            longitude = 0.0
        )
        localRepository.saveReminder(
            reminder
        )

        val result = localRepository.getReminder(reminder.id)

        assertThat(result, `is`(Result.Success(reminder)))
        result as Result.Success
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.location, `is`(reminder.location))
    }

    @Test
    fun removeReminder_assertThatDoesntExistAnymore() = runBlocking {
        val reminder = ReminderDTO(
            title = "title 2",
            description = "description 2",
            location = "location 2",
            latitude = 0.0,
            longitude = 0.0
        )
        localRepository.saveReminder(reminder)

        var result = localRepository.getReminder(reminder.id)

        assertThat(result, `is`(Result.Success(reminder)))

        localRepository.deleteAllReminders()

        result = localRepository.getReminder(reminder.id)

        assertThat(result, `is`(Result.Error("Reminder not found!")))
    }

    @Test
    fun getReminderThatDoesntExist() = runBlocking {
        val result = localRepository.getReminder("123321")

        assertThat(result, `is`(Result.Error("Reminder not found!")))
    }

}