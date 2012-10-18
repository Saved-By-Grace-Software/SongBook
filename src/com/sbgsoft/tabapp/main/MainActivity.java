package com.sbgsoft.tabapp.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
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
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ericharlow.DragNDrop.DragNDropListActivity;
import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.db.DBAdapter;
import com.sbgsoft.tabapp.db.DBStrings;
import com.sbgsoft.tabapp.files.OpenFile;
import com.sbgsoft.tabapp.items.Item;
import com.sbgsoft.tabapp.items.ItemArrayAdapter;
import com.sbgsoft.tabapp.items.SectionItem;
import com.sbgsoft.tabapp.items.SetItem;
import com.sbgsoft.tabapp.items.SongItem;
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
	private static int currentTab = 1;
	private static String currentSongGroup = SongsTab.ALL_SONGS_LABEL;
	private static String currentSetGroup = SetsTab.ALL_SETS_LABEL;
	
	private static String mSongAuthor = MainStrings.UNKNOWN;
	private static String mSongKey = MainStrings.UNKNOWN;
	public static DBAdapter dbAdapter;
	static ViewPager mViewPager;
	
	public Fragment currSetFragment;
	public Fragment setsFragment;
	public Fragment songsFragment;
	
	FragmentTransaction transaction;
	//private Cursor setsCursor;
	private Cursor setGroupsCursor;
	private String importFilePath = "";
	
	private ArrayList<Item> songsList = new ArrayList<Item>();
	private ArrayAdapter<Item> songsAdapter;
	private ArrayList<String> songGroupsList = new ArrayList<String>();
	private ArrayAdapter<String> songGroupsAdapter;
	private ArrayList<Item> currSetList = new ArrayList<Item>();
	private ArrayAdapter<Item> currSetAdapter;
	private ArrayList<Item> setsList = new ArrayList<Item>();
	private ArrayAdapter<Item> setsAdapter;
	
	
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
	        case R.id.menu_song_groups_create:
	        	createSongGroup();
	        	return true;
	        case R.id.menu_song_groups_delete:
	        	deleteSongGroup();
	        	return true;
	        case R.id.menu_song_groups_delete_all:
	        	deleteAllSongGroups();
	        	return true;
	        case R.id.menu_set_groups_create:
	        	createSetGroup();
	        	return true;
	        case R.id.menu_set_groups_delete:
	        	deleteSetGroup();
	        	return true;
	        case R.id.menu_set_groups_delete_all:
	        	deleteAllSetGroups();
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
    		menu.add(Menu.NONE, MainStrings.DELETE_SONG, MainStrings.DELETE_SONG, R.string.cmenu_songs_delete);
    		menu.add(Menu.NONE, MainStrings.EDIT_SONG, MainStrings.EDIT_SONG, R.string.cmenu_songs_edit);
    		menu.add(Menu.NONE, MainStrings.EDIT_SONG_ATT, MainStrings.EDIT_SONG_ATT, R.string.cmenu_songs_edit_att);
    		menu.add(Menu.NONE, MainStrings.SONG_GROUPS_ADD, MainStrings.SONG_GROUPS_ADD, R.string.cmenu_song_group_add);
    		menu.add(Menu.NONE, MainStrings.SONG_GROUPS_DEL, MainStrings.SONG_GROUPS_DEL, R.string.cmenu_song_group_delete);
    	}
    	// Sets context menu
    	else if (v.getId() == R.id.sets_list) {
    		menu.setHeaderTitle("Sets Menu");
    		menu.add(Menu.NONE, MainStrings.DELETE_SET, MainStrings.DELETE_SET, R.string.cmenu_sets_delete);
    		menu.add(Menu.NONE, MainStrings.EDIT_SET, MainStrings.EDIT_SET, R.string.cmenu_sets_edit);
    		menu.add(Menu.NONE, MainStrings.EDIT_SET_ATT, MainStrings.EDIT_SET_ATT, R.string.cmenu_sets_edit_att);
    		menu.add(Menu.NONE, MainStrings.REORDER_SET, MainStrings.REORDER_SET, R.string.cmenu_sets_reorder);
    		menu.add(Menu.NONE, MainStrings.SET_GROUPS_ADD, MainStrings.SET_GROUPS_ADD, R.string.cmenu_set_group_add);
    		menu.add(Menu.NONE, MainStrings.SET_GROUPS_DEL, MainStrings.SET_GROUPS_DEL, R.string.cmenu_set_group_delete);
    	}
    	// Current Set context menu
    	else if (v.getId() == R.id.current_list) {
    		menu.setHeaderTitle("Current Set Menu");
    		menu.add(Menu.NONE, MainStrings.EDIT_SONG_CS, MainStrings.EDIT_SONG_CS, R.string.cmenu_songs_edit);
    		menu.add(Menu.NONE, MainStrings.EDIT_SONG_ATT_CS, MainStrings.EDIT_SONG_ATT_CS, R.string.cmenu_songs_edit_att);
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
    		case MainStrings.DELETE_SONG:
    			// Get the song name and delete it
				songName = songsList.get(info.position).getName();
                deleteSong(songName);
            	
                return true;
    		case MainStrings.EDIT_SONG:
    			// Get the song name
    			songName = songsList.get(info.position).getName();
                    
				// Create the edit activity intent
            	Intent intent = new Intent(getBaseContext(), EditSongActivity.class);
                intent.putExtra(MainStrings.SONG_NAME_KEY, songName);
                
                // Start the activity
                startActivity(intent);
            	
                return true;
    		case MainStrings.EDIT_SONG_CS:
    			// Get the song name
    			songName = currSetList.get(info.position).getName();
                    
				// Create the edit activity intent
            	Intent intent1 = new Intent(getBaseContext(), EditSongActivity.class);
                intent1.putExtra(MainStrings.SONG_NAME_KEY, songName);
                
                // Start the activity
                startActivity(intent1);
            	
                return true;
    		case MainStrings.EDIT_SONG_ATT:
    			// Get the song name
    			songName = songsList.get(info.position).getName();
                    
				// Show the edit dialog
    			editSongAtt(songName);
    			
                return true;
    		case MainStrings.EDIT_SONG_ATT_CS:
    			// Get the song name
    			songName = currSetList.get(info.position).getName();
                    
				// Show the edit dialog
    			editSongAtt(songName);
    			
                return true;
    		case MainStrings.SONG_GROUPS_ADD:
    			// Get the song name
    			songName = songsList.get(info.position).getName();
    			
    			// Edit the songs groups
    			addSongToGroup(songName);
    			
    			return true;
    		case MainStrings.SONG_GROUPS_DEL:
    			// Get the song name
    			songName = songsList.get(info.position).getName();
    			
    			// Get the current group
    			Spinner s = (Spinner)findViewById(R.id.song_group_spinner);
    			int position = s.getSelectedItemPosition();
    			groupName = songGroupsList.get(position);
    			
    			// Remove the song from the group
    			if (!groupName.equals(SongsTab.ALL_SONGS_LABEL))
    				removeSongFromGroup(songName, groupName);
    			else
    				Toast.makeText(getBaseContext(), "Cannot remove song from " + SongsTab.ALL_SONGS_LABEL + " group", Toast.LENGTH_LONG).show();
    			
    			return true;
    		case MainStrings.DELETE_SET:
    			// Get the set selected
            	setName = setsList.get(info.position).getName();
            	
            	// Delete the set
                deleteSet(setName);
                return true;
    		case MainStrings.REORDER_SET:
    			// Get the set selected
    			setName = setsList.get(info.position).getName();
            	
            	// Get the set songs
            	Cursor c = dbAdapter.getSetSongs(setName);
            	//startManagingCursor(c);
            	String[] setSongs = new String[c.getCount()];
            	c.moveToFirst();
            	int songCounter = 0;
            	
            	// Loop through each song in the current set and add it to the array
            	while(!c.isAfterLast()) {
            		String song = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
                	setSongs[songCounter++] = song;
                	c.moveToNext();
            	}
            	
            	//stopManagingCursor(c);
            	
            	// Edit the set
            	Intent i = new Intent(getBaseContext(), DragNDropListActivity.class);
            	i.putExtra(MainStrings.SET_SONGS_KEY, setSongs);
            	i.putExtra(MainStrings.SET_NAME_KEY, setName);
            	startActivityForResult(i, 1);
            	return true;
    		case MainStrings.EDIT_SET:
    			// Get the set selected
    			setName = setsList.get(info.position).getName();
            	
            	// Show the dialog to edit songs
            	updateSetSongs(setName);
            	return true;
    		case MainStrings.EDIT_SET_ATT:
    			// Get the set selected
    			setName = setsList.get(info.position).getName();
            	
            	// Update the set attributes
            	editSetAtt(setName);
            	
            	return true;
    		case MainStrings.SET_GROUPS_ADD:
    			// Get the song name
    			setName = setsList.get(info.position).getName();
    			
    			// Edit the songs groups
    			addSetToGroup(setName);
    			
    			return true;
    		case MainStrings.SET_GROUPS_DEL:
    			// Get the song name
    			setName = setsList.get(info.position).getName();
    			
    			// Get the current group
    			Spinner s1 = (Spinner)findViewById(R.id.set_group_spinner);
    			int position1 = s1.getSelectedItemPosition();
    			setGroupsCursor.moveToPosition(position1);
    			groupName = setGroupsCursor.getString(setGroupsCursor.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME));
    			
    			// Remove the song from the group
    			if (!groupName.equals(SetsTab.ALL_SETS_LABEL))
    				removeSetFromGroup(setName, groupName);
    			else
    				Toast.makeText(getBaseContext(), "Cannot remove set from " + SetsTab.ALL_SETS_LABEL + " group", Toast.LENGTH_LONG).show();
    			
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

       currentTab = mViewPager.getCurrentItem();
    }
    
    /**
     * Called when the activity is paused
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	
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
        	String activityType = data.getStringExtra(MainStrings.ACTIVITY_RESPONSE_TYPE);
        	
        	// If returning from the file activity
        	if (activityType.equals(MainStrings.FILE_ACTIVITY)) {
	            importFilePath = data.getStringExtra(OpenFile.RESULT_PATH);
	            createSong();
        	}
        	
        	// If returning from the reorder activity
        	if (activityType.equals(MainStrings.REORDER_ACTIVITY)) {
        		String[] newOrder = data.getStringArrayExtra(MainStrings.SET_SONGS_KEY);
        		String setName = data.getStringExtra(MainStrings.SET_NAME_KEY);
        		
        		if(!dbAdapter.updateSet(setName, newOrder)) {
        			Toast.makeText(getApplicationContext(), "Could not update set order!", Toast.LENGTH_LONG).show();
        		}
        		
        		// Refresh the current set view
        		currSetAdapter.notifyDataSetChanged();
        	}
        } 

    }
    
    /**
     * Makes the gradient show smoothly
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
    
    /**
     * Sets the import file path
     * @param path The path to set it to
     */
    public void setImportFilePath(String path) {
    	importFilePath = path;
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
    	// Create the alert dialog
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Create Set");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set, (ViewGroup) findViewById(R.id.add_set_root));
    	alert.setView(dialoglayout);
    	final EditText setNameET = (EditText)dialoglayout.findViewById(R.id.add_set_name);
    	final DatePicker setDateDP = (DatePicker)dialoglayout.findViewById(R.id.add_set_date);

    	// Set the OK button
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Get the date and set name
	    		String setName = setNameET.getText().toString();
	    		String setDate = (setDateDP.getMonth() + 1) + "/" + setDateDP.getDayOfMonth() + "/" + setDateDP.getYear();
	    		
	    		if (setName.length() > 0) {
		    			selectSetSongs(setName, setDate);
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
    private void selectSetSongs(final String setName, final String setDate) {
    	Cursor c = dbAdapter.getSongNames(SongsTab.ALL_SONGS_LABEL);
    	
    	final CharSequence[] songNames = new CharSequence[c.getCount()];
    	final boolean[] songsChecked = new boolean[c.getCount()];
    	int counter = 0;
    	
    	// Add songs to list view
    	while(c.moveToNext()) {
    		songsChecked[counter] = false;
    		songNames[counter++] = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
    	}
    	
    	// Create the alert dialog
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
	    		
	    		// Create the set and refresh the list
	    		if(!dbAdapter.createSet(setName, setSongs, setDate + " "))
	    			Toast.makeText(getApplicationContext(), "Failed to create set!", Toast.LENGTH_LONG).show();
	    		else
	    			setsAdapter.notifyDataSetChanged();
	    		
	    		// Set the current tab
	        	currentTab = 2;
	        	
	        	// Add the set to a group
	        	addSetToGroup(setName);
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
     * Adds the song to a group
     * @param songName The song to add
     */
    private void addSetToGroup(final String setName) {
    	// Get the list of group names
    	Cursor c = dbAdapter.getSetGroupNames();
    	//startManagingCursor(c);
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount()];
    	int counter = 0;
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		groupNames[counter++] = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME));
    	}
    	
    	//stopManagingCursor(c);
    	
    	// Create the dialog to choose which group to add the song to
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Add Set to Group");
    	alert.setItems(groupNames, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dbAdapter.addSetToGroup(setName, groupNames[which].toString());
			}
		});

    	alert.show();
    }
    
    /**
     * Removes the set from the specified group
     * @param setName The set to remove
     * @param groupName The group to remove the song from
     */
    private void removeSetFromGroup(final String setName, final String groupName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Remove Set From Group?!");
    	alert.setMessage("Are you sure you want to remove '" + setName + "' from '" + groupName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Remove song from the group
	    		dbAdapter.removeSetFromGroup(setName, groupName);
	        	
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
    	Cursor c = dbAdapter.getSongNames(SongsTab.ALL_SONGS_LABEL);
    	//startManagingCursor(c);
    	
    	final CharSequence[] songNames = new CharSequence[c.getCount()];
    	final boolean[] songsChecked = new boolean[c.getCount()];
    	int counter = 0;
    	
    	// Add songs to list view
    	while(c.moveToNext()) {
    		String songName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
    		songsChecked[counter] = dbAdapter.isSongInSet(songName, setName);
    		songNames[counter++] = songName;
    	}
    	
    	//stopManagingCursor(c);
    	
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
	    		else {
	    			// Update the sets and current set list
	    			setsAdapter.notifyDataSetChanged();
	    			currSetAdapter.notifyDataSetChanged();
	    		}
	    		
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
	    		
	    		// Refresh the set and current set lists
	        	setsAdapter.notifyDataSetChanged();
	        	currSetAdapter.notifyDataSetChanged();
	        	
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
	    		
	    		// Refresh set and current set list
	    		setsAdapter.notifyDataSetChanged();
	        	currSetAdapter.notifyDataSetChanged();
	        	
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
     * Sets the sets array list
     */
    public void setSetsList() {
    	Cursor c = dbAdapter.getSetNames(currentSetGroup);
    	c.moveToFirst();
    	
    	// Clear the ArrayList
    	setsList.clear();
    	
    	// Populate the ArrayList
    	while (!c.isAfterLast()) {
    		// Get the strings from the cursor
        	String setName = c.getString(c.getColumnIndex(DBStrings.TBLSETS_NAME));
        	String setDate = c.getString(c.getColumnIndex(DBStrings.TBLSETS_DATE));
    		
        	// Add the song item
        	setsList.add(new SetItem(setName, setDate));
        	
        	// Move to the next song
        	c.moveToNext();
    	}
    }
    
    /**
     * Fills the sets list
     * @param v The view for the list
     */
    public void fillSetsList() {
    	// Fill the sets array list
    	setSetsList();
    	
    	// Set up the list view and adapter
        ListView lv = ((ListView)findViewById(R.id.sets_list));
        lv.setEmptyView(findViewById(R.id.empty_sets));
        setsAdapter = new ItemArrayAdapter(this, setsList);
        
        // Set the on click listener for each item
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	// Get the set to show
            	String setName = setsList.get(position).getName();
            	
            	// Set the current set and show it
            	dbAdapter.setCurrentSet(setName);
            	mViewPager.setCurrentItem(0, true);
            	fillCurrentSetListView();
            }
        });

        // Register the context menu and set the adapter
        registerForContextMenu(lv);
        lv.setAdapter(setsAdapter);
    }
    
    /**
     * Edits the set name and date
     * @param setName The set to edit
     */
    private void editSetAtt(final String setName) {
    	// Create the alert dialog
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Create Set");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set, (ViewGroup) findViewById(R.id.add_set_root));
    	alert.setView(dialoglayout);
    	final EditText setNameET = (EditText)dialoglayout.findViewById(R.id.add_set_name);
    	final DatePicker setDateDP = (DatePicker)dialoglayout.findViewById(R.id.add_set_date);
    	
    	// Populate the set fields
    	setNameET.setText(setName);
    	String temp[] = dbAdapter.getSetDate(setName).split("/");
    	setDateDP.updateDate(Integer.parseInt(temp[2].trim()), Integer.parseInt(temp[0].trim()) - 1, Integer.parseInt(temp[1].trim()));

    	// Set the OK button
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Get the date and set name
	    		String newSetName = setNameET.getText().toString();
	    		String setDate = (setDateDP.getMonth() + 1) + "/" + setDateDP.getDayOfMonth() + "/" + setDateDP.getYear();
	    		
	    		if (newSetName.length() > 0) {
	    			dbAdapter.updateSetAttributes(setName, newSetName, setDate);
	    			
	    			// Refresh set and current set list
	    			setsAdapter.notifyDataSetChanged();
		        	currSetAdapter.notifyDataSetChanged();
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
    
    
    /*****************************************************************************
     * 
     * Song Functions
     * 
     *****************************************************************************/
    /**
     * Prompts the user for a name and creates the set
     */
    public void createSong() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Add Song");

    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_song, (ViewGroup) findViewById(R.id.add_song_root));
    	alert.setView(dialoglayout);
    	final EditText songNameET = (EditText)dialoglayout.findViewById(R.id.add_song_name);
    	final EditText authorET = (EditText)dialoglayout.findViewById(R.id.add_song_author);
    	final EditText keyET = (EditText)dialoglayout.findViewById(R.id.add_song_key);

    	// Set the OK button
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Get the user inputs
	    		String songName = songNameET.getText().toString();
	    		if (authorET.getText().length() > 0)
	    			mSongAuthor = authorET.getText().toString().trim();
	    		if (keyET.getText().length() > 1)
	    			mSongKey = keyET.getText().toString().substring(0, 1).toUpperCase() + keyET.getText().toString().substring(1).trim();
	    		else if (keyET.getText().length() > 0)
	    			mSongKey = keyET.getText().toString().toUpperCase().trim();
	    		
	    		// Create the song
	    		if (songName.length() > 0) {
	    			String songFile = songName + ".txt";
		    		if(!dbAdapter.createSong(songName, songFile, mSongAuthor, mSongKey))
		    			Toast.makeText(getApplicationContext(), "Failed to create song!", Toast.LENGTH_LONG).show();
		    		else
		    		{
		    			// If a file is waiting to be imported
		    			if (importFilePath != "")
		    			{
		    				// Copy the file into the tabapp songs directory
		    				try {
		    					if (importFilePath.substring(importFilePath.length() - 3).equals("txt")) {
		    						importTextFile(importFilePath, songFile);
		    					}
		    					else {
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
		    					}
		    				} catch (Exception e) {
		    					// Delete the song since the file could not be imported
		    					dbAdapter.deleteSong(songName);
		    					
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
		    					dbAdapter.deleteSong(songName);
		    					
		    					// Alert that the song failed
		    					Toast.makeText(getApplicationContext(), "Could not create song file, Song deleted.", Toast.LENGTH_LONG).show();
		    				}
		    				
		    			}
		    			
		    			// Set the current tab
			        	currentTab = 3;
			        	
			        	// Add the song to a group
			        	addSongToGroup(songName);
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
     * Imports a straight text file into chord pro format
     * @param inputFilePath The song text file
     * @param outputFileName The chord pro output file
     * @throws Exception IO exception
     */
    private void importTextFile(String inputFilePath, String outputFileName) throws IOException {
    	InputStream fis = new FileInputStream(importFilePath);
    	DataInputStream in = new DataInputStream(fis);
    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        boolean startedSong = false;
        
        // Add author to song
        sb.append("{author:" + mSongAuthor + "}");
        sb.append(System.getProperty("line.separator"));
        
        // Read each line of the file
        while (line != null) {
        	// Check for song part tags
        	if(line.toLowerCase().contains("verse") || line.toLowerCase().contains("chorus") || line.toLowerCase().contains("bridge") || 
        			line.toLowerCase().contains("tag") || line.toLowerCase().contains("coda") || line.toLowerCase().contains("outro")) {
        		sb.append("{title:");
        		sb.append(line);
            	sb.append("}");
            	startedSong = true;
        	}
        	// Process the intro line
        	else if (line.toLowerCase().contains("intro")) {
        		sb.append("{intro:");
        		boolean chordStart = false;
        		boolean inChord = false;
        		
        		// Escape the chords from the line
        		for (int i = 0; i < line.length(); i++) {
        			char c = line.charAt(i);
        			
        			if (c == ':') {
        				sb.append(c);
        				chordStart = true;
        			}
        			else if (chordStart && !inChord) {
        				if (c >= 65 && c <= 71) {
        					sb.append("[");
        					inChord = true;
        				}
        				sb.append(c);
        			}
        			// In a chord
        			else if (inChord) {
        				// If the letter is a new chord and the previous character is not a '/'
        				if (c >= 65 && c <= 71 && line.charAt(i - 1) != 47) {
        					sb.append("][");
        				}
        				// Else if the letter is not part of a chord
        				else if (!((c >= 49 && c <= 57) || (c >= 65 && c <= 71) || c == 35 || c == 98 || c == 109 || c == 97 || c == 100 || c == 47)) {
        					sb.append(']');
        					inChord = false;
        				}
        				sb.append(c);
        			}
        			else {
        				sb.append(c);
        			}
        		}
        		
        		// If still in a chord, close the chord out
        		if (inChord)
        			sb.append("]");
        		
        		// End the intro tag
            	sb.append("}");
        	}
        	else if (!startedSong && line.length() > 0) {
        		sb.append("{comment:");
        		sb.append(line);
            	sb.append("}");
        	}
        	else if (startedSong) {
        		// Check to see if the line has chords
        		if (line.length() > 0) {
        			// If the line starts with a space or a chord character followed by a non chord item
        			if (line.charAt(0) == 32 || (line.matches("^[A-G][\\s#bmad1-9/suA-G]*[^h-ln-rtv-zH-Z]") || (line.matches("^[A-G]")))) { 
	        			// Read the next two lines, chord and lyrics
	            		String chords = line;
	            		String lyrics = br.readLine();
	            		int chordOffset = 0;
	            		
	            		// Check to see if we are still in the song
	            		if (chords.length() == 0 && lyrics.length() == 0) {
	            			startedSong = false;
	            		}
	            		else if (chords.length() == 0 && (lyrics.toLowerCase().contains("verse") || lyrics.toLowerCase().contains("chorus") || lyrics.toLowerCase().contains("bridge") || 
	            			lyrics.toLowerCase().contains("tag") || lyrics.toLowerCase().contains("coda"))) {
	            			sb.append(System.getProperty("line.separator"));
	            			sb.append("{title:");
	                		sb.append(lyrics);
	                    	sb.append("}");
	            		}
	            		else {
	            			int len = 0;
	            			
	            			// Set the length for the for loop
	            			if (lyrics.length() > chords.length())
	            				len = lyrics.length();
	            			else
	            				len = chords.length();
	            			
	            			// Cycle through the characters in the lines
	    	        		for (int i = 0; i < len; i++) {
	    	        			
	    	        			// Decrement the chordOffset
	    	        			if (chordOffset > 0)
	    	        				chordOffset--;
	    	        			
	    	        			// Add chords to the line
	    	        			if (i < chords.length() && chordOffset <= 0) {
	    	        				char c = chords.charAt(i);
	    	        				
	    	        				// If this is a new chord
	    	        				if (c >= 65 && c <= 71) {
	    	        					// Append an open bracket and the chord
	    	        					sb.append("[");
	    	        					sb.append(c);
	    	        					chordOffset++;
	    	        					
	    	        					// Cycle forward through the chord characters
	    	        					for (int j = i+1; j < chords.length(); j++) {
	    	        						c = chords.charAt(j);
	    	        						
	    	        						// If the next character is a space end the chord
	    	        						if (c == 32) {
	    	        							break;
	    	        						}
	    	        						// If the next character is a new chord, start new chord
	    	        						else if (c >= 65 && c <= 71 && chords.charAt(j - 1) != 47) {
	    	        							sb.append("][");
	    	        						}
	    	        						sb.append(c);
	            							chordOffset++;
	    	        					}
	    	        					sb.append("]");
	    	        				}
	    	        				// If it is not a space or chord character
	    	        				else if (c != 32) {
	    	        					// Append an open bracket and the chord
	    	        					sb.append("{cc:");
	    	        					sb.append(c);
	    	        					chordOffset++;
	    	        					
	    	        					// Cycle forward through the chord characters
	    	        					for (int j = i+1; j < chords.length(); j++) {
	    	        						c = chords.charAt(j);
	    	        						
	    	        						// If the next character is a new chord, break
	    	        						if (c >= 65 && c <= 71) {
	    	        							break;
	    	        						}
	    	        						sb.append(c);
	            							chordOffset++;
	    	        					}
	    	        					
	    	        					// End the cc
	    	        					sb.append("}");
	    	        				}
	    	        			}
	    	        			
	    	        			// Add the lyrics to the line
	    	        			if (i < lyrics.length())
	    	        				sb.append(lyrics.charAt(i));
	    	        		}
	            		}
	        		}
        			else {
        				sb.append("{comment:");
	            		sb.append(line);
	                	sb.append("}");
        			}
        		}
        	}
        	else {
            	sb.append(line);
        	}
        	
        	// Add system line break
        	sb.append(System.getProperty("line.separator"));
        	
        	// Read the next line
        	line = br.readLine();
        }
        
        // Close the buffered reader
        br.close();
        
        // Write the output file
        OutputStream out = openFileOutput(outputFileName, Context.MODE_PRIVATE);
        PrintStream ps = new PrintStream(out);
        ps.print(sb);
    }
    
    /**
     * Adds the song to a group
     * @param songName The song to add
     */
    private void addSongToGroup(final String songName) {
    	// Get the list of group names
    	Cursor c = dbAdapter.getSongGroupNames();
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount()];
    	int counter = 0;
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		groupNames[counter++] = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME));
    	}
    	
    	// Create the dialog to choose which group to add the song to
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Add Song to Group");
    	alert.setItems(groupNames, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dbAdapter.addSongToGroup(songName, groupNames[which].toString());
				
				// Refresh song list
    			songsAdapter.notifyDataSetChanged();
			}
		});

    	alert.show();
    }
    
    /**
     * Removes the song from the specified group
     * @param songName The song to remove
     * @param groupName The group to remove the song from
     */
    private void removeSongFromGroup(final String songName, final String groupName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Remove Song From Group?!");
    	alert.setMessage("Are you sure you want to remove '" + songName + "' from '" + groupName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Remove song from the group
	    		dbAdapter.removeSongFromGroup(songName, groupName);
	    		
	    		// Refresh the song list
	    		songsAdapter.notifyDataSetChanged();
	        	
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
	    		
	    		// Refresh song, set and current set lists
	    		songsAdapter.notifyDataSetChanged();
	    		setsAdapter.notifyDataSetChanged();
	    		currSetAdapter.notifyDataSetChanged();
	        	
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
	    		
	    		// Refresh the song and current set view
	    		songsAdapter.notifyDataSetChanged();
	    		currSetAdapter.notifyDataSetChanged();
	        	
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
     * Sets the current song list for the specified group
     * @param groupName The song group
     */
    public void setSongsList() {
    	Cursor c = dbAdapter.getSongNames(currentSongGroup);
    	c.moveToFirst();
    	
    	// Clear the ArrayList
    	songsList.clear();
    	
    	// Populate the ArrayList
    	while (!c.isAfterLast()) {
    		// Get the strings from the cursor
        	String songName = c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME));
        	String songAuthor = c.getString(c.getColumnIndex(DBStrings.TBLSONG_AUTHOR));
        	String songKey = c.getString(c.getColumnIndex(DBStrings.TBLSONG_KEY));
        	String songFile = c.getString(c.getColumnIndex(DBStrings.TBLSONG_FILE));
    		
    		// Check previous item to determine if this is the first song name with that letter
        	if (!c.isFirst()) {
        		c.moveToPrevious();
        		if (c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME)).charAt(0) != songName.charAt(0)) {
        			// This is the first item with that letter, add the separator
        			songsList.add(new SectionItem(songName.substring(0, 1)));
        		}
        		c.moveToNext();
        	}
        	else {
        		// This is the first item, add the separator
        		songsList.add(new SectionItem(songName.substring(0, 1)));
        	}
        	
        	// Add the song item
        	songsList.add(new SongItem(songName, songAuthor, songKey, songFile));
        	
        	// Move to the next song
        	c.moveToNext();
    	}
    }
    
    /**
     * Fills the songs list
     * @param v The view for the list
     * @param groupName The name of the group to populate
     */
    public void fillSongsListView() {
    	// Fill the songs array list
    	setSongsList();
    	
    	// Set up the list view and adapter
    	ListView lv = ((ListView)findViewById(R.id.songs_list));
        lv.setEmptyView(findViewById(R.id.empty_songs));
        songsAdapter = new ItemArrayAdapter(this, songsList);
        
        // Set the on click listener for each item
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {            	
            	// Get the song to show
            	SongItem song = (SongItem)songsList.get(position);
            	String songName = song.getName();
            	String songKey = song.getKey();
            	String songText = getSongText(song.getSongFile());
            	
            	// Show the song activity
            	SongActivity songA = new SongActivity();
            	Intent showSong = new Intent(v.getContext(), songA.getClass());
            	showSong.putExtra(MainStrings.SONG_NAME_KEY, songName);
            	showSong.putExtra(MainStrings.SONG_KEY_KEY, songKey);
            	showSong.putExtra(MainStrings.SONG_TEXT_KEY, songText);
                startActivity(showSong);
            }
        });
        
        // Register the context menu and add the adapter
        registerForContextMenu(lv);
        lv.setAdapter(songsAdapter);
    }
    
    /**
     * Gets the text from the specified file
     * @return The song text
     */
    private String getSongText(String fileName) {
    	String songText = "";
    	String chordLine = "";
    	String lyricLine = "";
    	
        try {
        	FileInputStream fis = openFileInput(fileName);
        	DataInputStream in = new DataInputStream(fis);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            // Read each line of the file
            while (line != null) {
            	boolean inChord = false;
            	boolean inDelimiter = false;
            	int skipCounter = 0, charCounter = 0, commentLoc = 0;
            	String delimiter = "";    
            	
            	// For intro add the line with chord formatting but all on the same line
        		if (line.startsWith("{intro:")) {
        			for (char c : line.substring(7, line.length() - 1).toCharArray()) {
        				if (c == '[') {
        					lyricLine += "<b><font color=\"#006B9F\">";
        					continue;
        				}
        				if (c == ']') {
        					lyricLine += "</font></b>";
        					continue;
        				}
        				lyricLine += c;
        			}
        			
        			if (!lyricLine.isEmpty()) {
		                sb.append(lyricLine);
		                sb.append("<br/>");
	            	}
        		} else {
        			// Step through each character in the line
                	for (char c : line.toCharArray()) {
                		// Increment the character counter
                		charCounter++;
                		
                		// If the character is an open bracket set inChord true and continue
                		if (c == '[' && !delimiter.equals("lc")) {
                			chordLine += "<b><font color=\"#006B9F\">";
                			inChord = true;
                			continue;
                		}
                		
                		// If the character is a closed bracket set inChord false and continue
                		if (c == ']' && !delimiter.equals("lc")) {
                			chordLine += "</font></b>";
                			inChord = false;
                			continue;
                		}
                		
                		// If the character is an open { set inComment true and continue
                		if (c == '{') {
                			inDelimiter = true;
                			
                			// Set the comment type
                			commentLoc = line.indexOf(":", charCounter);
                			delimiter = line.substring(charCounter, commentLoc);
                			
                			// For title add bold
                    		if (delimiter.equals("title")) {
                    			lyricLine += "<b>";
                    		}
                    		
                    		// For author add italics
                    		if (delimiter.equals("author")) {
                    			lyricLine += "<i>";
                    		}
                			
                			continue;
                		}
                		
                		// If the character is a closed } set inComment false and continue
                		if (c == '}') {
                			inDelimiter = false;
                			
                			// For title end bold
                    		if (delimiter.equals("title")) {
                    			lyricLine += "</b>";
                    		}
                    		
                    		// For author end italics
                    		if (delimiter.equals("author")) {
                    			lyricLine += "</i>";
                    		}
                    		
                			delimiter = "";
                			commentLoc = 0;
                			continue;
                		}
                		
                		// If in a comment
                		if (inDelimiter) {
                			// A chord comment type
                			if (delimiter.equals("cc")) {
                				if (charCounter > commentLoc + 1) {
                					chordLine += c;
                					skipCounter++;
                				}
                			}
                			
                			// A lyric chord type
                			if (delimiter.equals("lc")) {
                				if (charCounter > commentLoc + 1) {
                					if (c == '[') 
                						lyricLine += "<b><font color=\"#006B9F\">";
                					else if (c == ']')
                						lyricLine += "</font></b>";
                					else
                						lyricLine += c;
                				}
                			}
                			
                			// For comments just add the line with no formatting
                    		if (delimiter.equals("comment") || delimiter.equals("title") || delimiter.equals("author")) {
                    			//sb.append(line.substring(i + 1, line.length() - 1) + "<br/>");
                    			if (charCounter > commentLoc + 1)
                    				lyricLine += c;
                    		}
                    	
                    		continue;
                		}
                		
                		// If in a chord, add the chord to the chord line
                		if (inChord) {
                			chordLine += c;
                			skipCounter++;
                		} else {
                			if (skipCounter > 0)
                				skipCounter--;
                			else
                				chordLine += "&nbsp;";
                			lyricLine += c;
                		}
                	}
    	            	
                	// Add the chord and lyric lines to the overall string builder
                	if (!chordLine.isEmpty()) {
    	                sb.append(chordLine);
    	                sb.append("<br/>");
                	}
                	if (!lyricLine.isEmpty()) {
    	                sb.append(lyricLine);
    	                sb.append("<br/>");
                	}
                	if (chordLine.isEmpty() && lyricLine.isEmpty())
                		sb.append("<br/>");
        		}
        		
        		// Clear the chord and lyric lines
                chordLine = "";
                lyricLine = "";
                
                // Read the next line
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
        // Create the open file intent
        Intent intent = new Intent(getBaseContext(), OpenFile.class);
        
        // Start the activity
        startActivityForResult(intent, 1);
    }
    
    /**
     * Edits the song name, author and key
     * @param songName The song to edit
     */
    private void editSongAtt(final String songName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Add Song");

    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_song, (ViewGroup) findViewById(R.id.add_song_root));
    	alert.setView(dialoglayout);
    	final EditText songNameET = (EditText)dialoglayout.findViewById(R.id.add_song_name);
    	final EditText authorET = (EditText)dialoglayout.findViewById(R.id.add_song_author);
    	final EditText keyET = (EditText)dialoglayout.findViewById(R.id.add_song_key);
    	
    	// Populate the text boxes
    	songNameET.setText(songName);
    	authorET.setText(dbAdapter.getSongAuthor(songName));
    	keyET.setText(dbAdapter.getSongKey(songName));
    	
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		String key = keyET.getText().toString();
	    		if (key.length() > 1)
	    			key = key.substring(0, 1).toUpperCase() + key.substring(1).trim();
	    		else if (key.length() > 0)
	    			key = key.toUpperCase().trim();
	    		
	    		dbAdapter.updateSongAttributes(songName, songNameET.getText().toString(), authorET.getText().toString(), key);
	    		
	    		// Refresh the song list
				songsAdapter.notifyDataSetChanged();
	    	}
    	});
    	
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    	    // Canceled.
	    	}
    	});
    	
    	alert.show();
    }

    
    /*****************************************************************************
     * 
     * Current Set Functions
     * 
     *****************************************************************************/
    /**
     * Fills the current set array list
     */
    public void setCurrentSetList() {
    	Cursor c = dbAdapter.getCurrentSetSongs();
    	c.moveToFirst();
    	
    	// Clear the ArrayList
    	currSetList.clear();
    	
    	// Populate the ArrayList
    	while (!c.isAfterLast()) {
    		// Get the strings from the cursor
        	String songName = c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME));
        	String songAuthor = c.getString(c.getColumnIndex(DBStrings.TBLSONG_AUTHOR));
        	String songKey = c.getString(c.getColumnIndex(DBStrings.TBLSONG_KEY));
        	String songFile = c.getString(c.getColumnIndex(DBStrings.TBLSONG_FILE));
    		
        	// Add the song item
        	currSetList.add(new SongItem(songName, songAuthor, songKey, songFile));
        	
        	// Move to the next song
        	c.moveToNext();
    	}
    }
    
    /**
     * Fills the current set list
     * @param v The view for the list
     */
    public void fillCurrentSetListView() {
    	// Fill the current set array list
    	setCurrentSetList();
    	
    	// Set up the list view and adapter
        ListView lv = ((ListView)findViewById(R.id.current_list));
        lv.setEmptyView(findViewById(R.id.empty_current));
        currSetAdapter = new ItemArrayAdapter(this, currSetList);
        
        // Set the on click listener for each item
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	// Create the string array of the song text
            	ArrayList<String[]> setSongs = new ArrayList<String[]>();
            	
            	// Loop through each song in the current set and add it to the array
            	for (Item i : currSetList) {
            		String songName = i.getName();
                	String songText = getSongText(((SongItem)i).getSongFile());
                	setSongs.add(new String[] {songName, "<h2>" + songName + "</h2>" + songText});
            	}
            	
            	// Show the set activity
            	SetActivity song = new SetActivity();
            	Intent showSong = new Intent(v.getContext(), song.getClass());
            	showSong.putExtra(MainStrings.CURRENT_SONG_KEY, position);
            	showSong.putExtra(MainStrings.SET_SONGS_KEY, setSongs);
                startActivity(showSong);
            }
    	});
      
        // Register the context menu and add the adapter
        registerForContextMenu(lv);
        lv.setAdapter(currSetAdapter);
        
        // Append the current set name to the title
        TextView title = ((TextView)findViewById(R.id.current_set_tab_title));
        if (dbAdapter.getCurrentSetName() != "")
        	title.setText(getResources().getString(R.string.title_current_set) + " - " + dbAdapter.getCurrentSetName());
    }
    
    
    /*****************************************************************************
     * 
     * Song Group Functions
     * 
     *****************************************************************************/
    /**
     * Populates the song groups array list
     */
    public void setSongGroupsList() {
    	// Query the database
    	Cursor c = dbAdapter.getSongGroupNames();
    	//startManagingCursor(c);
    	
    	// Clear the existing groups list
    	songGroupsList.clear();
    	
    	// Populate the groups
    	c.moveToFirst();
    	while (!c.isAfterLast()) {
    		songGroupsList.add(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME)));
    		c.moveToNext();
    	}
    	
    	//stopManagingCursor(c);
    }
    
    /**
     * Fills the group list spinner
     * @param v
     */
    public void fillSongGroupsSpinner() {
    	// Set the groups list
    	setSongGroupsList();
        
    	// Create the spinner adapter
    	songGroupsAdapter = new ArrayAdapter<String>(this, R.layout.group_spinner_item, songGroupsList);
    	songGroupsAdapter.setDropDownViewResource( R.layout.group_spinner_dropdown_item );
    	final Spinner groupSpinner = (Spinner) findViewById(R.id.song_group_spinner);
    	
    	// Set the on click listener for each item
    	groupSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected item and populate the songs list
            	String groupName = songGroupsList.get(position);
            	currentSongGroup = groupName;
            	setSongsList();
            	fillSongsListView();
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	
    	// Set the adapter
    	groupSpinner.setAdapter(songGroupsAdapter);
    }
    
    /**
     * Creates a new song group
     */
    private void createSongGroup() {
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
	    			if(!dbAdapter.createSongGroup(value))
		    			Toast.makeText(getApplicationContext(), "Failed to create song group!", Toast.LENGTH_LONG).show();
	    		}
	    		else
	    			Toast.makeText(getApplicationContext(), "Cannot create a song group with no name!", Toast.LENGTH_LONG).show();
	    		
	    		// Refresh the song list
	    		songsAdapter.notifyDataSetChanged();
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
    private void deleteSongGroup() {
    	// Get the list of group names
    	Cursor c = dbAdapter.getSongGroupNames();
    	//startManagingCursor(c);
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount() - 1];
    	int counter = 0;
    	
    	// Don't show the all songs group
    	c.moveToFirst();
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		groupNames[counter++] = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME));
    	}
    	
    	//stopManagingCursor(c);
    	
    	// Create the dialog to choose which group to delete
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Choose Group to Delete");
    	alert.setItems(groupNames, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final String groupName = groupNames[which].toString();
				if (groupName.equals(SongsTab.ALL_SONGS_LABEL))
					Toast.makeText(getBaseContext(), "Cannot Delete the '" + SongsTab.ALL_SONGS_LABEL + "' group!", Toast.LENGTH_LONG).show();
				else {
					// Confirm they want to delete the group
					AlertDialog.Builder confirm = new AlertDialog.Builder(MainActivity.this);
					confirm.setTitle("Delete Group?!");
					confirm.setMessage("Are you sure you want to delete '" + groupName + "'?");
					
					confirm.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int whichButton) {
				    		dbAdapter.deleteSongGroup(groupName);
							
							// Refresh the song list
			    			songsAdapter.notifyDataSetChanged();
						}
			    	});

					confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int whichButton) { 	}
			    	});

					confirm.show();
				}
			}
		});

    	alert.show();
    }
    
    /**
     * Deletes all groups
     */
    private void deleteAllSongGroups() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete All Groups?!");
    	alert.setMessage("Are you sure you want to delete ALL groups???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Delete song from database
		    	dbAdapter.deleteAllSongGroups();
		    	currentSongGroup = SongsTab.ALL_SONGS_LABEL;
	    		
	    		// Refresh the song list
	    		songsAdapter.notifyDataSetChanged();
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
     * Set Group Functions
     * 
     *****************************************************************************/
    /**
     * Fills the group list
     * @param v
     */
    public void fillSetGroupsList(final View view) {
    	//groupsCursor = dbAdapter.getGroupsList(groupName);
    	setGroupsCursor = dbAdapter.getSetGroupNames();
    	startManagingCursor(setGroupsCursor);
    	
    	String[] from = new String[] { DBStrings.TBLSETGROUPS_NAME };
        int[] to = new int[] { R.id.group_spinner_item_text };
      
        SimpleCursorAdapter songs = new SimpleCursorAdapter(this, R.layout.group_spinner_item, setGroupsCursor, from, to);
        songs.setDropDownViewResource( R.layout.group_spinner_dropdown_item );
    	Spinner groupSpinner = (Spinner) findViewById(R.id.set_group_spinner);
    	
    	// Set the on click listener for each item
    	groupSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected item and populate the songs list
            	setGroupsCursor.moveToPosition(position);
            	String groupName = setGroupsCursor.getString(setGroupsCursor.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME));
            	fillSetsList();
            	
            	// Refresh the views
            	//refreshLists();
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	
    	groupSpinner.setAdapter(songs);
    }
    
    /**
     * Creates a new set group
     */
    private void createSetGroup() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Create Set Group");
    	alert.setMessage("Please enter the name of the set group (must be unique)");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		String value = input.getText().toString();
	    		if (value.length() > 0) {
	    			if(!dbAdapter.createSetGroup(value))
		    			Toast.makeText(getApplicationContext(), "Failed to create set group!", Toast.LENGTH_LONG).show();
	    		}
	    		else
	    			Toast.makeText(getApplicationContext(), "Cannot create a set group with no name!", Toast.LENGTH_LONG).show();
	    		
	    		// Refresh the view
	    		//refreshLists();
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
     * Deletes the specified set group
     * @param groupName The group to delete
     */
    private void deleteSetGroup() {
    	// Get the list of group names
    	Cursor c = dbAdapter.getSetGroupNames();
    	//startManagingCursor(c);
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount() - 1];
    	int counter = 0;
    	
    	// Don't show the all songs group
    	c.moveToFirst();
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		groupNames[counter++] = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME));
    	}
    	
    	//stopManagingCursor(c);
    	
    	// Create the dialog to choose which group to delete
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Choose Group to Delete");
    	alert.setItems(groupNames, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final String groupName = groupNames[which].toString();
				if (groupName.equals(SetsTab.ALL_SETS_LABEL))
					Toast.makeText(getBaseContext(), "Cannot Delete the '" + SetsTab.ALL_SETS_LABEL + "' group!", Toast.LENGTH_LONG).show();
				else {
					// Confirm they want to delete the group
					AlertDialog.Builder confirm = new AlertDialog.Builder(MainActivity.this);
					confirm.setTitle("Delete Group?!");
					confirm.setMessage("Are you sure you want to delete '" + groupName + "'?");
					
					confirm.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int whichButton) {
				    		dbAdapter.deleteSetGroup(groupName);
							
							// Refresh group and song lists
			    			//refreshLists();
						}
			    	});

					confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int whichButton) { 	}
			    	});

					confirm.show();
				}
			}
		});

    	alert.show();
    }
    
    /**
     * Deletes all groups
     */
    private void deleteAllSetGroups() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete All Set Groups?!");
    	alert.setMessage("Are you sure you want to delete ALL set groups???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Delete song from database
		    	dbAdapter.deleteAllSetGroups();
	    		
	    		// Refresh song list
	    		//refreshLists();
	        	
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

