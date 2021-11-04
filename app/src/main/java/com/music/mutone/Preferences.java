package com.music.mutone;

import android.content.Context;
import android.content.SharedPreferences;


public class Preferences {
    private static final String PLAYER_PREFERENCES = "PlayerPreferences";

    // Update Shared Preferences
    public static void update(Context context) {
        Player player = Player.getInstance(context);

        if (player != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PLAYER_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editSharedPref = sharedPreferences.edit();

            editSharedPref.putInt("songIndex", player.getIndex());
            editSharedPref.putBoolean("isRepeating", player.isRepeating());
            editSharedPref.putBoolean("isContinue", player.isContinue());
            editSharedPref.putBoolean("isVary", player.isVary());
            editSharedPref.apply();
        }
    }

    // Read from Shared Preferences
    public static void read(Context context) {
        Player player = Player.getInstance(context);

        if (player != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PLAYER_PREFERENCES, Context.MODE_PRIVATE);
            int index = sharedPreferences.getInt("songIndex", 0);
            player.setIndex(index);

            boolean isVary = sharedPreferences.getBoolean("isVary", false);
            player.setVary(isVary);

            boolean isRepeating = sharedPreferences.getBoolean("isRepeating", false);
            player.setRepeating(isRepeating);

            boolean isContinue = sharedPreferences.getBoolean("isContinue", false);
            player.setContinue(isContinue);
        }
    }
}
