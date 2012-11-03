package com.sbgsoft.tabapp.db;

import java.util.ArrayList;

import com.sbgsoft.tabapp.sets.SetsTab;
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
        mDb.close();
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
	public boolean createSet(String setName, String setDate) {
		// Create a new set with the specified name
		try {
			mDb.execSQL( "INSERT INTO " + DBStrings.SETS_TABLE + "(" + DBStrings.TBLSETS_NAME + ", " + DBStrings.TBLSETS_DATE +
					") VALUES ('" + setName + "', '" + setDate + "');" );
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
	public boolean createSet(String setName, ArrayList<String> setSongs, String setDate) {
		// Create a new set with the specified name
		try {
			// Add the set name
			if (createSet(setName, setDate)) {
				// Add the songs to the set
				for(String song : setSongs) {
					if (song != "") {
						mDb.execSQL( "INSERT INTO " + DBStrings.SETLOOKUP_TABLE + "(" + DBStrings.TBLSLOOKUP_SET + ", " + DBStrings.TBLSLOOKUP_SONG + ") " + 
								" VALUES ((SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "'), " + 
								" (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + song + "') );" );
					}
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
			mDb.execSQL("DELETE FROM " + DBStrings.SETLOOKUP_TABLE + " WHERE " + DBStrings.TBLSLOOKUP_SET +
					" = (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "')");
						
			// Add the songs in the new order
			for(String song : songs) {
				if (song != "") {
					mDb.execSQL( "INSERT INTO " + DBStrings.SETLOOKUP_TABLE + "(" + DBStrings.TBLSLOOKUP_SET + ", " + DBStrings.TBLSLOOKUP_SONG + ") " + 
							" VALUES ((SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "'), " + 
							" (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + song + "') );" );
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
	public Cursor getSetNames(String groupName) {
		String query = "";
		
		// Check if the group is the all sets group
		if (groupName.equals(SetsTab.ALL_SETS_LABEL)) {
			query = "SELECT " + DBStrings.TBLSETS_ID + " as _id, " + DBStrings.TBLSETS_NAME + ", " + DBStrings.TBLSETS_DATE +
					" FROM " + DBStrings.SETS_TABLE + " ORDER BY " + DBStrings.TBLSETS_NAME;
		} else {
			query = "SELECT " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + " as _id, " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_NAME +
					", " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_DATE +
					" FROM " + DBStrings.SETS_TABLE + 
					" INNER JOIN " + DBStrings.SETGPLOOKUP_TABLE + " ON " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + 
					" = " + DBStrings.SETGPLOOKUP_TABLE + "." + DBStrings.TBLSETGPLOOKUP_SET +
					" INNER JOIN " + DBStrings.SETGROUPS_TABLE + " ON " + DBStrings.SETGROUPS_TABLE + "." + DBStrings.TBLSETGROUPS_ID + 
					" = " + DBStrings.SETGPLOOKUP_TABLE + "." + DBStrings.TBLSETGPLOOKUP_GROUP +
					" WHERE " + DBStrings.SETGROUPS_TABLE + "." + DBStrings.TBLSETGROUPS_NAME + " = '" + groupName + "'" +
					" ORDER BY " + DBStrings.TBLSETS_NAME;
		}
		return mDb.rawQuery(query, null);
	}
	
	/**
	 * Updates the set to add it to the specified group
	 * @param setName The set to update
	 * @param groupName The group to add the song to
	 * @return True if success, False if failure
	 */
	public boolean addSetToGroup(String setName, String groupName) {
		try {
			if(!groupName.equals(SetsTab.ALL_SETS_LABEL)) {
				mDb.execSQL("INSERT INTO " + DBStrings.SETGPLOOKUP_TABLE + " (" + DBStrings.TBLSETGPLOOKUP_SET + ", " + DBStrings.TBLSETGPLOOKUP_GROUP + ") " +
						" VALUES ( " +
						" (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "'), " +
						" (SELECT " + DBStrings.TBLSETGROUPS_ID + " FROM " + DBStrings.SETGROUPS_TABLE + " WHERE " + DBStrings.TBLSETGROUPS_NAME + " = '" + groupName + "'))");
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the songs for the specified set
	 * @param setName The name of the set to get the songs for
	 * @return Cursor to the songs
	 */
	public Cursor getSetSongs(String setName) {
		try {
			// Get the list of songs from the sets lookup table
			String query = "SELECT " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_NAME + 
					", " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_FILE +
					" FROM " + DBStrings.SETLOOKUP_TABLE + ", " + DBStrings.CURRSET_TABLE +
					" INNER JOIN " + DBStrings.SONGS_TABLE + " ON " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SONG + 
					" = " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID +
					" INNER JOIN " + DBStrings.SETS_TABLE + " ON " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SET + 
					" = " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID +
					" WHERE " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + " = (SELECT " + DBStrings.TBLSETS_ID + 
					" FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "')";
			
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
			mDb.execSQL("DELETE FROM " + DBStrings.SETLOOKUP_TABLE);
			mDb.execSQL("DELETE FROM " + DBStrings.SETS_TABLE);
			mDb.execSQL("UPDATE " + DBStrings.CURRSET_TABLE + " SET " + DBStrings.TBLCURRSET_SET + " = 0");
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
			mDb.execSQL("DELETE FROM " + DBStrings.SETLOOKUP_TABLE + " WHERE " + DBStrings.TBLSLOOKUP_SET +
					" = (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "')");
			
			// Delete the set
			mDb.execSQL("DELETE FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "'");
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
			String query = "SELECT " + DBStrings.TBLSLOOKUP_ID + " as _id " +
					" FROM " + DBStrings.SETLOOKUP_TABLE + " as L " +
					" INNER JOIN " + DBStrings.SETS_TABLE + " as E ON E." + DBStrings.TBLSETS_ID + " = L." + DBStrings.TBLSLOOKUP_SET +
					" INNER JOIN " + DBStrings.SONGS_TABLE + " as O ON O." + DBStrings.TBLSONG_ID + " = L." + DBStrings.TBLSLOOKUP_SONG +
					" WHERE L." + DBStrings.TBLSLOOKUP_SET + "= (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "') AND " +
					" L." + DBStrings.TBLSLOOKUP_SONG + "= (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "') ";
			Cursor c = mDb.rawQuery(query, null);
			
			// Check record for song
			if(c.getCount() == 0) {
				c.close();
				return false;
			}
			else {
				c.close();
				return true;
			}
		} catch (SQLException e) {
			return false;
		}
	}
	
	/**
	 * Removes the set from the specified group
	 * @param setName The set to remove
	 * @param groupName The group to remove the set from
	 * @return True if success, False if failure
	 */
	public boolean removeSetFromGroup(String setName, String groupName) {
		try {
			if(!groupName.equals(SetsTab.ALL_SETS_LABEL)) {
				String query = "DELETE FROM " + DBStrings.SETGPLOOKUP_TABLE +
						" WHERE " + DBStrings.TBLSETGPLOOKUP_SET + " = " +
						" (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "') " +
						" AND " + DBStrings.TBLSETGPLOOKUP_GROUP + " = " +
						" (SELECT " + DBStrings.TBLSETGROUPS_ID + " FROM " + DBStrings.SETGROUPS_TABLE + " WHERE " + DBStrings.TBLSETGROUPS_NAME + " = '" + groupName + "')";
				mDb.execSQL(query);
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the date for the specified set
	 * @param setName The set
	 * @return The date
	 */
	public String getSetDate(String setName) {
		try {
			Cursor c = mDb.rawQuery("SELECT " + DBStrings.TBLSETS_ID + " as _id, " + DBStrings.TBLSETS_NAME + ", " + DBStrings.TBLSETS_DATE + " FROM " + DBStrings.SETS_TABLE +
					" WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "'", null);
			c.moveToFirst();
			String ret = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_DATE));
			c.close();
			return ret;
		} catch (IndexOutOfBoundsException e) {
			return "";
		} catch (SQLiteException s) {
			return "";
		}
	}
	
	/**
	 * Updates the set attributes
	 * @param oldSetName The original set name
	 * @param newSetName The new set name
	 * @param date The new set date
	 * @return True if success, False if failure
	 */
	public boolean updateSetAttributes(String oldSetName, String newSetName, String date) {
		try {
			String query = "UPDATE " + DBStrings.SETS_TABLE + 
					" SET " + DBStrings.TBLSETS_NAME + " = '" + newSetName + "', " +
					DBStrings.TBLSETS_DATE + " = '" + date + "' " + 
					" WHERE " + DBStrings.TBLSETS_NAME + " = '" + oldSetName + "'";
			mDb.execSQL(query);
		} catch (SQLException e) {
			return false;
		}
		return true;
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
	public boolean createSong(String songName, String fileName, String author, String key) {
		// Create a new set with the specified name
		try {
			mDb.execSQL( "INSERT INTO " + DBStrings.SONGS_TABLE + "(" + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_FILE + ", " +
					DBStrings.TBLSONG_AUTHOR + ", " + DBStrings.TBLSONG_KEY + 
					") VALUES ('" + songName + "', '" + fileName + "', '" + author + "', '" + key + "');" );
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
			query = "SELECT " + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_FILE + ", " +
					DBStrings.TBLSONG_AUTHOR + ", " + DBStrings.TBLSONG_KEY +
					" FROM " + DBStrings.SONGS_TABLE + " ORDER BY " + DBStrings.TBLSONG_NAME;
		} else {
			query = "SELECT " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.SONGS_TABLE + "." + 
					DBStrings.TBLSONG_NAME + ", " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_FILE + ", " +
					DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_AUTHOR + ", " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_KEY +
					" FROM " + DBStrings.SONGS_TABLE + 
					" INNER JOIN " + DBStrings.SONGGPLOOKUP_TABLE + " ON " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + " = " + 
					DBStrings.SONGGPLOOKUP_TABLE + "." + DBStrings.TBLSONGGPLOOKUP_SONG +
					" INNER JOIN " + DBStrings.SONGGROUPS_TABLE + " ON " + DBStrings.SONGGROUPS_TABLE + "." + DBStrings.TBLSONGGROUPS_ID + " = " + 
					DBStrings.SONGGPLOOKUP_TABLE + "." + DBStrings.TBLSONGGPLOOKUP_GROUP +
					" WHERE " + DBStrings.SONGGROUPS_TABLE + "." + DBStrings.TBLSONGGROUPS_NAME + " = '" + groupName + "'" +
					" ORDER BY " + DBStrings.TBLSONG_NAME;
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
			mDb.execSQL("DELETE from " + DBStrings.SONGS_TABLE);
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
			mDb.execSQL("DELETE from " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'");
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
			Cursor c = mDb.rawQuery("SELECT " + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_FILE + " FROM " + DBStrings.SONGS_TABLE +
					" WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'", null);
			c.moveToFirst();
			String ret = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_FILE));
			c.close();
			return ret;
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
				mDb.execSQL("INSERT INTO " + DBStrings.SONGGPLOOKUP_TABLE + " (" + DBStrings.TBLSONGGPLOOKUP_SONG + ", " + DBStrings.TBLSONGGPLOOKUP_GROUP + ") " +
						" VALUES ( " +
						" (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'), " +
						" (SELECT " + DBStrings.TBLSONGGROUPS_ID + " FROM " + DBStrings.SONGGROUPS_TABLE + " WHERE " + DBStrings.TBLSONGGROUPS_NAME + " = '" + groupName + "'))");
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
				String query = "DELETE FROM " + DBStrings.SONGGPLOOKUP_TABLE +
						" WHERE " + DBStrings.TBLSONGGPLOOKUP_SONG + " = " +
						" (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "') " +
						" AND " + DBStrings.TBLSONGGPLOOKUP_GROUP + " = " +
						" (SELECT " + DBStrings.TBLSONGGROUPS_ID + " FROM " + DBStrings.SONGGROUPS_TABLE + " WHERE " + DBStrings.TBLSONGGROUPS_NAME + " = '" + groupName + "')";
				mDb.execSQL(query);
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the song author
	 * @param songName The song to get the author for
	 * @return The song author
	 */
	public String getSongAuthor(String songName) {
		try {
			Cursor c = mDb.rawQuery("SELECT " + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_AUTHOR + " FROM " + DBStrings.SONGS_TABLE +
					" WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'", null);
			c.moveToFirst();
			String ret = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_AUTHOR));
			c.close();
			return ret;
		} catch (IndexOutOfBoundsException e) {
			return "";
		} catch (SQLiteException s) {
			return "";
		}
	}
	
	/**
	 * Gets the song key
	 * @param songName The song to get the key for
	 * @return The song key
	 */
	public String getSongKey(String songName) {
		try {
			Cursor c = mDb.rawQuery("SELECT " + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_KEY + " FROM " + DBStrings.SONGS_TABLE +
					" WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'", null);
			c.moveToFirst();
			String ret = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_KEY));
			c.close();
			return ret;
		} catch (IndexOutOfBoundsException e) {
			return "";
		} catch (SQLiteException s) {
			return "";
		}
	}
	
	/**
	 * Updates the song attributes
	 * @param origSongName The current name of the song
	 * @param newSongName The updated name of the song
	 * @param author The song author
	 * @param key The song key
	 * @return
	 */
	public boolean updateSongAttributes(String origSongName, String newSongName, String author, String key) {
		try {
			String query = "UPDATE " + DBStrings.SONGS_TABLE + 
					" SET " + DBStrings.TBLSONG_NAME + " = '" + newSongName + "', " +
					DBStrings.TBLSONG_AUTHOR + " = '" + author + "', " + 
					DBStrings.TBLSONG_KEY + " = '" + key + "' " + 
					" WHERE " + DBStrings.TBLSONG_NAME + " = '" + origSongName + "'";
			mDb.execSQL(query);
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
			mDb.execSQL("UPDATE " + DBStrings.CURRSET_TABLE + " SET " + DBStrings.TBLCURRSET_SET + 
					" = (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "')");
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
 	/**
	 * Gets the songs for the current set
	 * @return Cursor to the songs
	 */
	public Cursor getCurrentSetSongs() {
		try {	
			String query = "SELECT " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.SONGS_TABLE + "." + 
					DBStrings.TBLSONG_NAME + ", " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_FILE + ", " +
					DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_AUTHOR + ", " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_KEY +
					" FROM " + DBStrings.SETLOOKUP_TABLE + ", " + DBStrings.CURRSET_TABLE +
					" INNER JOIN " + DBStrings.SONGS_TABLE + " ON " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SONG + 
					" = " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID +
					" INNER JOIN " + DBStrings.SETS_TABLE + " ON " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SET + 
					" = " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID +
					" WHERE " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + " = " + DBStrings.CURRSET_TABLE + "." + DBStrings.TBLCURRSET_SET;
		
			return mDb.rawQuery(query, null);
		} catch (SQLiteException s) {
			return null;
		}
	}
	
	/**
	 * Gets the current set's name
	 * @return The set name
	 */
	public String getCurrentSetName() {
		try {
			Cursor c = mDb.rawQuery("SELECT " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_NAME + 
					" FROM " + DBStrings.SETS_TABLE + 
					" INNER JOIN " + DBStrings.CURRSET_TABLE + " ON " + DBStrings.CURRSET_TABLE + "." + DBStrings.TBLCURRSET_SET + 
					" = " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID, null);
			c.moveToFirst();
			String ret = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_NAME));
			c.close();
			return ret;
		} catch (IndexOutOfBoundsException e) {
			return "";
		} catch (SQLiteException s) {
			return "";
		}
	}
	
	
	/*****************************************************************************
    *
    * Song Groups Functions
    * 
    *****************************************************************************/	
	/**
	 * Gets a list of the group names
	 * @return A cursor to the query results
	 */
	public Cursor getSongGroupNames() {
		String query = "SELECT " + DBStrings.TBLSONGGROUPS_ID + " as _id, " + DBStrings.TBLSONGGROUPS_NAME + 
				" FROM " + DBStrings.SONGGROUPS_TABLE +
				" ORDER BY " + DBStrings.TBLSONGGROUPS_NAME;
		return mDb.rawQuery(query, null);
	}
	
	/**
	 * Creates a group with the specified name
	 * @param groupName The name of the group
	 * @return True if success, False if failure
	 */
	public boolean createSongGroup(String groupName) {
		try {
			mDb.execSQL( "INSERT INTO " + DBStrings.SONGGROUPS_TABLE + "(" + DBStrings.TBLSONGGROUPS_NAME + ", " + DBStrings.TBLSONGGROUPS_PARENT + ") values ('" + 
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
	public boolean deleteSongGroup(String groupName) {
		try {
			mDb.execSQL("DELETE FROM " + DBStrings.SONGGPLOOKUP_TABLE + " WHERE " + DBStrings.TBLSONGGPLOOKUP_GROUP + 
					" = (SELECT " + DBStrings.TBLSONGGROUPS_ID + " FROM " + DBStrings.SONGGROUPS_TABLE + " WHERE " + DBStrings.TBLSONGGROUPS_NAME + " = '" + groupName + "')");
			mDb.execSQL("DELETE FROM " + DBStrings.SONGGROUPS_TABLE + " WHERE " + DBStrings.TBLSONGGROUPS_NAME + " = '" + groupName + "'");
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Deletes all groups
	 * @return True if success, False if failure
	 */
	public boolean deleteAllSongGroups() {
		try {
			mDb.execSQL("DELETE FROM " + DBStrings.SONGGPLOOKUP_TABLE);
			mDb.execSQL("DELETE FROM " + DBStrings.SONGGROUPS_TABLE + " WHERE " + DBStrings.TBLSONGGROUPS_NAME + " != '" + SongsTab.ALL_SONGS_LABEL + "'");
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the number of songs per group
	 * @param groupName The group 
	 * @return The number of songs
	 */
	public int getNumSongsPerGroup(String groupName) {
		try {
			String query = "SELECT COUNT(" + DBStrings.SONGGPLOOKUP_TABLE + "." + DBStrings.TBLSONGGPLOOKUP_SONG + ") as numSongs " +
					"FROM " + DBStrings.SONGGPLOOKUP_TABLE + " " +
					"INNER JOIN " + DBStrings.SONGS_TABLE + " ON " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + " = " + 
					DBStrings.SONGGPLOOKUP_TABLE + "." + DBStrings.TBLSONGGPLOOKUP_SONG + " " +
					"INNER JOIN " + DBStrings.SONGGROUPS_TABLE + " ON " + DBStrings.SONGGROUPS_TABLE + "." + DBStrings.TBLSONGGROUPS_ID + " = " + 
					DBStrings.SONGGPLOOKUP_TABLE + "." + DBStrings.TBLSONGGPLOOKUP_GROUP + " " +
					"WHERE " + DBStrings.SONGGROUPS_TABLE + "." + DBStrings.TBLSONGGROUPS_NAME + " = '" + groupName + "'";
			Cursor c = mDb.rawQuery(query, null);
			c.moveToFirst();
			int ret = Integer.parseInt(c.getString(c.getColumnIndexOrThrow("numSongs")));
			c.close();
			return ret;
		} catch (IndexOutOfBoundsException e) {
			return 0;
		} catch (SQLiteException s) {
			return 0;
		}
	}
	
	/**
	 * Gets the number of songs 
	 * @return The number of songs
	 */
	public int getNumSongs() {
		try {
			String query = "SELECT COUNT(" + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + ") as numSongs " +
					"FROM " + DBStrings.SONGS_TABLE;
			Cursor c = mDb.rawQuery(query, null);
			c.moveToFirst();
			int ret = Integer.parseInt(c.getString(c.getColumnIndexOrThrow("numSongs")));
			c.close();
			return ret;
		} catch (IndexOutOfBoundsException e) {
			return 0;
		} catch (SQLiteException s) {
			return 0;
		}
	}
	
	
	/*****************************************************************************
    *
    * Set Groups Functions
    * 
    *****************************************************************************/	
	/**
	 * Gets a list of the group names
	 * @return A cursor to the query results
	 */
	public Cursor getSetGroupNames() {
		String query = "SELECT " + DBStrings.TBLSETGROUPS_ID + " as _id, " + DBStrings.TBLSETGROUPS_NAME + 
				" FROM " + DBStrings.SETGROUPS_TABLE +
				" ORDER BY " + DBStrings.TBLSETGROUPS_NAME;
		return mDb.rawQuery(query, null);
	}
	
	/**
	 * Creates a group with the specified name
	 * @param groupName The name of the group
	 * @return True if success, False if failure
	 */
	public boolean createSetGroup(String groupName) {
		try {
			mDb.execSQL( "INSERT INTO " + DBStrings.SETGROUPS_TABLE + "(" + DBStrings.TBLSETGROUPS_NAME + ", " + DBStrings.TBLSETGROUPS_PARENT + ") values ('" + 
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
	public boolean deleteSetGroup(String groupName) {
		try {
			mDb.execSQL("DELETE FROM " + DBStrings.SETGPLOOKUP_TABLE + " WHERE " + DBStrings.TBLSETGPLOOKUP_GROUP + 
					" = (SELECT " + DBStrings.TBLSETGROUPS_ID + " FROM " + DBStrings.SETGROUPS_TABLE + " WHERE " + DBStrings.TBLSETGROUPS_NAME + " = '" + groupName + "')");
			mDb.execSQL("DELETE FROM " + DBStrings.SETGROUPS_TABLE + " WHERE " + DBStrings.TBLSETGROUPS_NAME + " = '" + groupName + "'");
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Deletes all groups
	 * @return True if success, False if failure
	 */
	public boolean deleteAllSetGroups() {
		try {
			mDb.execSQL("DELETE FROM " + DBStrings.SETGPLOOKUP_TABLE);
			mDb.execSQL("DELETE FROM " + DBStrings.SETGROUPS_TABLE + " WHERE " + DBStrings.TBLSETGROUPS_NAME + " != '" + SetsTab.ALL_SETS_LABEL + "'");
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the number of sets per group
	 * @param groupName The group 
	 * @return The number of sets
	 */
	public int getNumSetsPerGroup(String groupName) {
		try {
			String query = "SELECT COUNT(" + DBStrings.SETGPLOOKUP_TABLE + "." + DBStrings.TBLSETGPLOOKUP_SET + ") as numSets " +
					"FROM " + DBStrings.SETGPLOOKUP_TABLE + " " +
					"INNER JOIN " + DBStrings.SETS_TABLE + " ON " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + " = " + 
					DBStrings.SETGPLOOKUP_TABLE + "." + DBStrings.TBLSETGPLOOKUP_SET + " " +
					"INNER JOIN " + DBStrings.SETGROUPS_TABLE + " ON " + DBStrings.SETGROUPS_TABLE + "." + DBStrings.TBLSETGROUPS_ID + " = " + 
					DBStrings.SETGPLOOKUP_TABLE + "." + DBStrings.TBLSETGPLOOKUP_GROUP + " " +
					"WHERE " + DBStrings.SETGROUPS_TABLE + "." + DBStrings.TBLSETGROUPS_NAME + " = '" + groupName + "'";
			Cursor c = mDb.rawQuery(query, null);
			c.moveToFirst();
			int ret = Integer.parseInt(c.getString(c.getColumnIndexOrThrow("numSets")));
			c.close();
			return ret;
		} catch (IndexOutOfBoundsException e) {
			return 0;
		} catch (SQLiteException s) {
			return 0;
		}
	}
	
	/**
	 * Gets the number of sets 
	 * @return The number of sets
	 */
	public int getNumSets() {
		try {
			String query = "SELECT COUNT(" + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + ") as numSets " +
					"FROM " + DBStrings.SETS_TABLE;
			Cursor c = mDb.rawQuery(query, null);
			c.moveToFirst();
			int ret = Integer.parseInt(c.getString(c.getColumnIndexOrThrow("numSets")));
			c.close();
			return ret;
		} catch (IndexOutOfBoundsException e) {
			return 0;
		} catch (SQLiteException s) {
			return 0;
		}
	}
	
	
	/*****************************************************************************
    *
    * DBHelper Class
    * 
    *****************************************************************************/
	private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DBStrings.DATABASE_NAME, null, DBStrings.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
    		// Add the tables
    		try{
    			db.beginTransaction();
    			
    			// Sets table
    			db.execSQL("create table " + DBStrings.SETS_TABLE +
    					"(" + DBStrings.TBLSETS_ID + " integer PRIMARY KEY autoincrement, " + 
    					DBStrings.TBLSETS_DATE + " date, " +
    					DBStrings.TBLSETS_NAME + " text UNIQUE); ");
    			
    			// Songs table
    			db.execSQL("create table " + DBStrings.SONGS_TABLE +
    					"(" + DBStrings.TBLSONG_ID + " integer PRIMARY KEY autoincrement, " + 
    					DBStrings.TBLSONG_NAME + " text UNIQUE, " + 
    					DBStrings.TBLSONG_AUTHOR + " text, " +
    					DBStrings.TBLSONG_KEY + " text, " +
    					DBStrings.TBLSONG_FILE + " text); " );
    			
    			// Song Group table
    			db.execSQL("create table " + DBStrings.SONGGROUPS_TABLE +
    					"(" + DBStrings.TBLSONGGROUPS_ID + " integer PRIMARY KEY autoincrement, " + 
    					DBStrings.TBLSONGGROUPS_NAME + " text UNIQUE, " + 
    					DBStrings.TBLSONGGROUPS_PARENT + " int ); " );
    			
    			// Set Group table
    			db.execSQL("create table " + DBStrings.SETGROUPS_TABLE +
    					"(" + DBStrings.TBLSETGROUPS_ID + " integer PRIMARY KEY autoincrement, " + 
    					DBStrings.TBLSETGROUPS_NAME + " text UNIQUE, " + 
    					DBStrings.TBLSETGROUPS_PARENT + " int ); " );
    			
    			// Current Set table
    			db.execSQL("create table " + DBStrings.CURRSET_TABLE +
    					"(" + DBStrings.TBLCURRSET_ID + " integer PRIMARY KEY autoincrement, " + 
    					DBStrings.TBLCURRSET_SET + " int ); " );
    			
    			// Set lookup table
    			db.execSQL("create table " + DBStrings.SETLOOKUP_TABLE +
    					"(" + DBStrings.TBLSLOOKUP_ID + " integer PRIMARY KEY autoincrement, " + 
    					DBStrings.TBLSLOOKUP_SET + " int, " + 
    					DBStrings.TBLSLOOKUP_SONG + " int ); " );
    			
    			// Song Group lookup table
    			db.execSQL("create table " + DBStrings.SONGGPLOOKUP_TABLE +
    					"(" + DBStrings.TBLSONGGPLOOKUP_ID + " integer PRIMARY KEY autoincrement, " + 
    					DBStrings.TBLSONGGPLOOKUP_SONG + " int, " + 
    					DBStrings.TBLSONGGPLOOKUP_GROUP + " int ); " );
    			
    			// Set Group lookup table
    			db.execSQL("create table " + DBStrings.SETGPLOOKUP_TABLE +
    					"(" + DBStrings.TBLSETGPLOOKUP_ID + " integer PRIMARY KEY autoincrement, " + 
    					DBStrings.TBLSETGPLOOKUP_SET + " int, " + 
    					DBStrings.TBLSETGPLOOKUP_GROUP + " int ); " );
    			
    			// Add default values
    			db.execSQL("insert into " + DBStrings.CURRSET_TABLE + "(" + DBStrings.TBLCURRSET_SET + ") values (0);" );
    			db.execSQL("INSERT INTO " + DBStrings.SONGGROUPS_TABLE + "(" + DBStrings.TBLSONGGROUPS_NAME + ", " + DBStrings.TBLSONGGROUPS_PARENT + ") VALUES ('" + SongsTab.ALL_SONGS_LABEL + "', -1)");
    			db.execSQL("INSERT INTO " + DBStrings.SETGROUPS_TABLE + "(" + DBStrings.TBLSETGROUPS_NAME + ", " + DBStrings.TBLSETGROUPS_PARENT + ") VALUES ('" + SetsTab.ALL_SETS_LABEL + "', -1)");
    			
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
