package com.music.mutone.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.music.mutone.Data.MediaFile;
import com.music.mutone.MediaPlaybackService;
import com.music.mutone.MediaViewModel;
import com.music.mutone.R;
import com.music.mutone.RecyclerViewAdapter;
import com.music.mutone.Tasks;
import com.music.mutone.databinding.FragmentPlaylistBinding;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistFragment extends Fragment implements RecyclerViewAdapter.ListItemClickHandler {

    private static final String TAG = "PLAT_LIST_FRAGMENT_LOG_TAG";

    // Intent actions for Broadcast Receiver . actions are used to control Player in the service
    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PAUSE = "PAUSE";
    private static final String ACTION_FORWARD = "FORWARD";
    private static final String ACTION_PREVIOUS = "PREVIOUS";

    FragmentPlaylistBinding binding;
    public RecyclerViewAdapter adapter;
    LinearLayoutManager layoutManager;
    MediaViewModel viewModel;

    MediaPlaybackService mediaPlaybackService;
    boolean mBound = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            mediaPlaybackService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    public PlaylistFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlaylistFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlaylistFragment newInstance(String param1, String param2) {
        PlaylistFragment fragment = new PlaylistFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(MediaViewModel.class);
        adapter = new RecyclerViewAdapter(this);


        binding.recyclerPlaylist.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerPlaylist.setLayoutManager(layoutManager);
        binding.recyclerPlaylist.setHasFixedSize(true);

        layoutManager.scrollToPositionWithOffset(viewModel.getIndex(), 0);

        Log.v("LOG_TAG", "Permission Granted is : " + viewModel.isStoragePermissionGranted());

        // Setting viewModel in xml to update UI
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(requireActivity());

        if (viewModel.isStoragePermissionGranted()) {
            // Start Player Service
            startPlayerService();

            readMediaFiles();
        }

        return binding.getRoot();
    }

    private void readMediaFiles() {
        ArrayList<MediaFile> mediaFiles = viewModel.getMediaFiles();

        // Add the newer arraylist to the adapter
        adapter.setMediaFiles(mediaFiles);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (clickedItemIndex != viewModel.getIndex()) {
            viewModel.setIndex(clickedItemIndex);
            mediaPlaybackService.initialize();
            mediaPlaybackService.play();
        }
        //move to back
        Tasks.moveTaskToBack(requireActivity());

    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager.scrollToPositionWithOffset(viewModel.getIndex(), 0);
    }

    private void startPlayerService() {
        Intent serviceIntent = new Intent(requireContext(), MediaPlaybackService.class);
        requireActivity().startService(serviceIntent);
        requireActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }
}