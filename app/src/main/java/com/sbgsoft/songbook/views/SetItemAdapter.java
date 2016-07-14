package com.sbgsoft.songbook.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.Item;
import com.sbgsoft.songbook.items.SetItem;
import com.sbgsoft.songbook.main.MainActivity;

import java.util.Collections;
import java.util.List;

public class SetItemAdapter extends RecyclerView.Adapter<SetItemViewHolder> {

    public enum SortType {
        DateRecent,
        DateOldest,
        Title
    }

    private static List<SetItem> mSets;
    private static MainActivity mMainActivity;
    private static SortType currentSortType;

    public SetItemAdapter(List<SetItem> sets, MainActivity mainActivity) {
        mSets = sets;
        mMainActivity = mainActivity;
    }

    @Override
    public SetItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sets_card, parent, false);
        return new SetItemViewHolder(itemView, mMainActivity);
    }

    @Override
    public void onBindViewHolder(SetItemViewHolder holder, int position) {
        SetItem item = mSets.get(position);
        holder.vSetName.setText(item.getName());
        holder.vSetDate.setText(item.getDate());
        holder.vSetSongs.setText(item.getSongs());
    }

    @Override
    public int getItemCount() {
        return mSets.size();
    }

    public void add(SetItem item) {
        mSets.add(item);
        notifyItemInserted(mSets.indexOf(item));
        sort();
        notifyDataSetChanged();
    }

    public void remove(SetItem item) {
        int position = mSets.indexOf(item);
        mSets.remove(position);
        notifyItemRemoved(position);
    }

    public SetItem get(String setName) {
        SetItem ret= null;

        // Loop through the list to find the specified set
        for (SetItem s : mSets) {
            if (s.getName().equals(setName)) {
                ret = s;
                break;
            }
        }

        return ret;
    }

    public void clear() {
        mSets.clear();
        notifyDataSetChanged();
    }

    public void refill(List<SetItem> newSets) {
        mSets.clear();
        mSets.addAll(newSets);
        sort();
        notifyDataSetChanged();
    }

    public void sort(SortType sortType) {
        currentSortType = sortType;
        sort();
    }

    private void sort() {
        // Sort the array list
        switch(currentSortType) {
            case DateRecent: // Date - Recent
                Collections.sort(mSets, new SetItem.SetItemComparableDateReverse());
                break;
            case DateOldest: // Date - Oldest
                Collections.sort(mSets, new SetItem.SetItemComparableDate());
                break;
            default:
            case Title: // Title
                Collections.sort(mSets, new Item.ItemComparableName());
                break;
        }

        // Notify data set change
        notifyDataSetChanged();
    }
}