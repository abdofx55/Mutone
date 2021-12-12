package com.music.mutone;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.music.mutone.Data.MediaFile;
import com.music.mutone.Data.Repository;

import java.util.ArrayList;

public class Player extends MediaPlayer {

    private static final String TAG = "PLAYER_LOG_TAG";

    //Singleton instance
    private static Player sInstance;

    private MediaPlayer player;
    AudioManager audioManager;
    // Data Repository
    Repository repository;
    private boolean isAudioFocusGranted;
    private AudioFocusChangeListener audioFocusChangeListener;
    // MediaFiles
    private ArrayList<MediaFile> mediaFiles;
    private MediaFile currentMediaFile;

    // UI related variables
    private String name, album;
    private int index, position, duration;
    private boolean isVary, isContinue, isRepeating;

    private Player(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioFocusChangeListener = new AudioFocusChangeListener();
        repository = new Repository(context);
    }

    public static Player getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Player(context);
        }
        return sInstance;
    }

    public void updateCurrentMediaData() {
        getCurrentMediaFile();
        getName();
        getAlbum();
        getPosition();
        getDurationTime();
    }

    public void initialize(Context context) {
        // release mediaPlayer
        release();

        // Update currentMediaFile data
        updateCurrentMediaData();

        // initialize mediaPlayer
        player = Player.create(context, currentMediaFile.getUri());

        // player != null to stop crashing
        if (player != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            player.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
        }
    }

    public void play() {
        if (player != null) {
            if (!player.isPlaying() && isAudioFocusGranted) {
                player.start();

            } else
                // request audio focus
                requestAudioFocus();
        }
    }

    public void pause() {
        // 1. pause playback
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            }
        }
    }


    public void release() {
        // 1. Stop playback
        if (player != null) {
            player.release();
            player = null;
            // 2. Give up audio focus
            abandonAudioFocus();
        }
    }

    private void requestAudioFocus() {
        if (!isAudioFocusGranted) {
            // Request audio focus for play back
            int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                isAudioFocusGranted = true;
                play();
            } else {
                isAudioFocusGranted = false;
                // FAILED
                Log.e(TAG, ">>>>>>>>>> FAILED TO GET AUDIO FOCUS <<<<<<<<<<<<<");
            }
        }
    }

    private void abandonAudioFocus() {
        int result = audioManager.abandonAudioFocus(audioFocusChangeListener);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            isAudioFocusGranted = false;
        } else {
            // FAILED
            Log.e(TAG, ">>>>>>>>>> FAILED TO ABANDON AUDIO FOCUS <<<<<<<<<<<<");
        }
        audioFocusChangeListener = null;
    }

    public void increment(Context context) {
        if (index == (mediaFiles.size() - 1)) {
            Toast.makeText(context, "Last media file", Toast.LENGTH_SHORT).show();
            return;
        }
        index++;
    }

    public void decrement(Context context) {
        if (index == 0) {
            Toast.makeText(context, "First media file", Toast.LENGTH_SHORT).show();

            return;
        }
        index--;
    }

    public void varyNext(Context context) {
        if (mediaFiles != null) {
            int random = (int) ((Math.random() * 10) + (Math.random() * 10));
            // check if media files ended
            if (!((index + random) > (mediaFiles.size() - 1))) {
                index = index + random;
            } else {
                // last media file
                index = mediaFiles.size() - 1;
                Toast.makeText(context, "Last media file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void varyPrevious(Context context) {
        if (mediaFiles != null) {
            int random = (int) ((Math.random() * 10) + (Math.random() * 10));
            //check if media files start
            if (!((index - random) < 0)) {
                index = index - random;
            } else {
                // First media files
                index = 0;
                Toast.makeText(context, "First media file", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Setters & Getters
    //**********************************************************************************************


    public ArrayList<MediaFile> getMediaFiles() {
        mediaFiles = repository.getMediaFiles();

        return mediaFiles;
    }

    public MediaFile getCurrentMediaFile() {
        if (mediaFiles != null && mediaFiles.size() > 0) {
            // Handle IndexOutOfBoundsException --> happens when mediaFiles changed to be lower than index
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

    public String getName() {
        getCurrentMediaFile();
        if (currentMediaFile != null)
            name = currentMediaFile.getName();

        return name;
    }

    public String getAlbum() {
        getCurrentMediaFile();
        if (currentMediaFile != null)
            album = currentMediaFile.getAlbum();

        return album;
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
        if (player != null)
            position = player.getCurrentPosition();

        return position;
    }

    public int getDurationTime() {
        if (player != null)
            duration = player.getDuration();

        return duration;
    }

    public boolean isVary() {
        isVary = repository.isVary();

        return isVary;
    }

    public void setVary(boolean vary) {
        isVary = vary;
        repository.setVary(vary);
    }

    public boolean isContinue() {
        isContinue = repository.isContinue();

        return isContinue;
    }

    public void setContinue(boolean aContinue) {
        isContinue = aContinue;
        repository.setContinue(aContinue);
    }

    public boolean isRepeating() {
        isRepeating = repository.isRepeating();

        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
        repository.setRepeating(repeating);
    }


    private class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.v("LOG_TAG", "AUDIOFOCUS_GAIN change listener");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    Log.v("LOG_TAG", "AUDIOFOCUS_GAIN_TRANSIENT");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    Log.v("LOG_TAG", "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                    if (player.isPlaying()) player.setVolume(0.25f, 0.25f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.v("LOG_TAG", "AUDIOFOCUS_LOSS");
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.v("LOG_TAG", "AUDIOFOCUS_LOSS_TRANSIENT");
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.v("LOG_TAG", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    Log.v("LOG_TAG", "AUDIOFOCUS_REQUEST_FAILED");
                    break;
            }
        }
    }

}










