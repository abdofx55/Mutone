package com.music.mutone.Data;

import android.net.Uri;

public class MediaFile {
    private String name;
    private String album;
    private int duration;
    private Uri uri;

    public MediaFile() {
    }

    public MediaFile(String name, String album, int duration, Uri uri) {
        this.name = name;
        this.album = album;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
