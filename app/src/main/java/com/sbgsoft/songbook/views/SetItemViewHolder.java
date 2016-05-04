package com.sbgsoft.songbook.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sbgsoft.songbook.R;

/**
 * Created by SamIAm on 5/4/2016.
 */
public class SetItemViewHolder extends RecyclerView.ViewHolder {
    protected SongBookThemeTextView vSetName;
    protected SongBookThemeTextView vSetDate;

    public SetItemViewHolder(View v) {
        super(v);
        vSetName = (SongBookThemeTextView)v.findViewById(R.id.sets_row_text);
        vSetDate = (SongBookThemeTextView)v.findViewById(R.id.sets_row_date);
    }
}
