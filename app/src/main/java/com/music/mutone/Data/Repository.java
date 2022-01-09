package com.music.mutone.Data;

import android.content.Context;

import java.util.ArrayList;

// This Class handle all data transfer operations

public class Repository {

    static Repository sInstance;

    MediaLoader mediaLoader;
    Preferences preferences;
    //Preferences
    private Integer index, position, duration;
    private Boolean isVary, isContinue, isRepeating;
    // MediaFiles
    private ArrayList<MediaFile> mediaFiles;
    private MediaFile currentMediaFile;

    public Repository(Context context) {
        mediaLoader = new MediaLoader(context);
        preferences = new Preferences(context);
    }

    public static Repository getInstance(Context context) {
        if (sInstance == null)
            sInstance = new Repository(context);

        return sInstance;
    }

    private void checkIfMediaFileChanged() {
        // check If currentMediaFile has changed --> Compare last duration in repository with current mediaFile duration
        int durationFromRepository = getDuration();
        int durationFromCurrentMediaFile = getCurrentMediaFile().getDuration();
        if (durationFromRepository != durationFromCurrentMediaFile)
            setIndex(0);
        setPosition(0);
    }

    public MediaFile getCurrentMediaFile() {
        getMediaFiles();
        getIndex();
        if (mediaFiles != null && mediaFiles.size() > 0) {
            // Handle IndexOutOfBoundsException --> happens when mediaFiles are changed to be lower than index
            // for example :mediaFiles = 30 & index = 30 then the user delete last mediafile
            try {
                currentMediaFile = mediaFiles.get(index);
            } catch (IndexOutOfBoundsException exception) {
                setIndex(0);
                currentMediaFile = mediaFiles.get(index);
            }
        }

        return currentMediaFile;
    }

    public ArrayList<MediaFile> getMediaFiles() {
        if (mediaFiles == null) {
            mediaFiles = mediaLoader.queryMediaStoreForMediaFiles();
//            checkIfMediaFileChanged();
        }

        return mediaFiles;
    }

    public int getIndex() {
        if (index == null)
            index = preferences.getIndex();

        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        preferences.setIndex(index);
    }

    public int getPosition() {
        if (position == null)
            position = preferences.getPosition();

        return position;
    }

    public void setPosition(int position) {
        this.position = position;
        preferences.setPosition(position);
    }

    public int getDuration() {
        if (duration == null)
            position = preferences.getDuration();

        return position;
    }

    public void setDuration(int duration) {
        this.position = duration;
        preferences.setDuration(duration);
    }


    public boolean isVary() {
        if (isVary == null)
            isVary = preferences.isVary();

        return isVary;
    }

    public void setVary(boolean vary) {
        isVary = vary;
        preferences.setVary(vary);
    }

    public boolean isContinue() {
        if (isContinue == null)
            isContinue = preferences.isContinue();

        return isContinue;
    }

    public void setContinue(boolean aContinue) {
        isContinue = aContinue;
        preferences.setContinue(aContinue);
    }

    public boolean isRepeating() {
        if (isRepeating == null)
            isRepeating = preferences.isRepeating();

        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
        preferences.setRepeating(repeating);
    }
}
