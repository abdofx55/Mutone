package com.music.mutone.Data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

public class MediaLoader {
    ContentResolver resolver;

    public MediaLoader(Context context) {
        resolver = context.getContentResolver();
    }

    public ArrayList<MediaFile> queryMediaStoreForMediaFiles() {
        // Do an asynchronous operation to fetch media files.
        String[] projection = new String[]{
                "_display_name",        // Name
                "album",                // Album
                "duration",             // Duration
                "_data"                 // Uri
        };


        Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        ArrayList<MediaFile> mediaFiles = convertCursorToList(cursor);
        // Close cursor
        cursor.close();

        return mediaFiles;
    }

    public ArrayList<MediaFile> convertCursorToList(Cursor cursor) {
        ArrayList<MediaFile> mediaFiles = new ArrayList<>();
        if (cursor != null) {
            // Cache column indices.
            int nameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int uriColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);


            while (cursor.moveToNext()) {
                // copy cursor to mediaFiles arraylist
                String name = cursor.getString(nameColumn);
                String album = cursor.getString(albumColumn);
                int duration = cursor.getInt(durationColumn);
                Uri uri = Uri.parse(cursor.getString(uriColumn));

                MediaFile mediaFile = new MediaFile(name, album, duration, uri);
                mediaFiles.add(mediaFile);
            }
        }
        return mediaFiles;
    }

}
