package com.sbgsoft.tabapp.sets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.main.MainActivity;

public class CurrentSetTab extends Fragment {
	private MainActivity mainActivity;
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		refreshCurrentSet();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tab_current_set, container, false);
		return view;
	}
	
	/**
	 * Refreshes the current set list
	 */
	public void refreshCurrentSet() {
		mainActivity = (MainActivity)getActivity();
		mainActivity.fillCurrentSetList(getView());
	}
	
}
