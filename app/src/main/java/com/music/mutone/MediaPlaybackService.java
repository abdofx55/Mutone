package com.music.mutone;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.MediaBrowserServiceCompat;

import com.music.mutone.Data.MediaFile;
import com.music.mutone.Data.Repository;
import com.music.mutone.ui.MainFragment;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = "PLAYER_SERVICE_LOG_TAG";

    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    // Intent actions for Broadcast Receiver . actions are used to control Player in the service
    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PAUSE = "PAUSE";
    private static final String ACTION_FORWARD = "FORWARD";
    private static final String ACTION_PREVIOUS = "PREVIOUS";

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    MediaPlayer player;
    AudioManager audioManager;
    // Data Repository
    Repository repository;
    private boolean isAudioFocusGranted;
    // MediaFiles
    private ArrayList<MediaFile> mediaFiles;

    private int index, position;

    private Handler handler;
    private Runnable runnable;


    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private PackageValidator packageValidator;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service is created ");

        repository = Repository.getInstance(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        packageValidator = new PackageValidator(this);


        // Create a MediaSessionCompat
        mediaSession = new MediaSessionCompat(this, TAG);

        // Enable callbacks from MediaButtons and TransportControls
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(new MySessionCallback());

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "service is onStartCommand ");
        getMediaFiles();
        getIndex();

        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "service is onBind");
        return binder;
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable @org.jetbrains.annotations.Nullable Bundle rootHints) {
// (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.
        if (allowBrowsing(clientPackageName, clientUid)) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
        }
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        //  Browsing not allowed
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentMediaId)) {
            result.sendResult(null);
            return;
        }

        // Assume for example that the music catalog is already loaded/cached.

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID.equals(parentMediaId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "service is onDestroy");
        player.release();
    }

    public void sendBroadcast(String action) {
        Intent intent = new Intent(MediaPlaybackService.this, MainFragment.PlayerReceiver.class);
        intent.setAction((action));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void initialize() {

        // release mediaPlayer
        release();

        // update current Media File
        getIndex();
        MediaFile currentMediaFile = repository.getCurrentMediaFile();

        if (currentMediaFile != null) {
            // initialize mediaPlayer
            player = MediaPlayer.create(this, currentMediaFile.getUri());

            player.setOnCompletionListener(this);

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
    }


    public void play() {
        try {
            if (!player.isPlaying() && isAudioFocusGranted) {
//                seekTo(repository.getPosition());
                player.start();

                // send BroadCast
                sendBroadcast(ACTION_PLAY);

                updateSeekbar();

            } else
                // request audio focus
                requestAudioFocus();

        } catch (NullPointerException e) {
            Log.d(TAG, "Play Exception ... initializing");
            // media player is not initialized
            initialize();
            play();
        }
    }

    public void pause() {
        // 1. pause playback
        if (player != null && player.isPlaying()) {
            player.pause();
            abandonAudioFocus();

            // send BroadCast
            sendBroadcast(ACTION_PAUSE);
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

    private void updateSeekbar() {
        // Duration
        if (player != null) {
            int duration = player.getDuration();
            repository.setDuration(duration);
        }


        // Position
        handler = new Handler();
        runnable = () -> {
            if (player != null) {
                position = player.getCurrentPosition();
                repository.setPosition(position);
            }


            handler.postDelayed(runnable, 1000);
        };

        handler.postDelayed(runnable, 1000);
    }

    private void requestAudioFocus() {
        if (!isAudioFocusGranted) {
            // Request audio focus for play back
            int result = audioManager.requestAudioFocus(this,
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
                stopSelf();
            }
        }
    }

    private void abandonAudioFocus() {
        int result = audioManager.abandonAudioFocus(this);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            isAudioFocusGranted = false;
        } else {
            // FAILED
            Log.e(TAG, ">>>>>>>>>> FAILED TO ABANDON AUDIO FOCUS <<<<<<<<<<<<");
        }
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void forward() {
        if (repository.isContinue())
            increment();

        else if (repository.isVary())
            varyNext();


        initialize();
        play();

        // send BroadCast
        sendBroadcast(ACTION_FORWARD);
    }

    public void previous() {
        if (repository.isContinue())
            decrement();

        else if (repository.isVary())
            varyPrevious();


        initialize();
        play();

        // send BroadCast
        sendBroadcast(ACTION_PREVIOUS);
    }

    public void seekTo(int progress) {
        if (player != null)
            player.seekTo(progress);
    }

    public void increment() {
        if (index == (mediaFiles.size() - 1)) {
            Toast.makeText(this, "Last media file", Toast.LENGTH_SHORT).show();
            return;
        }
        index++;
        // save new index
        setIndex(index);
    }

    public void decrement() {
        if (index == 0) {
            Toast.makeText(this, "First media file", Toast.LENGTH_SHORT).show();

            return;
        }
        index--;
        // save new index
        setIndex(index);
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
                Toast.makeText(this, "Last media file", Toast.LENGTH_SHORT).show();
            }

            // save new index
            setIndex(index);
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
                Toast.makeText(this, "First media file", Toast.LENGTH_SHORT).show();
            }

            // save new index
            setIndex(index);
        }
    }

    // Setters && Getters

    /******************************************************************************************/

    public ArrayList<MediaFile> getMediaFiles() {
        mediaFiles = repository.getMediaFiles();
        return mediaFiles;
    }


    public int getIndex() {
        index = repository.getIndex();
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        repository.setIndex(index);
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.v("LOG_TAG", "AUDIOFOCUS_GAIN change listener");
                player.setVolume(1.00f, 1.00f);
                play();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                Log.v("LOG_TAG", "AUDIOFOCUS_GAIN_TRANSIENT");
                player.setVolume(1.00f, 1.00f);
                play();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                Log.v("LOG_TAG", "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                player.setVolume(0.25f, 0.25f);
                play();
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

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (repository.isRepeating()) {
            initialize();
            play();
        }


        if (repository.isVary()) {
            varyNext();
            initialize();
            play();
        }

        if (repository.isContinue()) {
            increment();
            initialize();
            play();
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {

        public MediaPlaybackService getService() {
            // Return this instance of PlayerService so clients can call public methods
            return MediaPlaybackService.this;
        }
    }
}
