package com.music.mutone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PlayerReceiver extends BroadcastReceiver {

    private static final String TAG = "PLAYER_RECEIVER_LOG_TAG";

    // Intent actions for Broadcast Receiver . actions are used to control Player in the service
    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PAUSE = "PAUSE";
    private static final String ACTION_FORWARD = "FORWARD";
    private static final String ACTION_PREVIOUS = "PREVIOUS";

    Context context;
    Player player;

    public PlayerReceiver(Context context) {
        Log.v(TAG, "Receiver is created");
        this.context = context;
        player = Player.getInstance(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.v(TAG, "Action is : " + action);
        switch (action) {
            case ACTION_PLAY:
                player.play();
                break;

            case ACTION_PAUSE:
                player.pause();
                break;

            case ACTION_FORWARD:
                if (player.isContinue())
                    player.increment(context);

                else if (player.isVary())
                    player.varyNext(context);


                player.play();

                break;

            case ACTION_PREVIOUS:
                if (player.isContinue())
                    player.decrement(context);

                else if (player.isVary())
                    player.varyPrevious(context);


                player.play();
                break;
        }

    }
}
