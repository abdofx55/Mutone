package com.music.mutone;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.music.mutone.pojo.MediaFile;

import java.util.ArrayList;

public class MediaViewModel extends ViewModel {
    private MutableLiveData<ArrayList<MediaFile>> mediaFiles;
    private boolean isStoragePermissionGranted;

    public LiveData<ArrayList<MediaFile>> getMediaFiles(Context context) {
        if (mediaFiles == null) {
            mediaFiles = new MutableLiveData<>();
            loadMediaFiles(context);
        }
        return mediaFiles;
    }

    private void loadMediaFiles(Context context) {
        queryMediaStoreForMediaFiles(context);
    }

    private void queryMediaStoreForMediaFiles(Context context) {
        // Do an asynchronous operation to fetch media files.
        String[] projection = new String[] {
                "_id",
                "_data",
                "_display_name",
                "album",
                "duration"};


        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        copyCursorToLiveList(cursor , mediaFiles);
        // Close cursor
        cursor.close();
    }

    private void copyCursorToLiveList(Cursor cursor, MutableLiveData<ArrayList<MediaFile>> mediaFiles) {
        if (cursor != null) {
            ArrayList<MediaFile> mediaFilesArrayList = new ArrayList<>();
            // Cache column indices.
            int nameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int uriColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);


            while (cursor.moveToNext()) {
                // copy cursor to mediaFiles arraylist
                String name = cursor.getString(nameColumn);
                String album = cursor.getString(albumColumn);
                long duration = cursor.getLong(durationColumn);
                String uri = cursor.getString(uriColumn);

                MediaFile mediaFile = new MediaFile(name, album, duration, uri);
                mediaFilesArrayList.add(mediaFile);
            }
            // make it live data
            mediaFiles.setValue(mediaFilesArrayList);
        }
    }

    public boolean isStoragePermissionGranted() {
        return isStoragePermissionGranted;
    }

    public void setStoragePermissionGranted(boolean storagePermissionGranted) {
        isStoragePermissionGranted = storagePermissionGranted;
    }

}
