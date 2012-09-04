package com.sbgsoft.tabapp.songs;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.main.MainActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SetSongFragment extends Fragment {
	private MainActivity mainActivity;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//mainActivity = (MainActivity)getActivity();
		//mainActivity.fillSongsList(getView());
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.set_song, container, false);
		return view;
	}
}
