package com.sbgsoft.songbook.songs;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.futuremind.recyclerviewfastscroll.RecyclerViewScrollListener;
import com.futuremind.recyclerviewfastscroll.Utils;
import com.futuremind.recyclerviewfastscroll.viewprovider.DefaultBubbleBehavior;
import com.futuremind.recyclerviewfastscroll.viewprovider.ScrollerViewProvider;
import com.futuremind.recyclerviewfastscroll.viewprovider.ViewBehavior;
import com.futuremind.recyclerviewfastscroll.viewprovider.VisibilityAnimationManager;
import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.db.DBStrings;
import com.sbgsoft.songbook.items.Item;
import com.sbgsoft.songbook.items.SectionItem;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.items.SongSearchCriteria;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;
import com.sbgsoft.songbook.main.StaticVars;
import com.sbgsoft.songbook.views.ItemAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class SongsTab extends Fragment {
	public static final String ALL_SONGS_LABEL = "All Songs";
    private static String currentSongGroup = ALL_SONGS_LABEL;

    private static View mView;
    private static ItemAdapter adapter;
    private static RecyclerView songsRecyclerView;
    private static FastScroller songsFastScroller;
    private static LinearLayoutManager recyclerViewLayoutManager;
    private static int currentSongGroupSpinnerPosition = 0;

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
        songsFastScroller = (FastScroller) mView.findViewById(R.id.fastscroll);
        recyclerViewLayoutManager = new LinearLayoutManager(mView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        songsRecyclerView.setLayoutManager(recyclerViewLayoutManager);

        songsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0)
                    songsFastScroller.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("SONGBOOK", "Done Scrolling!");
                }
            }
        });

        // Specify the adapter for the recycler view
        MainActivity mainActivity = (MainActivity)getActivity();
        adapter = new ItemAdapter(songs, mainActivity);
        songsRecyclerView.setAdapter(adapter);
        songsFastScroller.setRecyclerView(songsRecyclerView);
        songsFastScroller.setBubbleColor(Color.TRANSPARENT);
        songsFastScroller.setVisibility(View.INVISIBLE);

        songsFastScroller.setViewProvider(new ScrollerViewProvider() {
            private TextView bubble;
            private View handle;

            @Override
            public View provideHandleView(ViewGroup container) {
                handle = new View(getContext());
                int dimen = getContext().getResources().getDimensionPixelSize(R.dimen.custom_handle_size);
                handle.setLayoutParams(new ViewGroup.LayoutParams(dimen, dimen));
                Utils.setBackground(handle, drawCircle(dimen, dimen, ContextCompat.getColor(getContext(), R.color.custom_handle_color)));
                handle.setVisibility(View.INVISIBLE);
                return handle;
            }

            @Override
            public View provideBubbleView(ViewGroup container) {
                bubble = new TextView(getContext());
                int dimen = getContext().getResources().getDimensionPixelSize(R.dimen.custom_bubble_size);
                bubble.setLayoutParams(new ViewGroup.LayoutParams(dimen, dimen));
                Utils.setBackground(bubble, drawCircle(dimen, dimen, ContextCompat.getColor(getContext(), R.color.custom_bubble_color)));
                bubble.setVisibility(View.INVISIBLE);
                bubble.setGravity(Gravity.CENTER);
                bubble.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                getScroller().addScrollerListener(new RecyclerViewScrollListener.ScrollerListener() {
                    @Override
                    public void onScroll(float relativePos) {
                        //Yeah, yeah, but we were so preoccupied with whether or not we could,
                        //that we didn't stop to think if we should.
                        bubble.setRotation(relativePos*360f);
                    }
                });
                return bubble;
            }

            @Override
            public TextView provideBubbleTextView() {
                return bubble;
            }

            @Override
            public int getBubbleOffset() {
                return (int) (getScroller().isVertical() ? (float)handle.getHeight()/2f-(float)bubble.getHeight()/2f : (float)handle.getWidth()/2f-(float)bubble.getWidth()/2);
            }

            @Override
            protected ViewBehavior provideHandleBehavior() {
                return new CustomHandleBehavior(
                        new VisibilityAnimationManager.Builder(handle)
                                .withHideDelay(2000)
                                .build(),
                        new CustomHandleBehavior.HandleAnimationManager.Builder(handle)
                                .withGrabAnimator(R.animator.custom_grab)
                                .withReleaseAnimator(R.animator.custom_release)
                                .build()
                );
            }

            @Override
            protected ViewBehavior provideBubbleBehavior() {
                return new DefaultBubbleBehavior(new VisibilityAnimationManager.Builder(bubble).withHideDelay(0).build());
            }

            private static ShapeDrawable drawCircle (int width, int height, int color) {
                ShapeDrawable oval = new ShapeDrawable (new OvalShape());
                oval.setIntrinsicHeight(height);
                oval.setIntrinsicWidth(width);
                oval.getPaint().setColor(color);
                return oval;
            }

            @Override
            public void onScrollFinished(){
                if(getHandleBehavior()!=null) getHandleBehavior().onScrollFinished();
                if(getBubbleBehavior()!=null) getBubbleBehavior().onScrollFinished();
            }
        });

        songsFastScroller.addScrollerListener(new RecyclerViewScrollListener.ScrollerListener() {
            @Override
            public void onScroll(float relativePos) {

            }
        });

        // Theme setup
        reColorSeparatorBar();

        // Populate the song spinners
        fillSongSortSpinner();
        fillSongGroupsSpinner(false, 0);

		return mView;
	}
    //endregion

    //region Song List Functions
    public ArrayList<Item> getSongsList(SongSearchCriteria songSearch) {
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

    /**
     * Refills the songs list
     */
    public int refillSongsList() {
        return refillSongsList(false, null);
    }

    /**
     * Refills the songs list
     * @param forceRedraw Determines whether or not to redraw the list
     * @return
     */
    public int refillSongsList(boolean forceRedraw) {
        return refillSongsList(forceRedraw, null);
    }

    /**
     * Refills the songs list with the specified search criteria
     * @param songSearch
     * @return
     */
    public int refillSongsList(SongSearchCriteria songSearch) {
        return refillSongsList(false, songSearch);
    }

    /**
     * Refills the set list
     * @param forceRedraw Determines whether or not to redraw the list
     * @param setSearch The search criteria for filling the list
     * @return
     */
    private int refillSongsList(boolean forceRedraw, SongSearchCriteria songSearch) {
        int ret = 0;

        if (adapter != null) {
            // Get the sets list and refill the adapter
            ArrayList<Item> songs = getSongsList(songSearch);
            adapter.refill(songs);

            // Redraw the list
            if (forceRedraw) {
                songsRecyclerView.setAdapter(null);
                songsRecyclerView.setLayoutManager(null);
                songsRecyclerView.setAdapter(adapter);
                songsRecyclerView.setLayoutManager(recyclerViewLayoutManager);
                adapter.notifyDataSetChanged();
            }

            // Return the number of sets
            ret = songs.size();
        }

        return ret;
    }
    //endregion

    //region Group Spinner Functions
    /**
     * Populates the song groups array list
     */
    public ArrayList<String> getSongGroupsList(boolean showSearchResults) {
        // Query the database
        Cursor c = MainActivity.dbAdapter.getSongGroupNames();

        // Clear the existing groups list
        ArrayList<String> songGroups = new ArrayList<>();

        // Populate the groups
        c.moveToFirst();
        while (!c.isAfterLast()) {
            songGroups.add(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME)));
            c.moveToNext();
        }
        c.close();

        // Sort the list alphabetically
        Collections.sort(songGroups, new MainActivity.SortIgnoreCase());

        // Add search results field
        if (showSearchResults) {
            songGroups.add(0, StaticVars.searchResultsText);
        }

        return songGroups;
    }

    /**
     * Fills the group list spinner
     * @param showSearchResults To show or not to show
     */
    public void fillSongGroupsSpinner(final boolean showSearchResults, final int numSearchResults) {
        fillSongGroupsSpinner(showSearchResults, numSearchResults, false);
    }

    /**
     * Fills the group list spinner
     * @param showSearchResults To show or not to show
     */
    public void fillSongGroupsSpinner(final boolean showSearchResults, final int numSearchResults, boolean stayInCurrentGroup) {
        ArrayAdapter<String> songGroupsAdapter;

        // Get the groups list
        final ArrayList<String> songGroupsList = getSongGroupsList(showSearchResults);

        // Create the spinner adapter
        if (showSearchResults)
            songGroupsAdapter = new SongGroupArrayAdapter(mView.getContext(), songGroupsList, numSearchResults);
        else
            songGroupsAdapter = new SongGroupArrayAdapter(mView.getContext(), songGroupsList);
        final Spinner groupSpinner = (Spinner) mView.findViewById(R.id.song_group_spinner);

        // Set the on click listener for each item
        groupSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
                // Get the selected item and populate the songs list
                String groupName = songGroupsList.get(position);

                // If the selection has actually changed
                if (!currentSongGroup.equals(groupName)) {
                    // Remove the search results option from the spinner
                    if (!groupName.equals(StaticVars.searchResultsText) &&
                            songGroupsList.get(0).equals(StaticVars.searchResultsText))
                        songGroupsList.remove(0);
                }

                // Refill song list (if not on search results)
                currentSongGroup = groupName;
                currentSongGroupSpinnerPosition = position;
                if (groupName != StaticVars.searchResultsText) {
                    refillSongsList();
                }

                // Set the sort by spinner back to default
                ((Spinner) mView.findViewById(R.id.song_sort_spinner)).setSelection(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // Nothing was clicked so ignore it
            }
        });

        // Set the adapter
        groupSpinner.setAdapter(songGroupsAdapter);

        // Set the selected group
        if (stayInCurrentGroup)
            groupSpinner.setSelection(currentSongGroupSpinnerPosition);
    }
    //endregion

    //region Sort Spinner Functions
    /**
     * Fills the song sort spinner
     */
    public void fillSongSortSpinner() {
        // Create the spinner adapter
        ArrayAdapter<String> songSortAdapter = new ArrayAdapter<>(mView.getContext(), R.layout.group_spinner_item, StaticVars.songSortBy);
        songSortAdapter.setDropDownViewResource( R.layout.group_spinner_dropdown_item );
        final Spinner sortSpinner = (Spinner) mView.findViewById(R.id.song_sort_spinner);

        // Set the on click listener for each item
        sortSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
                // Sort the songs and scroll to the top
                sortSongs(position);
                recyclerViewLayoutManager.scrollToPosition(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // Nothing was clicked so ignore it
            }
        });

        // Set the adapter
        sortSpinner.setAdapter(songSortAdapter);
    }

    private void sortSongs(int sortPosition) {
        ItemAdapter.SortType songListCurrentSort = ItemAdapter.SortType.values()[sortPosition];
        adapter.sort(songListCurrentSort);
    }
    //endregion

    //region Helper Functions
    public void reColorSeparatorBar() {
        // Get the current theme from the database
        SongBookTheme theme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Color the separator bars
        View setBar = mView.findViewById(R.id.song_separator_bar);
        setBar.setBackgroundColor(theme.getSeparatorBarColor());
    }
    //endregion
}
