package com.sbgsoft.songbook.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.Item;
import com.sbgsoft.songbook.items.SectionItem;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;

import java.util.List;

/**
 * Created by SamIAm on 5/18/2016.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    private List<Item> mItems;
    private MainActivity mMainActivity;

    public ItemAdapter(List<Item> sets, MainActivity mainActivity) {
        mItems = sets;
        mMainActivity = mainActivity;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_card, parent, false);
        return new ItemViewHolder(itemView, mMainActivity);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Item item = mItems.get(position);

        // Section Item
        if(item.getClass().equals(SectionItem.class)) {
            // Show the section item layout
            holder.vSectionItemLayout.setVisibility(View.VISIBLE);

            // Hide the song item layout
            holder.vSongItemLayout.setVisibility(View.GONE);

            // Set the data
            holder.vSectionName.setText(item.getName());
        }
        // Song Item
        else if(item.getClass().equals(SongItem.class)) {
            // Hide the section item layout
            holder.vSectionItemLayout.setVisibility(View.GONE);

            // Show the song item layout
            holder.vSongItemLayout.setVisibility(View.VISIBLE);

            // Set the data
            holder.vSongName.setText(item.getName());
            holder.vSongArtist.setText(((SongItem)item).getAuthor());
            holder.vSongKey.setText(((SongItem)item).getKey());
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void add(Item item) {
        mItems.add(item);
        notifyItemInserted(mItems.indexOf(item));
    }

    public void remove(Item item) {
        int position = mItems.indexOf(item);
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public Item get(String itemName) {
        Item ret= null;

        // Loop through the list to find the specified set
        for (Item s : mItems) {
            if (s.getName().equals(itemName)) {
                ret = s;
                break;
            }
        }

        return ret;
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void refill(List<Item> newSets) {
        mItems.clear();
        mItems.addAll(newSets);
        notifyDataSetChanged();
    }
}
