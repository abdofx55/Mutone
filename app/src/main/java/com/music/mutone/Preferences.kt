package com.music.mutone

import android.content.Context

class Preferences {
    private val PREFERENCES = "PlayerPreferences"

    // Update Shared Preferences
    fun update(context: Context) {
        val player = Player.getInstance(context)
        val sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val editSharedPref = sharedPreferences.edit()
        editSharedPref.putInt("songIndex", player.index)
        editSharedPref.putBoolean("isRepeating", player.isRepeating)
        editSharedPref.putBoolean("isContinue", player.isContinue)
        editSharedPref.putBoolean("isVary", player.isVary)
        editSharedPref.apply()

    }

    // Read from Shared Preferences
    fun read(context: Context) {
        val player = Player.getInstance(context)
        val sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val index = sharedPreferences.getInt("songIndex", 0)
        player.index = index
        val isVary = sharedPreferences.getBoolean("isVary", false)
        player.isVary = isVary
        val isRepeating = sharedPreferences.getBoolean("isRepeating", false)
        player.isRepeating = isRepeating
        val isContinue = sharedPreferences.getBoolean("isContinue", false)
        player.isContinue = isContinue

    }
}