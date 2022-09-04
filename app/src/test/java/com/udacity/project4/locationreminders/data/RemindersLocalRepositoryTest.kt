package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

class RemindersLocalRepositoryTest {

    private lateinit var repository: FakeDataSource

    private val reminders = mutableListOf<ReminderDTO>()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() = mainCoroutineRule.runBlockingTest {
        repository = FakeDataSource()
        ('A'..'D').forEach { letter ->
            reminders.add(
                ReminderDTO(
                    title = "title $letter",
                    description = "desc $letter",
                    location = "$letter location",
                    latitude = Random.nextDouble(20.00000, 110.00000),
                    longitude = Random.nextDouble(40.00000, 90.00000)
                )
            )
        }
        reminders.map { reminder ->
            repository.saveReminder(
                reminder
            )
        }
    }

    @After
    fun cleanUp() = mainCoroutineRule.runBlockingTest {
        repository.deleteAllReminders()
    }

    @Test
    fun `Should return reminders list from data Source`() = mainCoroutineRule.runBlockingTest {
        val _reminders = repository.getReminders() as Result.Success

        assertThat(_reminders.data, IsEqual(reminders))
    }

    @Test
    fun `Should return the reminder from Id`() = mainCoroutineRule.runBlockingTest {
        val reminder = repository.getReminder(reminders[0].id) as Result.Success

        assertThat(reminder.data, IsEqual(reminders[0]))
    }

    @Test
    fun `Should return error when dont find a reminder from Id`() =
        mainCoroutineRule.runBlockingTest {
            val reminder = repository.getReminder("123")

            assertThat(reminder, `is`(Result.Error(null)))
        }

    @Test
    fun `Should return empty List when all reminders are clear`() =
        mainCoroutineRule.runBlockingTest {
            repository.deleteAllReminders()
            val _reminders = repository.getReminders() as Result.Success
            assertThat(_reminders.data, IsEqual(emptyList()))
        }
}