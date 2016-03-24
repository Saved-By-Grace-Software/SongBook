package com.sbgsoft.songbook.items;

import java.util.ArrayList;

import com.sbgsoft.songbook.db.DBStrings;
import com.sbgsoft.songbook.main.MainActivity;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class SetItem implements Item, Parcelable {
	private String name;
	private String date;
	
	public ArrayList<SongItem> songs;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeString(date);
		out.writeSerializable(songs);
	}
	
	public static final Parcelable.Creator<SetItem> CREATOR = new Creator<SetItem>() {
		@SuppressWarnings("unchecked")
		public SetItem createFromParcel(Parcel source) {
			SetItem setItem = new SetItem();
			setItem.name = source.readString();
			setItem.date = source.readString();
			setItem.songs = (ArrayList<SongItem>)source.readSerializable();
			return setItem;
		}
		
		public SetItem[] newArray(int size) {
			return new SetItem[size];
		}
	};
	
	/**
	 * Constructor
	 * @param songName
	 */
	public SetItem(String setName, String setDate) {
		name = setName;
		date = setDate;
		songs = new ArrayList<SongItem>();
	}
	
	public SetItem() {
		setName("");
		setDate("");
		songs = new ArrayList<SongItem>();
	}

	/**
	 * Returns the song name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the song author
	 */
	public String getDate() {
		return date;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public void selfPopulateSongsList() {
		// Clear current songs list
		songs.clear();
		
    	Cursor c = MainActivity.dbAdapter.getSetSongs(name);
    	c.moveToFirst();
    	
    	// Populate the ArrayList
    	while (!c.isAfterLast()) {
    		// Get the strings from the cursor
        	String songName = c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME));
        	String songAuthor = c.getString(c.getColumnIndex(DBStrings.TBLSONG_AUTHOR));
        	String setKey = c.getString(c.getColumnIndex(DBStrings.TBLSONG_KEY));
        	String songFile = c.getString(c.getColumnIndex(DBStrings.TBLSONG_FILE));
        	String songKey = MainActivity.dbAdapter.getSongKey(songName);
            String songLink = c.getString(c.getColumnIndex(DBStrings.TBLSONG_LINK));
            int songBpm = c.getInt(c.getColumnIndex(DBStrings.TBLSONG_BPM));
            String songTime = c.getString(c.getColumnIndex(DBStrings.TBLSONG_TIME));

        	// Add the song item
        	songs.add(new SongItem(songName, songAuthor, songKey, songFile, setKey, songBpm, songTime, songLink));
        	
        	// Move to the next song
        	c.moveToNext();
    	}
    	
    	// Close the cursor
    	c.close();
	}
}
