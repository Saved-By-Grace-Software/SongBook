package com.sbgsoft.tabapp.db;

import java.util.ArrayList;

import com.sbgsoft.tabapp.songs.SongsTab;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	/*****************************************************************************
    *
    * Class Variables
    * 
    *****************************************************************************/
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;

	private static final String TAG = "TabAppDBAdapter";
    private static final String DATABASE_NAME = "tabAppDB";
    private static final int DATABASE_VERSION = 1;
    public static final String SETS_TABLE = "tblSets";
    public static final String SONGS_TABLE = "tblSongs";
    public static final String GROUPS_TABLE = "tblGroups";
    public static final String CURRSET_TABLE = "tblCurrSet";
    public static final String SETLOOKUP_TABLE = "tblSetLookup";
    public static final String GPLOOKUP_TABLE = "tblGroupLookup";
    public static final String TBLSONG_ID = "songID";
    public static final String TBLSONG_NAME = "songName";
    public static final String TBLSONG_FILE = "fileName";
    public static final String TBLSONG_GROUP = "groupID";
    public static final String TBLSETS_ID = "setID";
    public static final String TBLSETS_NAME = "setName";
    public static final String TBLSETS_SONGS = "songs";
    public static final String TBLGROUPS_ID = "groupID";
    public static final String TBLGROUPS_NAME = "groupName";
    public static final String TBLGROUPS_PARENT = "parentID";
    public static final String TBLCURRSET_ID = "currSetID";
    public static final String TBLCURRSET_SET = "setID";
    public static final String TBLSLOOKUP_ID = "ID";
    public static final String TBLSLOOKUP_SET = "setID";
    public static final String TBLSLOOKUP_SONG = "songID";
    public static final String TBLGPLOOKUP_ID = "ID";
    public static final String TBLGPLOOKUP_SONG = "songID";
    public static final String TBLGPLOOKUP_GROUP = "groupID";

    private final Context mCtx;
    
    
    /*****************************************************************************
    *
    * Class Functions
    * 
    *****************************************************************************/
	/**
	 * Constructor, creates or opens the database
	 */
	public DBAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    
    /*****************************************************************************
    *
    * Set Functions
    * 
    *****************************************************************************/
	/**
	 * Creates a new, empty, set
	 * @param setName The name of the set
	 * @return True if success, False if failure
	 */
	public boolean createSet(String setName) {
		// Create a new set with the specified name
		try {
			mDb.execSQL( "INSERT INTO " + SETS_TABLE + "(" + TBLSETS_NAME + ") VALUES ('" + setName + "');" );
		} catch (SQLiteException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Creates a new set with the specified song string
	 * @param setName The name of the set
	 * @param songs The string of song names
	 * @return True if success, False if failure
	 */
	public boolean createSet(String setName, ArrayList<String> setSongs) {
		// Create a new set with the specified name
		try {
			// Add the set name
			mDb.execSQL( "INSERT INTO " + SETS_TABLE + "(" + TBLSETS_NAME + ") VALUES ('" + setName + "');" );
			
			// Add the songs to the set
			for(String song : setSongs) {
				if (song != "") {
					mDb.execSQL( "INSERT INTO " + SETLOOKUP_TABLE + "(" + TBLSLOOKUP_SET + ", " + TBLSLOOKUP_SONG + ") " + 
							" VALUES ((SELECT " + TBLSETS_ID + " FROM " + SETS_TABLE + " WHERE " + TBLSETS_NAME + " = '" + setName + "'), " + 
							" (SELECT " + TBLSONG_ID + " FROM " + SONGS_TABLE + " WHERE " + TBLSONG_NAME + " = '" + song + "') );" );
				}
			}
		} catch (SQLiteException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Updates the set with the specified song string
	 * @param setName The name of the set to update
	 * @param songs The string of song names
	 * @return True if success, False if failure
	 */
	public boolean updateSet(String setName, String[] songs) {
		// Create a new set with the specified name
		try {
			// Delete the songs from the sets lookup table
			mDb.execSQL("DELETE FROM " + SETLOOKUP_TABLE + " WHERE " + TBLSLOOKUP_SET +
					" = (SELECT " + TBLSETS_ID + " FROM " + SETS_TABLE + " WHERE " + TBLSETS_NAME + " = '" + setName + "')");
						
			// Add the songs in the new order
			for(String song : songs) {
				if (song != "") {
					mDb.execSQL( "INSERT INTO " + SETLOOKUP_TABLE + "(" + TBLSLOOKUP_SET + ", " + TBLSLOOKUP_SONG + ") " + 
							" VALUES ((SELECT " + TBLSETS_ID + " FROM " + SETS_TABLE + " WHERE " + TBLSETS_NAME + " = '" + setName + "'), " + 
							" (SELECT " + TBLSONG_ID + " FROM " + SONGS_TABLE + " WHERE " + TBLSONG_NAME + " = '" + song + "') );" );
				}
			}
		} catch (SQLiteException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Gets all existing set names
	 * @return Cursor to the query
	 */	
	public Cursor getSetNames() {
		return mDb.rawQuery("SELECT " + TBLSETS_ID + " as _id, " + TBLSETS_NAME + " FROM " + SETS_TABLE, null);
	}
	
	/**
	 * Gets the songs for the current set
	 * @return Cursor to the songs
	 */
	public Cursor getCurrentSetSongs() {
		try {	
			// Get the list of songs from the sets lookup table
			String query = "SELECT " + SONGS_TABLE + "." + TBLSONG_ID + " as _id, " + SONGS_TABLE + "." + TBLSONG_NAME + ", " + SONGS_TABLE + "." + TBLSONG_FILE +
					" FROM " + SETLOOKUP_TABLE + ", " + CURRSET_TABLE +
					" INNER JOIN " + SONGS_TABLE + " ON " + SETLOOKUP_TABLE + "." + TBLSLOOKUP_SONG + " = " + SONGS_TABLE + "." + TBLSONG_ID +
					" INNER JOIN " + SETS_TABLE + " ON " + SETLOOKUP_TABLE + "." + TBLSLOOKUP_SET + " = " + SETS_TABLE + "." + TBLSETS_ID +
					" WHERE " + SETS_TABLE + "." + TBLSETS_ID + " = " + CURRSET_TABLE + "." + TBLCURRSET_SET;

			return mDb.rawQuery(query, null);
		} catch (SQLiteException s) {
			return null;
		}
	}

	/**
	 * Gets the songs for the specified set
	 * @param setName The name of the set to get the songs for
	 * @return Cursor to the songs
	 */
	public Cursor getSetSongs(String setName) {
		try {
			// Get the list of songs from the sets lookup table
			String query = "SELECT " + SONGS_TABLE + "." + TBLSONG_ID + " as _id, " + SONGS_TABLE + "." + TBLSONG_NAME + ", " + SONGS_TABLE + "." + TBLSONG_FILE +
					" FROM " + SETLOOKUP_TABLE + ", " + CURRSET_TABLE +
					" INNER JOIN " + SONGS_TABLE + " ON " + SETLOOKUP_TABLE + "." + TBLSLOOKUP_SONG + " = " + SONGS_TABLE + "." + TBLSONG_ID +
					" INNER JOIN " + SETS_TABLE + " ON " + SETLOOKUP_TABLE + "." + TBLSLOOKUP_SET + " = " + SETS_TABLE + "." + TBLSETS_ID +
					" WHERE " + SETS_TABLE + "." + TBLSETS_ID + " = (SELECT " + TBLSETS_ID + " FROM " + SETS_TABLE + " WHERE " + TBLSETS_NAME + " = '" + setName + "')";
			
			return mDb.rawQuery(query, null);
		} catch (SQLiteException s) {
			return null;
		}
	}
	
	/**
	 * Deletes all sets in the database
	 * @return True if success, False if failure
	 */
	public boolean deleteAllSets() {
		try {
			mDb.execSQL("DELETE FROM " + SETLOOKUP_TABLE);
			mDb.execSQL("DELETE FROM " + SETS_TABLE);
			mDb.execSQL("UPDATE " + CURRSET_TABLE + " SET " + TBLCURRSET_SET + " = 0");
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Deletes the specified set
	 * @param setName The set name to delete
	 * @return True if success, False if failure
	 */
	public boolean deleteSet(String setName) {
		try {
			// Delete the songs from the sets lookup table
			mDb.execSQL("DELETE FROM " + SETLOOKUP_TABLE + " WHERE " + TBLSLOOKUP_SET +
					" = (SELECT " + TBLSETS_ID + " FROM " + SETS_TABLE + " WHERE " + TBLSETS_NAME + " = '" + setName + "')");
			
			// Delete the set
			mDb.execSQL("DELETE FROM " + SETS_TABLE + " WHERE " + TBLSETS_NAME + " = '" + setName + "'");
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Determines if the song exists in the set
	 * @param songName The song name to check
	 * @param setName The set to check against
	 * @return True if it exists in the set, False if it does not exist in the set
	 */
	public boolean isSongInSet(String songName, String setName) {
		try {
			// Query for song
			String query = "SELECT " + TBLSLOOKUP_ID + " as _id " +
					" FROM " + SETLOOKUP_TABLE + " as L " +
					" INNER JOIN " + SETS_TABLE + " as E ON E." + TBLSETS_ID + " = L." + TBLSLOOKUP_SET +
					" INNER JOIN " + SONGS_TABLE + " as O ON O." + TBLSONG_ID + " = L." + TBLSLOOKUP_SONG +
					" WHERE L." + TBLSLOOKUP_SET + "= (SELECT " + TBLSETS_ID + " FROM " + SETS_TABLE + " WHERE " + TBLSETS_NAME + " = '" + setName + "') AND " +
					" L." + TBLSLOOKUP_SONG + "= (SELECT " + TBLSONG_ID + " FROM " + SONGS_TABLE + " WHERE " + TBLSONG_NAME + " = '" + songName + "') ";
			Cursor c = mDb.rawQuery(query, null);
			
			// Check record for song
			if(c.getCount() == 0)
				return false;
			else
				return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	
	/*****************************************************************************
    *
    * Song Functions
    * 
    *****************************************************************************/
	/**
	 * Creates a new, empty, song
	 * @param songName The name of the set
	 * @param fileName The name of the file 
	 * @return True if success, False if failure
	 */
	public boolean createSong(String songName, String fileName) {
		// Create a new set with the specified name
		try {
			mDb.execSQL( "insert into " + SONGS_TABLE + "(" + TBLSONG_NAME + ", " + TBLSONG_FILE + ") values ('" + 
					songName + "', '" + fileName + "' );" );
		} catch (SQLiteException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Gets all existing song names
	 * @return Cursor to the query
	 */	
	public Cursor getSongNames(String groupName) {
		String query = "";
		
		// Check if the group is the all songs group
		if (groupName.equals(SongsTab.ALL_SONGS_LABEL)) {
			query = "SELECT " + TBLSONG_ID + " as _id, " + TBLSONG_NAME + ", " + TBLSONG_FILE + 
					" FROM " + SONGS_TABLE + " ORDER BY " + TBLSONG_NAME;
		} else {
			query = "SELECT " + SONGS_TABLE + "." + TBLSONG_ID + " as _id, " + SONGS_TABLE + "." + TBLSONG_NAME + ", " + SONGS_TABLE + "." + TBLSONG_FILE + 
					" FROM " + SONGS_TABLE + 
					" INNER JOIN " + GPLOOKUP_TABLE + " ON " + SONGS_TABLE + "." + TBLSONG_ID + " = " + GPLOOKUP_TABLE + "." + TBLGPLOOKUP_SONG +
					" INNER JOIN " + GROUPS_TABLE + " ON " + GROUPS_TABLE + "." + TBLGROUPS_ID + " = " + GPLOOKUP_TABLE + "." + TBLGPLOOKUP_GROUP +
					" WHERE " + GROUPS_TABLE + "." + TBLGROUPS_NAME + " = '" + groupName + "'" +
					" ORDER BY " + TBLSONG_NAME;
		}
		return mDb.rawQuery(query, null);
	}

	/**
	 * Deletes all songs in the database
	 * @return True if success, False if failure
	 */
	public boolean deleteAllSongs() {
		try {
			deleteAllSets();
			mDb.execSQL("DELETE from " + SONGS_TABLE);
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Deletes the specified song
	 * @param songName The song name to delete
	 * @return True if success, False if failure
	 */
	public boolean deleteSong(String songName) {
		try {
			mDb.execSQL("DELETE from " + SONGS_TABLE + " WHERE " + TBLSONG_NAME + " = '" + songName + "'");
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the file name of the specified song
	 * @param songName The song to get the file for
	 * @return The file name
	 */
	public String getSongFile(String songName) {
		try {
			Cursor c = mDb.rawQuery("SELECT " + TBLSONG_ID + " as _id, " + TBLSONG_NAME + ", " + TBLSONG_FILE + " FROM " + SONGS_TABLE +
					" WHERE " + TBLSONG_NAME + " = '" + songName + "'", null);
			c.moveToFirst();
			return c.getString(c.getColumnIndexOrThrow(TBLSONG_FILE));
		} catch (IndexOutOfBoundsException e) {
			return "";
		} catch (SQLiteException s) {
			return "";
		}
		
	}
	
	/**
	 * Updates the song to add it to the specified group
	 * @param songName The song to update
	 * @param groupName The group to add the song to
	 * @return True if success, False if failure
	 */
	public boolean addSongToGroup(String songName, String groupName) {
		try {
			if(!groupName.equals(SongsTab.ALL_SONGS_LABEL)) {
				mDb.execSQL("INSERT INTO " + GPLOOKUP_TABLE + " (" + TBLGPLOOKUP_SONG + ", " + TBLGPLOOKUP_GROUP + ") " +
						" VALUES ( " +
						" (SELECT " + TBLSONG_ID + " FROM " + SONGS_TABLE + " WHERE " + TBLSONG_NAME + " = '" + songName + "'), " +
						" (SELECT " + TBLGROUPS_ID + " FROM " + GROUPS_TABLE + " WHERE " + TBLGROUPS_NAME + " = '" + groupName + "'))");
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Removes the song from the specified group
	 * @param songName The song to remove
	 * @param groupName The group to remove the song from
	 * @return True if success, False if failure
	 */
	public boolean removeSongFromGroup(String songName, String groupName) {
		try {
			if(!groupName.equals(SongsTab.ALL_SONGS_LABEL)) {
				String query = "DELETE FROM " + GPLOOKUP_TABLE +
						" WHERE " + TBLGPLOOKUP_SONG + " = " +
						" (SELECT " + TBLSONG_ID + " FROM " + SONGS_TABLE + " WHERE " + TBLSONG_NAME + " = '" + songName + "') " +
						" AND " + TBLGPLOOKUP_GROUP + " = " +
						" (SELECT " + TBLGROUPS_ID + " FROM " + GROUPS_TABLE + " WHERE " + TBLGROUPS_NAME + " = '" + groupName + "')";
				mDb.execSQL(query);
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	
	/*****************************************************************************
    *
    * Current Set Functions
    * 
    *****************************************************************************/
	/**
	 * Sets the current set
	 * @param setName The set name to set as current
	 * @return True if success, False if failure
	 */
	public boolean setCurrentSet(String setName) {
		try {
			mDb.execSQL("UPDATE " + CURRSET_TABLE + " SET " + TBLCURRSET_SET + 
					" = (SELECT " + TBLSETS_ID + " FROM " + SETS_TABLE + " WHERE " + TBLSETS_NAME + " = '" + setName + "')");
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	
	/*****************************************************************************
    *
    * Groups Functions
    * 
    *****************************************************************************/	
	/**
	 * Gets a list of the group names
	 * @return A cursor to the query results
	 */
	public Cursor getGroupNames() {
		String query = "SELECT 0 as _id, '" + SongsTab.ALL_SONGS_LABEL + "' as " + TBLGROUPS_NAME + " FROM " + GROUPS_TABLE + " UNION " +
				" SELECT " + TBLGROUPS_ID + " as _id, " + TBLGROUPS_NAME + " FROM " + GROUPS_TABLE;
		return mDb.rawQuery(query, null);
	}
	
	/**
	 * Creates a group with the specified name
	 * @param groupName The name of the group
	 * @return True if success, False if failure
	 */
	public boolean createGroup(String groupName) {
		try {
			mDb.execSQL( "INSERT INTO " + GROUPS_TABLE + "(" + TBLGROUPS_NAME + ", " + TBLGROUPS_PARENT + ") values ('" + 
					groupName + "', -1 );" );
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Deletes the specified group
	 * @param groupName The group to delete
	 * @return True if success, False if failure
	 */
	public boolean deleteGroup(String groupName) {
		try {
			mDb.execSQL("DELETE from " + GROUPS_TABLE + " WHERE " + TBLGROUPS_NAME + " = '" + groupName + "'");
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	
	/*****************************************************************************
    *
    * DBHelper Class
    * 
    *****************************************************************************/
	private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
    		// Add the tables
    		try{
    			db.beginTransaction();
    			
    			// Sets table
    			db.execSQL("create table " + SETS_TABLE +
    					"(" + TBLSETS_ID + " integer PRIMARY KEY autoincrement, " + 
    					TBLSETS_NAME + " text UNIQUE); ");
    			
    			// Songs table
    			db.execSQL("create table " + SONGS_TABLE +
    					"(" + TBLSONG_ID + " integer PRIMARY KEY autoincrement, " + 
    					TBLSONG_NAME + " text UNIQUE, " + 
    					TBLSONG_FILE + " text); " );
    			
    			// Group table
    			db.execSQL("create table " + GROUPS_TABLE +
    					"(" + TBLGROUPS_ID + " integer PRIMARY KEY autoincrement, " + 
    					TBLGROUPS_NAME + " text UNIQUE, " + 
    					TBLGROUPS_PARENT + " int ); " );
    			
    			// Current Set table
    			db.execSQL("create table " + CURRSET_TABLE +
    					"(" + TBLCURRSET_ID + " integer PRIMARY KEY autoincrement, " + 
    					TBLCURRSET_SET + " int ); " );
    			
    			// Set lookup table
    			db.execSQL("create table " + SETLOOKUP_TABLE +
    					"(" + TBLSLOOKUP_ID + " integer PRIMARY KEY autoincrement, " + 
    					TBLSLOOKUP_SET + " int, " + 
    					TBLSLOOKUP_SONG + " int ); " );
    			
    			// Group lookup table
    			db.execSQL("create table " + GPLOOKUP_TABLE +
    					"(" + TBLGPLOOKUP_ID + " integer PRIMARY KEY autoincrement, " + 
    					TBLGPLOOKUP_SONG + " int, " + 
    					TBLGPLOOKUP_GROUP + " int ); " );
    			
    			db.execSQL("insert into " + CURRSET_TABLE + "(" + TBLCURRSET_SET + ") values (0);" );
    			
    			db.setTransactionSuccessful(); 
    		}catch(SQLiteException e) {
    			Log.e(TAG, e.getMessage());
    		}finally{
    			db.endTransaction();
    		}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
            //        + newVersion + ", which will destroy all old data");
            //db.execSQL("DROP TABLE IF EXISTS notes");
            //onCreate(db);
        	//TODO: Add upgrade functionality
        }
    }
}
