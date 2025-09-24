package com.antago30.a7dtd_lukomorie.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class NotificationPermissionManager(
    private val context: Context,
    private val requestPermissionLauncher: ActivityResultLauncher<String>
) {

    companion object {
        const val PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    }

    fun requestPermissionIfNeeded(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(context, PERMISSION)
            if (permission == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                requestPermissionLauncher.launch(PERMISSION)
            }
        } else {
            // На Android < 13 разрешение не требуется
            onPermissionGranted()
        }
    }

    fun handlePermissionResult(
        isGranted: Boolean,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (isGranted) {
            onGranted()
        } else {
            onDenied()
        }
    }
}