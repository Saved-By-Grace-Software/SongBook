package com.sbgsoft.tabapp.items;

public class SetItem implements Item {
	private String name;
	private String date;
	
	/**
	 * Constructor
	 * @param songName
	 */
	public SetItem(String setName, String setDate) {
		name = setName;
		date = setDate;
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

}
