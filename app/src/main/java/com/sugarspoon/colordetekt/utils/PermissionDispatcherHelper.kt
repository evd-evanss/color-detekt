package com.sugarspoon.colordetekt.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionDispatcherHelper {
    private var permissions: Array<String>? = null
    private var activity: Activity? = null
    private var fragment: androidx.fragment.app.Fragment? = null
    private val requestCode: Int
    private var onPermissionResult: OnPermissionResult? = null

    constructor(
        activity: Activity,
        requestCode: Int,
        permissions: Array<String>,
        onPermissionResult: OnPermissionResult
    ) {
        this.permissions = permissions
        this.activity = activity
        this.requestCode = requestCode
        this.onPermissionResult = onPermissionResult
    }

    constructor(
        fragment: androidx.fragment.app.Fragment,
        requestCode: Int,
        permissions: Array<String>,
        onPermissionResult: OnPermissionResult
    ) {
        this.permissions = permissions
        this.fragment = fragment
        activity = fragment.activity
        this.requestCode = requestCode
        this.onPermissionResult = onPermissionResult
    }

    fun dispatchPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            areAllPermissionsGranted()?.run {
                if (this) {
                    onPermissionResult?.onAllPermissionsGranted(requestCode)
                } else {
                    requestPermissions(permissions, requestCode)
                }
            }
        } else {
            //permissions before SDK M (23) are granted when user installs the app
            onPermissionResult?.onAllPermissionsGranted(requestCode)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissions(permissions: Array<String>?, requestCode: Int) {
        if (isFromFragment()) {
            fragment?.requestPermissions(permissions!!, requestCode)
        } else {
            activity!!.requestPermissions(permissions!!, requestCode)
        }
    }

    private fun isFromFragment() = fragment != null

    private fun checkPermissions(permission: String) =
        ContextCompat.checkSelfPermission(activity!!, permission)

    private fun shouldShowRequestPermissionRationale(permission: String) = if (isFromFragment()) {
        fragment!!.shouldShowRequestPermissionRationale(permission)
    } else {
        ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)
    }

    private fun areAllPermissionsGranted() = permissions?.none {
        checkPermissions(it) != PackageManager.PERMISSION_GRANTED
    }

    private fun getDeniedPermissions(
        permissions: Array<String>,
        grantResults: IntArray
    ): List<String> {
        val deniedPermissions = mutableListOf<String>()
        grantResults.forEachIndexed { index, result ->
            if (result != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[index])
            }
        }
        return deniedPermissions
    }

    private fun getPermissionsWithNeverAskAgainOption(permissions: Array<String>): List<String> {
        val permissionsWithNeverAskAgainOption = mutableListOf<String>()
        permissions.forEach {
            if (!shouldShowRequestPermissionRationale(it)) {
                permissionsWithNeverAskAgainOption.add(it)
            }
        }
        return permissionsWithNeverAskAgainOption
    }

    fun onRequestPermissionsResult(
        permsRequestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (permsRequestCode == requestCode) {

            val deniedPermissions = getDeniedPermissions(permissions, grantResults)

            if (deniedPermissions.isEmpty()) {
                onPermissionResult?.onAllPermissionsGranted(requestCode)
            } else {
                val deniedPermissionsWithNeverAskAgain =
                    getPermissionsWithNeverAskAgainOption(deniedPermissions.toTypedArray())
                if (deniedPermissionsWithNeverAskAgain.isEmpty()) {
                    onPermissionResult?.onPermissionsDenied(
                        requestCode,
                        deniedPermissions,
                        listOf()
                    )
                } else {
                    onPermissionResult?.onPermissionsDenied(
                        requestCode,
                        deniedPermissions,
                        deniedPermissionsWithNeverAskAgain
                    )
                }
            }
        }
    }


    interface OnPermissionResult {
        fun onAllPermissionsGranted(requestCode: Int)

        fun onPermissionsDenied(
            requestCode: Int,
            deniedPermissions: List<String>,
            deniedPermissionsWithNeverAskAgainOption: List<String>
        )
    }
}