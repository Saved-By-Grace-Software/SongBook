package com.sbgsoft.songbook.db;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sbgsoft.songbook.main.MainStrings;
import com.sbgsoft.songbook.sets.SetsTab;
import com.sbgsoft.songbook.songs.SongsTab;
import com.sbgsoft.songbook.songs.TimeSignature;

public class DBAdapter {
	
	// *****************************************************************************
    // *
    // * Class Variables
    // * 
    // *****************************************************************************
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;
	private static final String TAG = "TabAppDBAdapter";
    private final Context mCtx;
    
    //region Class Functions
    // *****************************************************************************
    // *
    // * Class Functions
    // * 
    // *****************************************************************************
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
    //endregion

    
    //region Set Functions
    // *****************************************************************************
    // *
    // * Set Functions
    // * 
    // *****************************************************************************
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
		String song = "";
		int order = 1;
		// Create a new set with the specified name
		try {
			// Add the set name
			if (createSet(setName, setDate)) {
				// Add the songs to the set
				for(int i = 0; i < setSongs.size(); i++) {
					song = setSongs.get(i);
					order = i + 1;
					if (song != "") {
						mDb.execSQL( "INSERT INTO " + DBStrings.SETLOOKUP_TABLE + "(" + DBStrings.TBLSLOOKUP_SET + ", " + 
								DBStrings.TBLSLOOKUP_SONG + ", " + DBStrings.TBLSLOOKUP_KEY + ", " + DBStrings.TBLSLOOKUP_ORDER + ") " + 
								" VALUES ((SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "'), " + 
								" (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + song + "'), " +
								" (SELECT " + DBStrings.TBLSONG_KEY + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + song + "'), " +
								" " + order + " );" );
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
		String song = "";
		int order = 1;
		
		try {
			// Delete the songs from the sets lookup table
			mDb.execSQL("DELETE FROM " + DBStrings.SETLOOKUP_TABLE + " WHERE " + DBStrings.TBLSLOOKUP_SET +
					" = (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "')");
						
			// Add the songs in the new order
			for(int i = 0; i < songs.length; i++) {
				song = songs[i];
				order = i + 1;
				if (song != "") {
					mDb.execSQL( "INSERT INTO " + DBStrings.SETLOOKUP_TABLE + "(" + DBStrings.TBLSLOOKUP_SET + ", " + 
							DBStrings.TBLSLOOKUP_SONG + ", " + DBStrings.TBLSLOOKUP_KEY + ", " + DBStrings.TBLSLOOKUP_ORDER + ") " +
							" VALUES ((SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "'), " + 
							" (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + song + "'), " +
							" (SELECT " + DBStrings.TBLSONG_KEY + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + song + "'), " +
							order + ");" );
				}
			}
		} catch (SQLiteException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Reorders the songs in the set
	 * @param setName The set to reorder
	 * @param newOrder A string array with songs in the new order
	 * @return True if success, False if failure
	 */
	public boolean reorderSet(String setName, String[] newOrder) {
		int order = 0;
		
		try {
            // Loop through the new order and update the order for each item
            for(String song : newOrder) {
                // Increment the order
                order++;

                // Execute the update query for this song
                String query = "UPDATE " + DBStrings.SETLOOKUP_TABLE + " SET " + DBStrings.TBLSLOOKUP_ORDER + " = " + order + " " +
                        "WHERE " + DBStrings.TBLSLOOKUP_SET + " = " +
                        "(SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "') " +
                        "AND " + DBStrings.TBLSLOOKUP_SONG + " = " +
                        "(SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + song + "') ";
                mDb.execSQL(query);
            }
		} catch (SQLiteException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Adds the song to the set
	 * @param setName The set to add to
	 * @param songName The song to add
	 * @return True if success, False if failure
	 */
	public boolean addSongToSet(String setName, String songName) {
		try {
			String query = "";
			
			// Get order number
			query = "SELECT max(" + DBStrings.TBLSLOOKUP_ORDER + ") as maxOrder " +
					"FROM " + DBStrings.SETLOOKUP_TABLE + " " +
					"WHERE " + DBStrings.TBLSLOOKUP_SET + " = " +
					"(SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + 
					DBStrings.TBLSETS_NAME + " = '" + setName + "')";
			Cursor c = mDb.rawQuery(query, null);
			c.moveToFirst();
			int order = c.getInt(c.getColumnIndexOrThrow("maxOrder")) + 1;
			
			// Add the song to the set
			query = "INSERT INTO " + DBStrings.SETLOOKUP_TABLE + "(" + DBStrings.TBLSLOOKUP_SET + ", " + 
					DBStrings.TBLSLOOKUP_SONG + ", " + DBStrings.TBLSLOOKUP_KEY + ", " + DBStrings.TBLSLOOKUP_ORDER + ") " +
					" VALUES ((SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "'), " + 
					" (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'), " +
					" (SELECT " + DBStrings.TBLSONG_KEY + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'), " +
					order + ");";
			mDb.execSQL(query);
		} catch (SQLiteException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Removes the song from the set
	 * @param setName The set to remove the song from
	 * @param songName The song to remove
	 * @return True if success, False if failure
	 */
	public boolean removeSongFromSet(String setName, String songName, int songOrder) {
		try {
			String query;

            // Set local order because of 1 based counting
            int order = songOrder + 1;
			
			// Remove the song from the set
			query = "DELETE FROM " + DBStrings.SETLOOKUP_TABLE + " WHERE " + DBStrings.TBLSLOOKUP_SET + " = " +
					" (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "') " + 
					" AND " + DBStrings.TBLSLOOKUP_SONG + " = " +
					" (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "') " +
                    " AND " + DBStrings.TBLSLOOKUP_ORDER + " = " + order;
			mDb.execSQL(query);
			
			// Update the set order
			query = "UPDATE " + DBStrings.SETLOOKUP_TABLE + " SET " + DBStrings.TBLSLOOKUP_ORDER + " = " + DBStrings.TBLSLOOKUP_ORDER + " - 1" + " " +
					"WHERE " + DBStrings.TBLSLOOKUP_SET + " = " +
					"(SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "') " + 
					"AND " + DBStrings.TBLSLOOKUP_ORDER + " > " + order;
			mDb.execSQL(query);
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
					", " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_FILE + ", " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_AUTHOR + 
					", " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_KEY +
					" FROM " + DBStrings.SETLOOKUP_TABLE + ", " + DBStrings.CURRSET_TABLE +
					" INNER JOIN " + DBStrings.SONGS_TABLE + " ON " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SONG + 
					" = " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID +
					" INNER JOIN " + DBStrings.SETS_TABLE + " ON " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SET + 
					" = " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID +
					" WHERE " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + " = (SELECT " + DBStrings.TBLSETS_ID + 
					" FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "')" +
					" ORDER BY " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_ORDER;
			
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
			mDb.execSQL("DELETE FROM " + DBStrings.SETGPLOOKUP_TABLE);
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
	//endregion


    //region Song Functions
    // *****************************************************************************
    // *
    // * Song Functions
    // * 
    // *****************************************************************************
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
	public Cursor getSongs(String groupName) {
		String query = "";
		
		// Check if the group is the all songs group
		if (groupName.equals(SongsTab.ALL_SONGS_LABEL)) {
			query = "SELECT " + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_FILE + ", " +
					DBStrings.TBLSONG_AUTHOR + ", " + DBStrings.TBLSONG_KEY + ", " + DBStrings.TBLSONG_BPM + ", " +
                    DBStrings.TBLSONG_LINK + ", " + DBStrings.TBLSONG_TIME +
					" FROM " + DBStrings.SONGS_TABLE + " ORDER BY " + DBStrings.TBLSONG_NAME;
		} else {
			query = "SELECT " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + " as _id, " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_NAME + ", " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_FILE + ", " +
					DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_AUTHOR + ", " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_KEY + ", " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_BPM + ", " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_LINK + ", " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_TIME +
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
			mDb.execSQL("DELETE FROM " + DBStrings.SONGGPLOOKUP_TABLE);
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
			mDb.execSQL("DELETE from " + DBStrings.SONGGPLOOKUP_TABLE + " WHERE " + DBStrings.TBLSONGGPLOOKUP_SONG +
                    " = ( SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " +
                    DBStrings.TBLSONG_NAME + " = '" + songName + "')");
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
			if(!isSongInGroup(songName, groupName) && !groupName.equals(SongsTab.ALL_SONGS_LABEL)) {
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
     * Determines if the specified song is in the specified group
     * @param songName The song name
     * @param groupName The group name
     * @return True if in the group, false if not
     */
	public boolean isSongInGroup(String songName, String groupName) {
		boolean ret = false;
		
		try {
			Cursor c = mDb.rawQuery("SELECT * FROM " + DBStrings.SONGGPLOOKUP_TABLE +
					" WHERE " +
					DBStrings.TBLSONGGPLOOKUP_SONG + " =  (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "') AND " +
					DBStrings.TBLSONGGPLOOKUP_GROUP + " = (SELECT " + DBStrings.TBLSONGGROUPS_ID + " FROM " + DBStrings.SONGGROUPS_TABLE + " WHERE " + DBStrings.TBLSONGGROUPS_NAME + " = '" + groupName + "')", null);
			if (c.moveToFirst())
				ret = true;
			c.close();
		} catch (IndexOutOfBoundsException e) {
			ret = false;
		} catch (SQLiteException s) {
			ret = false;
		}
		
		return ret;
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
     * Gets the song beats per minute
     * @param songName The song to get the beats per minute for
     * @return The song beats per minute
     */
    public int getSongBpm(String songName) {
        try {
            Cursor c = mDb.rawQuery("SELECT " + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_BPM + " FROM " + DBStrings.SONGS_TABLE +
                    " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'", null);
            c.moveToFirst();
            int ret = c.getInt(c.getColumnIndexOrThrow(DBStrings.TBLSONG_BPM));
            c.close();
            return ret;
        } catch (IndexOutOfBoundsException e) {
            return -1;
        } catch (SQLiteException s) {
            return -1;
        }
    }

    /**
     * Gets the song time signature
     * @param songName The song to get the time signature for
     * @return The song time signature
     */
    public TimeSignature getSongTimeSignature(String songName) {
        try {
            Cursor c = mDb.rawQuery("SELECT " + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_TIME + " FROM " + DBStrings.SONGS_TABLE +
                    " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'", null);
            c.moveToFirst();
            String ts = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_TIME));
            c.close();
            return new TimeSignature(ts);
        } catch (IndexOutOfBoundsException e) {
            return null;
        } catch (SQLiteException s) {
            return null;
        }
    }

    /**
     * Gets the song link
     * @param songName The song to get the link for
     * @return The song link
     */
    public String getSongLink(String songName) {
        try {
            Cursor c = mDb.rawQuery("SELECT " + DBStrings.TBLSONG_ID + " as _id, " + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_LINK + " FROM " + DBStrings.SONGS_TABLE +
                    " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'", null);
            c.moveToFirst();
            String ret = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_LINK));
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
     * @param timeSignature The song time signature
	 * @return
	 */
	public boolean updateSongAttributes(String origSongName, String newSongName, String author, String key, String timeSignature, String songLink) {
		try {
			String query = "UPDATE " + DBStrings.SONGS_TABLE + 
					" SET " + DBStrings.TBLSONG_NAME + " = '" + newSongName + "', " +
					DBStrings.TBLSONG_AUTHOR + " = '" + author + "', " + 
					DBStrings.TBLSONG_KEY + " = '" + key + "', " +
                    DBStrings.TBLSONG_LINK + " = '" + songLink + "', " +
                    DBStrings.TBLSONG_TIME + " = '" + timeSignature + "' " +
					" WHERE " + DBStrings.TBLSONG_NAME + " = '" + origSongName + "'";
			mDb.execSQL(query);
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

    /**
     * Updates the song attributes
     * @param origSongName The current name of the song
     * @param newSongName The updated name of the song
     * @param author The song author
     * @param key The song key
     * @param bpm The song beats per minute
     * @param timeSignature The song time signature
     * @return
     */
    public boolean updateSongAttributes(String origSongName, String newSongName, String author, String key, String timeSignature, String songLink, int bpm) {
        try {
            String query = "UPDATE " + DBStrings.SONGS_TABLE +
                    " SET " + DBStrings.TBLSONG_NAME + " = '" + newSongName + "', " +
                    DBStrings.TBLSONG_AUTHOR + " = '" + author + "', " +
                    DBStrings.TBLSONG_KEY + " = '" + key + "', " +
                    DBStrings.TBLSONG_BPM + " = " + bpm + ", " +
                    DBStrings.TBLSONG_LINK + " = '" + songLink + "', " +
                    DBStrings.TBLSONG_TIME + " = '" + timeSignature + "' " +
                    " WHERE " + DBStrings.TBLSONG_NAME + " = '" + origSongName + "'";
            mDb.execSQL(query);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

	/**
	 * Updates the song key for the set
	 * @param setName The set to update
	 * @param songName The song to update
	 * @param newKey The key to set
	 * @return
	 */
	public boolean setSongKeyForSet(String setName, String songName, String newKey) {
		try {
			String query = "UPDATE " + DBStrings.SETLOOKUP_TABLE + " SET " + DBStrings.TBLSLOOKUP_KEY + " = '" + newKey + "'" +
					" WHERE " + DBStrings.TBLSLOOKUP_SET + 
					" = (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "')" +
					" AND " + DBStrings.TBLSLOOKUP_SONG +
					" = (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "')";
			mDb.execSQL(query);
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the song key for the specified song and set
	 * @param setName The name of the set
	 * @param songName The name of the song
	 * @return String value containing the song key for that set
	 */
	public String getSongKeyForSet(String setName, String songName) {
		String ret = "";
		try {
			String query = "SELECT " + DBStrings.TBLSLOOKUP_KEY + " " +
							"FROM " + DBStrings.SETLOOKUP_TABLE + " " +
							"WHERE " + DBStrings.TBLSLOOKUP_SET + 
							" = (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "')" +
							" AND " + DBStrings.TBLSLOOKUP_SONG +
							" = (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "')";
			Cursor c = mDb.rawQuery(query, null);
			c.moveToFirst();
			ret = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_KEY));
			c.close();
		} catch (SQLException e) {
			ret = "";
		}
		
		return ret;
	}
	
	/**
	 * Gets the list of last times a song was used
	 * @param songName The song to query
	 * @return The list of times the song was used in a set
	 */
	public Cursor getSongLastFive(String songName) {
		String query = "";
		
		query = "SELECT " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_NAME + ", " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_DATE + " " +
				"FROM " + DBStrings.SETLOOKUP_TABLE + ", " + DBStrings.SETS_TABLE + " " +
				"WHERE " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + " = " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SET + " and " +
				DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SONG + " = " +
				"(SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "') " +
				"ORDER BY date(" + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_DATE + ") DESC " +
				"LIMIT 5";
		
		return mDb.rawQuery(query, null);
	}
	
	/**
	 * Returns the total usage percentage of the song
	 * @param songName The song to get usage for
	 * @return Percentage of use
	 */
	public float getSongTotalUsage(String songName) {
		int totalSets = 0;
		int songSets = 0;
		float percentage = 0;
		
		try {
			// Get the total number of sets
			String query = "SELECT COUNT(DISTINCT " + DBStrings.TBLSETS_ID + ") AS total FROM " + DBStrings.SETS_TABLE;
			Cursor c = mDb.rawQuery(query, null);
			c.moveToFirst();
			totalSets = c.getInt(c.getColumnIndexOrThrow("total"));
			
			// Get the number of sets the song is in
			query = "SELECT COUNT(DISTINCT " + DBStrings.TBLSLOOKUP_SET + ") AS sets " +
					"FROM " + DBStrings.SETLOOKUP_TABLE + " " + 
					"WHERE " + DBStrings.TBLSLOOKUP_SONG + " = " +
					"(SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "')";
			c = mDb.rawQuery(query, null);
			c.moveToFirst();
			songSets = c.getInt(c.getColumnIndexOrThrow("sets"));
			
			// Calculate the percentage
			percentage = ((float)songSets / (float)totalSets) * 100;
			
			c.close();
		} catch (IndexOutOfBoundsException e) {
			return 0;
		} catch (SQLiteException s) {
			return 0;
		}
		
		return percentage;
	}
	
	/**
	 * Gets the groups of which the song is a member
	 * @param songName The song to query
	 * @return The list of song groups
	 */
	public Cursor getSongGroups(String songName) {
		String query = "";
		
		query = "SELECT " + DBStrings.SONGGROUPS_TABLE + "." + DBStrings.TBLSONGGROUPS_NAME + " " +
				"FROM " + DBStrings.SONGGROUPS_TABLE + ", " + DBStrings.SONGGPLOOKUP_TABLE + " " +
				"WHERE " + DBStrings.SONGGROUPS_TABLE + "." + DBStrings.TBLSONGGROUPS_ID + " = " + DBStrings.SONGGPLOOKUP_TABLE + "." + DBStrings.TBLSONGGPLOOKUP_GROUP + " and " +
				DBStrings.SONGGPLOOKUP_TABLE + "." + DBStrings.TBLSONGGPLOOKUP_SONG + " = " +
				"(SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "')";
		
		return mDb.rawQuery(query, null);
	}

    /**
     * Gets the set order for the specified song and set
     * @param setName The set
     * @param songName The song
     * @return An integer with the set order
     */
    public int getSetOrderForSong(String setName, String songName) {
        int ret;

        try {
            String query = "SELECT " + DBStrings.TBLSLOOKUP_ORDER +
            " FROM " + DBStrings.SETLOOKUP_TABLE +
            " WHERE setID = (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE setName = '" + setName + "')" +
            " AND songID = (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE songName = '" + songName + "')";

            Cursor c = mDb.rawQuery(query, null);
            c.moveToFirst();
            ret = c.getInt(c.getColumnIndexOrThrow(DBStrings.TBLSLOOKUP_ORDER));
            c.close();
        } catch (IndexOutOfBoundsException e) {
            ret = -1;
        } catch (SQLiteException s) {
            ret = -1;
        }

        return ret;
    }
	//endregion


	//region Current Set Functions
	// *****************************************************************************
    // *
    // * Current Set Functions
    // * 
    // *****************************************************************************
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
			String query = "SELECT " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + " as _id, " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_NAME + ", " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_FILE + ", " +
					DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_AUTHOR + ", " +
                    DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_KEY + ", " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_BPM + ", " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_LINK + ", " +
                    DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_TIME +
                    " FROM " + DBStrings.SETLOOKUP_TABLE + ", " + DBStrings.CURRSET_TABLE +
					" INNER JOIN " + DBStrings.SONGS_TABLE + " ON " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SONG + 
					" = " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID +
					" INNER JOIN " + DBStrings.SETS_TABLE + " ON " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SET + 
					" = " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID +
					" WHERE " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + " = " + DBStrings.CURRSET_TABLE + "." + DBStrings.TBLCURRSET_SET +
					" ORDER BY " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_ORDER;
		
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
	//endregion


	//region Song Group Functions
	// *****************************************************************************
    // *
    // * Song Groups Functions
    // * 
    // *****************************************************************************	
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
	//endregion


    //region Set Group Functions
	// *****************************************************************************
    // *
    // * Set Groups Functions
    // * 
    // *****************************************************************************	
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
	//endregion


    //region Import / Export Functions
	// *****************************************************************************
    // *
    // * Import / Export Functions
    // * 
    // *****************************************************************************
	/**
	 * Exports all the database data into an sql file
	 * @return True if success, False if failure
	 */
	public String exportDBData() {
		StringBuilder output = new StringBuilder();
		try {
			// Add songs to the export file
			String query = "SELECT * FROM " + DBStrings.SONGS_TABLE;
			Cursor c = mDb.rawQuery(query, null);
			
			while(c.moveToNext()) {
				// Get the song properties
				String songName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
				String songFileName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_FILE));
				String author = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_AUTHOR));
				String key = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_KEY));
				
				// Append the insert statement with a line ending
				output.append("INSERT INTO " + DBStrings.SONGS_TABLE + "(" + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_FILE + ", " +
						DBStrings.TBLSONG_AUTHOR + ", " + DBStrings.TBLSONG_KEY + 
						") VALUES ('" + songName + "', '" + songFileName + "', '" + author + "', '" + key + "'); ");
				output.append(MainStrings.EOL);
			}
			
			// Add sets to the export file
			query = "SELECT * FROM " + DBStrings.SETS_TABLE;
			c = mDb.rawQuery(query, null);
			
			while(c.moveToNext()) {
				// Get the song properties
				String setName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_NAME));
				String setDate = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_DATE));
				
				// Append the insert statement with a line ending
				output.append("INSERT INTO " + DBStrings.SETS_TABLE + "(" + DBStrings.TBLSETS_NAME + ", " + DBStrings.TBLSETS_DATE +
						") VALUES ('" + setName + "', '" + setDate + "'); ");
				output.append(MainStrings.EOL);
			}
			
			// Add set lookup to the export file
			query = "SELECT " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_NAME + ", " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_NAME + ", " +
					DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_KEY + ", " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_ORDER + 
					" FROM " + DBStrings.SETLOOKUP_TABLE +
					" INNER JOIN " + DBStrings.SONGS_TABLE + " ON " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + 
					" = " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SONG +
					" INNER JOIN " + DBStrings.SETS_TABLE + " ON " +DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + 
					" = " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SET;
			c = mDb.rawQuery(query, null);
			
			while(c.moveToNext()) {
				// Get the song properties
				String songName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
				String setName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_NAME));
				String songKey = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSLOOKUP_KEY));
				int order = c.getInt(c.getColumnIndexOrThrow(DBStrings.TBLSLOOKUP_ORDER));
				
				// Append the insert statement with a line ending
				output.append("INSERT INTO " + DBStrings.SETLOOKUP_TABLE + "(" + DBStrings.TBLSLOOKUP_SET + ", " + DBStrings.TBLSLOOKUP_SONG + ", " + 
						DBStrings.TBLSLOOKUP_KEY + ", " + DBStrings.TBLSLOOKUP_ORDER + ") " + 
						" VALUES ((SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "'), " + 
						" (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'), " +
						"'" + songKey + "', " + order + "); ");
				output.append(MainStrings.EOL);
			}
			
			// Add song groups to the export file
			query = "SELECT * FROM " + DBStrings.SONGGROUPS_TABLE;
			c = mDb.rawQuery(query, null);
			
			while(c.moveToNext()) {
				// Get the song properties
				String groupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME));
				String parentID = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_PARENT));
				
				// Append the insert statement with a line ending
				output.append("INSERT INTO " + DBStrings.SONGGROUPS_TABLE + "(" + DBStrings.TBLSONGGROUPS_NAME + ", " + 
						DBStrings.TBLSONGGROUPS_PARENT + ") values ('" + groupName + "', " + parentID + " ); ");
				output.append(MainStrings.EOL);
			}
			
			
			// Add song group lookup to the export file
			query = "SELECT " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_NAME + ", " + DBStrings.SONGGROUPS_TABLE + "." + DBStrings.TBLSONGGROUPS_NAME +
					" FROM " + DBStrings.SONGGPLOOKUP_TABLE +
					" INNER JOIN " + DBStrings.SONGS_TABLE + " ON " + DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + 
					" = " + DBStrings.SONGGPLOOKUP_TABLE + "." + DBStrings.TBLSONGGPLOOKUP_SONG +
					" INNER JOIN " + DBStrings.SONGGROUPS_TABLE + " ON " + DBStrings.SONGGROUPS_TABLE + "." + DBStrings.TBLSONGGROUPS_ID + 
					" = " + DBStrings.SONGGPLOOKUP_TABLE + "." + DBStrings.TBLSONGGPLOOKUP_GROUP;
			c = mDb.rawQuery(query, null);
			
			while(c.moveToNext()) {
				// Get the song properties
				String songName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
				String groupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME));
				
				// Append the insert statement with a line ending
				output.append("INSERT INTO " + DBStrings.SONGGPLOOKUP_TABLE + "(" + DBStrings.TBLSONGGPLOOKUP_GROUP + ", " + DBStrings.TBLSONGGPLOOKUP_SONG + ") " + 
						" VALUES ((SELECT " + DBStrings.TBLSONGGROUPS_ID + " FROM " + DBStrings.SONGGROUPS_TABLE + " WHERE " + DBStrings.TBLSONGGROUPS_NAME + " = '" + groupName + "'), " + 
						" (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "') ); ");
				output.append(MainStrings.EOL);
			}
			
			// Add set groups to the export file
			query = "SELECT * FROM " + DBStrings.SETGROUPS_TABLE;
			c = mDb.rawQuery(query, null);
			
			while(c.moveToNext()) {
				// Get the song properties
				String groupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME));
				String parentID = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_PARENT));
				
				// Append the insert statement with a line ending
				output.append("INSERT INTO " + DBStrings.SETGROUPS_TABLE + "(" + DBStrings.TBLSETGROUPS_NAME + ", " + 
						DBStrings.TBLSETGROUPS_PARENT + ") values ('" + groupName + "', " + parentID + " ); ");
				output.append(MainStrings.EOL);
			}
			
			
			// Add set group lookup to the export file
			query = "SELECT " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_NAME + ", " + DBStrings.SETGROUPS_TABLE + "." + DBStrings.TBLSETGROUPS_NAME +
					" FROM " + DBStrings.SETGPLOOKUP_TABLE +
					" INNER JOIN " + DBStrings.SETS_TABLE + " ON " + DBStrings.SETS_TABLE + "." + DBStrings.TBLSETS_ID + 
					" = " + DBStrings.SETGPLOOKUP_TABLE + "." + DBStrings.TBLSETGPLOOKUP_SET +
					" INNER JOIN " + DBStrings.SETGROUPS_TABLE + " ON " + DBStrings.SETGROUPS_TABLE + "." + DBStrings.TBLSETGROUPS_ID + 
					" = " + DBStrings.SETGPLOOKUP_TABLE + "." + DBStrings.TBLSETGPLOOKUP_GROUP;
			c = mDb.rawQuery(query, null);
			
			while(c.moveToNext()) {
				// Get the song properties
				String setName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_NAME));
				String groupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME));
				
				// Append the insert statement with a line ending
				output.append("INSERT INTO " + DBStrings.SETGPLOOKUP_TABLE + "(" + DBStrings.TBLSETGPLOOKUP_GROUP + ", " + DBStrings.TBLSETGPLOOKUP_SET + ") " + 
						" VALUES ((SELECT " + DBStrings.TBLSETGROUPS_ID + " FROM " + DBStrings.SETGROUPS_TABLE + " WHERE " + DBStrings.TBLSETGROUPS_NAME + " = '" + groupName + "'), " + 
						" (SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "') ); ");
				output.append(MainStrings.EOL);
			}
			
			// Close the cursor
			c.close();
		}
		catch (Exception e) {
			// Error
		}
		
		return output.toString();
	}

    public String exportSetDBData(String setName) {
        StringBuilder output = new StringBuilder();
        try {
            // Add the set to the export file
            String setDate = getSetDate(setName);

            // Append the insert statement with a line ending
            output.append("INSERT INTO " + DBStrings.SETS_TABLE + "(" + DBStrings.TBLSETS_NAME + ", " + DBStrings.TBLSETS_DATE +
                    ") VALUES ('" + setName + "', '" + setDate + "'); ");
            output.append(MainStrings.EOL);

            // Add songs to the export file
            Cursor c = getSetSongs(setName);

            while(c.moveToNext()) {
                // Get the song properties
                String songName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
                String songFileName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_FILE));
                String author = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_AUTHOR));
                String key = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_KEY));
                String setKey = getSongKeyForSet(setName, songName);
                int setOrder = getSetOrderForSong(setName, songName);

                // Append the insert statement with a line ending for adding the song
                output.append("INSERT INTO " + DBStrings.SONGS_TABLE + "(" + DBStrings.TBLSONG_NAME + ", " + DBStrings.TBLSONG_FILE + ", " +
                        DBStrings.TBLSONG_AUTHOR + ", " + DBStrings.TBLSONG_KEY +
                        ") VALUES ('" + songName + "', '" + songFileName + "', '" + author + "', '" + key + "'); ");
                output.append(MainStrings.EOL);

                // Append the insert statement with a line ending for adding the set lookup
                output.append("INSERT INTO " + DBStrings.SETLOOKUP_TABLE + "(" + DBStrings.TBLSLOOKUP_SET + ", " + DBStrings.TBLSLOOKUP_SONG + ", " +
                        DBStrings.TBLSLOOKUP_KEY + ", " + DBStrings.TBLSLOOKUP_ORDER + ") " +
                        " VALUES ((SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE + " WHERE " + DBStrings.TBLSETS_NAME + " = '" + setName + "'), " +
                        " (SELECT " + DBStrings.TBLSONG_ID + " FROM " + DBStrings.SONGS_TABLE + " WHERE " + DBStrings.TBLSONG_NAME + " = '" + songName + "'), " +
                        "'" + setKey + "', " + setOrder + "); ");
                output.append(MainStrings.EOL);
            }

            // Close the cursor
            c.close();
        }
        catch (Exception e) {
            // Error
        }

        return output.toString();
    }
	
	/**
	 * Runs the specified sql statement to import data
	 * @param sqlQuery The sql query to run
	 * @return True if success, False if failure
	 */
	public boolean importDBData(String sqlQuery) {
        mDb.beginTransaction();
        String queries[] = sqlQuery.split(System.getProperty("line.separator"));
        for (String query : queries) {
            try {
                mDb.execSQL(query);
            } catch (SQLException e) {
                // Failed this query, move to the next
            }
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
		return true;
	}
	
	/**
	 * Clears the database of all data and then inserts the default values
	 * @return True if successful, False if failure
	 */
	public boolean clearDB() {
		try {
			// Clear tables
			mDb.execSQL("DELETE FROM " + DBStrings.SETGPLOOKUP_TABLE);
			mDb.execSQL("DELETE FROM " + DBStrings.SONGGPLOOKUP_TABLE);
			mDb.execSQL("DELETE FROM " + DBStrings.SETGROUPS_TABLE);
			mDb.execSQL("DELETE FROM " + DBStrings.SONGGROUPS_TABLE);
			mDb.execSQL("DELETE FROM " + DBStrings.SETS_TABLE);
			mDb.execSQL("DELETE FROM " + DBStrings.SONGS_TABLE);
			mDb.execSQL("DELETE FROM " + DBStrings.CURRSET_TABLE);
			mDb.execSQL("DELETE FROM " + DBStrings.SETLOOKUP_TABLE);
			
			// Add default values
			mDb.execSQL("insert into " + DBStrings.CURRSET_TABLE + "(" + DBStrings.TBLCURRSET_SET + ") values (0);" );
			
		} catch (SQLException e) {
			return false;
		} 
		return true;
	}
	
	/**
	 * Adds the default values into the database
	 * @return True if success, False if failure
	 */
	public boolean addDBDefaults() {
		try {
			mDb.execSQL("DELETE FROM " + DBStrings.CURRSET_TABLE);
			mDb.execSQL("insert into " + DBStrings.CURRSET_TABLE + "(" + DBStrings.TBLCURRSET_SET + ") values (0);" );
			mDb.execSQL("INSERT INTO " + DBStrings.SONGGROUPS_TABLE + "(" + DBStrings.TBLSONGGROUPS_NAME + ", " + DBStrings.TBLSONGGROUPS_PARENT + ") VALUES ('" + SongsTab.ALL_SONGS_LABEL + "', -1)");
			mDb.execSQL("INSERT INTO " + DBStrings.SETGROUPS_TABLE + "(" + DBStrings.TBLSETGROUPS_NAME + ", " + DBStrings.TBLSETGROUPS_PARENT + ") VALUES ('" + SetsTab.ALL_SETS_LABEL + "', -1)");
		} catch (SQLException e) {
			return false;
		} 
		return true;
	}
	//endregion


    //region DBHelper Class
	// *****************************************************************************
    // *
    // * DBHelper Class
    // * 
    // *****************************************************************************
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
                        DBStrings.TBLSONG_BPM + " int, " +
                        DBStrings.TBLSONG_TIME + " text, " +
                        DBStrings.TBLSONG_LINK + " text, " +
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
    					DBStrings.TBLSLOOKUP_SONG + " int, " +
    					DBStrings.TBLSLOOKUP_KEY + " text, " +
    					DBStrings.TBLSLOOKUP_ORDER + " int ); " );
    			
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
        	// Add the tables
    		try{
    			db.beginTransaction();
    			
    			// Updates from DB version 1
    			if (oldVersion == 1) {
	    			// Add set key column
	    			db.execSQL("ALTER TABLE " + DBStrings.SETLOOKUP_TABLE + " ADD COLUMN " + DBStrings.TBLSLOOKUP_KEY + " text");
	    			db.execSQL("UPDATE " + DBStrings.SETLOOKUP_TABLE + " SET " + DBStrings.TBLSLOOKUP_KEY + " = " +
	    					"(SELECT " + DBStrings.TBLSONG_KEY + " FROM " + DBStrings.SONGS_TABLE + " WHERE " +
	    					DBStrings.SONGS_TABLE + "." + DBStrings.TBLSONG_ID + " = " + DBStrings.SETLOOKUP_TABLE + "." + DBStrings.TBLSLOOKUP_SONG + ")");
    			}
    			
    			// Updates from DB version 2 or lower
    			if (oldVersion <= 2) {
	    			// Add set order column
	    			db.execSQL("ALTER TABLE " + DBStrings.SETLOOKUP_TABLE + " ADD COLUMN " + DBStrings.TBLSLOOKUP_ORDER + " int");
	    			
	    			// Update current sets to add set order
	    			addSetOrder(db);
    			}

                // Updates from DB version 4 or lower
                if (oldVersion <= 4) {
                    // Add bpm and time signature columns
                    db.execSQL("ALTER TABLE " + DBStrings.SONGS_TABLE + " ADD COLUMN " + DBStrings.TBLSONG_BPM + " int");
                    db.execSQL("ALTER TABLE " + DBStrings.SONGS_TABLE + " ADD COLUMN " + DBStrings.TBLSONG_TIME + " text");
                }

                // Updates from DB version 5 or lower
                if (oldVersion <= 6) {
                    // Add link column to the songs table
                    db.execSQL("ALTER TABLE " + DBStrings.SONGS_TABLE + " ADD COLUMN " + DBStrings.TBLSONG_LINK + " text");
                }
    			
    			db.setTransactionSuccessful(); 
    		}catch(SQLiteException e) {
    			Log.e(TAG, e.getMessage());
    		}finally{
    			db.endTransaction();
    		}
        }
        
        /**
         * Adds the set order to current sets on upgrade
         * @param db The database
         */
        private void addSetOrder(SQLiteDatabase db) {
        	int counter = 0;
        	
        	// Get a list of all set IDs
        	Cursor c = db.rawQuery("SELECT " + DBStrings.TBLSETS_ID + " FROM " + DBStrings.SETS_TABLE, null);
        	int setIDs[] = new int[c.getCount()];
        	while (c.moveToNext()) {
        		setIDs[counter++] = c.getInt(c.getColumnIndexOrThrow(DBStrings.TBLSETS_ID));
        	}
        	
        	// For each set ID, add the orders
        	for (int i : setIDs) {
        		counter = 1;
        		
        		// Get the rows of the current set
        		c = db.rawQuery("SELECT " + DBStrings.TBLSLOOKUP_SONG + " " +
        				"FROM " + DBStrings.SETLOOKUP_TABLE + " " +
        				"WHERE " + DBStrings.TBLSLOOKUP_SET + " = " + i, null);
        		
        		// Update each row for the current set
        		while (c.moveToNext()) {
        			int songID = c.getInt(c.getColumnIndexOrThrow(DBStrings.TBLSLOOKUP_SONG));
        			db.execSQL("UPDATE " + DBStrings.SETLOOKUP_TABLE + " " +
        					"SET " + DBStrings.TBLSLOOKUP_ORDER + " = " + counter + " " +
        					"WHERE " + DBStrings.TBLSLOOKUP_SET + " = " + i + " AND " +
        					DBStrings.TBLSLOOKUP_SONG + " = " + songID);
        			counter++;
        		}
        	}
        }
    }
    //endregion
}
