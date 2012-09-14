package com.sbgsoft.tabapp.sets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.main.MainActivity;

public class SetsTab extends Fragment {
	public static final String ALL_SETS_LABEL = "All Sets";
	
	private MainActivity mainActivity;
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		refreshSongsList(ALL_SETS_LABEL);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tab_sets, container, false);
		return view;
	}
	
	/**
	 * Refreshes set list
	 */
	public void refreshSongsList(String groupName) {
		mainActivity = (MainActivity)getActivity();
		mainActivity.fillSetsList(getView(), groupName);
		mainActivity.fillSetGroupsList(getView());
	}
}
