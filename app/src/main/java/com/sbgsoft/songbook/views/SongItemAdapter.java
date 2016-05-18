package com.sbgsoft.songbook.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.Item;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;

import java.util.Collections;
import java.util.List;

/**
 * Created by SamIAm on 5/18/2016.
 */
public class SongItemAdapter extends RecyclerView.Adapter<SongItemViewHolder> {
    public enum SortType {
        DateRecent,
        DateOldest,
        Title
    }

    private List<SongItem> mSongs;
    private MainActivity mMainActivity;
    private SortType currentSortType;

    public SongItemAdapter(List<SongItem> sets, MainActivity mainActivity) {
        mSongs = sets;
        mMainActivity = mainActivity;
    }

    @Override
    public SongItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_card, parent, false);
        return new SongItemViewHolder(itemView, mMainActivity);
    }

    @Override
    public void onBindViewHolder(SongItemViewHolder holder, int position) {
        SongItem item = mSongs.get(position);
        holder.vSongName.setText(item.getName());
        holder.vSongArtist.setText(item.getAuthor());
        holder.vSongKey.setText(item.getSetKey());
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public void add(SongItem item) {
        mSongs.add(item);
        notifyItemInserted(mSongs.indexOf(item));
        sort();
        notifyDataSetChanged();
    }

    public void remove(SongItem item) {
        int position = mSongs.indexOf(item);
        mSongs.remove(position);
        notifyItemRemoved(position);
    }

    public SongItem get(String setName) {
        SongItem ret= null;

        // Loop through the list to find the specified set
        for (SongItem s : mSongs) {
            if (s.getName().equals(setName)) {
                ret = s;
                break;
            }
        }

        return ret;
    }

    public void clear() {
        mSongs.clear();
        notifyDataSetChanged();
    }

    public void refill(List<SongItem> newSets) {
        mSongs.clear();
        mSongs.addAll(newSets);
        sort();
        notifyDataSetChanged();
    }

    public void sort(SortType sortType) {
        currentSortType = sortType;
        sort();
    }

    private void sort() {
//        // Sort the array list
//        switch(currentSortType) {
//            case DateRecent: // Date - Recent
//                Collections.sort(mSongs, new SongItem.SetItemComparableDateReverse());
//                break;
//            case DateOldest: // Date - Oldest
//                Collections.sort(mSongs, new SongItem.SetItemComparableDate());
//                break;
//            default:
//            case Title: // Title
//                Collections.sort(mSongs, new Item.ItemComparableName());
//                break;
//        }
//
//        // Notify data set change
//        notifyDataSetChanged();
    }
}
