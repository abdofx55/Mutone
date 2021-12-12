package com.music.mutone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.music.mutone.Data.MediaFile;

import java.util.ArrayList;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.SongViewHolder> {
//    private Cursor mCursor;
    private ArrayList<MediaFile> mediaFiles;

    final private ListItemClickHandler mOnClickListener;


    public RecyclerViewAdapter(ListItemClickHandler listener) {
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_list, parent , false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.bind(mediaFiles , position);
    }

    @Override
    public int getItemCount() {
        if (mediaFiles != null)
            return mediaFiles.size();
        else
            return 0;
    }

    public void setMediaFiles(ArrayList<MediaFile> mediaFiles) {
        if (this.mediaFiles == mediaFiles) { return; }
        this.mediaFiles = mediaFiles;
    }

    public interface ListItemClickHandler{
        void onListItemClick(int clickedItemIndex);
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView number, name,album ,duration;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.list_number);
            name = itemView.findViewById(R.id.list_name);
            album = itemView.findViewById(R.id.list_album);
            duration = itemView.findViewById(R.id.list_duration);
            itemView.setOnClickListener(this);
        }

        void bind(ArrayList<MediaFile> mediaFiles , int position){
            if (mediaFiles != null) {
                MediaFile mediaFile = mediaFiles.get(position);

                String id = String.valueOf(position + 1);
                String name = mediaFile.getName();
                String album = mediaFile.getAlbum();
                String duration = Tasks.formatMilliSecond(mediaFile.getDuration());

                number.setText(id);
                this.name.setText(name);
                this.album.setText(album);
                this.duration.setText(duration);
            }
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }
}
