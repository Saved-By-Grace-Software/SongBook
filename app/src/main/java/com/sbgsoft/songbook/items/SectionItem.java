package com.sbgsoft.songbook.items;

public class SectionItem implements Item {
	private String name;
	
	/**
	 * Constructor
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
