package com.pipo2tr.cricskore.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.pipo2tr.cricskore.R

fun truncateText(text: String, maxLength: Int): String {
    if (text.length - 1 < maxLength) {
        return text
    }
    return text.substring(0, maxLength) + "..."
}

fun getAppPref(context: Context): SharedPreferences {
    return context.getSharedPreferences(
        context.getString(R.string.app_name),
        Context.MODE_PRIVATE
    )
}
