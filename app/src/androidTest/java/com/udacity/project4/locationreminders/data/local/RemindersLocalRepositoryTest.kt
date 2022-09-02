package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
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
    private val reminder = ReminderDTO(
        title = "title",
        description = "description",
        location = "location",
        latitude = 0.0,
        longitude = 0.0
    )

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        localRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_getReminders() = runBlocking {
        localRepository.saveReminder(
            reminder
        )

        val result: Result<ReminderDTO> = localRepository.getReminder(reminder.id)

        assertThat(result, `is`(Result.Success(reminder)))
        result as Result.Success
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.location, `is`(reminder.location))
    }

    @Test
    fun getReminderThatDoesntExist() = runBlocking {
        val result = localRepository.getReminder("123321")

        assertThat(result, `is`(Result.Error("Reminder not found!")))
    }

}