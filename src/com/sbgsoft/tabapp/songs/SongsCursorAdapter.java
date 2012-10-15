package com.sbgsoft.tabapp.songs;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.db.DBStrings;

public class SongsCursorAdapter extends CursorAdapter {
   	private final LayoutInflater mInflater;
   	protected ListView mListView;
   	protected Cursor mCursor;
   	
   	protected static class RowViewHolder {
        public TextView mTitle;
        public TextView mText;
    }

   	public SongsCursorAdapter(Context context, Cursor c, ListView lv) {
   		super(context, c);
   		mInflater=LayoutInflater.from(context);
   		mListView = lv;
   		mCursor = c;
   	}

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    	// Get the strings from the cursor
    	String songName = cursor.getString(cursor.getColumnIndex(DBStrings.TBLSONG_NAME));
    	String songAuthor = cursor.getString(cursor.getColumnIndex(DBStrings.TBLSONG_AUTHOR));
    	String songKey = cursor.getString(cursor.getColumnIndex(DBStrings.TBLSONG_KEY));
    	
    	// Get the text views
    	TextView songNameTV = (TextView)view.findViewById(R.id.songs_row_text);
    	TextView songAuthorTV = (TextView)view.findViewById(R.id.songs_row_author);
    	TextView songKeyTV = (TextView)view.findViewById(R.id.songs_row_key);
    	TextView separatorTV = (TextView)view.findViewById(R.id.songs_row_text_separator);
    	
    	// Check previous item to determine if this is the first song name with that letter
    	if (!cursor.isFirst()) {
    		cursor.moveToPrevious();
    		if (cursor.getString(cursor.getColumnIndex(DBStrings.TBLSONG_NAME)).charAt(0) != songName.charAt(0)) {
    			// This is the first item with that letter, show the separator
    			separatorTV.setVisibility(View.VISIBLE);
    			separatorTV.setText(songName.substring(0, 1));
    			separatorTV.setClickable(false);
    		}
    		else {
    			// Hide the separator text view
    			separatorTV.setVisibility(View.GONE);
    		}
    	}
    	else {
    		// This is the first item with that letter, show the separator
			separatorTV.setVisibility(View.VISIBLE);
			separatorTV.setText(songName.substring(0, 1));
			separatorTV.setClickable(false);
    	}

    	// Set the text view text
        songNameTV.setText(songName);
        songAuthorTV.setText(songAuthor + " ");
        songKeyTV.setText(songKey + " ");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view=mInflater.inflate(R.layout.songs_row,parent,false); 
        return view;
    }
    
}