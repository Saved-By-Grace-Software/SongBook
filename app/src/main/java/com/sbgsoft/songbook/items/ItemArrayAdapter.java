package com.sbgsoft.songbook.items;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sbgsoft.songbook.R;

public class ItemArrayAdapter extends ArrayAdapter<Item> {
	private ArrayList<Item> items;
	private LayoutInflater vi;

	public ItemArrayAdapter (Context context, ArrayList<Item> items) {
		super(context,0, items);
		this.items = items;
		vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		final Item i = items.get(position);
		if (i != null) {
			if(i.getClass().equals(SectionItem.class)) {
				SectionItem si = (SectionItem)i;
				v = vi.inflate(R.layout.songs_row_section, null);

				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				
				final TextView sectionView = (TextView) v.findViewById(R.id.songs_row_section);
				sectionView.setText(si.getName());
			}
			else if (i.getClass().equals(SongItem.class)) {
				SongItem si = (SongItem)i;
				
				// Get the strings from the item
		    	String songName = si.getName();
		    	String songAuthor = si.getAuthor();
		    	
		    	String songKey = "";
		    	if (si.getSetKey() == "") {
		    		songKey = si.getKey();
		    	} else {
		    		songKey = si.getSetKey();
		    	}
		    	
		    	// Get the text views
		    	v = vi.inflate(R.layout.songs_row, null);
		    	TextView songNameTV = (TextView)v.findViewById(R.id.songs_row_text);
		    	TextView songAuthorTV = (TextView)v.findViewById(R.id.songs_row_author);
		    	TextView songKeyTV = (TextView)v.findViewById(R.id.songs_row_key);				
				
		    	// Set the text view text
		        songNameTV.setText(songName);
		        songAuthorTV.setText(songAuthor + " ");
		        songKeyTV.setText(songKey + " ");
			}
		}
		return v;
	}
}
