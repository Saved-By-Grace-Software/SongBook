package com.sbgsoft.songbook.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;

/**
 * Created by SamIAm on 5/4/2016.
 */
public class SetItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected SongBookThemeTextView vSetName;
    protected SongBookThemeTextView vSetDate;
    private MainActivity mMainActivity;

    public SetItemViewHolder(View v, MainActivity mainActivity) {
        super(v);
        vSetName = (SongBookThemeTextView)v.findViewById(R.id.sets_row_text);
        vSetDate = (SongBookThemeTextView)v.findViewById(R.id.sets_row_date);
        mMainActivity = mainActivity;
    }

    @Override
    public void onClick(View view) {
        mMainActivity.showCurrentSet(vSetName.getText().toString());
    }
}
