package com.udacity.project4.locationreminders.domain.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomLocation(val name: String = "", val position: LatLng) : Parcelable
