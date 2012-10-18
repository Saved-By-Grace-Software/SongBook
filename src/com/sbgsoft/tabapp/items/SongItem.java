package com.sbgsoft.tabapp.items;

public class SongItem implements Item {
	private String name;
	private String author;
	private String key;
	private String file;
	
	/**
	 * Constructor
	 * @param songName
	 */
	public SongItem(String songName, String songAuthor, String songKey, String songFile) {
		name = songName;
		author = songAuthor;
		key = songKey;
		file = songFile;
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
	public String getAuthor() {
		return author;
	}
	
	/**
	 * Returns the song key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Returns the song file
	 */
	public String getSongFile() {
		return file;
	}

}
