package com.sbgsoft.tabapp.files;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sbgsoft.tabapp.R;

public class FileArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;
 
	public FileArrayAdapter(Context context, List<String> item) {
		super(context, R.layout.open_file_row, item);
		this.context = context;
		this.values = item;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.open_file_row, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.open_file_row_text);
		textView.setText(values.get(position));
 
		// Change icon based on name
		String s = values.get(position);
 
		if (s.endsWith("/"))
			textView.setCompoundDrawablesWithIntrinsicBounds( R.drawable.folder, 0, 0, 0 );
		else
			textView.setCompoundDrawablesWithIntrinsicBounds( R.drawable.file, 0, 0, 0 );
		textView.setCompoundDrawablePadding(10);
 
		return rowView;
	}
}
