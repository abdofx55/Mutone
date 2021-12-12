package com.music.mutone;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PlayerService extends Service {
    private static final String TAG = "PLAYER_SERVICE_LOG_TAG";
    // Intent actions for Broadcast Receiver . actions are used to control Player in the service
    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PAUSE = "PAUSE";
    private static final String ACTION_FORWARD = "FORWARD";
    private static final String ACTION_PREVIOUS = "PREVIOUS";
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    Player player;
    PlayerReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "service is created ");
        player = Player.getInstance(this);
        receiver = new PlayerReceiver(this);

        registerPlayerReceiver();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "service is onStartCommand ");
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerPlayerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_FORWARD);
        filter.addAction(ACTION_PREVIOUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "service is onBind");
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "service is onDestroy");
        player.release();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    public void play() {
        player.play();
    }

    public void pause() {
        player.pause();
    }

    public void forward() {
        if (player.isContinue())
            player.increment(this);

        else if (player.isVary())
            player.varyNext(this);


        player.initialize(this);
        play();
    }

    public void previous() {
        if (player.isContinue())
            player.decrement(this);

        else if (player.isVary())
            player.varyPrevious(this);


        player.initialize(this);
        play();
    }

    public void seekTo(int progress) {
        player.seekTo(progress);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {

        public PlayerService getService() {
            // Return this instance of PlayerService so clients can call public methods
            return PlayerService.this;
        }
    }
}
