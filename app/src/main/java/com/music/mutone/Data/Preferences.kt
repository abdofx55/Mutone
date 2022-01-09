package com.music.mutone.Data;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String PLAYER_PREFERENCES = "PlayerPreferences";
    private static final String INDEX = "Index";
    private static final String IS_VARY = "isVary";
    private static final String IS_REPEATING = "isRepeating";
    private static final String IS_CONTINUE = "isContinue";
    SharedPreferences sharedPreferences;
    private Integer index;
    private Boolean isVary;
    private Boolean isContinue;
    private Boolean isRepeating;


    public Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PLAYER_PREFERENCES, Context.MODE_PRIVATE);
    }

    // Setters & getters

    public Integer getIndex() {
        index = sharedPreferences.getInt(INDEX, 0);
        return index;
    }

    public void setIndex(Integer index) {
        SharedPreferences.Editor editSharedPref = sharedPreferences.edit();
        editSharedPref.putInt(INDEX, index);
        editSharedPref.apply();
        this.index = index;
    }

    public Boolean getVary() {
        isVary = sharedPreferences.getBoolean(IS_VARY, false);
        return isVary;
    }

    public void setVary(Boolean vary) {
        SharedPreferences.Editor editSharedPref = sharedPreferences.edit();
        editSharedPref.putBoolean(IS_VARY, vary);
        editSharedPref.apply();
        isVary = vary;
    }

    public Boolean getContinue() {
        isContinue = sharedPreferences.getBoolean(IS_CONTINUE, false);
        return isContinue;
    }

    public void setContinue(Boolean aContinue) {
        SharedPreferences.Editor editSharedPref = sharedPreferences.edit();
        editSharedPref.putBoolean(IS_CONTINUE, aContinue);
        editSharedPref.apply();
        isContinue = aContinue;
    }

    public Boolean getRepeating() {
        isRepeating = sharedPreferences.getBoolean(IS_REPEATING, false);
        return isRepeating;
    }

    public void setRepeating(Boolean repeating) {
        SharedPreferences.Editor editSharedPref = sharedPreferences.edit();
        editSharedPref.putBoolean(IS_REPEATING, repeating);
        editSharedPref.apply();
        isRepeating = repeating;
    }
}
