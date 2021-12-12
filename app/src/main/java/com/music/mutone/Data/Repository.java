package com.music.mutone.Data;

import android.content.Context;

import java.util.ArrayList;

// This Class handle all data transfer operations

public class Repository {

    MediaLoader mediaLoader;
    Preferences preferences;
    //Preferences
    private int index;
    private boolean isVary, isContinue, isRepeating;
    // MediaFiles
    private ArrayList<MediaFile> mediaFiles;

    public Repository(Context context) {
        mediaLoader = new MediaLoader(context);
        preferences = new Preferences(context);
    }

    public int getIndex() {
        index = preferences.getIndex();
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        preferences.setIndex(index);
    }

    public boolean isVary() {
        isVary = preferences.getVary();
        return isVary;
    }

    public void setVary(boolean vary) {
        isVary = vary;
        preferences.setVary(vary);
    }

    public boolean isContinue() {
        isContinue = preferences.getContinue();
        return isContinue;
    }

    public void setContinue(boolean aContinue) {
        isContinue = aContinue;
        preferences.setContinue(aContinue);
    }

    public boolean isRepeating() {
        isContinue = preferences.getRepeating();
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
        preferences.setRepeating(repeating);
    }

    public ArrayList<MediaFile> getMediaFiles() {
        mediaFiles = mediaLoader.queryMediaStoreForMediaFiles();
        return mediaFiles;
    }

}
