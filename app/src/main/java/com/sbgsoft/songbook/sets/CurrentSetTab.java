package com.sbgsoft.songbook.sets;

import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.db.DBStrings;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;
import com.sbgsoft.songbook.views.CurrentSetItemAdapter;
import com.sbgsoft.songbook.views.SongItemTouchHelperCallback;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class CurrentSetTab extends Fragment {

    private static View mView;
    public static CurrentSetItemAdapter adapter;
    private static FastScrollRecyclerView currentSetRecyclerView;
    private static LinearLayoutManager recyclerViewLayoutManager;
    private static ItemTouchHelper mItemTouchHelper;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view
        mView = inflater.inflate(R.layout.tab_current_set, container, false);

        // Get the song list for the current set
        ArrayList<SongItem> songs = getCurrentSetList();

        // Set up the current set recycler view
        currentSetRecyclerView = (FastScrollRecyclerView)mView.findViewById(R.id.current_list);
        currentSetRecyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(mView.getContext());
        recyclerViewLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        currentSetRecyclerView.setLayoutManager(recyclerViewLayoutManager);
        reColorFastScroll();

        // Specify the adapter for the recycler view
        MainActivity mainActivity = (MainActivity)getActivity();
        adapter = new CurrentSetItemAdapter(songs, mainActivity);
        currentSetRecyclerView.setAdapter(adapter);

        // Setup the title bar
        setTitleBar();

        // Theme setup
        reColorSeparatorBar();

        // Add the item touch helper
        ItemTouchHelper.Callback callback = new SongItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(currentSetRecyclerView);

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

        if (adapter != null) {
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

            // Set the title bar
            setTitleBar();
        }
    }

    public void refillCurrentSetList() {
        refillCurrentSetList(false);
    }

    private void setTitleBar() {
        String setName = MainActivity.dbAdapter.getCurrentSetName();

        // Update the current set title
        if (setName != "") {

            // Append the current set name to the title
            TextView title = ((TextView) mView.findViewById(R.id.current_set_tab_title));
            title.setText(setName);

            // Add the set link
            String setLink = MainActivity.dbAdapter.getSetLink(setName);
            if (setLink != null) {
                TextView link = ((TextView) mView.findViewById(R.id.current_set_tab_link));
                link.setMovementMethod(LinkMovementMethod.getInstance());
                if (setLink.length() > 25)
                    setLink = "<a href=\"" + setLink + "\">" + setLink.substring(0, 25) + "...</a>";
                else
                    setLink = "<a href=\"" + setLink + "\">" + setLink + "</a>";
                link.setText(Html.fromHtml(setLink));
            }
        }
    }
    //endregion

    //region Helper Functions
    public void reColorSeparatorBar() {
        // Get the current theme from the database
        SongBookTheme theme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Color the separator bars
        View setBar = mView.findViewById(R.id.currset_separator_bar);
        setBar.setBackgroundColor(theme.getSeparatorBarColor());
    }

    public void reColorFastScroll() {
        // Get the current theme from the database
        SongBookTheme theme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Theme the fast scroller
        currentSetRecyclerView.setPopupBgColor(theme.getSpinnerFontColor());
        currentSetRecyclerView.setPopupTextColor(theme.getMainFontColor());
        currentSetRecyclerView.setThumbColor(theme.getTitleFontColor());
    }
    //endregion
}
