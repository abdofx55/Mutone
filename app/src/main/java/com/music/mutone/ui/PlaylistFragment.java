package com.music.mutone.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.music.mutone.Player;
import com.music.mutone.R;
import com.music.mutone.RecyclerViewAdapter;
import com.music.mutone.databinding.FragmentPlaylistBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistFragment extends Fragment implements RecyclerViewAdapter.ListItemClickHandler {
    FragmentPlaylistBinding binding;
    Activity activity;
    Player player;
    private int songChosen;
    LinearLayoutManager layoutManager;

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

        if (isAdded())
            activity = getActivity();

        player = Player.getInstance(activity);

        binding.recyclerPlaylistActivity.setAdapter(MainFragment.adapter);
        layoutManager = new LinearLayoutManager(activity);
        binding.recyclerPlaylistActivity.setLayoutManager(layoutManager);
        binding.recyclerPlaylistActivity.setHasFixedSize(true);

        layoutManager.scrollToPositionWithOffset(player.getIndex(), 0);
        songChosen = player.getIndex();

        if (!(MainFragment.isStoragePermissionGranted))
            binding.emptyPlaylistTextView.setText(R.string.empty_due_permission);

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        player.setIndex(clickedItemIndex);
        if (player.getIndex() != songChosen) {
            Intent intent = activity.getIntent();
            intent.putExtra("isSongChosen", true);
            activity.setResult(Activity.RESULT_OK, intent);
        }
        moveTaskToBack(activity);
    }


    private void moveTaskToBack(Activity activity) {
        if (activity != null)
            activity.moveTaskToBack(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager.scrollToPositionWithOffset(player.getIndex(), 0);
    }
}