package com.sbgsoft.songbook.sets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;

public class SetsTab extends Fragment {
	public static final String ALL_SETS_LABEL = "All Sets";
	
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
		View view = inflater.inflate(R.layout.tab_sets, container, false);
		return view;
	}
}
