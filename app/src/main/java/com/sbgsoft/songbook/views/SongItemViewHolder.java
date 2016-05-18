package com.sbgsoft.songbook.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.StaticVars;
import com.sbgsoft.songbook.sets.SetsTab;
import com.sbgsoft.songbook.songs.EditSongRawActivity;

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
                mMainActivity.registerForContextMenu(vContextMenuButton);
                mMainActivity.openContextMenu(vContextMenuButton);
            }
        });

        // Long press context menu
        v.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Current Set Menu");

        MenuItem edit = menu.add(Menu.NONE, StaticVars.EDIT_SONG_CS, StaticVars.EDIT_SONG_CS, R.string.cmenu_songs_edit);
        MenuItem editAtt = menu.add(Menu.NONE, StaticVars.EDIT_SONG_ATT_CS, StaticVars.EDIT_SONG_ATT_CS, R.string.cmenu_songs_edit_att);
        MenuItem setSongKeySet = menu.add(Menu.NONE, StaticVars.SET_SONG_KEY_CS, StaticVars.SET_SONG_KEY_CS, R.string.cmenu_sets_set_song_key);
        MenuItem share = menu.add(Menu.NONE, StaticVars.SHARE_SONG_CS, StaticVars.SHARE_SONG_CS, R.string.cmenu_songs_share);
        MenuItem removeFromSet = menu.add(Menu.NONE, StaticVars.REMOVE_SONG_FROM_SET, StaticVars.REMOVE_SONG_FROM_SET, R.string.cmenu_sets_remove_song);
        MenuItem songStats = menu.add(Menu.NONE, StaticVars.SONG_STATS_CS, StaticVars.SONG_STATS_CS, R.string.cmenu_songs_stats);

        // Add this as the listener
        edit.setOnMenuItemClickListener(this);
        editAtt.setOnMenuItemClickListener(this);
        setSongKeySet.setOnMenuItemClickListener(this);
        share.setOnMenuItemClickListener(this);
        removeFromSet.setOnMenuItemClickListener(this);
        songStats.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        String songName = vSongName.getText().toString();

        switch (item.getItemId()) {
            case StaticVars.EDIT_SONG_CS:
                // Get the song file name
                String songFileName = MainActivity.dbAdapter.getSongFile(songName);

                // Create the edit activity intent
                Intent i = new Intent(mMainActivity, EditSongRawActivity.class);
                i.putExtra(StaticVars.SONG_NAME_KEY, songName);
                i.putExtra(StaticVars.SONG_FILE_KEY, songFileName);

                // Start the activity
                mMainActivity.startActivity(i);
                return true;
            case StaticVars.EDIT_SONG_ATT_CS:
//                // Get the song name
//                songName = currSetList.get(info.position).getName();
//
//                // Show the edit dialog
//                editSongAtt(songName);

                return true;
            case StaticVars.SHARE_SONG_CS:
//                // Get the song name
//                songI = (SongItem)currSetList.get(info.position);
//
//                // Email the song
//                shareSong(songI);

                return true;
            case StaticVars.SET_SONG_KEY_CS:
//                songI = (SongItem)currSetList.get(info.position);
//                setName = dbAdapter.getCurrentSetName();
//
//                setSongKeyForSet(setName, songI);

                return true;
            case StaticVars.REMOVE_SONG_FROM_SET:
//                // Get the set name and song name
//                songName = currSetList.get(info.position).getName();
//                setName = dbAdapter.getCurrentSetName();
//                final String fsongName = songName;
//                final String fsetName = setName;
//                final int fsongOrder = info.position;
//
//                alert = new AlertDialog.Builder(this);
//
//                alert.setTitle("Remove Song?!");
//                alert.setMessage("Are you sure you want to remove '" + songName + "' from the set '" + setName + "'?");
//
//                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        // Remove the song from the set
//                        dbAdapter.removeSongFromSet(fsetName, fsongName, fsongOrder);
//
//                        // Refresh the current set list
//                        fillCurrentSetListView();
//                    }
//                });
//
//                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        //Do nothing
//                    }
//                });
//
//                alert.show();

                return true;
            case StaticVars.SONG_STATS_CS:
                // Show the song stats dialog
                mMainActivity.showSongStats(songName);
                return true;
        }
        return true;
    }
}
