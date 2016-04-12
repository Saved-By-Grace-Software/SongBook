package com.sbgsoft.songbook.main;

import com.sbgsoft.songbook.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class StaticVars {

	// Keys
	public static final String SONG_ITEM_KEY = "songItem";
	public static final String SONG_NAME_KEY = "songName";
	public static final String SONG_TEXT_KEY = "songText";
	public static final String SONG_KEY_KEY = "songKey";
	public static final String SONG_FILE_KEY = "songFile";
	public static final String CURRENT_SONG_KEY = "setCurrentSong";
	public static final String SET_SONGS_KEY = "setSongs";
	public static final String SET_NAME_KEY = "setName";
	public static final String FILE_ACTIVITY_KEY = "fileActivityKey";
	public static final String FILE_ACTIVITY_TYPE_KEY = "fileActivityTypeKey";
    public static final String SHOW_EDIT_INSET_KEY = "showEditButtonInSet";
    public static final String SHOW_TRANSPOSE_INSET_KEY = "showTransposeButtonInSet";
	
	// Activity Strings
	public static final String ACTIVITY_RESPONSE_TYPE = "activityResponseType";
	public static final String REORDER_ACTIVITY = "reorderActivity";
	public static final String IMPORT_SONG_ACTIVITY = "importSongActivity";
	public static final String IMPORT_DB_ACTIVITY = "importDBActivity";
    public static final String IMPORT_SET_ACTIVITY = "importSetActivity";
	public static final String EXPORT_DB_ACTIVITY = "exportDBActivity";
    public static final String EXPORT_SET_ACTIVITY = "exportSetActivity";
	public static final String FILE_ACTIVITY_FILE = "fileActivityFile";
	public static final String FILE_ACTIVITY_FOLDER = "fileActivityFolder";

    // Settings Strings
    public static final String SETTINGS_METRONOME_STATE_ON = "ON";
    public static final String SETTINGS_METRONOME_STATE_OFF = "OFF";
    public static final String SETTINGS_METRONOME_STATE_WITHBPM = "WITHBPM";
    public static final String SETTINGS_SET_TRANSPOSE_ON = "ON";
    public static final String SETTINGS_SET_TRANSPOSE_OFF = "OFF";
    public static final String SETTINGS_SET_EDIT_ON = "ON";
    public static final String SETTINGS_SET_EDIT_OFF = "OFF";

	// General
	public static final String EOL = System.getProperty("line.separator");
	public static final String UNKNOWN = "Unknown";
	public static final String EXPORT_SQL_FILE = "dbbak.sql";
	public static final String EXPORT_ZIP_FILE = "sbgvsb.bak";
	public static final ArrayList<String> songParts = new ArrayList<String>(
			Arrays.asList("verse", "chorus", "coda", "outro", "bridge", "tag", "refrain", "prechorus", "pre-chorus", "guitar", "instrumental", "interlude"));
	public static final ArrayList<String> songKeys = new ArrayList<String>(Arrays.asList("G#", "A", "Bb", "B", "C", "C#", "D", "Eb", "E", "F", "F#", "G"));
	public static final ArrayList<String> songKeys_transpose = new ArrayList<String>(
			Arrays.asList("C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B", "C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B", "C"));
	public static final HashMap<String, String> songKeyMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 94512194672735973L;
		{
			put("Abm", "G#"); put("Ab", "G#"); put("Am", "A"); put("Bbm", "Bb"); put("A#", "Bb");
			put("A#m", "Bb"); put("Bm", "B"); put("Cm", "C"); put("C#m", "C#"); put("Db", "C#"); put("Dbm", "C#");
			put("Dm", "Db"); put("Ebm", "Eb"); put("D#", "Eb"); put("D#m", "Eb"); put("Em", "E"); put("Fm", "F");
			put("F#m", "F#"); put("Gb", "F#"); put("Gbm", "F#"); put("Gm", "G");
		}
	};
    public static final HashMap<String, String> chordKeyMap = new HashMap<String, String>() {
        private static final long serialVersionUID = 94512194672735973L;
        {
            put("Ab", "G#"); put("Abm", "G#m");
            put("A#", "Bb"); put("A#m", "Bbm");
            put("Db", "C#"); put("Dbm", "C#m");
            put("D#", "Eb"); put("D#m", "Ebm");
            put("Gb", "F#"); put("Gbm", "F#m");
        }
    };
    public static final String chordMarkupStart = "|";
    public static final String chordMarkupEnd = "|";
    public static final String chordMarkupRegex = "\\|[\\w/#]+\\|";
    public static final String chordClickHackFix = "http://this.is.a.hack.fix";
    public static final String searchResultsText = "Search Results";

    // Permission request codes
    public final static int PERMISSIONS_BACKUP_IMPORT = 10;
    public final static int PERMISSIONS_BACKUP_EXPORT = 20;
    public final static int PERMISSIONS_SONG_IMPORT = 30;
    public final static int PERMISSIONS_SET_EXPORT = 40;
    public final static int PERMISSIONS_SET_IMPORT = 50;
	
	// Song Context Menu Keys
	public static final int EDIT_SONG = 1;
	public static final int EDIT_SONG_ATT = 2;
	public static final int ADD_SONG_SET = 3;
	public static final int ADD_SONG_CURR_SET = 4;
	public static final int SONG_GROUPS_ADD = 5;
	public static final int SONG_GROUPS_DEL = 6;
    public static final int SONG_STATS = 7;
	public static final int SHARE_SONG = 8;
    public static final int DELETE_SONG = 9;
	
	// Set Context Menu Keys
	public static final int EDIT_SET = 100;
	public static final int EDIT_SET_ATT = 110;
	public static final int REORDER_SET = 120;
	public static final int SET_GROUPS_ADD = 130;
	public static final int SET_GROUPS_DEL = 140;
	public static final int SHARE_SET = 150;
    public static final int DELETE_SET = 160;
	
	// Current Set Context Menu Keys
	public static final int EDIT_SONG_CS = 200;
	public static final int EDIT_SONG_ATT_CS = 210;
	public static final int SET_SONG_KEY_CS = 220;
    public static final int SONG_STATS_CS = 230;
	public static final int SHARE_SONG_CS = 240;
	public static final int REMOVE_SONG_FROM_SET = 250;
	
	// Sort Arrays
	public static final ArrayList<String> songSortBy = new ArrayList<String>(Arrays.asList("Song Title", "Song Author", "Song Key"));
	public static final ArrayList<String> setSortBy = new ArrayList<String>(Arrays.asList("Set Date - Recent", "Set Date - Oldest", "Set Title"));
	
	//region Enums
	public enum SongFileType {
		plainText,
		chordPro,
		PDF
	}
    //endregion

    //region How Tos
    public static final ArrayList<String> howToCreateSet = new ArrayList<String>(
            Arrays.asList(
                    "Open the menu, select Sets... -> Create Set",
                    "Give the set a name",
                    "Select a date for the set and hit OK",
                    "Here you can select songs for the set but I find it easier to add them later. When you are done selecting songs (if any) click OK",
                    "Here you can add the set to a group or groups if you want. When you are done selecting the group(s) click OK")

    );
    public static final ArrayList<String> howToAddSongToSet = new ArrayList<String>(
            Arrays.asList(
                    "Go to the SETS tab by either clicking or swiping",
                    "Click on the set you want to add songs to, it will take you to the CURRENT SET tab. This tab will show any songs that are in the set",
                    "Go to the SONGS tab by either clicking or swiping",
                    "Find the song you want to add and long-press on it",
                    "Select \"Add Song To Current Set\"",
                    "Select OK to confirm",
                    "Repeate steps 4-7 to continue adding songs until you have added all the songs you want")

    );
    public static final ArrayList<String> howToImportSong = new ArrayList<String>(
            Arrays.asList(
                    "Find the song file in ChordPro of Text format and transfer it to your device",
                    "Open Songbook, open the menu, select Songs... -> Import Song",
                    "Browse to the location on your device that you stored the song file",
                    "Type the song name (this name must be unique, if you already have a song with the same name you will need to choose a different name)",
                    "Type the artist name (this field is not required)",
                    "Type the song key",
                    "Click OK",
                    "Here you can add the song to a group or groups if you want. When you are done selecting the group(s) click OK",
                    "The song is now in your list and can be added to a set using the other instructions")

    );
    public static final ArrayList<String> howToOrderSongs = new ArrayList<String>(
            Arrays.asList(
                    "Go to the SETS tab by either clicking or swiping",
                    "Long-press on the set and select Reorder Set",
                    "Drag the songs into the correct order using the drag icons to the left of the song names",
                    "When the songs are in the order you want, click Save Set")

    );
    public static final ArrayList<String> howToChangeSetKey = new ArrayList<String>(
            Arrays.asList(
                    "Go to the SETS tab by either clicking or swiping",
                    "Click the set you want to change",
                    "Long-press on the song you want to change the key for",
                    "Select Change Song Key For Set",
                    "Select the key you want to transpose to")

    );
    public static final ArrayList<String> howToUseMetronome = new ArrayList<String>(
            Arrays.asList(
                    "The song must have a BPM set for the metronome to be displayed",
                    "Tap on the metronome to start/stop it",
                    "Double-Tap on the metronome to enter Tap Tempo mode",
                    "In Tap Tempo - tap the tempo of the song in the box",
                    "In Tap Tempo - click Set Tempo when you are at the desired tempo",
                    "In Tap Tempo - select Yes to permanently set that tempo for the song")

    );
    //endregion
}
