package com.sbgsoft.songbook.songs;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.StaticVars;

public class SongGroupArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;
    private int mNumSearchResults;
 
	public SongGroupArrayAdapter(Context context, List<String> item) {
		super(context, R.layout.group_spinner_item, item);
		this.context = context;
		this.values = item;
        mNumSearchResults = -1;
	}

    public SongGroupArrayAdapter(Context context, List<String> item, int numSearchResults) {
        super(context, R.layout.group_spinner_item, item);
        this.context = context;
        this.values = item;
        mNumSearchResults = numSearchResults;
    }
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Inflate the view
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.group_spinner_item, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.group_spinner_item_text);
		
		// Get the number of songs for the group
		String groupName = values.get(position);
		int numSongs;
		if (groupName.equals(SongsTab.ALL_SONGS_LABEL))
			numSongs = MainActivity.dbAdapter.getNumSongs();
        else if (groupName.equals(StaticVars.searchResultsText))
            numSongs = mNumSearchResults;
		else
			numSongs = MainActivity.dbAdapter.getNumSongsPerGroup(groupName);
		
		// Set the text
		textView.setText(groupName + " {" + numSongs + "}");
 
		return rowView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView,
	ViewGroup parent) {
		// Inflate the view
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.group_spinner_dropdown_item, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.group_spinner_dropdown_item_text);

		// Get the number of songs for the group
		String groupName = values.get(position);
		int numSongs;
		if (groupName.equals(SongsTab.ALL_SONGS_LABEL))
			numSongs = MainActivity.dbAdapter.getNumSongs();
        else if (groupName.equals(StaticVars.searchResultsText))
            numSongs = mNumSearchResults;
		else
			numSongs = MainActivity.dbAdapter.getNumSongsPerGroup(groupName);


		
		// Set the text
		textView.setText(groupName + " {" + numSongs + "}");
			
		return rowView;
	}
}
