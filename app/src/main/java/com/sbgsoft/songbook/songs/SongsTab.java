package com.sbgsoft.songbook.songs;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;

public class SongsTab extends Fragment {
	public static final String ALL_SONGS_LABEL = "All Songs";

    private View mView;

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
		mView = inflater.inflate(R.layout.tab_songs, container, false);
        reColorSeparatorBar();
		return mView;
	}

    public void reColorSeparatorBar() {
        // Get the current theme from the database
        SongBookTheme theme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Color the separator bars
        View setBar = mView.findViewById(R.id.song_separator_bar);
        setBar.setBackgroundColor(theme.getSeparatorBarColor());
    }
}
