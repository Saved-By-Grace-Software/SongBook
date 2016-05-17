package com.sbgsoft.songbook.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SetItem;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.StaticVars;
import com.sbgsoft.songbook.sets.SetsTab;
import com.sbgsoft.songbook.songs.EditSongRawActivity;
import com.sbgsoft.songbook.songs.SongsTab;

/**
 * Created by SamIAm on 5/4/2016.
 */
public class SetItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener , MenuItem.OnMenuItemClickListener{
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

        // Single click listener
        v.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mMainActivity.showCurrentSet(vSetName.getText().toString());
            }
        });

        // More button listener
        vContextMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainActivity.registerForContextMenu(vContextMenuButton);
                mMainActivity.openContextMenu(vContextMenuButton);
            }
        });

        // Long press context menu
        v.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Sets Menu");
        MenuItem test = menu.add(Menu.NONE, StaticVars.DELETE_SET, StaticVars.DELETE_SET, R.string.cmenu_sets_delete);
        menu.add(Menu.NONE, StaticVars.EDIT_SET, StaticVars.EDIT_SET, R.string.cmenu_sets_edit);
        menu.add(Menu.NONE, StaticVars.EDIT_SET_ATT, StaticVars.EDIT_SET_ATT, R.string.cmenu_sets_edit_att);
        menu.add(Menu.NONE, StaticVars.REORDER_SET, StaticVars.REORDER_SET, R.string.cmenu_sets_reorder);
        menu.add(Menu.NONE, StaticVars.SET_GROUPS_ADD, StaticVars.SET_GROUPS_ADD, R.string.cmenu_set_group_add);
        menu.add(Menu.NONE, StaticVars.SET_GROUPS_DEL, StaticVars.SET_GROUPS_DEL, R.string.cmenu_set_group_delete);
        menu.add(Menu.NONE, StaticVars.SHARE_SET, StaticVars.SHARE_SET, R.string.cmenu_sets_share);

        test.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        String setName = vSetName.getText().toString();

        switch (item.getItemId()) {
            case StaticVars.DELETE_SET:
                // Delete the set
                mMainActivity.deleteSet(setName);
                return true;
            case StaticVars.REORDER_SET:
                // Trigger reordering of the set
                mMainActivity.reorderSet(setName);

                return true;
            case StaticVars.EDIT_SET:
                // Show the dialog to edit songs
                mMainActivity.updateSetSongs(setName);
                return true;
            case StaticVars.EDIT_SET_ATT:
                // Update the set attributes
                mMainActivity.editSetAtt(setName);

                return true;
            case StaticVars.SHARE_SET:
                // Email the song
                //mMainActivity.shareSet(setI);
                return true;
        }
        return true;
    }
}