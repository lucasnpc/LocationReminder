package com.udacity.project4.utils

import java.util.concurrent.TimeUnit

const val LOCATION_SELECTED_KEY = "location_selected_key"
const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
const val GEOFENCE_RADIUS_IN_METERS = 30f
val GEOFENCE_EXPIRATION = TimeUnit.HOURS.toMillis(1)