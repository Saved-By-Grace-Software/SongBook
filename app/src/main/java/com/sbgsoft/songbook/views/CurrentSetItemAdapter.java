package com.sbgsoft.songbook.views;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.StaticVars;
import com.sbgsoft.songbook.sets.SetsTab;

import java.util.Collections;
import java.util.List;

public class CurrentSetItemAdapter extends RecyclerView.Adapter<CurrentSetItemViewHolder>
    implements ItemTouchHelperAdapter {

    private static List<SongItem> mSongs;
    private static MainActivity mMainActivity;

    public CurrentSetItemAdapter(List<SongItem> sets, MainActivity mainActivity) {
        mSongs = sets;
        mMainActivity = mainActivity;
    }

    @Override
    public CurrentSetItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_card, parent, false);
        return new CurrentSetItemViewHolder(itemView, mMainActivity);
    }

    @Override
    public void onBindViewHolder(CurrentSetItemViewHolder holder, int position) {
        SongItem item = mSongs.get(position);
        holder.vSongName.setText(item.getName());
        holder.vSongArtist.setText(item.getAuthor());
        holder.vSongKey.setText(item.getSetKey());
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    @Override
    public void onItemDismiss(int position) {
        Log.d("SONGBOOK", "item dismissed");
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // Swap the items in the list
        Collections.swap(mSongs, fromPosition, toPosition);

        // Create the string array to switch the songs in the database
        String[] newOrder = new String[mSongs.size()];
        for (int i = 0; i < mSongs.size(); i++) {
            newOrder[i] = mSongs.get(i).getName();
        }

        // Get the current set name
        String setName = MainActivity.dbAdapter.getCurrentSetName();

        // Try to reorder the set
        if(!MainActivity.dbAdapter.reorderSet(setName, newOrder)) {
            Toast.makeText(mMainActivity, "Could not update set order!", Toast.LENGTH_LONG).show();
        } else {
            // Refresh set list so song order is correct and notify android of change
            //((SetsTab)mMainActivity.setsFragment).refillSetsList();
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    public void add(SongItem item) {
        mSongs.add(item);
        notifyItemInserted(mSongs.indexOf(item));
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

    public void refill(List<SongItem> newSongs) {
        mSongs.clear();
        mSongs.addAll(newSongs);
        notifyDataSetChanged();
    }
}
