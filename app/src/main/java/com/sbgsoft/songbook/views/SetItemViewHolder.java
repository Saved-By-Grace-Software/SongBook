package com.sbgsoft.songbook.views;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;

/**
 * Created by SamIAm on 5/4/2016.
 */
public class SetItemViewHolder extends RecyclerView.ViewHolder {
    protected SongBookThemeTextView vSetName;
    protected SongBookThemeTextView vSetDate;
    protected ImageView vContextMenuButton;
    private MainActivity mMainActivity;

    public SetItemViewHolder(View v, final MainActivity mainActivity) {
        super(v);
        vSetName = (SongBookThemeTextView)v.findViewById(R.id.sets_row_text);
        vSetDate = (SongBookThemeTextView)v.findViewById(R.id.sets_row_date);
        vContextMenuButton = (ImageView)v.findViewById(R.id.set_more_button);

        mMainActivity = mainActivity;

        v.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mMainActivity.showCurrentSet(vSetName.getText().toString());
            }
        });

        vContextMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mainActivity, "Long Press", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
