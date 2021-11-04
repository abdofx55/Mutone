package com.music.mutone.pojo;

public class MediaFile {
    private String name;
    private String album;
    private long duration;
    private String uri;

    public MediaFile() {
    }

    public MediaFile(String name, String album, long duration, String uri) {
        this.name = name;
        this.album = album;
        this.duration = duration;
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

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
