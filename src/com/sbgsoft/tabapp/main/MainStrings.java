package com.sbgsoft.tabapp.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainStrings {

	// Keys
	public static final String SONG_NAME_KEY = "songName";
	public static final String SONG_TEXT_KEY = "songText";
	public static final String SONG_KEY_KEY = "songKey";
	public static final String CURRENT_SONG_KEY = "setCurrentSong";
	public static final String SET_SONGS_KEY = "setSongs";
	public static final String SET_NAME_KEY = "setName";
	
	// Activity Strings
	public static final String ACTIVITY_RESPONSE_TYPE = "activityResponseType";
	public static final String REORDER_ACTIVITY = "reorderActivity";
	public static final String FILE_ACTIVITY = "fileActivity";

	// General
	public static final String UNKNOWN = "Unknown";
	public static final String EXPORT_SQL_FILE = "dbbak.sql";
	public static final String EXPORT_ZIP_FILE = "sbgvsb.bak";
	public static final ArrayList<String> songParts = new ArrayList<String>(Arrays.asList("verse", "chorus", "coda", "outro", "bridge", "tag", "refrain"));
	public static final ArrayList<String> songKeys = new ArrayList<String>(Arrays.asList("Ab", "A", "Bb", "B", "C", "C#", "D", "Eb", "E", "F", "F#", "G"));
	public static final HashMap<String, String> keyMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 94512194672735973L;
		{
			put("Abm", "Ab"); put("G#", "Ab"); put("G#m", "Ab"); put("Am", "A"); put("Bbm", "Bb"); put("A#", "Bb");
			put("A#m", "Bb"); put("Bm", "B"); put("Cm", "C"); put("C#m", "C#"); put("Db", "C#"); put("Dbm", "C#");
			put("Dm", "Db"); put("Ebm", "Eb"); put("D#", "Eb"); put("D#m", "Eb"); put("Em", "E"); put("Fm", "F");
			put("F#m", "F#"); put("Gb", "F#"); put("Gbm", "F#"); put("Gm", "G");
		}
	};
	
	// Song Context Menu Keys
	public static final int DELETE_SONG = 1;
	public static final int EDIT_SONG = 2;
	public static final int EDIT_SONG_ATT = 3;
	public static final int SONG_GROUPS_ADD = 4;
	public static final int SONG_GROUPS_DEL = 5;
	public static final int EMAIL_SONG = 6;
	
	// Set Context Menu Keys
	public static final int DELETE_SET = 10;
	public static final int EDIT_SET = 11;
	public static final int EDIT_SET_ATT = 12;
	public static final int REORDER_SET = 13;
	public static final int SET_GROUPS_ADD = 14;
	public static final int SET_GROUPS_DEL = 15;
	
	// Current Set Context Menu Keys
	public static final int EDIT_SONG_CS = 20;
	public static final int EDIT_SONG_ATT_CS = 21;
	public static final int EMAIL_SONG_CS = 22;
	
	// Sort Arrays
	public static final ArrayList<String> songSortBy = new ArrayList<String>(Arrays.asList("Song Title", "Song Author", "Song Key"));
	public static final ArrayList<String> setSortBy = new ArrayList<String>(Arrays.asList("Set Title", "Set Date"));
}
