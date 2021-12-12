package com.music.mutone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.music.mutone.Data.MediaFile;
import com.music.mutone.MediaViewModel;
import com.music.mutone.Player;
import com.music.mutone.PlayerReceiver;
import com.music.mutone.R;
import com.music.mutone.RecyclerViewAdapter;
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
    Player player;
    public RecyclerViewAdapter adapter;
    LinearLayoutManager layoutManager;
    MediaViewModel viewModel;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
        player = Player.getInstance(requireActivity());
        adapter = new RecyclerViewAdapter(this);
        ArrayList<MediaFile> mediaFiles = viewModel.getMediaFiles().getValue();
        adapter.setMediaFiles(mediaFiles);


        binding.recyclerPlaylistActivity.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerPlaylistActivity.setLayoutManager(layoutManager);
        binding.recyclerPlaylistActivity.setHasFixedSize(true);

        layoutManager.scrollToPositionWithOffset(player.getIndex(), 0);

        Log.v("LOG_TAG", "Permission Granted is : " + viewModel.isStoragePermissionGranted());

        // Setting viewModel in xml to update UI
        binding.setViewModel(viewModel);

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    public void sendBroadcast(String action) {
        Intent intent = new Intent(requireContext(), PlayerReceiver.class);
        intent.setAction((action));
        LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (clickedItemIndex != player.getIndex()) {
            player.setIndex(clickedItemIndex);
            player.initialize(requireContext());
            sendBroadcast(ACTION_PLAY);
        }
        //move to back
        getParentFragmentManager().popBackStack();
    }


    private void moveTaskToBack() {
        requireActivity().moveTaskToBack(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager.scrollToPositionWithOffset(player.getIndex(), 0);
    }
}