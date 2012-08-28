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
	 * @param songs The string of song IDs
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
	 * Gets all existing set names
	 * @return Cursor to the query
	 */	
	public Cursor getSongNames() {
		return mDb.rawQuery("SELECT " + TBLSONG_ID + " as _id, " + TBLSONG_NAME + ", " + TBLSONG_FILE + " FROM " + SONGS_TABLE, null);
	}
	
	/**
	 * Deletes all sets in the database
	 * @return True if success, False if failure
	 */
	public boolean deleteAllSets() {
		try {
		mDb.execSQL("DELETE from " + SETS_TABLE);
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
    			
    			db.execSQL("insert into " + GROUPS_TABLE + "(" + TBLGROUPS_NAME + ", " + TBLGROUPS_PARENT + ") values ('Uncategorized', '-1' );" );
    			
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
