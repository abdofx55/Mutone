package com.music.mutone;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.music.mutone.Data.MediaFile;
import com.music.mutone.Data.Repository;

import java.util.ArrayList;

public class MediaViewModel extends AndroidViewModel {

    private static final String TAG = "VIEW_MODEL_LOG_TAG";

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private boolean isStoragePermissionGranted;
    Repository repository;

    private ArrayList<MediaFile> mediaFiles;
    private MediaFile currentMediaFile;
    private int index, position, duration;

    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> album = new MutableLiveData<>();

    private boolean isVary, isContinue, isRepeating;

    Application application;


    public MediaViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        repository = Repository.getInstance(application);
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
        if (mediaFiles == null) {

            // Read data from data repository
            mediaFiles = repository.getMediaFiles();

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
        MediaFile mediaFile = repository.getCurrentMediaFile();
            if (mediaFile != null) {
                name.setValue(mediaFile.getName());
            }

        return name;
    }

    public MutableLiveData<String> getAlbum() {
        MediaFile mediaFile = repository.getCurrentMediaFile();
        if (mediaFile != null)
            album.setValue(mediaFile.getAlbum());

        return album;
    }

    public Integer getDuration() {
        MediaFile mediaFile = repository.getCurrentMediaFile();
        if (mediaFile != null) {
            duration = mediaFile.getDuration();
            Log.d(TAG, "Duration is : " + Tasks.formatMilliSecond(duration));
        }
        return duration;
    }

    public int getIndex() {
        index = repository.getIndex();
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        repository.setIndex(index);
    }

    public int getPosition() {
        position = repository.getPosition();

        // check if the mediafile has changed
        if (getDuration() != repository.getDuration())
            setPosition(0);

        return position;
    }

    public void setPosition(int position) {
        this.position = position;
        repository.setIndex(index);
    }

    public boolean isVary() {
        isVary = repository.isVary();
        return isVary;
    }

    public void setVary(boolean vary) {
        isVary = vary;
        repository.setVary(vary);

        if (vary) {
            setContinue(false);
            setRepeating(false);
        }
    }

    public boolean isContinue() {
        isContinue = repository.isContinue();
        return isContinue;
    }

    public void setContinue(boolean aContinue) {
        isContinue = aContinue;
        repository.setContinue(aContinue);

        if (aContinue) {
            setVary(true);
            setRepeating(true);
        }
    }

    public boolean isRepeating() {
        isRepeating = repository.isRepeating();
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
        repository.setRepeating(repeating);

        if (repeating) {
            setVary(false);
            setContinue(false);
        }
    }

}
