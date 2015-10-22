package com.sbgsoft.songbook.sets;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;

public class SetGroupArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;
 
	public SetGroupArrayAdapter(Context context, List<String> item) {
		super(context, R.layout.group_spinner_item, item);
		this.context = context;
		this.values = item;
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
		int numSets = 0;
		if (groupName.equals(SetsTab.ALL_SETS_LABEL))
			numSets = MainActivity.dbAdapter.getNumSets();
		else
			numSets = MainActivity.dbAdapter.getNumSetsPerGroup(groupName);
		
		// Set the text
		textView.setText(groupName + " {" + numSets + "}");
 
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
		int numSets = 0;
		if (groupName.equals(SetsTab.ALL_SETS_LABEL))
			numSets = MainActivity.dbAdapter.getNumSets();
		else
			numSets = MainActivity.dbAdapter.getNumSetsPerGroup(groupName);
		
		// Set the text
		textView.setText(groupName + " {" + numSets + "}");
			
		return rowView;
	}
}
