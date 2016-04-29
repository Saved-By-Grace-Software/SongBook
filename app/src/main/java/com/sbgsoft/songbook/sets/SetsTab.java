package com.sbgsoft.songbook.sets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;

public class SetsTab extends Fragment {
	public static final String ALL_SETS_LABEL = "All Sets";

    private View mView;
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Populate the set tab lists
		MainActivity mainActivity = (MainActivity)getActivity();
		mainActivity.fillSetsListView();
		mainActivity.fillSetGroupsSpinner();
		mainActivity.fillSetSortSpinner();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.tab_sets, container, false);
        reColorSeparatorBar();
		return mView;
	}

    public void reColorSeparatorBar() {
        // Get the current theme from the database
        SongBookTheme theme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Color the separator bars
        View setBar = mView.findViewById(R.id.set_separator_bar);
        setBar.setBackgroundColor(theme.getSeparatorBarColor());
    }
}
