package com.life4.imagetotext.data

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MyPreference @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val inputType = "KEY_INPUT_TYPE"

    fun setInputType(inputType: String?) {
        prefs.edit().putString(this.inputType, inputType).apply()
    }

    fun getInputType(): String? = prefs.getString(inputType, null)
}