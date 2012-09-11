package com.sbgsoft.tabapp.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.ericharlow.DragNDrop.DragNDropListActivity;
import com.lamerman.FileDialog;
import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.db.DBAdapter;
import com.sbgsoft.tabapp.sets.CurrentSetTab;
import com.sbgsoft.tabapp.sets.SetActivity;
import com.sbgsoft.tabapp.sets.SetsTab;
import com.sbgsoft.tabapp.songs.EditSongActivity;
import com.sbgsoft.tabapp.songs.SongActivity;
import com.sbgsoft.tabapp.songs.SongsTab;

public class MainActivity extends FragmentActivity {
	
	/*****************************************************************************
     *
     * Class Variables
     * 
     *****************************************************************************/
	public static final String SONG_NAME_KEY = "songName";
	public static final String SONG_TEXT_KEY = "songText";
	public static final String CURRENT_SONG_KEY = "setCurrentSong";
	public static final String SET_SONGS_KEY = "setSongs";
	public static final String SET_NAME_KEY = "setName";
	public static final String ACTIVITY_RESPONSE_TYPE = "activityResponseType";
	public static final String REORDER_ACTIVITY = "reorderActivity";
	public static final String FILE_ACTIVITY = "fileActivity";
	private static final int DELETE_SONG = 1;
	private static final int EDIT_SONG = 2;
	private static final int DELETE_SET = 3;
	private static final int EDIT_SET = 4;
	private static final int REORDER_SET = 5;
	private static final int SONG_GROUPS_ADD = 6;
	private static final int SONG_GROUPS_DEL = 7;
	
	private static int currentTab = 1;
	public static DBAdapter dbAdapter;
	static ViewPager mViewPager;
	
	public Fragment currSetFragment;
	public Fragment setsFragment;
	public Fragment songsFragment;
	
	FragmentTransaction transaction;
	private Cursor setsCursor;
	private Cursor songsCursor;
	private Cursor currSetCursor;
	private Cursor groupsCursor;
	private String importFilePath = "";
	
	
	/*****************************************************************************
     * 
     * Class Functions
     * 
     *****************************************************************************/
    /**
     *  Called when the activity is first created. 
     **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setsFragment = new SetsTab();
        songsFragment = new SongsTab();
        currSetFragment = new CurrentSetTab();
        
        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(currSetFragment);
        mPagerAdapter.addFragment(setsFragment);
        mPagerAdapter.addFragment(songsFragment);
       
        
        //transaction = getSupportFragmentManager().beginTransaction();
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
		
		mViewPager.setOnPageChangeListener(
	            new ViewPager.SimpleOnPageChangeListener() {
	                @Override
	                public void onPageSelected(int position) {
	                    // When swiping between pages, select the
	                    // corresponding tab.
	                    getActionBar().setSelectedNavigationItem(position);
	                    currentTab = position;
	                }
	            });
        
        ActionBar ab = getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ab.setDisplayShowTitleEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        Tab tab3 = ab.newTab().setText("Current Set")
				.setTabListener(new TabListener<CurrentSetTab>(
						this, "tabcurrent", CurrentSetTab.class)); 
        
        Tab tab1 = ab.newTab().setText("Sets")
        		.setTabListener(new TabListener<SetsTab>(
                        this, "tabsets", SetsTab.class));

		Tab tab2 = ab.newTab().setText("Songs")
				.setTabListener(new TabListener<SongsTab>(
                        this, "tabsongs", SongsTab.class));
		
		ab.addTab(tab3);
		ab.addTab(tab1);
		ab.addTab(tab2);
				
		// Set up the database
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();
    }
    
    /**
     * Creates the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	// Determine which menu item was selected
    	switch (item.getItemId())
        {
	        case R.id.menu_sets_create:
	        	// Create a new set and refresh the list view
	        	createSet();
	            return true; 
	        case R.id.menu_sets_clear:
	        	// Delete all sets and refresh the list view
	        	deleteAllSets();
	        	return true;
	        case R.id.menu_songs_clear:
	        	deleteAllSongs();
	        	return true;
	        case R.id.menu_songs_create:
	        	createSong();
	        	return true;
	        case R.id.menu_songs_import:
	        	importSong();
	        	return true;
	        case R.id.menu_groups_create:
	        	createGroup();
	        	return true;
	        case R.id.menu_groups_delete:
	        	deleteGroup("");
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Creates the context menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	// Songs context menu
    	if (v.getId() == R.id.songs_list) {
    		menu.setHeaderTitle("Song Menu");
    		menu.add(Menu.NONE, DELETE_SONG, DELETE_SONG, R.string.cmenu_songs_delete);
    		menu.add(Menu.NONE, EDIT_SONG, EDIT_SONG, R.string.cmenu_songs_edit);
    		menu.add(Menu.NONE, SONG_GROUPS_ADD, SONG_GROUPS_ADD, R.string.cmenu_songs_group_add);
    		menu.add(Menu.NONE, SONG_GROUPS_DEL, SONG_GROUPS_DEL, R.string.cmenu_songs_group_delete);
    	}
    	// Sets context menu
    	else if (v.getId() == R.id.sets_list) {
    		menu.setHeaderTitle("Sets Menu");
    		menu.add(Menu.NONE, DELETE_SET, DELETE_SET, R.string.cmenu_sets_delete);
    		menu.add(Menu.NONE, EDIT_SET, EDIT_SET, R.string.cmenu_sets_edit);
    		menu.add(Menu.NONE, REORDER_SET, REORDER_SET, R.string.cmenu_sets_reorder);
    	}
    }
    
    /**
     * Responds to context menu click
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	String setName = "", songName = "", groupName = "";
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	switch (item.getItemId()) {
    		case DELETE_SONG:
    			// Get the song name and delete it
    			songsCursor.moveToPosition(info.position);
				songName = songsCursor.getString(songsCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_NAME));
                deleteSong(songName);
            	
                return true;
    		case EDIT_SONG:
    			// Get the song name
    			songsCursor.moveToPosition(info.position);
    			songName = songsCursor.getString(songsCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_NAME));
                    
				// Create the edit activity intent
            	Intent intent = new Intent(getBaseContext(), EditSongActivity.class);
                intent.putExtra(SONG_NAME_KEY, songName);
                
                // Start the activity
                startActivity(intent);
            	
                return true;
    		case SONG_GROUPS_ADD:
    			// Get the song name
    			songsCursor.moveToPosition(info.position);
    			songName = songsCursor.getString(songsCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_NAME));
    			
    			// Edit the songs groups
    			addSongToGroup(songName);
    			
    			return true;
    		case SONG_GROUPS_DEL:
    			// Get the song name
    			songsCursor.moveToPosition(info.position);
    			songName = songsCursor.getString(songsCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_NAME));
    			
    			// Get the current group
    			Spinner s = (Spinner)findViewById(R.id.song_group_spinner);
    			int position = s.getSelectedItemPosition();
    			groupsCursor.moveToPosition(position);
    			groupName = groupsCursor.getString(groupsCursor.getColumnIndexOrThrow(DBAdapter.TBLGROUPS_NAME));
    			
    			// Remove the song from the group
    			if (!groupName.equals(SongsTab.ALL_SONGS_LABEL))
    				removeSongFromGroup(songName, groupName);
    			else
    				Toast.makeText(getBaseContext(), "Cannot remove song from " + SongsTab.ALL_SONGS_LABEL + " group", Toast.LENGTH_LONG).show();
    			
    			return true;
    		case DELETE_SET:
    			// Get the set selected
    			setsCursor.moveToPosition(info.position);
            	setName = setsCursor.getString(setsCursor.getColumnIndexOrThrow(DBAdapter.TBLSETS_NAME));
            	
            	// Delete the set
                deleteSet(setName);
                return true;
    		case REORDER_SET:
    			// Get the set selected
    			setsCursor.moveToPosition(info.position);
            	setName = setsCursor.getString(setsCursor.getColumnIndexOrThrow(DBAdapter.TBLSETS_NAME));
            	
            	// Get the set songs
            	Cursor c = dbAdapter.getSetSongs(setName);
            	startManagingCursor(c);
            	String[] setSongs = new String[c.getCount()];
            	c.moveToFirst();
            	int songCounter = 0;
            	
            	// Loop through each song in the current set and add it to the array
            	while(!c.isAfterLast()) {
            		String song = c.getString(c.getColumnIndexOrThrow(DBAdapter.TBLSONG_NAME));
                	setSongs[songCounter++] = song;
                	c.moveToNext();
            	}
            	
            	stopManagingCursor(c);
            	
            	// Edit the set
            	Intent i = new Intent(getBaseContext(), DragNDropListActivity.class);
            	i.putExtra(SET_SONGS_KEY, setSongs);
            	i.putExtra(SET_NAME_KEY, setName);
            	startActivityForResult(i, 1);
            	return true;
    		case EDIT_SET:
    			// Get the set selected
    			setsCursor.moveToPosition(info.position);
            	setName = setsCursor.getString(setsCursor.getColumnIndexOrThrow(DBAdapter.TBLSETS_NAME));
            	
            	// Show the dialog to edit songs
            	updateSetSongs(setName);
            	return true;
    	}
    	return false;
    }
    
    /**
     * Called when the activity is stopped
     */
    @Override
    protected void onStop(){
       super.onStop();

       stopManagingCursor(songsCursor);
       stopManagingCursor(currSetCursor);
       stopManagingCursor(setsCursor);
       
       currentTab = mViewPager.getCurrentItem();
    }
    
    /**
     * Called when the activity is paused
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	stopManagingCursor(songsCursor);
        stopManagingCursor(currSetCursor);
        stopManagingCursor(setsCursor);
    	
    	currentTab = mViewPager.getCurrentItem();
    }
    
    /**
     * Called when the activity is started    
     */
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	mViewPager.setCurrentItem(currentTab);
    }
    
    /**
     * Called when the activity is resumed
     */
    @Override
    public void onResume() {
    	super.onResume();
    	
    	mViewPager.setCurrentItem(currentTab);
    }
    
    /**
     * Get the return from the file dialog activity
     */
    public synchronized void onActivityResult(final int requestCode,
        int resultCode, final Intent data) {

        if (resultCode == Activity.RESULT_OK) {
        	String activityType = data.getStringExtra(ACTIVITY_RESPONSE_TYPE);
        	
        	// If returning from the file activity
        	if (activityType.equals(FILE_ACTIVITY)) {
	            importFilePath = data.getStringExtra(FileDialog.RESULT_PATH);
	            createSong();
        	}
        	
        	// If returning from the reorder activity
        	if (activityType.equals(REORDER_ACTIVITY)) {
        		String[] newOrder = data.getStringArrayExtra(SET_SONGS_KEY);
        		String setName = data.getStringExtra(SET_NAME_KEY);
        		
        		if(!dbAdapter.updateSet(setName, newOrder)) {
        			Toast.makeText(getApplicationContext(), "Could not update set order!", Toast.LENGTH_LONG).show();
        		}
        		
        		// Refresh the views
	        	((CurrentSetTab)currSetFragment).refreshCurrentSet();
        	}
        } 

    }
    
    
    /*****************************************************************************
     * 
     * Set Functions
     * 
     *****************************************************************************/
    /**
     * Prompts the user for a name and creates the set
     */
    private void createSet() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Create Set");
    	alert.setMessage("Please enter the name of the set (must be unique)");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		String value = input.getText().toString();
	    		if (value.length() > 0) {
		    			selectSetSongs(value);
	    		}
	    		else
	    			Toast.makeText(getApplicationContext(), "Cannot create a set with no name!", Toast.LENGTH_LONG).show();
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    	    // Canceled.
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Selects the songs for the set
     */
    private void selectSetSongs(final String setName) {
    	songsCursor = dbAdapter.getSongNames(SongsTab.ALL_SONGS_LABEL);
    	startManagingCursor(songsCursor);
    	
    	final CharSequence[] songNames = new CharSequence[songsCursor.getCount()];
    	final boolean[] songsChecked = new boolean[songsCursor.getCount()];
    	int counter = 0;
    	
    	// Add songs to list view
    	while(songsCursor.moveToNext()) {
    		songsChecked[counter] = false;
    		songNames[counter++] = songsCursor.getString(songsCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_NAME));
    	}
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Select Songs");
    	alert.setMultiChoiceItems( songNames, songsChecked, new OnMultiChoiceClickListener() {
    		public void onClick (DialogInterface dialog, int whichItem, boolean isChecked) {
    			// Set item checked/unchecked
    			songsChecked[whichItem] = isChecked;
    		}
    	});

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Set all selected items to the songs for the set	    		
	    		ArrayList<String> setSongs = new ArrayList<String>();
	    		for (int i = 0; i < songsChecked.length; i++) {
	    			if(songsChecked[i]) {
	    				setSongs.add(songNames[i].toString());
	    			}
	    		}
	    		
	    		// Create the set
	    		if(!dbAdapter.createSet(setName, setSongs))
	    			Toast.makeText(getApplicationContext(), "Failed to create set!", Toast.LENGTH_LONG).show();
	    		else
	    			setsCursor.requery();
	    		
	    		// Set the current tab
	        	currentTab = 2;
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Set the current tab
	        	currentTab = 2;
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Updates the songs for the set
     */
    private void updateSetSongs(final String setName) {
    	songsCursor = dbAdapter.getSongNames(SongsTab.ALL_SONGS_LABEL);
    	startManagingCursor(songsCursor);
    	
    	final CharSequence[] songNames = new CharSequence[songsCursor.getCount()];
    	final boolean[] songsChecked = new boolean[songsCursor.getCount()];
    	int counter = 0;
    	
    	// Add songs to list view
    	while(songsCursor.moveToNext()) {
    		String songName = songsCursor.getString(songsCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_NAME));
    		songsChecked[counter] = dbAdapter.isSongInSet(songName, setName);
    		songNames[counter++] = songName;
    	}
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Select Songs");
    	alert.setMultiChoiceItems( songNames, songsChecked, new OnMultiChoiceClickListener() {
    		public void onClick (DialogInterface dialog, int whichItem, boolean isChecked) {
    			// Set item checked/unchecked
    			songsChecked[whichItem] = isChecked;
    		}
    	});

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Set all selected items to the songs for the set	    		
	    		ArrayList<String> setSongs = new ArrayList<String>();
	    		for (int i = 0; i < songsChecked.length; i++) {
	    			if(songsChecked[i]) {
	    				setSongs.add(songNames[i].toString());
	    			}
	    		}
	    		
	    		// Create the set
	    		if(!dbAdapter.updateSet(setName, setSongs.toArray(new String[setSongs.size()])))
	    			Toast.makeText(getApplicationContext(), "Failed to create set!", Toast.LENGTH_LONG).show();
	    		else
	    			setsCursor.requery();
	    		
	    		// Set the current tab
	        	currentTab = 2;
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Set the current tab
	        	currentTab = 2;
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Prompts the user to confirm then deletes all sets
     */
    private void deleteAllSets() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete All?!");
    	alert.setMessage("Are you sure you want to delete ALL sets???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		dbAdapter.deleteAllSets();
	    		
	    		// Refresh the views
	        	setsCursor.requery();
	        	((CurrentSetTab)currSetFragment).refreshCurrentSet();
	        	
	        	// Set the current tab
	        	currentTab = 2;
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Set the current tab
	        	currentTab = 2;
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Prompts the user to confirm then deletes the specified set
     */
    private void deleteSet(final String setName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete Song?!");
    	alert.setMessage("Are you sure you want to delete '" + setName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Delete set from database
	    		dbAdapter.deleteSet(setName);
	    		
	    		// Refresh song list
	        	setsCursor.requery();
	        	
	        	// Set the current tab
	        	currentTab = 2;
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Set the current tab
	        	currentTab = 2;
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Fills the sets list
     * @param v The view for the list
     */
    public void fillSetsList(View v) {
    	setsCursor = dbAdapter.getSetNames();
    	startManagingCursor(setsCursor);
    	
    	String[] from = new String[] { DBAdapter.TBLSETS_NAME };
        int[] to = new int[] { R.id.sets_row_text };
        
        SimpleCursorAdapter sets = new SimpleCursorAdapter(this, R.layout.sets_row, setsCursor, from, to);
        ListView lv = ((ListView)v.findViewById(R.id.sets_list));
        lv.setEmptyView(findViewById(R.id.empty_sets));
        
        // Set the on click listener for each item
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	// Get the set to show
            	setsCursor.moveToPosition(position);
            	String setName = setsCursor.getString(setsCursor.getColumnIndexOrThrow(DBAdapter.TBLSETS_NAME));
            	
            	// Set the current set and show it
            	dbAdapter.setCurrentSet(setName);
            	((CurrentSetTab)currSetFragment).refreshCurrentSet();
            	mViewPager.setCurrentItem(0, true);
            }
        });

        // Register the context menu and set the adapter
        registerForContextMenu(lv);
        lv.setAdapter(sets);
    }
    
    
    /*****************************************************************************
     * 
     * Song Functions
     * 
     *****************************************************************************/
    /**
     * Prompts the user for a name and creates the set
     */
    private void createSong() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Add Song");
    	alert.setMessage("Please enter the name of the song (must be unique)");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		String value = input.getText().toString();
	    		if (value.length() > 0) {
	    			//String songFile = getFilesDir() + "/" + value + ".txt";
	    			String songFile = value + ".txt";
		    		if(!dbAdapter.createSong(value, songFile))
		    			Toast.makeText(getApplicationContext(), "Failed to create song!", Toast.LENGTH_LONG).show();
		    		else
		    		{
		    			// If a file is waiting to be imported
		    			if (importFilePath != "")
		    			{
		    				// Copy the file into the tabapp songs directory
		    				try {
			    				InputStream in = new FileInputStream(importFilePath);
			    				//OutputStream out = new FileOutputStream(songFile);
			    				OutputStream out = openFileOutput(songFile, Context.MODE_PRIVATE);
			    				byte[] buf = new byte[1024];
			    				int len;
			    				while ((len = in.read(buf)) > 0) {
			    				   out.write(buf, 0, len);
			    				}
			    				in.close();
			    				out.close(); 
		    				} catch (Exception e) {
		    					// Delete the song since the file could not be imported
		    					dbAdapter.deleteSong(value);
		    					
		    					// Alert that the song failed
		    					Toast.makeText(getApplicationContext(), "Could not import file, Song deleted.", Toast.LENGTH_LONG).show();
		    				}

		    				// Clear the import file path
		    				importFilePath = "";
		    			}
		    			else {
		    				try {
		    					OutputStream out = openFileOutput(songFile, Context.MODE_PRIVATE);
		    					out.close();
		    				} catch (IOException e) {
		    					// Delete the song since the file could not be imported
		    					dbAdapter.deleteSong(value);
		    					
		    					// Alert that the song failed
		    					Toast.makeText(getApplicationContext(), "Could not create song file, Song deleted.", Toast.LENGTH_LONG).show();
		    				}
		    				
		    			}
		    			
		    			// Set the current tab
			        	currentTab = 3;
			        	
			        	// Add the song to a group
			        	addSongToGroup(value);
		    		}
	    		}
	    		else
	    			Toast.makeText(getApplicationContext(), "Cannot create a song with no name!", Toast.LENGTH_LONG).show();
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    	    // Canceled.
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Adds the song to a group
     * @param songName The song to add
     */
    private void addSongToGroup(final String songName) {
    	Cursor c = dbAdapter.getGroupNames();
    	startManagingCursor(c);
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount()];
    	int counter = 0;
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		groupNames[counter++] = c.getString(c.getColumnIndexOrThrow(DBAdapter.TBLGROUPS_NAME));
    	}
    	
    	stopManagingCursor(c);
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Add Song to Group");
    	alert.setItems(groupNames, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dbAdapter.addSongToGroup(songName, groupNames[which].toString());
				
				// Refresh song list
    			//groupsCursor.requery();
				((SongsTab)songsFragment).refreshSongsList(SongsTab.ALL_SONGS_LABEL);
			}
		});

    	alert.show();
    }
    
    private void removeSongFromGroup(final String songName, final String groupName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Remove Song From Group?!");
    	alert.setMessage("Are you sure you want to remove '" + songName + "' from '" + groupName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Remove song from the group
	    		dbAdapter.removeSongFromGroup(songName, groupName);
	    		
	    		// Refresh the views
	    		((SongsTab)songsFragment).refreshSongsList(SongsTab.ALL_SONGS_LABEL);
	    		groupsCursor.requery();
	        	setsCursor.requery();
	        	((CurrentSetTab)currSetFragment).refreshCurrentSet();
	        	
	        	// Set the current tab
	        	currentTab = 3;
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Set the current tab
	        	currentTab = 3;
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Prompts the user to confirm then deletes all songs
     */
    private void deleteAllSongs() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete All?!");
    	alert.setMessage("Are you sure you want to delete ALL songs???" + System.getProperty("line.separator") +
    			"This will delete all sets as well...");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		if(dbAdapter.deleteAllSongs()) {
	    			// Get a list of all internal files
	    			String[] files = fileList();
	    			
	    			// Delete each file
	    			for (String file : files) {
	    				deleteFile(file);
	    			}
	    		}
	    		
	    		// Refresh the views
	    		((SongsTab)songsFragment).refreshSongsList(SongsTab.ALL_SONGS_LABEL);
	    		groupsCursor.requery();
	        	setsCursor.requery();
	        	((CurrentSetTab)currSetFragment).refreshCurrentSet();
	        	
	        	// Set the current tab
	        	currentTab = 3;
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Set the current tab
	        	currentTab = 3;
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Prompts the user to confirm then deletes the specified song
     */
    private void deleteSong(final String songName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete Song?!");
    	alert.setMessage("Are you sure you want to delete '" + songName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Get song file
	    		String fileToDelete = dbAdapter.getSongFile(songName);
	    		if (fileToDelete != "") {
	    			// Delete song file
	    			deleteFile(fileToDelete);
		    		
		    		// Delete song from database
		    		dbAdapter.deleteSong(songName);
	    		}
	    		
	    		// Refresh the views
	    		((SongsTab)songsFragment).refreshSongsList(SongsTab.ALL_SONGS_LABEL);
	    		groupsCursor.requery();
	        	setsCursor.requery();
	        	((CurrentSetTab)currSetFragment).refreshCurrentSet();
	        	
	        	// Set the current tab
	        	currentTab = 3;
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Set the current tab
	        	currentTab = 3;
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Fills the songs list
     * @param v The view for the list
     * @param groupName The name of the group to populate
     */
    public void fillSongsList(View v, String groupName) {
    	songsCursor = dbAdapter.getSongNames(groupName);
    	startManagingCursor(songsCursor);
    	
    	String[] from = new String[] { DBAdapter.TBLSONG_NAME };
        int[] to = new int[] { R.id.songs_row_text };
        
        SimpleCursorAdapter songs = new SimpleCursorAdapter(this, R.layout.songs_row, songsCursor, from, to);
        ListView lv = ((ListView)v.findViewById(R.id.songs_list));
        lv.setEmptyView(findViewById(R.id.empty_songs));
        
        // Set the on click listener for each item
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	// Get the song to show
            	songsCursor.moveToPosition(position);
            	String songName = songsCursor.getString(songsCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_NAME));
            	String songText = getSongText(songsCursor.getString(songsCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_FILE)));
            	
            	// Show the song activity
            	SongActivity song = new SongActivity();
            	Intent showSong = new Intent(v.getContext(), song.getClass());
            	showSong.putExtra(SONG_NAME_KEY, songName);
            	showSong.putExtra(SONG_TEXT_KEY, songText);
                startActivity(showSong);
            }
        });
        
        // Register the context menu and add the adapter
        registerForContextMenu(lv);
        lv.setAdapter(songs);
    }
    
    /**
     * Gets the text from the specified file
     * @return The song text
     */
    private String getSongText(String fileName) {
    	String songText = "";
    	
        try {
        	FileInputStream fis = openFileInput(fileName);
        	DataInputStream in = new DataInputStream(fis);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
                line = br.readLine();
            }
            songText = sb.toString();
            br.close();
        } catch (Exception e) {
    		Toast.makeText(getApplicationContext(), "Could not open song file!", Toast.LENGTH_LONG).show();
        }     	
    	
    	return songText;
    }
    
    /**
     * Imports a song text file into the db
     */
    private void importSong() {
    	// Create the file dialog intent
    	Intent intent = new Intent(getBaseContext(), FileDialog.class);
        intent.putExtra(FileDialog.START_PATH, "/");
        
        // User cannot select directories
        intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
        
        // Set the file filter to text files
        intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "txt" });
        
        // Start the activity
        startActivityForResult(intent, 1);
    }
    
    
    /*****************************************************************************
     * 
     * Current Set Functions
     * 
     *****************************************************************************/
    /**
     * Fills the current set list
     * @param v The view for the list
     */
    public void fillCurrentSetList(View v) {
    	currSetCursor = dbAdapter.getCurrentSetSongs();
    	startManagingCursor(currSetCursor);
    	
    	String[] from = new String[] { DBAdapter.TBLSONG_NAME };
        int[] to = new int[] { R.id.curr_sets_row_text };
        
        SimpleCursorAdapter current = new SimpleCursorAdapter(this, R.layout.current_set_row, currSetCursor, from, to);
        ListView lv = ((ListView)v.findViewById(R.id.current_list));
        lv.setEmptyView(findViewById(R.id.empty_current));
        
        // Set the on click listener for each item
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	// Create the string array of the song text
            	int songCounter = 0;
            	String[] setSongs = new String[currSetCursor.getCount()];
            	currSetCursor.moveToFirst();
            	
            	// Loop through each song in the current set and add it to the array
            	while(!currSetCursor.isAfterLast()) {
            		String songName = currSetCursor.getString(currSetCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_NAME));
                	String songText = getSongText(currSetCursor.getString(currSetCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_FILE)));
                	setSongs[songCounter++] = songName + System.getProperty("line.separator") + songText;
                	currSetCursor.moveToNext();
            	}
            	
            	// Show the set activity
            	SetActivity song = new SetActivity();
            	Intent showSong = new Intent(v.getContext(), song.getClass());
            	showSong.putExtra(CURRENT_SONG_KEY, position);
            	showSong.putExtra(SET_SONGS_KEY, setSongs);
                startActivity(showSong);
            }
    	});
        
        // Set the long click listener for each item
        lv.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long row) {
            	// TODO: Show the long click menu
            	            	            	
                return true;
            }
        });
        
        lv.setAdapter(current);
    }
    
    
    /*****************************************************************************
     * 
     * Group Functions
     * 
     *****************************************************************************/
    /**
     * Fills the group list
     * @param v
     */
    public void fillGroupsList(final View view) {
    	//groupsCursor = dbAdapter.getGroupsList(groupName);
    	groupsCursor = dbAdapter.getGroupNames();
    	startManagingCursor(groupsCursor);
    	
    	String[] from = new String[] { DBAdapter.TBLGROUPS_NAME };
        int[] to = new int[] { android.R.id.text1 };
      
        SimpleCursorAdapter songs = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, groupsCursor, from, to);
        songs.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
    	Spinner groupSpinner = (Spinner) findViewById(R.id.song_group_spinner);
    	
    	
    	// Set the on click listener for each item
    	groupSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected item and populate the songs list
            	groupsCursor.moveToPosition(position);
            	String groupName = groupsCursor.getString(groupsCursor.getColumnIndexOrThrow(DBAdapter.TBLGROUPS_NAME));
            	fillSongsList(view, groupName);
            	
            	// Refresh the views
            	songsCursor.requery();
	    		groupsCursor.requery();
	        	setsCursor.requery();
	        	((CurrentSetTab)currSetFragment).refreshCurrentSet();
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });

    	
    	
    	groupSpinner.setAdapter(songs);
    }
    
    /**
     * Creates a new song group
     */
    private void createGroup() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Create Song Group");
    	alert.setMessage("Please enter the name of the song group (must be unique)");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		String value = input.getText().toString();
	    		if (value.length() > 0) {
	    			if(!dbAdapter.createGroup(value))
		    			Toast.makeText(getApplicationContext(), "Failed to create song group!", Toast.LENGTH_LONG).show();
	    		}
	    		else
	    			Toast.makeText(getApplicationContext(), "Cannot create a song group with no name!", Toast.LENGTH_LONG).show();
	    		
	    		// Refresh the view
	    		groupsCursor.requery();
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    	    // Canceled.
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Deletes the specified group
     * @param groupName The group to delete
     */
    private void deleteGroup(final String groupName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete Group?!");
    	alert.setMessage("Are you sure you want to delete '" + groupName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Delete song from database
		    	dbAdapter.deleteGroup(groupName);
	    		
	    		// Refresh song list
	    		groupsCursor.requery();
	        	
	        	// Set the current tab
	        	currentTab = 3;
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Set the current tab
	        	currentTab = 3;
	    	}
    	});

    	alert.show();
    }
    
    
    /*****************************************************************************
     * 
     * Classes
     * 
     *****************************************************************************/
    /**
     * Tab Listener class for displaying each tab
     * @author SamIAm
     *
     * @param <T>
     */
    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;

        /** Constructor used each time a new tab is created.
          * @param activity  The host Activity, used to instantiate the fragment
          * @param tag  The identifier tag for the fragment
          * @param clz  The fragment's Class, used to instantiate the fragment
          */
        public TabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

        /* The following are each of the ActionBar.TabListener callbacks */

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Usually do nothing.
        }

		public void onTabReselected(Tab arg0,
				android.app.FragmentTransaction arg1) {
			
		}

		public void onTabSelected(Tab arg0, android.app.FragmentTransaction arg1) {
			mViewPager.setCurrentItem(arg0.getPosition());
		}

		public void onTabUnselected(Tab arg0,
				android.app.FragmentTransaction arg1) {
			
		}
    }
    
    /**
     * Page adapter for switching between tabs
     * @author SamIAm
     *
     */
    public class PagerAdapter extends FragmentPagerAdapter {

        private final ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

        public PagerAdapter(FragmentManager manager) {
            super(manager);
        }

        public void addFragment(Fragment fragment) {
            mFragments.add(fragment);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }
    
}

