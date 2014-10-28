package com.sbgsoft.songbook.items;

import java.util.ArrayList;

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
}
