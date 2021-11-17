package com.music.mutone.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.music.mutone.MediaViewModel;
import com.music.mutone.Player;
import com.music.mutone.PreferencesUtil;
import com.music.mutone.R;
import com.music.mutone.RecyclerViewAdapter;
import com.music.mutone.Tasks;
import com.music.mutone.databinding.FragmentMainBinding;
import com.music.mutone.pojo.MediaFile;

import java.util.ArrayList;


public class MainFragment extends Fragment implements RecyclerViewAdapter.ListItemClickHandler {
    static final int ASK_QUESTION_REQUEST = 1;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static final int STORAGE_PERMISSION_CODE = 1;
    private static final int INITIALISE_UI = 0;
    private static final int UPDATE_MEDIA_DATA = 1;
    private static final int PLAY = 2;
    private static final int PAUSE = 3;
    public static RecyclerViewAdapter adapter;
    public Player player;
    FragmentMainBinding binding;
    AudioManager audioManager;
    LinearLayoutManager layoutManager;
    MediaViewModel viewModel;
    private Activity activity;
    private PlayerClickListener playbackClickListener;
    private OnSeekBarChangeListener seekBarChangeListener;
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted.
                    Log.v("LOG_TAG", "permission granted");
                    viewModel.setStoragePermissionGranted(true);
                    binding.emptyMainlistTextView.setText("");
                    readMediaFiles();
                } else {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // User has denied the permission
                        Log.v("LOG_TAG", "permission denied");
                    } else {
                        //// User has denied the permission permanently
                        Log.v("LOG_TAG", "permission denied permanently");
                    }
                    viewModel.setStoragePermissionGranted(false);
                    binding.emptyMainlistTextView.setText(R.string.empty_due_permission);
                }
            });
    private MediaFilesObserver mediaFilesObserver;
    Animation alfaAscAnimation;
    Toast toast;

    private Handler handler;
    private Runnable runnable;
    private CompletionListener completionListener;


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

        if (isAdded())
            activity = getActivity();

        initializeValues();

        // Read Preferences
        PreferencesUtil.CRUD.read(activity);

        updateUI(INITIALISE_UI);
        attachListeners();

        // Check Permission State
        checkStoragePermissionState();

        if (viewModel.isStoragePermissionGranted()) {
            // Read MediaFiles if permission granted
            readMediaFiles();
        }


        return binding.getRoot();
    }


    public void checkStoragePermissionState() {
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            viewModel.setStoragePermissionGranted(true);

        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            viewModel.setStoragePermissionGranted(false);
            binding.emptyMainlistTextView.setText(R.string.empty_due_permission);
            requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    private void readMediaFiles() {
        viewModel.getMediaFiles(activity).observe(getViewLifecycleOwner(), mediaFilesObserver);
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
        layoutManager.scrollToPositionWithOffset(player.getIndex(), 0);
        if (player.isSongChosen()) {
            alfaAscAnimation = AnimationUtils.loadAnimation(activity, R.anim.alfa_asc);
            binding.playerView.play.setAnimation(alfaAscAnimation);
            player.initialize();
            player.play();
        }

        player.setSongChosen(false);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the request code is same as what is passed  here it is 1
        if (requestCode == ASK_QUESTION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                boolean isSongChosen = data.getBooleanExtra("isSongChosen", false);
                player.setSongChosen(isSongChosen);
            }
        }
    }

    private void initializeValues() {
        viewModel = new ViewModelProvider(this).get(MediaViewModel.class);
        player = Player.getInstance(activity);
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        playbackClickListener = new PlayerClickListener();
        seekBarChangeListener = new SeekBarChangeListener();
        completionListener = new CompletionListener();
        adapter = new RecyclerViewAdapter(this);
        layoutManager = new LinearLayoutManager(activity);
        alfaAscAnimation = AnimationUtils.loadAnimation(activity, R.anim.alfa_asc);
        mediaFilesObserver = new MediaFilesObserver();
        handler = new Handler();
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (player != null) {
            //  update index
            player.setIndex(clickedItemIndex);

            //initialize mediaPlayer
            player.initialize();

            // play mediaPlayer
            player.play();

            // update UI
            updateUI(UPDATE_MEDIA_DATA);
            updateUI(PLAY);
        }
    }


    private void initializeSeekBar() {
        if (player != null) {
            Log.v("LOG_TAG", player.toString() + "     " + (player != null));
            int duration = player.getDuration();      // in milliSeconds
            binding.playerView.seekBar.setMax(duration / 1000);
            binding.playerView.duration.setText(Tasks.formatMilliSecond(duration));
//            Log.v("LOG_TAG", "duration is : " + duration/1000);

            runnable = () -> {
                int position = (int) player.getPosition();      // im milliSeconds
                binding.playerView.seekBar.setProgress(position / 1000);
                binding.playerView.position.setText(Tasks.formatMilliSecond(position));
//                Log.v("LOG_TAG", "position is :" + position/1000);


                handler.postDelayed(runnable, 1000);
            };
            handler.postDelayed(runnable, 1000);
        }
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

        // Attaching OnCompletionListener
        if (player != null)
            player.setOnCompletionListener(completionListener);
    }

    private void updateUI(int state) {
        if (player == null)
            player = Player.getInstance(activity);

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
                    MediaFile currentMediaFile = player.getCurrentMediaFile();

                    if (currentMediaFile != null) {
                        binding.albumName.setText(currentMediaFile.getAlbum());
                        binding.songName.setText(currentMediaFile.getName());
                    }

                    initializeSeekBar();
                }

                break;

            case PLAY:
                //set Animation
                makeAlfaAnimation(binding.playerView.play);
                makeAlfaAnimation(binding.playerView.playFilter);

                // Active playback.
                binding.playerView.pause.setVisibility(View.INVISIBLE);
                binding.playerView.play.setVisibility(View.VISIBLE);
                binding.playerView.playFilter.setVisibility(View.VISIBLE);
                break;

            case PAUSE:
                //clear Animation
                binding.playerView.play.clearAnimation();
                binding.playerView.playFilter.clearAnimation();

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
            Animation alfaAscAnimation = AnimationUtils.loadAnimation(activity, R.anim.alfa_asc);
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
                Tasks.moveTaskToBack(activity);


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
//                    player.setRepeating(false);
                    player.setLooping(false);
                    player.setContinue(false);
                    player.setVary(true);

                    // update sharedPreferences
                    PreferencesUtil.CRUD.update(activity);

                } else if (player.isVary()) {
                    binding.playerView.vary.setImageResource(R.drawable.continued);
                    binding.playerView.repeat.setImageResource(R.drawable.repeat_not_active);
//                    player.setRepeating(false);
                    player.setLooping(false);
                    player.setContinue(true);
                    player.setVary(false);

                    // update sharedPreferences
                    PreferencesUtil.CRUD.update(activity);

                } else {
                    binding.playerView.vary.setImageResource(R.drawable.vary_not_active);
//                    player.setRepeating(false);
                    player.setLooping(false);
                    player.setContinue(false);
                    player.setVary(false);

                    // update sharedPreferences
                    PreferencesUtil.CRUD.update(activity);
                }

//            previous Button
//             *************************************************************************************
            } else if (view == binding.playerView.previous) {
                makeAlfaAnimation(binding.playerView.previous);
                if (player != null && player.getMediaFiles() != null) {
                    if (player.isVary()) {
                        player.varyPrevious();
                    } else {
                        player.decrement();
                    }
                    player.initialize();
                    player.play();
                    if (player.isPlaying()) {
                        updateUI(UPDATE_MEDIA_DATA);
                        updateUI(PLAY);
                    }

                    //scroll to list item position
                    binding.recyclerMainActivity.scrollTo(player.getIndex(), 0);
                    layoutManager.scrollToPositionWithOffset(player.getIndex(), 0);
                }

//            play Button
//             *************************************************************************************
            } else if (view == binding.playerView.playLayout) {
                if (player != null && player.getMediaFiles() != null) {
                    if (!player.isPlaying()) {
                        player.play();
                        if (player.isPlaying())
                            updateUI(PLAY);

                    } else {
                        player.pause();
                        if (!player.isPlaying())
                            updateUI(PAUSE);
                    }
                }

                // next Button
                // *************************************************************************************
            } else if (view == binding.playerView.fastForward) {
                makeAlfaAnimation(binding.playerView.fastForward);
                if (player != null && player.getMediaFiles() != null) {
                    if (player.isVary()) {
                        player.varyNext();
                    } else {
                        player.increment();
                    }
                    player.initialize();
                    player.play();

                    if (player.isPlaying()) {
                        updateUI(UPDATE_MEDIA_DATA);
                        updateUI(PLAY);
                    }

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

//                    player.setRepeating(true);
                    player.setLooping(true);
                    player.setContinue(false);
                    player.setVary(false);

                } else {
                    binding.playerView.repeat.setImageResource(R.drawable.repeat_not_active);
                    player.setRepeating(false);
                }

                // update sharedPreferences
                PreferencesUtil.CRUD.update(activity);
            }
        }
    }

    private class SeekBarChangeListener implements OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (player != null && fromUser) {
                Log.v("LOG_TAG", "seekBar Changed ..... progress is : " + progress);
                player.seekTo(progress * 1000);
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

    private class MediaFilesObserver implements Observer<ArrayList<MediaFile>> {

        @Override
        public void onChanged(ArrayList<MediaFile> mediaFiles) {
            Log.v("LOG_TAG", "mediaFiles has changed");
            player.setMediaFiles(mediaFiles);

            // Bail early if the arraylist is null or there is less than 1 row in the arraylist
            if (mediaFiles == null) {
                binding.emptyMainlistTextView.setText("Failed to load media files");
                Toast.makeText(activity, "Failed to load media files", Toast.LENGTH_SHORT).show();

            } else if (mediaFiles.size() == 0) {
                binding.emptyMainlistTextView.setText("No media found on this device");
                Toast.makeText(activity, "No media found on this device", Toast.LENGTH_SHORT).show();
            } else {
                binding.emptyMainlistTextView.setText("");

                // Add the newer arraylist to the adapter
                adapter.setMediaFiles(mediaFiles);
                binding.recyclerMainActivity.setAdapter(adapter);

                // Initialize MediaPlayer
                player.initialize();

                //update UI with the current file
                updateUI(UPDATE_MEDIA_DATA);
            }
        }
    }

    private class CompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if (player.isRepeating()) {
                player.initialize();
                player.play();
                updateUI(UPDATE_MEDIA_DATA);
            }


            if (player.isVary()) {
                player.varyNext();
                player.initialize();
                player.play();
                updateUI(UPDATE_MEDIA_DATA);
            }

            if (player.isContinue()) {
                player.setIndex(player.getIndex() + 1);
                player.initialize();
                player.play();
                updateUI(UPDATE_MEDIA_DATA);
            }

        }
    }
}



