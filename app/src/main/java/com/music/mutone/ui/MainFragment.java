package com.music.mutone.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
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
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.music.mutone.Data.MediaFile;
import com.music.mutone.MediaViewModel;
import com.music.mutone.Player;
import com.music.mutone.PlayerReceiver;
import com.music.mutone.PlayerService;
import com.music.mutone.R;
import com.music.mutone.RecyclerViewAdapter;
import com.music.mutone.Tasks;
import com.music.mutone.databinding.FragmentMainBinding;

import java.util.ArrayList;


public class MainFragment extends Fragment implements RecyclerViewAdapter.ListItemClickHandler {

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
    public Player player;
    FragmentMainBinding binding;
    LinearLayoutManager layoutManager;
    MediaViewModel viewModel;
    MediaFilesObserver observer;
    private PlayerClickListener playbackClickListener;
    private OnSeekBarChangeListener seekBarChangeListener;
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.

    Animation alfaAscAnimation;
    Toast toast;

    private Handler handler;
    private Runnable runnable;
    private CompletionListener completionListener;

    PlayerService mService;
    boolean mBound = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);

        initializeValues();
        attachListeners();

        // Setting viewModel in xml to update UI
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(requireActivity());

        viewModel.isStoragePermissionGranted().observe(this, aBoolean -> {
            if (aBoolean) {
                // Start Player Service
                startPlayerService();

                viewModel.getMediaFiles().observe(this, observer);
            }
        });

        return binding.getRoot();
    }

    private void initializeValues() {
        viewModel = new ViewModelProvider(requireActivity()).get(MediaViewModel.class);
        player = Player.getInstance(requireContext());
        observer = new MediaFilesObserver();
        playbackClickListener = new PlayerClickListener();
        seekBarChangeListener = new SeekBarChangeListener();
        completionListener = new CompletionListener();
        adapter = new RecyclerViewAdapter(this);
        layoutManager = new LinearLayoutManager(requireContext());
        alfaAscAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.alfa_asc);
        handler = new Handler();
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


    protected void onRestart() {
//        layoutManager.scrollToPositionWithOffset(player.getIndex(), 0);
//        if (player.isSongChosen()) {
//            alfaAscAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.alfa_asc);
//            binding.playerView.play.setAnimation(alfaAscAnimation);
//            player.initialize();
//            player.play();
//        }
//
//        player.setSongChosen(false);

    }

    private void startPlayerService() {
        Intent serviceIntent = new Intent(requireContext(), PlayerService.class);
        requireActivity().startService(serviceIntent);
        requireActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    public void sendBroadcast(String action) {
        Intent intent = new Intent(requireContext(), PlayerReceiver.class);
        intent.setAction((action));
        LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);
    }


    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (player != null) {
            //  update index
            player.setIndex(clickedItemIndex);

            //initialize mediaPlayer
            player.initialize(requireContext());

            // play mediaPlayer
            player.play();

            // update UI
            updateUI(UPDATE_MEDIA_DATA);
            updateUI(PLAY);
        }
    }


    private void initializeSeekBar() {
        // Duration
        viewModel.getDuration().observe(this, duration -> {
            binding.playerView.seekBar.setMax(duration);
            binding.playerView.duration.setText(Tasks.formatMilliSecond(duration));
        });

        // Position
        runnable = () -> {
            viewModel.getPosition().observe(this, new Observer<Integer>() {
                @Override
                public void onChanged(Integer position) {
                    Log.d(TAG, "position is : " + position);
                    binding.playerView.seekBar.setProgress(position);
                    binding.playerView.position.setText(Tasks.formatMilliSecond(position));
                }
            });

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


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null) {
            // Attaching OnCompletionListener
            player.setOnCompletionListener(completionListener);

            updateUI(INITIALISE_UI);
            updateUI(UPDATE_MEDIA_DATA);
            initializeSeekBar();

            if (player.isPlaying()) {
                updateUI(PLAY);
                player.play();
            }
        }
    }

    private void updateUI(int state) {
        if (player == null)
            player = Player.getInstance(requireContext());

        switch (state) {
            case INITIALISE_UI:
                binding.recyclerMainActivity.setLayoutManager(layoutManager);
                binding.recyclerMainActivity.setHasFixedSize(true);
                binding.recyclerMainActivity.setAdapter(adapter);
                binding.recyclerMainActivity.scrollTo(player.getIndex(), 0);
                layoutManager.scrollToPositionWithOffset(player.getIndex(), 0);

                if (player.isVary())
                    binding.playerView.vary.setImageResource(R.drawable.vary_active);
                if (player.isContinue())
                    binding.playerView.vary.setImageResource(R.drawable.continued);
                if (player.isRepeating())
                    binding.playerView.repeat.setImageResource(R.drawable.repeat_active);

                break;

            case UPDATE_MEDIA_DATA:
                // Media Data
                if (player != null) {
                    initializeSeekBar();
                }

                viewModel.getName();
                viewModel.getAlbum();
                viewModel.getPosition();
                viewModel.getDuration();

                break;

            case PLAY:
                // Active playback.
                binding.playerView.pause.setVisibility(View.INVISIBLE);
                binding.playerView.play.setVisibility(View.VISIBLE);
                binding.playerView.playFilter.setVisibility(View.VISIBLE);
                break;

            case PAUSE:
                // Not playing because playback is paused, ended
                binding.playerView.play.setVisibility(View.INVISIBLE);
                binding.playerView.playFilter.setVisibility(View.INVISIBLE);
                binding.playerView.pause.setVisibility(View.VISIBLE);
                break;
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
                if (!player.isVary() & !player.isContinue()) {
                    binding.playerView.vary.setImageResource(R.drawable.vary_active);
                    binding.playerView.repeat.setImageResource(R.drawable.repeat_not_active);
                    player.setRepeating(false);
                    player.setContinue(false);
                    player.setVary(true);



                } else if (player.isVary()) {
                    binding.playerView.vary.setImageResource(R.drawable.continued);
                    binding.playerView.repeat.setImageResource(R.drawable.repeat_not_active);
                    player.setRepeating(false);
                    player.setContinue(true);
                    player.setVary(false);

                } else {
                    binding.playerView.vary.setImageResource(R.drawable.vary_not_active);
                    player.setRepeating(false);
                    player.setContinue(false);
                    player.setVary(false);
                }

//            previous Button
//             *************************************************************************************
            } else if (view == binding.playerView.previous) {
                makeAlfaAnimation(binding.playerView.previous);
                if (player != null && player.getMediaFiles() != null) {
                    mService.previous();
//                    sendBroadcast(ACTION_PLAY);

                    updateUI(UPDATE_MEDIA_DATA);
                    updateUI(PLAY);

                    //scroll to list item position
                    binding.recyclerMainActivity.scrollTo(player.getIndex(), 0);
                    layoutManager.scrollToPositionWithOffset(player.getIndex(), 0);
                }

//            play Button
//             *************************************************************************************
            } else if (view == binding.playerView.playLayout) {
                if (player != null && player.getMediaFiles() != null) {
                    if (!player.isPlaying()) {
                        mService.play();
//                        sendBroadcast(ACTION_PLAY);

                        //set Animation
                        makeAlfaAnimation(binding.playerView.play);
                        makeAlfaAnimation(binding.playerView.playFilter);
                        // Update UI
                        updateUI(PLAY);

                    } else {
                        sendBroadcast(ACTION_PAUSE);

                        //clear Animation
                        binding.playerView.play.clearAnimation();
                        binding.playerView.playFilter.clearAnimation();
                        // Update UI
                        updateUI(PAUSE);

                    }
                }

                // next Button
                // *************************************************************************************
            } else if (view == binding.playerView.fastForward) {
                makeAlfaAnimation(binding.playerView.fastForward);
                if (player != null && player.getMediaFiles() != null) {
                    mService.forward();
//                    sendBroadcast(ACTION_PLAY);

                    updateUI(UPDATE_MEDIA_DATA);
                    updateUI(PLAY);

                    //scroll to list item position
                    binding.recyclerMainActivity.scrollTo(player.getIndex(), 0);
                    layoutManager.scrollToPositionWithOffset(player.getIndex(), 0);
                }

                // repeat Button
                // *************************************************************************************
            } else if (view == binding.playerView.repeat) {
                if (!player.isRepeating()) {
                    binding.playerView.repeat.setImageResource(R.drawable.repeat_active);
                    binding.playerView.vary.setImageResource(R.drawable.vary_not_active);

                    player.setRepeating(true);
                    player.setContinue(false);
                    player.setVary(false);

                } else {
                    binding.playerView.repeat.setImageResource(R.drawable.repeat_not_active);
                    player.setRepeating(false);
                }
            }
        }
    }

    private class MediaFilesObserver implements Observer<ArrayList<MediaFile>> {

        @Override
        public void onChanged(ArrayList<MediaFile> mediaFiles) {
            Log.v(TAG, "mediaFiles has changed");

            // Bail early if the arraylist is null or there is less than 1 row in the arraylist
            if (mediaFiles == null) {
                binding.emptyMainlistTextView.setText("Failed to load media files");
                Toast.makeText(requireContext(), "Failed to load media files", Toast.LENGTH_SHORT).show();

            } else if (mediaFiles.size() == 0) {
                binding.emptyMainlistTextView.setText("No media found on this device");
                Toast.makeText(requireContext(), "No media found on this device", Toast.LENGTH_SHORT).show();
            } else {
                binding.emptyMainlistTextView.setText("");

                // Add the newer arraylist to the adapter
                adapter.setMediaFiles(mediaFiles);
                binding.recyclerMainActivity.setAdapter(adapter);

                // Initialize MediaPlayer
                player.initialize(requireContext());

                //update UI with the current file
                updateUI(UPDATE_MEDIA_DATA);
            }
        }
    }

    private class SeekBarChangeListener implements OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (player != null && fromUser) {
                Log.d(TAG, "progress changed to : " + progress);
                mService.seekTo(progress);
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

    private class CompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if (player.isRepeating()) {
                player.initialize(requireContext());
                player.play();
                updateUI(UPDATE_MEDIA_DATA);
            }


            if (player.isVary()) {
                player.varyNext(requireContext());
                player.initialize(requireContext());
                player.play();
                updateUI(UPDATE_MEDIA_DATA);
            }

            if (player.isContinue()) {
                player.setIndex(player.getIndex() + 1);
                player.initialize(requireContext());
                player.play();
                updateUI(UPDATE_MEDIA_DATA);
            }

        }
    }
}



