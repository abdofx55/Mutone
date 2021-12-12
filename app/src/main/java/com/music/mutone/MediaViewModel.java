package com.music.mutone;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.music.mutone.Data.MediaFile;

import java.util.ArrayList;

public class MediaViewModel extends AndroidViewModel {

    private static final String TAG = "VIEW_MODEL_LOG_TAG";
    public final MutableLiveData<String> storagePermissionState = new MutableLiveData<>();
    private final Player player;

    private final MutableLiveData<Boolean> isStoragePermissionGranted = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<MediaFile>> mediaFiles = new MutableLiveData<>();
    private final MutableLiveData<Integer> index = new MutableLiveData<>();
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> album = new MutableLiveData<>();
    private final MutableLiveData<Integer> position = new MutableLiveData<>();
    private final MutableLiveData<Integer> duration = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isVary = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isContinue = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRepeating = new MutableLiveData<>();
    Application application;


    public MediaViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        player = Player.getInstance(application);
    }

    public void readDataFromRepository() {
        getMediaFiles();
        getIndex();
        getName();
        getAlbum();
        getPosition();
        getDuration();
        getIsVary();
        getIsRepeating();
        getIsContinue();
    }


    // Getters
    //**********************************************************************************************

    public MutableLiveData<Boolean> isStoragePermissionGranted() {
        return isStoragePermissionGranted;
    }

    public void setStoragePermissionGranted(boolean storagePermissionGranted) {
        isStoragePermissionGranted.setValue(storagePermissionGranted);

        if (storagePermissionGranted)
            storagePermissionState.setValue("");
        else
            storagePermissionState.setValue(application.getString(R.string.empty_due_permission));
    }


    public MutableLiveData<ArrayList<MediaFile>> getMediaFiles() {
        if (mediaFiles.getValue() == null && player != null) {
            // Read data from data repository
            mediaFiles.setValue(player.getMediaFiles());
        }
        return mediaFiles;
    }


    public MutableLiveData<String> getName() {
        if (player != null) {
            MediaFile mediaFile = player.getCurrentMediaFile();
            if (mediaFile != null) {
                name.setValue(mediaFile.getName());
            }
        }
        return name;
    }

    public MutableLiveData<String> getAlbum() {
        if (player != null) {
            MediaFile mediaFile = player.getCurrentMediaFile();

            if (mediaFile != null)
                album.setValue(mediaFile.getAlbum());
        }
        return album;
    }


    public MutableLiveData<Integer> getPosition() {
        if (player != null)
            position.setValue(player.getPosition());

        return position;
    }

    public MutableLiveData<Integer> getDuration() {
        if (player != null)
            duration.setValue(player.getDurationTime());

        return duration;
    }

    public MutableLiveData<Integer> getIndex() {
        if (player != null)
            index.setValue(player.getIndex());

        return index;
    }

    public MutableLiveData<Boolean> getIsVary() {
        isVary.setValue(player.isVary());

        return isVary;
    }

    public MutableLiveData<Boolean> getIsContinue() {
        isContinue.setValue(player.isContinue());

        return isContinue;
    }

    public MutableLiveData<Boolean> getIsRepeating() {
        isRepeating.setValue(player.isRepeating());

        return isRepeating;
    }

}
