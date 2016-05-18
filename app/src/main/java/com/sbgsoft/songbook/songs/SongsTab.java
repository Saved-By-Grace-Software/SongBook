package com.sbgsoft.songbook.songs;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.db.DBStrings;
import com.sbgsoft.songbook.items.Item;
import com.sbgsoft.songbook.items.SectionItem;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.items.SongSearchCriteria;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;
import com.sbgsoft.songbook.views.ItemAdapter;
import com.sbgsoft.songbook.views.SongItemAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class SongsTab extends Fragment {
	public static final String ALL_SONGS_LABEL = "All Songs";
    private static String currentSongGroup = ALL_SONGS_LABEL;

    private View mView;
    private ItemAdapter adapter;
    private RecyclerView songsRecyclerView;
    private LinearLayoutManager recyclerViewLayoutManager;

    //region Fragment Functions
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.tab_songs, container, false);

        // Get the songs list
        ArrayList<Item> songs = getSongsList(null);

        // Set up the songs recycler view
        songsRecyclerView = (RecyclerView)mView.findViewById(R.id.songs_list);
        songsRecyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(mView.getContext());
        recyclerViewLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        songsRecyclerView.setLayoutManager(recyclerViewLayoutManager);

        // Specify the adapter for the recycler view
        MainActivity mainActivity = (MainActivity)getActivity();
        adapter = new ItemAdapter(songs, mainActivity);
        songsRecyclerView.setAdapter(adapter);

        // Theme setup
        reColorSeparatorBar();

        // Populate the song spinners

        // Set default sort

		return mView;
	}
    //endregion

    //region Song List Functions
    private ArrayList<Item> getSongsList(SongSearchCriteria songSearch) {
        ArrayList<Item> temp = new ArrayList<>();
        Cursor c;

        // Determine if we are searching or using the song group
        if (songSearch == null)
            c = MainActivity.dbAdapter.getSongs(currentSongGroup);
        else
            c = MainActivity.dbAdapter.getSongsSearch(songSearch);

        // Move to the first and get the count
        c.moveToFirst();

        // Display error message for searching
        if (c.getCount() <= 0 && songSearch != null)
            Toast.makeText(mView.getContext(), "No songs match that search", Toast.LENGTH_LONG).show();

        // Populate the ArrayList
        while (!c.isAfterLast()) {
            // Create the song item
            SongItem songItem = new SongItem();

            // Set the song item values
            songItem.setName(c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME)));
            songItem.setAuthor(c.getString(c.getColumnIndex(DBStrings.TBLSONG_AUTHOR)));
            songItem.setKey(c.getString(c.getColumnIndex(DBStrings.TBLSONG_KEY)));
            songItem.setFile(c.getString(c.getColumnIndex(DBStrings.TBLSONG_FILE)));
            songItem.setBpm(c.getInt(c.getColumnIndex(DBStrings.TBLSONG_BPM)));
            songItem.setTimeSignature(c.getString(c.getColumnIndex(DBStrings.TBLSONG_TIME)));
            songItem.setSongLink(c.getString(c.getColumnIndex(DBStrings.TBLSONG_LINK)));

            // Add the song item
            temp.add(songItem);

            // Move to the next song
            c.moveToNext();
        }

        c.close();

        // Create the song list
        ArrayList<Item> songsList = new ArrayList<>();

        // Add section headers
        for (int i = 0; i < temp.size(); i++) {
            if (i != 0) {
                if (Character.toLowerCase(temp.get(i).getName().charAt(0)) !=
                        Character.toLowerCase(temp.get(i-1).getName().charAt(0))) {
                    // This is the first item with that letter, add the separator
                    songsList.add(new SectionItem(temp.get(i).getName().substring(0, 1).toUpperCase(Locale.US)));
                }
            }
            else {
                // First item, add section
                songsList.add(new SectionItem(temp.get(i).getName().substring(0, 1).toUpperCase(Locale.US)));
            }

            songsList.add(temp.get(i));
        }

        return songsList;
    }
    //endregion

    public void reColorSeparatorBar() {
        // Get the current theme from the database
        SongBookTheme theme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Color the separator bars
        View setBar = mView.findViewById(R.id.song_separator_bar);
        setBar.setBackgroundColor(theme.getSeparatorBarColor());
    }
}
