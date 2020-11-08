package com.sugarspoon.colordetekt.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences

    fun setPermissionsGranted(permissionsGranted: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_PERMISSIONS, permissionsGranted).apply()
    }

    val getPermissionsGranted: Boolean?
        get() = sharedPreferences.getBoolean(PREF_PERMISSIONS, false)

    companion object {
        private const val SHARED_PREFERENCES_NAME: String = ".SHARED_PREFERENCES"
        private const val PREF_PERMISSIONS: String = ".PREF_PERMISSIONS"

        private var isInstance: PreferencesHelper? = null

        @Synchronized
        fun initializeInstance(context: Context) {
            if (this.isInstance == null) {
                this.isInstance = PreferencesHelper(context)
            }
        }

        @get:Synchronized
        val instance: PreferencesHelper?
            get() {
                checkNotNull(this.isInstance) {
                    PreferencesHelper::class.java.simpleName +
                            " is not initialized, call initializeInstance(..) method first."
                }
                return this.isInstance
            }
    }

    init {
        sharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
}