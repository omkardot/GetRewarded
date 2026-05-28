package com.varram.taskquest.Helpers
import android.content.Context
import android.content.SharedPreferences

class RewaredSharedPref(context: Context) {

    private val PREF_NAME = "MyAppPrefs"
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Save data
    fun saveString(key: String, value: String) {
        sharedPref.edit().putString(key, value).apply()
    }

    fun saveInt(key: String, value: Int) {
        sharedPref.edit().putInt(key, value).apply()
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPref.edit().putBoolean(key, value).apply()
    }

    // Get data
    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPref.getString(key, defaultValue) ?: defaultValue
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPref.getInt(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPref.getBoolean(key, defaultValue)
    }

    // Clear all
    fun clear() {
        sharedPref.edit().clear().apply()
    }

    // Remove specific key
    fun remove(key: String) {
        sharedPref.edit().remove(key).apply()
    }
}
