package com.udacity.project4.utils

import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.foregroundAndBackgroundLocationPermissionApproved(): Boolean {
    val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        this,
                        permission.ACCESS_FINE_LOCATION
                    ))
    val backgroundPermissionApproved =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        this, permission.ACCESS_BACKGROUND_LOCATION
                    )
        else
            true

    return foregroundLocationApproved && backgroundPermissionApproved
}
