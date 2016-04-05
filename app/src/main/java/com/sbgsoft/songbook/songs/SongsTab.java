package com.sbgsoft.songbook.songs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;

public class SongsTab extends Fragment {
	public static final String ALL_SONGS_LABEL = "All Songs";

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Populate the song tab lists
		MainActivity mainActivity = (MainActivity)getActivity();
		mainActivity.fillSongsListView();
		mainActivity.fillSongGroupsSpinner();
		mainActivity.fillSongSortSpinner();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tab_songs, container, false);
		return view;
	}
}
