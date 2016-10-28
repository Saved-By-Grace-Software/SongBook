package com.sbgsoft.songbook.sets;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.db.DBStrings;
import com.sbgsoft.songbook.items.Item;
import com.sbgsoft.songbook.items.SectionItem;
import com.sbgsoft.songbook.items.SetItem;
import com.sbgsoft.songbook.items.SetSearchCriteria;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;
import com.sbgsoft.songbook.main.StaticVars;
import com.sbgsoft.songbook.views.SetItemAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class SetsTab extends Fragment {
	public static final String ALL_SETS_LABEL = "All Sets";
    private static String currentSetGroup = ALL_SETS_LABEL;

    public static SetItemAdapter adapter;
    private static View mView;
    private static FastScrollRecyclerView setsRecyclerView;
    private static LinearLayoutManager recyclerViewLayoutManager;
    private static int currentSetGroupSpinnerPosition = 0;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.tab_sets, container, false);

        // Get the set list
        ArrayList<SetItem> sets = getSetsList(null);

        // Set up the sets recycler view
        setsRecyclerView = (FastScrollRecyclerView)mView.findViewById(R.id.sets_list);
        setsRecyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(mView.getContext());
        recyclerViewLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setsRecyclerView.setLayoutManager(recyclerViewLayoutManager);

        // Theme the fast scroller
        SongBookTheme theme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();
        setsRecyclerView.setPopupBgColor(theme.getSpinnerFontColor());
        setsRecyclerView.setPopupTextColor(theme.getMainFontColor());
        setsRecyclerView.setThumbColor(theme.getTitleFontColor());

        // Specify the adapter for the recycler view
        MainActivity mainActivity = (MainActivity)getActivity();
        adapter = new SetItemAdapter(sets, mainActivity);
        setsRecyclerView.setAdapter(adapter);

        // Theme setup
        reColorSeparatorBar();

        // Populate the set spinners
        fillSetGroupsSpinner(false, 0);
        fillSetSortSpinner();

        // Set default sort
        sortSets(0);

		return mView;
	}

    //region Helper Functions
    public void reColorSeparatorBar() {
        // Get the current theme from the database
        SongBookTheme theme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Color the separator bars
        View setBar = mView.findViewById(R.id.set_separator_bar);
        setBar.setBackgroundColor(theme.getSeparatorBarColor());
    }
    //endregion

    //region Set List Functions
    /**
     * Sets the sets array list
     */
    private ArrayList<SetItem> getSetsList(SetSearchCriteria setSearch) {
        Cursor c;

        // Determine if we are searching or using the song group
        if (setSearch == null)
            c = MainActivity.dbAdapter.getSets(currentSetGroup);
        else
            c = MainActivity.dbAdapter.getSetsSearch(setSearch);

        c.moveToFirst();

        // Clear the ArrayList
        ArrayList<SetItem> sets = new ArrayList<>();

        // Display error message for searching
        if (c.getCount() <= 0 && setSearch != null)
            Toast.makeText(mView.getContext(), "No sets match that search", Toast.LENGTH_LONG).show();

        // Populate the ArrayList
        while (!c.isAfterLast()) {
            // Get the strings from the cursor
            String setName = c.getString(c.getColumnIndex(DBStrings.TBLSETS_NAME));
            String setLink = c.getString(c.getColumnIndex(DBStrings.TBLSETS_LINK));
            String setDate = c.getString(c.getColumnIndex(DBStrings.TBLSETS_DATE)).replaceAll("\\s", "");
            String[] datesplit = setDate.split("-");
            setDate = datesplit[1] + "/" + datesplit[2] + "/" + datesplit[0];

            // Create a new set item
            SetItem tmp = new SetItem(setName, setDate, setLink);
            tmp.selfPopulateSongsList();

            // Add the set item
            sets.add(tmp);

            // Move to the next song
            c.moveToNext();
        }
        c.close();

        return sets;
    }

    /**
     * Refills the sets list
     */
    public int refillSetsList() {
        return refillSetsList(false, null);
    }

    /**
     * Refills the sets list
     * @param forceRedraw Determines whether or not to redraw the list
     * @return
     */
    public int refillSetsList(boolean forceRedraw) {
        return refillSetsList(forceRedraw, null);
    }

    /**
     * Refills the sets list with the specified search criteria
     * @param setSearch
     * @return
     */
    public int refillSetsList(SetSearchCriteria setSearch) {
        return refillSetsList(false, setSearch);
    }

    /**
     * Refills the set list
     * @param forceRedraw Determines whether or not to redraw the list
     * @param setSearch The search criteria for filling the list
     * @return
     */
    private int refillSetsList(boolean forceRedraw, SetSearchCriteria setSearch) {
        int ret = 0;

        if (adapter != null) {
            // Get the sets list and refill the adapter
            ArrayList<SetItem> sets = getSetsList(setSearch);
            adapter.refill(sets);

            // Redraw the list
            if (forceRedraw) {
                setsRecyclerView.setAdapter(null);
                setsRecyclerView.setLayoutManager(null);
                setsRecyclerView.setAdapter(adapter);
                setsRecyclerView.setLayoutManager(recyclerViewLayoutManager);
                adapter.notifyDataSetChanged();
            }

            // Return the number of sets
            ret = sets.size();
        }

        return ret;
    }

    /**
     * Gets the set item by its name
     * @param setName
     * @return
     */
    public SetItem getSetItem(String setName) {
        return adapter.get(setName);
    }
    //endregion

    //region Group Spinner Functions
    /**
     * Sets the set group array list
     */
    public ArrayList<String> getSetGroupsList(boolean showSearchResults) {
        // Query the database
        Cursor c = MainActivity.dbAdapter.getSetGroupNames();

        // Clear the existing groups list
        ArrayList<String> setGroups = new ArrayList<>();

        // Populate the groups
        c.moveToFirst();
        while (!c.isAfterLast()) {
            setGroups.add(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME)));
            c.moveToNext();
        }
        c.close();

        // Sort the list alphabetically
        Collections.sort(setGroups, new MainActivity.SortIgnoreCase());

        // Add search results field
        if (showSearchResults) {
            setGroups.add(0, StaticVars.searchResultsText);
        }

        return setGroups;
    }

    /**
     * Fills the group list spinner
     */
    public void fillSetGroupsSpinner(final boolean showSearchResults, final int numSearchResults) {
        fillSetGroupsSpinner(showSearchResults, numSearchResults, false);
    }

    /**
     * Fills the group list spinner
     */
    public void fillSetGroupsSpinner(final boolean showSearchResults, final int numSearchResults, boolean stayInCurrentGroup) {
        ArrayAdapter<String> setGroupsAdapter;

        // Set the groups list
        final ArrayList<String> setGroupsList = getSetGroupsList(showSearchResults);

        // Create the spinner adapter
        if (showSearchResults)
            setGroupsAdapter = new SetGroupArrayAdapter(mView.getContext(), setGroupsList, numSearchResults);
        else
            setGroupsAdapter = new SetGroupArrayAdapter(mView.getContext(), setGroupsList);
        final Spinner groupSpinner = (Spinner) mView.findViewById(R.id.set_group_spinner);

        // Set the on click listener for each item
        groupSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
                // Get the selected item and populate the sets list
                String groupName = setGroupsList.get(position);

                // If the selection has actually changed
                if (!currentSetGroup.equals(groupName)) {
                    // Remove the search results option from the spinner
                    if (!groupName.equals(StaticVars.searchResultsText) &&
                            setGroupsList.get(0).equals(StaticVars.searchResultsText))
                        setGroupsList.remove(0);
                }

                // Refill set list (if not on search results)
                currentSetGroupSpinnerPosition = position;
                currentSetGroup = groupName;
                if (groupName != StaticVars.searchResultsText) {
                    refillSetsList();
                }

                // Set the sort by spinner back to default
                ((Spinner)mView.findViewById(R.id.set_sort_spinner)).setSelection(0);

                // Scroll back to the top
                recyclerViewLayoutManager.scrollToPosition(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // Nothing was clicked so ignore it
            }
        });

        groupSpinner.setAdapter(setGroupsAdapter);

        // Set the selected group
        if (stayInCurrentGroup)
            groupSpinner.setSelection(currentSetGroupSpinnerPosition);
    }
    //endregion

    //region Sort Spinner Functions
    /**
     * Fills the song sort spinner
     */
    public void fillSetSortSpinner() {
        // Create the spinner adapter
        ArrayAdapter<String> setSortAdapter = new ArrayAdapter<>(mView.getContext(), R.layout.group_spinner_item, StaticVars.setSortBy);
        setSortAdapter.setDropDownViewResource( R.layout.group_spinner_dropdown_item );
        final Spinner sortSpinner = (Spinner) mView.findViewById(R.id.set_sort_spinner);

        // Set the on click listener for each item
        sortSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
                // Sort the sets and scroll to the top
                sortSets(position);
                recyclerViewLayoutManager.scrollToPosition(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // Nothing was clicked so ignore it
            }
        });

        // Set the adapter
        sortSpinner.setAdapter(setSortAdapter);
    }

    /**
     * Sorts the set list by the selected item
     */
    private void sortSets(int sortPosition) {
        SetItemAdapter.SortType setsListCurrentSort = SetItemAdapter.SortType.values()[sortPosition];
        adapter.sort(setsListCurrentSort);
    }
    //endregion
}
