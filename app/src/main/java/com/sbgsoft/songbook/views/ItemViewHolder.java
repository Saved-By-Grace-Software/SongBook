package com.sbgsoft.songbook.views;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;
import com.sbgsoft.songbook.main.StaticVars;
import com.sbgsoft.songbook.songs.EditSongRawActivity;
import com.sbgsoft.songbook.songs.SongsTab;

/**
 * Created by SamIAm on 5/18/2016.
 */
public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener , MenuItem.OnMenuItemClickListener {
    protected CardView vSongItemCardView;
    protected CardView vSectionItemCardView;
    protected SongBookThemeTextView vSectionName;
    protected SongBookThemeTextView vSongName;
    protected SongBookThemeTextView vSongArtist;
    protected SongBookThemeTextView vSongKey;
    protected ImageView vContextMenuButton;
    private MainActivity mMainActivity;
    private View mView;

    public ItemViewHolder(View v, final MainActivity mainActivity) {
        super(v);
        vSongItemCardView = (CardView)v.findViewById(R.id.song_card_view);
        vSectionItemCardView = (CardView)v.findViewById(R.id.section_card_view);
        vSectionName = (SongBookThemeTextView)v.findViewById(R.id.songs_row_section);
        vSongName = (SongBookThemeTextView)v.findViewById(R.id.songs_row_text);
        vSongArtist = (SongBookThemeTextView)v.findViewById(R.id.songs_row_author);
        vSongKey = (SongBookThemeTextView)v.findViewById(R.id.songs_row_key);
        vContextMenuButton = (ImageView)v.findViewById(R.id.song_more_button);

        mMainActivity = mainActivity;
        mView = v;

        // Single click listener
        vSongItemCardView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mMainActivity.showSong(vSongName.getText().toString());
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
        vSongItemCardView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Song Menu");

        // Add the menu items
        MenuItem edit = menu.add(Menu.NONE, StaticVars.EDIT_SONG, StaticVars.EDIT_SONG, R.string.cmenu_songs_edit);
        MenuItem editAtt = menu.add(Menu.NONE, StaticVars.EDIT_SONG_ATT, StaticVars.EDIT_SONG_ATT, R.string.cmenu_songs_edit_att);
        MenuItem delete = menu.add(Menu.NONE, StaticVars.DELETE_SONG, StaticVars.DELETE_SONG, R.string.cmenu_songs_delete);
        MenuItem addSongSet = menu.add(Menu.NONE, StaticVars.ADD_SONG_SET, StaticVars.ADD_SONG_SET, R.string.cmenu_song_add_set);
        MenuItem addSongCurrSet = menu.add(Menu.NONE, StaticVars.ADD_SONG_CURR_SET, StaticVars.ADD_SONG_CURR_SET, R.string.cmenu_song_add_curr_set);
        MenuItem addSongGroup = menu.add(Menu.NONE, StaticVars.SONG_GROUPS_ADD, StaticVars.SONG_GROUPS_ADD, R.string.cmenu_song_group_add);
        MenuItem remSongGroup = menu.add(Menu.NONE, StaticVars.SONG_GROUPS_DEL, StaticVars.SONG_GROUPS_DEL, R.string.cmenu_song_group_delete);
        MenuItem share = menu.add(Menu.NONE, StaticVars.SHARE_SONG, StaticVars.SHARE_SONG, R.string.cmenu_songs_share);
        MenuItem stats = menu.add(Menu.NONE, StaticVars.SONG_STATS, StaticVars.SONG_STATS, R.string.cmenu_songs_stats);

        // Add this as the listener
        edit.setOnMenuItemClickListener(this);
        editAtt.setOnMenuItemClickListener(this);
        delete.setOnMenuItemClickListener(this);
        addSongSet.setOnMenuItemClickListener(this);
        addSongCurrSet.setOnMenuItemClickListener(this);
        addSongGroup.setOnMenuItemClickListener(this);
        remSongGroup.setOnMenuItemClickListener(this);
        share.setOnMenuItemClickListener(this);
        stats.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        String songName = vSongName.getText().toString();

        switch (item.getItemId()) {
            case StaticVars.EDIT_SONG:
                // Get the song file
                String songFile = MainActivity.dbAdapter.getSongFile(songName);

                // Create the edit activity intent
                Intent i = new Intent(mView.getContext(), EditSongRawActivity.class);
                i.putExtra(StaticVars.SONG_NAME_KEY, songName);
                i.putExtra(StaticVars.SONG_FILE_KEY, songFile);

                // Start the activity
                mMainActivity.startActivity(i);
                return true;

            case StaticVars.EDIT_SONG_ATT:
//                // Get the song name
//                songName = songsList.get(info.position).getName();
//
//                // Show the edit dialog
//                editSongAtt(songName);
//                return true;

            case StaticVars.ADD_SONG_SET:
//                // Get the song name
//                songName = songsList.get(info.position).getName();
//
//                // Edit the songs groups
//                addSongToSet(songName);
//                return true;

            case StaticVars.ADD_SONG_CURR_SET:
//                // Get the song name
//                songName = songsList.get(info.position).getName();
//
//                // Edit the songs groups
//                addSongToCurrentSet(songName);
//                return true;

            case StaticVars.SONG_GROUPS_ADD:
//                // Get the song name
//                songName = songsList.get(info.position).getName();
//
//                // Edit the songs groups
//                addSongToGroup(songName);
                return true;

            case StaticVars.SONG_GROUPS_DEL:
//                // Get the song name
//                songName = songsList.get(info.position).getName();
//
//                // Get the current group
//                Spinner s = (Spinner)findViewById(R.id.song_group_spinner);
//                int position = s.getSelectedItemPosition();
//                groupName = songGroupsList.get(position);
//
//                // Remove the song from the group
//                if (!groupName.equals(SongsTab.ALL_SONGS_LABEL))
//                    removeSongFromGroup(songName, groupName);
//                else
//                    Toast.makeText(getBaseContext(), "Cannot remove song from " + SongsTab.ALL_SONGS_LABEL + " group", Toast.LENGTH_LONG).show();
                return true;

            case StaticVars.SONG_STATS:
//                // Get the song name
//                songName = songsList.get(info.position).getName();
//
//                // Show the song stats dialog
//                showSongStats(songName);
//                return true;

            case StaticVars.SHARE_SONG:
//                // Get the song name
//                songName = songsList.get(info.position).getName();
//
//                // Email the song
//                shareSong(songName);
//                return true;

            case StaticVars.DELETE_SONG:
                // Delete the song
                mMainActivity.deleteSong(songName);
                return true;
        }

        return true;
    }

    public void setCardBackground(int bgColor) {
        CardView section = (CardView)mView.findViewById(R.id.section_card_view);
        if (section != null)
            section.setCardBackgroundColor(bgColor);
    }
}
