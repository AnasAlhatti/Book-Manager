package com.example.bookManager.core.util

import android.content.Context
import android.content.SharedPreferences

class FilterPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("filter_prefs", Context.MODE_PRIVATE)

    fun saveFilter(type: String, value: String?) {
        prefs.edit()
            .putString("filter_type", type)
            .putString("filter_value", value ?: "")
            .apply()
    }

    fun getFilter(): Pair<String?, String?> {
        val type = prefs.getString("filter_type", null)
        val value = prefs.getString("filter_value", null)
        return Pair(type, value)
    }

    fun clearFilter() {
        prefs.edit().clear().apply()
    }
}