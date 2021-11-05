package com.music.mutone;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.music.mutone.pojo.MediaFile;

import java.util.ArrayList;

public class Player extends MediaPlayer {
    private static final String TAG = "LOG_TAG";
    private static Player sInstance;

//    private static final String CMD_NAME = "command";
//    private static final String CMD_PAUSE = "pause";
//    private static final String CMD_STOP = "pause";
//    private static final String CMD_PLAY = "play";
//
//    // Jellybean
//    private static String SERVICE_CMD = "com.sec.android.app.music.musicservicecommand";
//    private static String PAUSE_SERVICE_CMD = "com.sec.android.app.music.musicservicecommand.pause";
//    private static String PLAY_SERVICE_CMD = "com.sec.android.app.music.musicservicecommand.play";
//
//    // Honeycomb
//    {
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//            SERVICE_CMD = "com.android.music.musicservicecommand";
//            PAUSE_SERVICE_CMD = "com.android.music.musicservicecommand.pause";
//            PLAY_SERVICE_CMD = "com.android.music.musicservicecommand.play";
//        }
//    };

    private final Context context;
    private int index;
    private boolean isAudioFocusGranted;
    private boolean isPlaying;
    private boolean isVary;
    private boolean isContinue;
    private boolean isRepeating;
    private boolean isSongChosen;
    private MediaPlayer player;
    private int duration;
    private long position;
    private ArrayList<MediaFile> mediaFiles;
    private MediaFile currentMediaFile;
    private AudioFocusChangeListener audioFocusChangeListener;
    private final CompletionListener completionListener;


    private Player(Context context) {
        this.context = context;
        completionListener = new CompletionListener();
        audioFocusChangeListener = new AudioFocusChangeListener();
    }

    public static Player getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Player(context);
        }
        return sInstance;
    }

    public void initialize() {
        if (mediaFiles != null && mediaFiles.size() != 0) {
            Log.v("LOG_TAG", "initializing mediaPlayer");

            // update current MediaFile info
            currentMediaFile = mediaFiles.get(index);
            Uri uri = Uri.parse(currentMediaFile.getUri());
            Log.v("LOG_TAG", index + "Name is :"+ currentMediaFile.getName());

            // release mediaPlayer
            release();

            // initialize mediaPlayer
            player = Player.create(context, uri);

            // player != null to stop crashing
            if (player != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                player.setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                );
            }
            // Add completion listener
            if (player != null)
                player.setOnCompletionListener(completionListener);

            // update shared preferences index parameter
            Preferences.update(context);
        }
    }


    public void play() {
        if (!isPlaying) {
            if (player == null) {
                initialize();
            }


            if (isAudioFocusGranted) {
                player.start();


////                // 2. Kill off any other play back sources
////                forceMusicStop();
////                // 3. Register broadcast receiver for player intents
////                setupBroadcastReceiver();
            } else
                // request audio focus
                requestAudioFocus();

            isPlaying = true;
        }
    }

    public void pause() {
        // 1. pause playback
        if (player != null) {
            if (isAudioFocusGranted && isPlaying) {
                player.pause();
                isPlaying = false;
            }
        }
    }
//    private BroadcastReceiver broadcastReceiver;
//    private boolean isReceiverRegistered;

    public void release() {
        // 1. Stop playback
        if (player != null) {
            player.release();
            player = null;
            isPlaying = false;
            // 2. Give up audio focus
            abandonAudioFocus();
        }
    }

    public void seekTo(int progress){
        player.seekTo(progress);
    }

    private void requestAudioFocus() {
        if (!isAudioFocusGranted) {
            AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
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
                Log.e(TAG,
                        ">>>>>>>>>>>>> FAILED TO GET AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
            }
        }
    }

    private void abandonAudioFocus() {
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.abandonAudioFocus(audioFocusChangeListener);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            isAudioFocusGranted = false;
        } else {
            // FAILED
            Log.e(TAG,
                    ">>>>>>>>>>>>> FAILED TO ABANDON AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
        }
        audioFocusChangeListener = null;
    }

    public void increment() {
        if (index == (mediaFiles.size() - 1)) {
            Toast.makeText(context, "Last media file", Toast.LENGTH_SHORT).show();
            return;
        }
        index++;
    }

    public void decrement() {
        if (index == 0) {
            Toast.makeText(context, "First media file", Toast.LENGTH_SHORT).show();

            return;
        }
        index--;
    }

    public void varyNext() {
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

    public void varyPrevious() {
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
        return mediaFiles;
    }

    public void setMediaFiles(ArrayList<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    public MediaFile getCurrentMediaFile() {
        return currentMediaFile;
    }

    public void setCurrentMediaFile(MediaFile currentMediaFile) {
        this.currentMediaFile = currentMediaFile;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isPlaying() { return isPlaying; }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isVary() {
        return isVary;
    }

    public void setVary(boolean vary) {
        isVary = vary;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public void setContinue(boolean aContinue) {
        isContinue = aContinue;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }

    public boolean isSongChosen() {
        return isSongChosen;
    }

    public void setSongChosen(boolean songChosen) {
        isSongChosen = songChosen;
    }

    public int getDuration(){
        duration = player.getDuration();
        return duration;
    }

    public long getPosition() {
        position = player.getCurrentPosition();
        return position;
    }



    private class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener{

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
                default:
                    //
            }
        }
    }

    private class CompletionListener  implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if (isRepeating)
                play();

            if (isVary){
                varyNext();
                play();
            }

            if (isContinue){
                index++;
                play();
            }
        }
    }

//    private void setupBroadcastReceiver() {
//        broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                String cmd = intent.getStringExtra(CMD_NAME);
//                Log.i(TAG, "broadcastReceiver.onReceive " + action + " / " + cmd);
//
//                if (PAUSE_SERVICE_CMD.equals(action)
//                        || (SERVICE_CMD.equals(action) && CMD_PAUSE.equals(cmd))) {
//                    play();
//                }
//
//                if (PLAY_SERVICE_CMD.equals(action)
//                        || (SERVICE_CMD.equals(action) && CMD_PLAY.equals(cmd))) {
//                    pause();
//                }
//            }
//        };
//
//        // Do the right thing when something else tries to play
//        if (!isReceiverRegistered) {
//            IntentFilter commandFilter = new IntentFilter();
//            commandFilter.addAction(SERVICE_CMD);
//            commandFilter.addAction(PAUSE_SERVICE_CMD);
//            commandFilter.addAction(PLAY_SERVICE_CMD);
//            context.registerReceiver(broadcastReceiver, commandFilter);
//            isReceiverRegistered = true;
//        }
//    }
//
//    private void forceMusicStop() {
//        AudioManager audioManager = (AudioManager) context
//                .getSystemService(Context.AUDIO_SERVICE);
//        if (audioManager.isMusicActive()) {
//            Intent intentToStop = new Intent(SERVICE_CMD);
//            intentToStop.putExtra(CMD_NAME, CMD_STOP);
//            context.sendBroadcast(intentToStop);
//        }
//    }
}










