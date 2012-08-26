package com.sbgsoft.tabapp.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.tabapp.MainActivity;
import com.sbgsoft.tabapp.R;

public class SetsTab extends Fragment {
	private MainActivity mainActivity;
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mainActivity = (MainActivity)getActivity();
		mainActivity.fillSetsList(getView());
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tab_sets, container, false);
		return view;
	}
	
}
