package com.music.mutone.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.music.mutone.Data.MediaFile;
import com.music.mutone.MediaPlaybackService;
import com.music.mutone.MediaViewModel;
import com.music.mutone.R;
import com.music.mutone.RecyclerViewAdapter;
import com.music.mutone.Tasks;
import com.music.mutone.databinding.FragmentMainBinding;

import java.util.ArrayList;


public class MainFragment extends Fragment implements RecyclerViewAdapter.ListItemClickHandler, ServiceConnection {

    private static final String TAG = "MAIN_FRAGMENT_LOG_TAG";

    // Intent actions for Broadcast Receiver . actions are used to control Player in the service
    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PAUSE = "PAUSE";
    private static final String ACTION_FORWARD = "FORWARD";
    private static final String ACTION_PREVIOUS = "PREVIOUS";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int INITIALISE_UI = 0;
    private static final int UPDATE_MEDIA_DATA = 1;
    private static final int PLAY = 2;
    private static final int PAUSE = 3;
    public RecyclerViewAdapter adapter;
    LinearLayoutManager layoutManager;
    FragmentMainBinding binding;
    MediaViewModel viewModel;
    private PlayerClickListener playbackClickListener;
    private OnSeekBarChangeListener seekBarChangeListener;
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.

    Animation alfaAscAnimation;

    private Handler handler;
    private Runnable runnable;

    MediaPlaybackService mediaPlaybackService;
    boolean mBound = false;

    PlayerReceiver receiver;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);

        initializeValues();
        attachListeners();
        initializeUI();

        // Check if Storage Permission granted
        if (viewModel.isStoragePermissionGranted()) {
            // 1 -> read media files
            readMediaFiles();

            // 2 -> Update UI with the current file
            updateUI();

            // 3 -> Start Player Service
            bindPlayerService();

            // 4 -> register Player BroadCast Receiver
            registerPlayerReceiver();
        }

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
        unBindPlayerService();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        Log.d(TAG, "onServiceConnected");
        MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) iBinder;
        mediaPlaybackService = binder.getService();
        updatePlayState(mediaPlaybackService.isPlaying());
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceDisconnected");
        mBound = false;
    }

    private void bindPlayerService() {
        Intent serviceIntent = new Intent(requireContext(), MediaPlaybackService.class);
        requireActivity().startService(serviceIntent);
        requireActivity().bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
    }

    private void unBindPlayerService() {
        requireActivity().unbindService(this);
    }

    private void readMediaFiles() {
        ArrayList<MediaFile> mediaFiles = viewModel.getMediaFiles();

        // Add the newer arraylist to the adapter
        adapter.setMediaFiles(mediaFiles);


    }

    private void initializeValues() {
        viewModel = new ViewModelProvider(requireActivity()).get(MediaViewModel.class);
        playbackClickListener = new PlayerClickListener();
        seekBarChangeListener = new SeekBarChangeListener();
        adapter = new RecyclerViewAdapter(this);
        layoutManager = new LinearLayoutManager(requireContext());
        alfaAscAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.alfa_asc);
        handler = new Handler();
        receiver = new PlayerReceiver();

        // Setting viewModel in xml to update UI
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(requireActivity());
    }

    private void attachListeners() {
        binding.playerView.playLayout.setOnClickListener(playbackClickListener);
        binding.playList.setOnClickListener(playbackClickListener);
        binding.playerView.previous.setOnClickListener(playbackClickListener);
        binding.playerView.fastForward.setOnClickListener(playbackClickListener);
        binding.playerView.repeat.setOnClickListener(playbackClickListener);
        binding.playerView.vary.setOnClickListener(playbackClickListener);
        binding.back.setOnClickListener(playbackClickListener);
        binding.favorite.setOnClickListener(playbackClickListener);
        binding.playerView.seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

    }

    private void registerPlayerReceiver() {
        if (receiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_PLAY);
            filter.addAction(ACTION_PAUSE);
            filter.addAction(ACTION_FORWARD);
            filter.addAction(ACTION_PREVIOUS);
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, filter);
        }
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        //  update index
        viewModel.setIndex(clickedItemIndex);

        //initialize mediaPlayer
        mediaPlaybackService.initialize();

        // play mediaPlayer
        mediaPlaybackService.play();
    }


    private void initializeSeekBar() {
        // Duration
        binding.playerView.seekBar.setMax(viewModel.getDuration());
        binding.playerView.duration.setText(Tasks.formatMilliSecond(viewModel.getDuration()));
        Log.d(TAG, "Duration is : " + Tasks.formatMilliSecond(viewModel.getDuration()));

        // Position
        runnable = () -> {
            binding.playerView.seekBar.setProgress(viewModel.getPosition());
            binding.playerView.position.setText(Tasks.formatMilliSecond(viewModel.getPosition()));

            handler.postDelayed(runnable, 1000);
        };

        handler.postDelayed(runnable, 1000);
    }


//    public void changeListItemColor() {
//        listName.setTextColor(Color.parseColor("#ffce5b"));
//        listAlbum.setTextColor(Color.parseColor("#ffce5b"));
//        listNumber.setTextColor(Color.parseColor("#ffce5b"));
//        listDuration.setTextColor(Color.parseColor("#ffce5b"));
//    }


    private void initializeUI() {
        binding.recyclerMain.setLayoutManager(layoutManager);
        binding.recyclerMain.setHasFixedSize(true);
        binding.recyclerMain.setAdapter(adapter);
        layoutManager.scrollToPositionWithOffset(viewModel.getIndex(), 0);


        if (viewModel.isVary())
            binding.playerView.vary.setImageResource(R.drawable.vary_active);
        if (viewModel.isContinue())
            binding.playerView.vary.setImageResource(R.drawable.continue_active);
        if (viewModel.isRepeating())
            binding.playerView.repeat.setImageResource(R.drawable.repeat_active);
    }

    private void updateUI() {
        // Media Data
        viewModel.getName();
        viewModel.getAlbum();
        initializeSeekBar();

    }

    private void updatePlayState(boolean isPlaying) {
        if (isPlaying) {
            // Active playback.
            binding.playerView.pause.setVisibility(View.INVISIBLE);
            binding.playerView.play.setVisibility(View.VISIBLE);
            binding.playerView.playFilter.setVisibility(View.VISIBLE);
        } else {
            // Not playing because playback is paused, ended
            binding.playerView.play.setVisibility(View.INVISIBLE);
            binding.playerView.playFilter.setVisibility(View.INVISIBLE);
            binding.playerView.pause.setVisibility(View.VISIBLE);
        }
    }

    private void makeAlfaAnimation(View view) {
        if (view != null) {
            view.clearAnimation();
            Animation alfaAscAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.alfa_asc);
            view.startAnimation(alfaAscAnimation);
        }
    }

    private void openPlaylistFragment() {
        NavDirections action = MainFragmentDirections.actionMainFragmentToPlaylistFragment();
        NavController navController = NavHostFragment.findNavController(this);
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination != null && navDestination.getId() == R.id.mainFragment)
            navController.navigate(action);
    }

    class PlayerClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // back Button
            // *****************************************************************************************
            if (view == binding.back) {
                Tasks.moveTaskToBack(requireActivity());


                // playList Button
                // *************************************************************************************

            } else if (view == binding.playList) {
                openPlaylistFragment();

                // favorite Button
                // *************************************************************************************
            } else if (view == binding.favorite) {
                if (binding.favorite.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.favorite).getConstantState())) {
                    binding.favorite.setImageResource(R.drawable.favorite_colored);
                } else {
                    binding.favorite.setImageResource(R.drawable.favorite);
                }

                // Vary Button
                // *************************************************************************************
            } else if (view == binding.playerView.vary) {
                if (!viewModel.isVary() && !viewModel.isContinue()) {
                    viewModel.setVary(true);
                    binding.playerView.vary.setImageResource(R.drawable.vary_active);

                } else if (viewModel.isVary()) {
                    viewModel.setContinue(true);
                    binding.playerView.vary.setImageResource(R.drawable.continue_active);

                } else {
                    viewModel.setContinue(false);
                    binding.playerView.vary.setImageResource(R.drawable.vary_not_active);
                }

                // previous Button
                // *************************************************************************************
            } else if (view == binding.playerView.previous && viewModel.isStoragePermissionGranted()) {
                makeAlfaAnimation(binding.playerView.previous);

                mediaPlaybackService.previous();

                //scroll to list item position
                layoutManager.scrollToPositionWithOffset(viewModel.getIndex(), 0);

//            play Button
//             *************************************************************************************
            } else if (view == binding.playerView.playLayout && viewModel.isStoragePermissionGranted()) {
                if (!mediaPlaybackService.isPlaying()) {
                    mediaPlaybackService.play();

                    //set Animation
                    makeAlfaAnimation(binding.playerView.play);
                    makeAlfaAnimation(binding.playerView.playFilter);

                } else {
                    mediaPlaybackService.pause();

                    //clear Animation
                    binding.playerView.play.clearAnimation();
                    binding.playerView.playFilter.clearAnimation();

                }

                // next Button
                // *************************************************************************************
            } else if (view == binding.playerView.fastForward && viewModel.isStoragePermissionGranted()) {
                makeAlfaAnimation(binding.playerView.fastForward);
                mediaPlaybackService.forward();

                //scroll to list item position
                layoutManager.scrollToPositionWithOffset(viewModel.getIndex(), 0);

                // repeat Button
                // *************************************************************************************
            } else if (view == binding.playerView.repeat) {
                if (!viewModel.isRepeating()) {
                    viewModel.setRepeating(true);
                    binding.playerView.repeat.setImageResource(R.drawable.repeat_active);
                } else {
                    viewModel.setRepeating(false);
                    binding.playerView.repeat.setImageResource(R.drawable.repeat_not_active);
                }
            }
        }
    }

    private class SeekBarChangeListener implements OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                Log.d(TAG, "progress changed to : " + progress);
                mediaPlaybackService.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.v("LOG_TAG", "seekBar StartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.v("LOG_TAG", "seekBar StopTrackingTouch");
        }
    }

    public class PlayerReceiver extends BroadcastReceiver {

        public PlayerReceiver() {
            Log.v(TAG, "Receiver is created");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            String action = intent.getAction();
            Log.v(TAG, "Action is : " + action);
            switch (action) {
                case ACTION_PLAY:
                    updateUI();
                    updatePlayState(true);
                    break;
                case ACTION_PAUSE:
                    updatePlayState(false);
                    break;
                case ACTION_FORWARD:
                case ACTION_PREVIOUS:
                    //scroll to list item position
                    layoutManager.scrollToPositionWithOffset(viewModel.getIndex(), 0);

                    break;
            }

        }
    }
}



