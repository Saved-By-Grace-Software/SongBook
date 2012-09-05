package com.sbgsoft.tabapp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;

	private static final String TAG = "TabAppDBAdapter";
    private static final String DATABASE_NAME = "tabAppDB";
    private static final int DATABASE_VERSION = 1;
    public static final String SETS_TABLE = "tblSets";
    public static final String SONGS_TABLE = "tblSongs";
    public static final String GROUPS_TABLE = "tblGroups";
    public static final String CURRSET_TABLE = "tblCurrSet";
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

    private final Context mCtx;
	
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
    
	/**
	 * Creates a new, empty, set
	 * @param setName The name of the set
	 * @return True if success, False if failure
	 */
	public boolean createSet(String setName) {
		// Create a new set with the specified name
		try {
			mDb.execSQL( "INSERT INTO " + SETS_TABLE + "(" + TBLSETS_NAME + ", " + TBLSETS_SONGS + ") VALUES ('" + setName + "', '' );" );
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
	public boolean createSet(String setName, String songs) {
		// Create a new set with the specified name
		try {
			mDb.execSQL( "INSERT INTO " + SETS_TABLE + "(" + TBLSETS_NAME + ", " + TBLSETS_SONGS + ") VALUES ('" + setName + "', '" + songs + "' );" );
		} catch (SQLiteException e) {
			return false;
		}
		
		return true;
	}
	
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
	 * Gets all existing set names
	 * @return Cursor to the query
	 */	
	public Cursor getSetNames() {
		return mDb.rawQuery("SELECT " + TBLSETS_ID + " as _id, " + TBLSETS_NAME + " FROM " + SETS_TABLE, null);
	}
	
	/**
	 * Gets the songs for the set
	 * @return Cursor to the songs
	 */
	public Cursor getSetSongs() {
		try {
			// Get the list of songs from the sets table
			String query = "SELECT " + SETS_TABLE + "." + TBLSETS_ID + " as _id, " + TBLSETS_NAME + ", " + TBLSETS_SONGS + 
					" FROM " + SETS_TABLE + ", " + CURRSET_TABLE +
					" WHERE " + SETS_TABLE + "." + TBLSETS_ID + " = " + CURRSET_TABLE + "." + TBLCURRSET_SET;
			Cursor c = mDb.rawQuery(query, null);
			
			if (c.getCount() == 0)
				return null;
			
			c.moveToFirst();
			String  songs = c.getString(c.getColumnIndexOrThrow(TBLSETS_SONGS));
			
			// Create the query to get the list of songs
			String[] songsArray = songs.split(",");
			query = "SELECT " + TBLSONG_ID + " as _id, " + TBLSONG_NAME + ", " + TBLSONG_FILE + 
					" FROM " + SONGS_TABLE + 
					" WHERE " + TBLSONG_NAME + " IN (";
			
			for (String song : songsArray) {
				query += "'" + song + "',";
			}
			query = query.replaceAll("\\,$", "");
			query += ") ORDER BY " + TBLSONG_NAME;
			
			// Return the cursor to the songs list
			return mDb.rawQuery(query, null);
		} catch (SQLiteException s) {
			return null;
		}
	}
	
	/**
	 * Gets all existing set names
	 * @return Cursor to the query
	 */	
	public Cursor getSongNames() {
		return mDb.rawQuery("SELECT " + TBLSONG_ID + " as _id, " + TBLSONG_NAME + ", " + TBLSONG_FILE + 
				" FROM " + SONGS_TABLE + " ORDER BY " + TBLSONG_NAME, null);
	}
	
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
	
	/**
	 * Deletes all sets in the database
	 * @return True if success, False if failure
	 */
	public boolean deleteAllSets() {
		try {
			mDb.execSQL("DELETE from " + SETS_TABLE);
			mDb.execSQL("UPDATE " + CURRSET_TABLE + " SET " + TBLCURRSET_SET + " = 0");
		} catch (SQLException e) {
			return false;
		}
		return true;
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
	 * Deletes the specified set
	 * @param setName The set name to delete
	 * @return True if success, False if failure
	 */
	public boolean deleteSet(String setName) {
		try {
			mDb.execSQL("DELETE from " + SETS_TABLE + " WHERE " + TBLSETS_NAME + " = '" + setName + "'");
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
    					TBLSETS_NAME + " text UNIQUE, "+ 
    					TBLSETS_SONGS + " text ); " );
    			
    			// Songs table
    			db.execSQL("create table " + SONGS_TABLE +
    					"(" + TBLSONG_ID + " integer PRIMARY KEY autoincrement, " + 
    					TBLSONG_NAME + " text UNIQUE, " + 
    					TBLSONG_FILE + " text, " + 
    					TBLSONG_GROUP + " int ); " );
    			
    			// Group table
    			db.execSQL("create table " + GROUPS_TABLE +
    					"(" + TBLGROUPS_ID + " integer PRIMARY KEY autoincrement, " + 
    					TBLGROUPS_NAME + " text UNIQUE, " + 
    					TBLGROUPS_PARENT + " int ); " );
    			
    			// Current Set table
    			db.execSQL("create table " + CURRSET_TABLE +
    					"(" + TBLCURRSET_ID + " integer PRIMARY KEY autoincrement, " + 
    					TBLCURRSET_SET + " int ); " );
    			
    			db.execSQL("insert into " + GROUPS_TABLE + "(" + TBLGROUPS_NAME + ", " + TBLGROUPS_PARENT + ") values ('Uncategorized', '-1' );" );
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
