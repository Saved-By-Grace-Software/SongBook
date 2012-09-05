package com.sbgsoft.tabapp.songs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.main.MainActivity;

public class SongsTab extends Fragment {
	private MainActivity mainActivity;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		refreshSongsList();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tab_songs, container, false);
		return view;
	}
	
	/**
	 * Refreshes song list
	 */
	public void refreshSongsList() {
		mainActivity = (MainActivity)getActivity();
		mainActivity.fillSongsList(getView());
	}
}
