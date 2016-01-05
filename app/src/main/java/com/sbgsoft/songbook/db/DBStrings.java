package com.sbgsoft.songbook.db;

public class DBStrings {

	// Database Strings
	public static final String DATABASE_NAME = "tabAppDB";
	public static final int DATABASE_VERSION = 5;
	
	// Sets Table
	public static final String SETS_TABLE = "tblSets";
	public static final String TBLSETS_ID = "setID";
	public static final String TBLSETS_NAME = "setName";
	public static final String TBLSETS_SONGS = "songs";
	public static final String TBLSETS_DATE = "setDate";
	
	// Songs Table
	public static final String SONGS_TABLE = "tblSongs";
	public static final String TBLSONG_ID = "songID";
	public static final String TBLSONG_NAME = "songName";
	public static final String TBLSONG_FILE = "fileName";
	public static final String TBLSONG_GROUP = "groupID";
	public static final String TBLSONG_AUTHOR = "author";
	public static final String TBLSONG_KEY = "songKey";
    public static final String TBLSONG_BPM = "beatsPerMinute";
    public static final String TBLSONG_TIME = "timeSignature";
	
	// Song Groups Table
	public static final String SONGGROUPS_TABLE = "tblSongGroups";
	public static final String TBLSONGGROUPS_ID = "songGroupID";
	public static final String TBLSONGGROUPS_NAME = "songGroupName";
	public static final String TBLSONGGROUPS_PARENT = "parentID";
	
	// Current Set Table
	public static final String CURRSET_TABLE = "tblCurrSet";
	public static final String TBLCURRSET_ID = "currSetID";
	public static final String TBLCURRSET_SET = "setID";
	
	// Set Groups Table
	public static final String SETGROUPS_TABLE = "tblSetGroups";
	public static final String TBLSETGROUPS_ID = "setGroupID";
	public static final String TBLSETGROUPS_NAME = "setGroupName";
	public static final String TBLSETGROUPS_PARENT = "parentID";
	
	// Set Lookup Table
	public static final String SETLOOKUP_TABLE = "tblSetLookup";
	public static final String TBLSLOOKUP_ID = "ID";
	public static final String TBLSLOOKUP_SET = "setID";
	public static final String TBLSLOOKUP_SONG = "songID";
	public static final String TBLSLOOKUP_KEY = "songKey";
	public static final String TBLSLOOKUP_ORDER = "setOrder";
	
	// Song Group Lookup Table
	public static final String SONGGPLOOKUP_TABLE = "tblSongGroupLookup";
	public static final String TBLSONGGPLOOKUP_ID = "ID";
	public static final String TBLSONGGPLOOKUP_SONG = "songID";
	public static final String TBLSONGGPLOOKUP_GROUP = "groupID";
	
	// Set Group Lookup Table
	public static final String SETGPLOOKUP_TABLE = "tblSetGroupLookup";
	public static final String TBLSETGPLOOKUP_ID = "ID";
	public static final String TBLSETGPLOOKUP_SET = "setID";
	public static final String TBLSETGPLOOKUP_GROUP = "setGroupID";
	
}
