package com.sbgsoft.songbook.views;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.StaticVars;
import com.sbgsoft.songbook.sets.SetsTab;

/**
 * Created by SamIAm on 5/18/2016.
 */
public class SongItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener , MenuItem.OnMenuItemClickListener {
    protected SongBookThemeTextView vSongName;
    protected SongBookThemeTextView vSongArtist;
    protected SongBookThemeTextView vSongKey;
    protected ImageView vContextMenuButton;
    private MainActivity mMainActivity;

    public SongItemViewHolder(View v, final MainActivity mainActivity) {
        super(v);
        vSongName = (SongBookThemeTextView)v.findViewById(R.id.songs_row_text);
        vSongArtist = (SongBookThemeTextView)v.findViewById(R.id.songs_row_author);
        vSongKey = (SongBookThemeTextView)v.findViewById(R.id.songs_row_key);
        vContextMenuButton = (ImageView)v.findViewById(R.id.song_more_button);

        mMainActivity = mainActivity;

        // Single click listener
        v.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mMainActivity.viewCurrentSet(vSongName.getText().toString());
            }
        });

        // More button listener
        vContextMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mMainActivity.registerForContextMenu(vContextMenuButton);
                //mMainActivity.openContextMenu(vContextMenuButton);
            }
        });

        // Long press context menu
        v.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

//        menu.setHeaderTitle("Sets Menu");
//
//        // Add the menu items
//        MenuItem edit = menu.add(Menu.NONE, StaticVars.EDIT_SET, StaticVars.EDIT_SET, R.string.cmenu_sets_edit);
//        MenuItem editAtt = menu.add(Menu.NONE, StaticVars.EDIT_SET_ATT, StaticVars.EDIT_SET_ATT, R.string.cmenu_sets_edit_att);
//        MenuItem reorder = menu.add(Menu.NONE, StaticVars.REORDER_SET, StaticVars.REORDER_SET, R.string.cmenu_sets_reorder);
//        MenuItem groupAdd = menu.add(Menu.NONE, StaticVars.SET_GROUPS_ADD, StaticVars.SET_GROUPS_ADD, R.string.cmenu_set_group_add);
//        MenuItem groupDel = menu.add(Menu.NONE, StaticVars.SET_GROUPS_DEL, StaticVars.SET_GROUPS_DEL, R.string.cmenu_set_group_delete);
//        MenuItem share = menu.add(Menu.NONE, StaticVars.SHARE_SET, StaticVars.SHARE_SET, R.string.cmenu_sets_share);
//        MenuItem delete = menu.add(Menu.NONE, StaticVars.DELETE_SET, StaticVars.DELETE_SET, R.string.cmenu_sets_delete);
//
//        // Add this as the listener
//        edit.setOnMenuItemClickListener(this);
//        editAtt.setOnMenuItemClickListener(this);
//        reorder.setOnMenuItemClickListener(this);
//        groupAdd.setOnMenuItemClickListener(this);
//        groupDel.setOnMenuItemClickListener(this);
//        share.setOnMenuItemClickListener(this);
//        delete.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
//        String setName = vSongName.getText().toString();
//
//        switch (item.getItemId()) {
//            case StaticVars.EDIT_SET:
//                // Show the dialog to edit songs
//                mMainActivity.updateSetSongs(setName);
//                return true;
//            case StaticVars.EDIT_SET_ATT:
//                // Update the set attributes
//                mMainActivity.editSetAtt(setName);
//                return true;
//            case StaticVars.REORDER_SET:
//                // Trigger reordering of the set
//                mMainActivity.reorderSet(setName);
//                return true;
//            case StaticVars.SET_GROUPS_ADD:
//                // Edit the songs groups
//                mMainActivity.addSetToGroup(setName);
//                return true;
//            case StaticVars.SET_GROUPS_DEL:
//                // Get the current group
//                Spinner s1 = (Spinner)mMainActivity.findViewById(R.id.set_group_spinner);
//                String groupName = s1.getSelectedItem().toString();
//
//                // Remove the song from the group
//                if (!groupName.equals(SetsTab.ALL_SETS_LABEL))
//                    mMainActivity.removeSetFromGroup(setName, groupName);
//                else
//                    Toast.makeText(mMainActivity, "Cannot remove set from " + SetsTab.ALL_SETS_LABEL + " group", Toast.LENGTH_LONG).show();
//                return true;
//            case StaticVars.SHARE_SET:
//                // Email the song
//                mMainActivity.shareSet(setName);
//                return true;
//            case StaticVars.DELETE_SET:
//                // Delete the set
//                mMainActivity.deleteSet(setName);
//                return true;
//
//        }
        return true;
    }
}
