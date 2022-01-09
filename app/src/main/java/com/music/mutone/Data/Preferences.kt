package com.music.mutone.Data

import android.content.Context
import android.content.SharedPreferences

class Preferences(context: Context) {

    companion object {
        private const val PLAYER_PREFERENCES = "PlayerPreferences"
        private const val INDEX = "index"
        private const val POSITION = "position"
        private const val DURATION = "duration"
        private const val IS_VARY = "isVary"
        private const val IS_REPEATING = "isRepeating"
        private const val IS_CONTINUE = "isContinue"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PLAYER_PREFERENCES, Context.MODE_PRIVATE)


    // Setters & getters
    var index = 0
        get() {
            field = sharedPreferences.getInt(INDEX, 0)
            return field
        }
        set(index) {
            field = index
            val editSharedPref = sharedPreferences.edit()
            editSharedPref.putInt(INDEX, index)
            editSharedPref.apply()
        }

    var position = 0
        get() {
            field = sharedPreferences.getInt(POSITION, 0)
            return field
        }
        set(position) {
            field = position
            val editSharedPref = sharedPreferences.edit()
            editSharedPref.putInt(POSITION, position)
            editSharedPref.apply()
        }

    var duration = 0
        get() {
            position = sharedPreferences.getInt(POSITION, 0)
            return position
        }
        set(duration) {
            field = duration
            val editSharedPref = sharedPreferences.edit()
            editSharedPref.putInt(DURATION, duration)
            editSharedPref.apply()
        }

    var isVary = false
        get() {
            field = sharedPreferences.getBoolean(IS_VARY, false)
            return field
        }
        set(vary) {
            field = vary
            val editSharedPref = sharedPreferences.edit()
            editSharedPref.putBoolean(IS_VARY, vary)
            editSharedPref.apply()
        }

    var isContinue = false
        get() {
            field = sharedPreferences.getBoolean(IS_CONTINUE, false)
            return field
        }
        set(aContinue) {
            field = aContinue
            val editSharedPref = sharedPreferences.edit()
            editSharedPref.putBoolean(IS_CONTINUE, aContinue)
            editSharedPref.apply()
        }

    var isRepeating = false
        get() {
            field = sharedPreferences.getBoolean(IS_REPEATING, false)
            return field
        }
        set(repeating) {
            field = repeating
            val editSharedPref = sharedPreferences.edit()
            editSharedPref.putBoolean(IS_REPEATING, repeating)
            editSharedPref.apply()
        }
}