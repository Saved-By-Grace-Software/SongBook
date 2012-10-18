package com.sbgsoft.tabapp.songs.items;

public class SectionItem implements Item {
	private String name;
	
	/**
	 * Constructor
	 * @param songName
	 */
	public SectionItem(String sectionName) {
		name = sectionName;
	}

	/**
	 * Returns the item name
	 */
	public String getName() {
		return name;
	}
}
