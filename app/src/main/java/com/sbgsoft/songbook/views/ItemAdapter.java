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
import com.sbgsoft.songbook.main.SongBookTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by SamIAm on 5/18/2016.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    public enum SortType {
        SongTitle,
        SongAuthor,
        SongKey
    }

    private ArrayList<Item> mItems;
    private MainActivity mMainActivity;
    private SortType currentSortType;

    public ItemAdapter(ArrayList<Item> items, MainActivity mainActivity) {
        mItems = items;
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
            holder.vSectionItemCardView.setVisibility(View.VISIBLE);

            // Hide the song item layout
            holder.vSongItemCardView.setVisibility(View.GONE);

            // Set the data
            holder.vSectionName.setText(item.getName());

            // Set the background
            SongBookTheme theme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();
            holder.setCardBackground(theme.getSectionHeaderColor());
        }
        // Song Item
        else if(item.getClass().equals(SongItem.class)) {
            // Hide the section item layout
            holder.vSectionItemCardView.setVisibility(View.GONE);

            // Show the song item layout
            holder.vSongItemCardView.setVisibility(View.VISIBLE);

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
        sort();
        notifyDataSetChanged();
    }

    public void remove(Item item) {
        int position = mItems.indexOf(item);
        mItems.remove(position);
        notifyItemRemoved(position);
        sort();
        notifyDataSetChanged();
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
        sort();
        notifyDataSetChanged();
    }

    //region Sort Functions
    public void sort(SortType sortType) {
        currentSortType = sortType;
        sort();
    }

    /**
     * Sorts the song list by the selected item
     * @param sortByPosition The position in the song sort array list
     */
    private void sort() {
        // Remove section items
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).getClass().equals(SectionItem.class)) {
                mItems.remove(i);
            }
        }

        // Sort the array list
        switch(currentSortType) {
            case SongTitle: //Title
                // Sort the list
                Collections.sort(mItems, new Item.ItemComparableName());

                // Add the title section headers
                addTitleSectionHeaders(mItems);
                notifyDataSetChanged();
                break;
            case SongAuthor: //Author
                // Sort the list
                Collections.sort(mItems, new SongItem.SongItemComparableAuthor());

                // Add the author section headers
                addAuthorSectionHeaders(mItems);
                notifyDataSetChanged();
                break;
            case SongKey: //Key
                // Sort the list
                Collections.sort(mItems, new SongItem.SongItemComparableKey());

                // Add the key section headers
                addKeySectionHeaders(mItems);
                notifyDataSetChanged();
                break;
        }
    }
    //endregion

    //region Sort Helper Functions
    private void addTitleSectionHeaders(ArrayList<Item> items) {
        if(items.size() > 0) {
            // First item, add section
            items.add(0, new SectionItem(items.get(0).getName().substring(0, 1).toUpperCase(Locale.US)));

            for (int i = 2; i < items.size(); i++) {
                if (Character.toLowerCase(items.get(i).getName().charAt(0)) !=
                        Character.toLowerCase(items.get(i - 1).getName().charAt(0))) {
                    // This is the first item with that letter, add the separator
                    items.add(i, new SectionItem(items.get(i).getName().substring(0, 1).toUpperCase(Locale.US)));
                    i++;
                }
            }
        }
    }

    private void addAuthorSectionHeaders(ArrayList<Item> items) {
        if(items.size() > 0) {
            // First item, add section
            items.add(0, new SectionItem(((SongItem) items.get(0)).getAuthor().substring(0, 1).toUpperCase(Locale.US)));

            // Add the rest of the sections
            for (int i = 2; i < items.size(); i++) {
                if (Character.toLowerCase(((SongItem) items.get(i)).getAuthor().charAt(0)) !=
                        Character.toLowerCase(((SongItem) items.get(i - 1)).getAuthor().charAt(0))) {
                    // This is the first item with that letter, add the separator
                    items.add(i, new SectionItem(((SongItem) items.get(i)).getAuthor().substring(0, 1).toUpperCase(Locale.US)));
                    i++;
                }
            }
        }
    }

    private void addKeySectionHeaders(ArrayList<Item> items) {
        if(items.size() > 0) {
            // First item, add section
            items.add(0, new SectionItem(((SongItem) items.get(0)).getKey()));

            // Add the rest of the sections
            for (int i = 2; i < items.size(); i++) {
                if (!((SongItem) items.get(i)).getKey().equals(((SongItem) items.get(i - 1)).getKey())) {
                    // This is the first item with that key, add the separator
                    items.add(i, new SectionItem(((SongItem) items.get(i)).getKey()));
                    i++;
                }
            }
        }
    }
    //endregion
}
