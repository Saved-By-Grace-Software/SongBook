package com.sbgsoft.songbook.sets;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.db.DBStrings;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;
import com.sbgsoft.songbook.views.SetItemAdapter;
import com.sbgsoft.songbook.views.SongItemAdapter;

import java.util.ArrayList;

public class CurrentSetTab extends Fragment {

    private View mView;
    public SongItemAdapter adapter;
    private RecyclerView currentSetRecyclerView;
    private LinearLayoutManager recyclerViewLayoutManager;
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.tab_current_set, container, false);

        // Get the song list for the current set
        ArrayList<SongItem> songs = getCurrentSetList();

        // Set up the current set recycler view
        currentSetRecyclerView = (RecyclerView)mView.findViewById(R.id.current_list);
        currentSetRecyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(mView.getContext());
        recyclerViewLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        currentSetRecyclerView.setLayoutManager(recyclerViewLayoutManager);

        // Specify the adapter for the recycler view
        MainActivity mainActivity = (MainActivity)getActivity();
        adapter = new SongItemAdapter(songs, mainActivity);
        currentSetRecyclerView.setAdapter(adapter);

        // Theme setup
        reColorSeparatorBar();

		return mView;
	}

    //region Current Set List Functions
    public ArrayList<SongItem> getCurrentSetList() {
        Cursor c = MainActivity.dbAdapter.getCurrentSetSongs();
        c.moveToFirst();

        // Clear the ArrayList
        ArrayList<SongItem> songs = new ArrayList<>();

        // Populate the ArrayList
        while (!c.isAfterLast()) {
            // Create the song item
            SongItem songItem = new SongItem();

            // Set the song item values
            songItem.setName(c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME)));
            songItem.setAuthor(c.getString(c.getColumnIndex(DBStrings.TBLSONG_AUTHOR)));
            songItem.setSetKey(c.getString(c.getColumnIndex(DBStrings.TBLSLOOKUP_KEY)));
            songItem.setFile(c.getString(c.getColumnIndex(DBStrings.TBLSONG_FILE)));
            songItem.setBpm(c.getInt(c.getColumnIndex(DBStrings.TBLSONG_BPM)));
            songItem.setTimeSignature(c.getString(c.getColumnIndex(DBStrings.TBLSONG_TIME)));
            songItem.setKey(MainActivity.dbAdapter.getSongKey(c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME))));
            songItem.setSongLink(c.getString(c.getColumnIndex(DBStrings.TBLSONG_LINK)));

            // Add the song item
            songs.add(songItem);

            // Move to the next song
            c.moveToNext();
        }
        c.close();

        return songs;
    }

    public void refillCurrentSetList(boolean forceRedraw) {
        // Get the list and refill the adapter
        ArrayList<SongItem> songs = getCurrentSetList();
        adapter.refill(songs);

        // Redraw the list
        if (forceRedraw) {
            currentSetRecyclerView.setAdapter(null);
            currentSetRecyclerView.setLayoutManager(null);
            currentSetRecyclerView.setAdapter(adapter);
            currentSetRecyclerView.setLayoutManager(recyclerViewLayoutManager);
            adapter.notifyDataSetChanged();
        }
    }

    public void refillCurrentSetList() {
        refillCurrentSetList(false);
    }
    //endregion

    public void reColorSeparatorBar() {
        // Get the current theme from the database
        SongBookTheme theme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Color the separator bars
        View setBar = mView.findViewById(R.id.currset_separator_bar);
        setBar.setBackgroundColor(theme.getSeparatorBarColor());
    }
}
