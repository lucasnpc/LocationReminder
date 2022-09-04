package com.udacity.project4.locationreminders.data

import android.util.Log
import com.udacity.project4.ErrorMessage
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeRepository(private val reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error(ErrorMessage)
        }
        return Result.Success(
            listOf(
                ReminderDTO(
                    title = "title",
                    description = "description",
                    location = "location",
                    longitude = 0.0,
                    latitude = 0.0
                )
            )
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        Log.e("add", reminder.toString())
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return try {
            if (shouldReturnError)
                throw Exception(ErrorMessage)
            return Result.Success(reminders.find { it.id == id }!!)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}