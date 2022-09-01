package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.ErrorMessage
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false
    private var shouldReturnNoData = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    fun setReturnNoData(value: Boolean) {
        shouldReturnNoData = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError)
            return Result.Error(ErrorMessage)
        if (shouldReturnNoData)
            return Result.Success(listOf())
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return Result.Success(reminders.get(index = id.toInt()))
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}