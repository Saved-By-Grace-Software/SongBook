package com.sbgsoft.songbook.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SetItem;

import java.util.List;

/**
 * Created by SamIAm on 5/4/2016.
 */
public class SetItemAdapter extends RecyclerView.Adapter<SetItemViewHolder> {

    private List<SetItem> mSets;

    public SetItemAdapter(List<SetItem> sets) {
        mSets = sets;
    }

    @Override
    public SetItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sets_card, parent, false);
        return new SetItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SetItemViewHolder holder, int position) {
        SetItem item = mSets.get(position);
        holder.vSetName.setText(item.getName());
        holder.vSetDate.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return mSets.size();
    }

    public void add(SetItem item) {
        mSets.add(item);
        notifyItemInserted(mSets.indexOf(item));
    }

    public void remove(SetItem item) {
        int position = mSets.indexOf(item);
        mSets.remove(position);
        notifyItemRemoved(position);
    }
}