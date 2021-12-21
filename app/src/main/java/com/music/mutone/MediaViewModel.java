package com.music.mutone;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.music.mutone.Data.MediaFile;

import java.util.ArrayList;

public class MediaViewModel extends AndroidViewModel {

    private static final String TAG = "VIEW_MODEL_LOG_TAG";
    private final Player player;

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private boolean isStoragePermissionGranted;
    private ArrayList<MediaFile> mediaFiles;
    private int index;

    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> album = new MutableLiveData<>();
    private final MutableLiveData<Integer> position = new MutableLiveData<>();
    private final MutableLiveData<Integer> duration = new MutableLiveData<>();
    private boolean isVary, isContinue, isRepeating;

    Application application;


    public MediaViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        player = Player.getInstance(application);
    }


    // Getters
    //**********************************************************************************************

    public boolean isStoragePermissionGranted() {
        return isStoragePermissionGranted;
    }

    public void setStoragePermissionGranted(boolean storagePermissionGranted) {
        isStoragePermissionGranted = storagePermissionGranted;

        if (storagePermissionGranted)
            setErrorMessage("");
        else
            setErrorMessage(application.getString(R.string.permission_required));
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        errorMessage.setValue(message);
    }

    public ArrayList<MediaFile> getMediaFiles() {
        if (mediaFiles == null && player != null) {
            mediaFiles = new ArrayList<>();
            // Read data from data repository
            mediaFiles = player.getMediaFiles();

            // Bail early if the arraylist is null or there is less than 1 row in the arraylist
            if (mediaFiles == null) {
                setErrorMessage(application.getString(R.string.failed_to_load));
                Toast.makeText(application, application.getString(R.string.failed_to_load), Toast.LENGTH_SHORT).show();

            } else if (mediaFiles.size() == 0) {
                setErrorMessage(application.getString(R.string.no_media_found));
                Toast.makeText(application, application.getString(R.string.no_media_found), Toast.LENGTH_SHORT).show();

            } else {
                setErrorMessage("");
            }
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

    public int getIndex() {
        if (player != null)
            index = player.getIndex();

        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        player.setIndex(index);
    }

    public boolean isVary() {
        if (player != null)
            isVary = player.isVary();
        return isVary;
    }

    public void setVary(boolean vary) {
        isVary = vary;
        if (player != null)
            player.setVary(vary);

        if (vary) {
            setContinue(false);
            setRepeating(false);
        }
    }

    public boolean isContinue() {
        if (player != null)
            isContinue = player.isContinue();
        return isContinue;
    }

    public void setContinue(boolean aContinue) {
        isContinue = aContinue;
        if (player != null)
            player.setContinue(aContinue);

        if (aContinue) {
            setVary(true);
            setRepeating(true);
        }
    }

    public boolean isRepeating() {
        if (player != null)
            isRepeating = player.isRepeating();
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
        if (player != null)
            player.setRepeating(repeating);

        if (repeating) {
            setVary(false);
            setContinue(false);
        }
    }

}
