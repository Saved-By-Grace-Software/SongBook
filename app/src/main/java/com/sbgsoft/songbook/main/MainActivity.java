package com.sbgsoft.songbook.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ericharlow.dragndrop.DragNDropListActivity;
import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.db.DBAdapter;
import com.sbgsoft.songbook.db.DBStrings;
import com.sbgsoft.songbook.files.OpenFile;
import com.sbgsoft.songbook.items.Item;
import com.sbgsoft.songbook.items.ItemArrayAdapter;
import com.sbgsoft.songbook.items.SectionItem;
import com.sbgsoft.songbook.items.SetItem;
import com.sbgsoft.songbook.items.SetSearchCriteria;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.items.SongSearchCriteria;
import com.sbgsoft.songbook.main.StaticVars.SongFileType;
import com.sbgsoft.songbook.sets.CurrentSetTab;
import com.sbgsoft.songbook.sets.SetActivity;
import com.sbgsoft.songbook.sets.SetGroupArrayAdapter;
import com.sbgsoft.songbook.sets.SetsTab;
import com.sbgsoft.songbook.songs.ChordDisplay;
import com.sbgsoft.songbook.songs.ChordProParser;
import com.sbgsoft.songbook.songs.EditSongRawActivity;
import com.sbgsoft.songbook.songs.SongActivity;
import com.sbgsoft.songbook.songs.SongGroupArrayAdapter;
import com.sbgsoft.songbook.songs.SongsTab;
import com.sbgsoft.songbook.songs.TextFileImporter;
import com.sbgsoft.songbook.songs.TimeSignature;
import com.sbgsoft.songbook.views.AutoFitTextView;
import com.sbgsoft.songbook.zip.Compress;
import com.sbgsoft.songbook.zip.Decompress;

public class MainActivity extends FragmentActivity {

    //region Class Variables
	// *****************************************************************************
	// * 
	// * Class Variables
	// * 
	// *****************************************************************************
	private static int currentTab = 1;
	private static String currentSongGroup = SongsTab.ALL_SONGS_LABEL;
	private static String currentSetGroup = SetsTab.ALL_SETS_LABEL;
	
	public static DBAdapter dbAdapter;
	static ViewPager mViewPager;
	public Fragment currSetFragment;
	public Fragment setsFragment;
	public Fragment songsFragment;
	
	FragmentTransaction transaction;
	private String importFilePath = "";
	private int setsCurrentScrollPosition = 0;
	private int setsCurrentScrollOffset = 0;
	private int songsCurrentScrollPosition = 0;
	private int songsCurrentScrollOffset = 0;
	
	private int setsListSortByIndex = 0;
	
	private ArrayList<Item> songsList = new ArrayList<Item>();
	private ArrayAdapter<Item> songsAdapter;
	private ArrayList<String> songGroupsList = new ArrayList<String>();
	private ArrayAdapter<String> songGroupsAdapter;
	private ArrayList<Item> currSetList = new ArrayList<Item>();
	private ArrayAdapter<Item> currSetAdapter;
	private ArrayList<Item> setsList = new ArrayList<Item>();
	private ArrayAdapter<Item> setsAdapter;
	private ArrayList<String> setGroupsList = new ArrayList<String>();
	private ArrayAdapter<String> setGroupsAdapter;
	private ArrayAdapter<String> songSortAdapter;
	private ArrayAdapter<String> setSortAdapter;
	
	private Map<String, Boolean> addSongsDialogMap = new HashMap<String, Boolean>();
	private ArrayList<String> addSongsDialogList = new ArrayList<String>();
	
	private Map<String, Boolean> addSetsDialogMap = new HashMap<String, Boolean>();
	private ArrayList<String> addSetsDialogList = new ArrayList<String>();
	
	private ProgressDialog progressDialog;

    private SetItem setToExport;
	//endregion


    //region Class Functions
	// *****************************************************************************
    // * 
    // * Class Functions
    // * 
    // *****************************************************************************
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
            case R.id.menu_sets_import:
                permissionRequiredFunction(StaticVars.PERMISSIONS_SET_IMPORT);
                return true;
            case R.id.menu_sets_find:
                findSetDialog();
                return true;
	        case R.id.menu_songs_clear:
	        	deleteAllSongs();
	        	return true;
	        case R.id.menu_songs_create:
	        	createSong();
	        	return true;
	        case R.id.menu_songs_import:
                permissionRequiredFunction(StaticVars.PERMISSIONS_SONG_IMPORT);
	        	return true;
            case R.id.menu_songs_find:
                findSongDialog();
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
	        case R.id.menu_backup_export:
                permissionRequiredFunction(StaticVars.PERMISSIONS_BACKUP_EXPORT);
	        	return true;
	        case R.id.menu_backup_import:
                permissionRequiredFunction(StaticVars.PERMISSIONS_BACKUP_IMPORT);
	        	return true;
            case R.id.menu_about_howto:
                showHowTos();
                return true;
	        case R.id.menu_about_about:
	        	showAboutBox();
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
    		menu.add(Menu.NONE, StaticVars.EDIT_SONG, StaticVars.EDIT_SONG, R.string.cmenu_songs_edit);
    		menu.add(Menu.NONE, StaticVars.EDIT_SONG_ATT, StaticVars.EDIT_SONG_ATT, R.string.cmenu_songs_edit_att);
    		menu.add(Menu.NONE, StaticVars.DELETE_SONG, StaticVars.DELETE_SONG, R.string.cmenu_songs_delete);
    		menu.add(Menu.NONE, StaticVars.ADD_SONG_SET, StaticVars.ADD_SONG_SET, R.string.cmenu_song_add_set);
    		menu.add(Menu.NONE, StaticVars.ADD_SONG_CURR_SET, StaticVars.ADD_SONG_CURR_SET, R.string.cmenu_song_add_curr_set);
    		menu.add(Menu.NONE, StaticVars.SONG_GROUPS_ADD, StaticVars.SONG_GROUPS_ADD, R.string.cmenu_song_group_add);
    		menu.add(Menu.NONE, StaticVars.SONG_GROUPS_DEL, StaticVars.SONG_GROUPS_DEL, R.string.cmenu_song_group_delete);
    		menu.add(Menu.NONE, StaticVars.SHARE_SONG, StaticVars.SHARE_SONG, R.string.cmenu_songs_share);
    		menu.add(Menu.NONE, StaticVars.SONG_STATS, StaticVars.SONG_STATS, R.string.cmenu_songs_stats);
    	}
    	// Sets context menu
    	else if (v.getId() == R.id.sets_list) {
    		menu.setHeaderTitle("Sets Menu");
    		menu.add(Menu.NONE, StaticVars.DELETE_SET, StaticVars.DELETE_SET, R.string.cmenu_sets_delete);
    		menu.add(Menu.NONE, StaticVars.EDIT_SET, StaticVars.EDIT_SET, R.string.cmenu_sets_edit);
    		menu.add(Menu.NONE, StaticVars.EDIT_SET_ATT, StaticVars.EDIT_SET_ATT, R.string.cmenu_sets_edit_att);
    		menu.add(Menu.NONE, StaticVars.REORDER_SET, StaticVars.REORDER_SET, R.string.cmenu_sets_reorder);
    		menu.add(Menu.NONE, StaticVars.SET_GROUPS_ADD, StaticVars.SET_GROUPS_ADD, R.string.cmenu_set_group_add);
    		menu.add(Menu.NONE, StaticVars.SET_GROUPS_DEL, StaticVars.SET_GROUPS_DEL, R.string.cmenu_set_group_delete);
    		menu.add(Menu.NONE, StaticVars.SHARE_SET, StaticVars.SHARE_SET, R.string.cmenu_sets_share);
    	}
    	// Current Set context menu
    	else if (v.getId() == R.id.current_list) {
    		menu.setHeaderTitle("Current Set Menu");
    		menu.add(Menu.NONE, StaticVars.EDIT_SONG_CS, StaticVars.EDIT_SONG_CS, R.string.cmenu_songs_edit);
    		menu.add(Menu.NONE, StaticVars.EDIT_SONG_ATT_CS, StaticVars.EDIT_SONG_ATT_CS, R.string.cmenu_songs_edit_att);
    		menu.add(Menu.NONE, StaticVars.SET_SONG_KEY_CS, StaticVars.SET_SONG_KEY_CS, R.string.cmenu_sets_set_song_key);
    		menu.add(Menu.NONE, StaticVars.SHARE_SONG_CS, StaticVars.SHARE_SONG_CS, R.string.cmenu_songs_share);
    		menu.add(Menu.NONE, StaticVars.REMOVE_SONG_FROM_SET, StaticVars.REMOVE_SONG_FROM_SET, R.string.cmenu_sets_remove_song);
    		menu.add(Menu.NONE, StaticVars.SONG_STATS_CS, StaticVars.SONG_STATS_CS, R.string.cmenu_songs_stats);
    	}
    }
    
    /**
     * Responds to context menu click
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	String setName, songName, groupName;
    	Intent i;
    	SongItem songI;
    	SetItem setI;
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	AlertDialog.Builder alert;
    	
    	switch (item.getItemId()) {
    		case StaticVars.DELETE_SONG:
    			// Get the song name and delete it
				songName = songsList.get(info.position).getName();
                deleteSong(songName);
            	
                return true;
    		case StaticVars.EDIT_SONG:
    			// Get the song name
    			final String editSongName = songsList.get(info.position).getName();
    			final String editSongFile = ((SongItem)songsList.get(info.position)).getSongFile();
    			
    			// Create the edit activity intent
            	i = new Intent(getBaseContext(), EditSongRawActivity.class);
                i.putExtra(StaticVars.SONG_NAME_KEY, editSongName);
                i.putExtra(StaticVars.SONG_FILE_KEY, editSongFile);
                
                // Start the activity
                startActivity(i);
                return true;
    		case StaticVars.EDIT_SONG_CS:
    			// Get the song name
    			final String editSongCSName = currSetList.get(info.position).getName();
    			final String editSongCSFile = ((SongItem)currSetList.get(info.position)).getSongFile();
    		
    			// Create the edit activity intent
            	i = new Intent(getBaseContext(), EditSongRawActivity.class);
                i.putExtra(StaticVars.SONG_NAME_KEY, editSongCSName);
                i.putExtra(StaticVars.SONG_FILE_KEY, editSongCSFile);
                
                // Start the activity
                startActivity(i);
                return true;
    		case StaticVars.EDIT_SONG_ATT:
    			// Get the song name
    			songName = songsList.get(info.position).getName();
                    
				// Show the edit dialog
    			editSongAtt(songName);
    			
                return true;
    		case StaticVars.EDIT_SONG_ATT_CS:
    			// Get the song name
    			songName = currSetList.get(info.position).getName();
                    
				// Show the edit dialog
    			editSongAtt(songName);
    			
                return true;
    		case StaticVars.SHARE_SONG:
    			// Get the song name
    			songI = (SongItem)songsList.get(info.position);
    			
    			// Email the song
    			shareSong(songI);
    			
    			return true;
    		case StaticVars.SHARE_SONG_CS:
    			// Get the song name
    			songI = (SongItem)currSetList.get(info.position);
    			
    			// Email the song
    			shareSong(songI);
    			
    			return true;
    		case StaticVars.SET_SONG_KEY_CS:
    			songI = (SongItem)currSetList.get(info.position);
    			setName = dbAdapter.getCurrentSetName();
    			
    			setSongKeyForSet(setName, songI);
    			
    			return true;
    		case StaticVars.REMOVE_SONG_FROM_SET:
    			// Get the set name and song name
    			songName = currSetList.get(info.position).getName();
    			setName = dbAdapter.getCurrentSetName();
    			final String fsongName = songName;
    			final String fsetName = setName;
                final int fsongOrder = info.position;
    			
    			alert = new AlertDialog.Builder(this);

    	    	alert.setTitle("Remove Song?!");
    	    	alert.setMessage("Are you sure you want to remove '" + songName + "' from the set '" + setName + "'?");

    	    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		    	public void onClick(DialogInterface dialog, int whichButton) {
    		    		// Remove the song from the set
    	    			dbAdapter.removeSongFromSet(fsetName, fsongName, fsongOrder);
    	    			
    	    			// Refresh the current set list
    	    			fillCurrentSetListView();
    				}
    	    	});

    	    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    		    	public void onClick(DialogInterface dialog, int whichButton) {
    		    		//Do nothing
    		    	}
    	    	});

    	    	alert.show();
    	    	
    			return true;
    		case StaticVars.ADD_SONG_SET:
    			// Get the song name
    			songName = songsList.get(info.position).getName();
    			
    			// Edit the songs groups
    			addSongToSet(songName);
    			
    			return true;
    		case StaticVars.ADD_SONG_CURR_SET:
    			// Get the song name
    			songName = songsList.get(info.position).getName();
    			
    			// Edit the songs groups
    			addSongToCurrentSet(songName);
    			
    			return true;
    		case StaticVars.SONG_STATS:
    			// Get the song name
    			songName = songsList.get(info.position).getName();
    			
    			// Show the song stats dialog
    			showSongStats(songName);
    			
    			return true;
    		case StaticVars.SONG_STATS_CS:
    			// Get the song name
    			songName = currSetList.get(info.position).getName();
    			
    			// Show the song stats dialog
    			showSongStats(songName);
    			
    			return true;
    		case StaticVars.SONG_GROUPS_ADD:
    			// Get the song name
    			songName = songsList.get(info.position).getName();
    			
    			// Edit the songs groups
    			addSongToGroup(songName);
    			
    			return true;
    		case StaticVars.SONG_GROUPS_DEL:
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
    		case StaticVars.DELETE_SET:
    			// Get the set selected
            	setName = setsList.get(info.position).getName();
            	
            	// Delete the set
                deleteSet(setName);
                return true;
    		case StaticVars.REORDER_SET:
    			// Get the set selected
    			setName = setsList.get(info.position).getName();

                // Trigger reordering of the set
                reorderSet(setName);

            	return true;
    		case StaticVars.EDIT_SET:
    			// Get the set selected
    			setName = setsList.get(info.position).getName();
            	
            	// Show the dialog to edit songs
            	updateSetSongs(setName);
            	return true;
    		case StaticVars.EDIT_SET_ATT:
    			// Get the set selected
    			setName = setsList.get(info.position).getName();
            	
            	// Update the set attributes
            	editSetAtt(setName);
            	
            	return true;
    		case StaticVars.SHARE_SET:
    			// Get the song name
    			setI = (SetItem)setsList.get(info.position);
    			
    			// Email the song
    			shareSet(setI);
    			return true;
    		case StaticVars.SET_GROUPS_ADD:
    			// Get the song name
    			setName = setsList.get(info.position).getName();
    			
    			// Edit the songs groups
    			addSetToGroup(setName);
    			
    			return true;
    		case StaticVars.SET_GROUPS_DEL:
    			// Get the song name
    			setName = setsList.get(info.position).getName();
    			
    			// Get the current group
    			Spinner s1 = (Spinner)findViewById(R.id.set_group_spinner);
    			int position1 = s1.getSelectedItemPosition();
    			groupName = setGroupsList.get(position1);
    			
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
     * Called when the activity is destroyed
     */
    @Override
    protected void onDestroy() {
    	super.onDestroy();
        dbAdapter.close();
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
        	String activityType = data.getStringExtra(StaticVars.ACTIVITY_RESPONSE_TYPE);
        	
        	// If returning from an import song activity
        	if (activityType.equals(StaticVars.IMPORT_SONG_ACTIVITY)) {
	            importFilePath = data.getStringExtra(OpenFile.RESULT_PATH);
	            createSong();
        	}
        	// If returning from an import database activity
        	else if (activityType.equals(StaticVars.IMPORT_DB_ACTIVITY)) {
        		String filePath = data.getStringExtra(OpenFile.RESULT_PATH);
        		importFile(filePath, true, "This will erase all data currently in your database.  Do you want to continue?");
        	}
            // If returning from an import set activity
            else if (activityType.equals(StaticVars.IMPORT_SET_ACTIVITY)) {
                String filePath = data.getStringExtra(OpenFile.RESULT_PATH);
                importFile(filePath, false, "Are you sure you want to import \"" + filePath + "\"");
            }
        	// If returning from an export database activity
        	else if (activityType.equals(StaticVars.EXPORT_DB_ACTIVITY)) {
        		String folder = data.getStringExtra(OpenFile.RESULT_PATH);
        		exportAll(folder);
        	}
            // If returning from an export set activity
            else if (activityType.equals(StaticVars.EXPORT_SET_ACTIVITY)) {
                String folder = data.getStringExtra(OpenFile.RESULT_PATH);
                exportSet(folder);
            }
        	// If returning from the reorder activity
        	else if (activityType.equals(StaticVars.REORDER_ACTIVITY)) {
        		String[] newOrder = data.getStringArrayExtra(StaticVars.SET_SONGS_KEY);
        		String setName = data.getStringExtra(StaticVars.SET_NAME_KEY);
        		
        		if(!dbAdapter.reorderSet(setName, newOrder)) {
        			Toast.makeText(getApplicationContext(), "Could not update set order!", Toast.LENGTH_LONG).show();
        		}
        		
        		// Refresh the current set view
        		fillCurrentSetListView();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case StaticVars.PERMISSIONS_BACKUP_IMPORT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    executePermReqFunction(requestCode);
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Must have access to External Storage for this function!", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    //endregion


    //region Other Functions
    /**
     * Shows the about box with app information
     */
    public void showAboutBox() {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);

        // Set the dialog view to show the about message
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.simple_text_dialog, (ViewGroup) findViewById(R.id.simple_dialog_root));
        alert.setView(dialoglayout);
        final TextView tv = (TextView)dialoglayout.findViewById(R.id.simple_dialog_text);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

    	// Create the dialog
    	alert.setTitle("About " + getString(R.string.app_name));
    	
    	// Build the message
    	int start, end, startTitle = 0, endTitle;
    	SpannableStringBuilder message = new SpannableStringBuilder();
    	StyleSpan italics;
    	RelativeSizeSpan smallFont;

    	try {
			message.append(getString(R.string.full_app_name) + " v" + 
					getPackageManager().getPackageInfo(getPackageName(), 0).versionName +
					StaticVars.EOL);
		} catch (NameNotFoundException e) {
			message.append(getString(R.string.full_app_name) + StaticVars.EOL);
		}
    	message.append("Database Version " + DBStrings.DATABASE_VERSION + StaticVars.EOL);
        endTitle = message.length();
    	message.append(StaticVars.EOL);
    	message.append("Truth Gaming & Saved By Grace Software" + StaticVars.EOL);
    	message.append("Pittsburgh, PA" + StaticVars.EOL);
    	message.append("http://truthgaming.net" + StaticVars.EOL);
        message.append("savedbygracesoft@gmail.com" + StaticVars.EOL);
    	message.append(StaticVars.EOL);
    	message.append("Virtual SongBook is designed to allow you to carry all of your guitar music with you wherever " + 
    			"you go on your Android phone or tablet. It also allows you to create sets of songs for performances, gigs " +
    			"or worship. If you have any problems or questions please send us an email.  God Bless!!" + StaticVars.EOL);
    	message.append(StaticVars.EOL);
    	start = message.length();
    	message.append("\"For by grace you have been saved through faith. And this is not your own doing; it is the gift of God, " +
    			"not a result of works so that no one may boast." +
                StaticVars.EOL + "-Ephesians 2:8-9");
        end = message.length();

        // Set the spans
        italics = new StyleSpan(Typeface.ITALIC);
        smallFont = new RelativeSizeSpan(0.75f);
        message.setSpan(italics, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        message.setSpan(smallFont, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        message.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), startTitle, endTitle,  Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        message.setSpan(new StyleSpan(Typeface.BOLD), startTitle, endTitle, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        // Make the links clickable
        Linkify.addLinks(message, Linkify.ALL);

        // Display information
        tv.setText(message);
    	
    	// Add an OK button
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Show the dialog and make the links clickable
        alert.show();
    }

    /**
     * Shows the how to instructions
     */
    public void showHowTos() {
        // Create the options array
        final CharSequence[] options = getResources().getStringArray(R.array.how_tos);

        // Create the options dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("How To What?");

        // Set the items
        alert.setItems(options, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichItem) {
                StringBuilder message = new StringBuilder();
                ArrayList<String> instructions = new ArrayList<String>();
                int counter = 1;

                // Create the instructions dialog
                AlertDialog.Builder instrAlert = new AlertDialog.Builder(MainActivity.this);
                instrAlert.setTitle("How To " + options[whichItem]);

                // Make the text for instructions small
                message.append("<small>");

                // Build the instructions to show
                switch (whichItem) {
                    case 0:     // Create a set
                        instructions = StaticVars.howToCreateSet;
                        break;
                    case 1:     // Add songs to a set
                        // Add the special note to the message
                        message.append("<i>*If you don't have a song in your list already you will need to import it</i><br /><br />");
                        instructions = StaticVars.howToAddSongToSet;
                        break;
                    case 2:     // Import a song
                        instructions = StaticVars.howToImportSong;
                        break;
                    case 3:     // Change the order of songs in a set
                        instructions = StaticVars.howToOrderSongs;
                        break;
                    case 4:     // Change the key a song uses in a set
                        instructions = StaticVars.howToChangeSetKey;
                        break;
                    case 5:     // Use the metronome
                        instructions = StaticVars.howToUseMetronome;
                        break;
                }

                // Build the how to message string
                for (String i : instructions) {
                    // Add the step number
                    message.append(counter + ")  ");

                    // Add the instruction
                    //message.append("<i>" + i + "</i><br /><br />");
                    message.append(i + "<br /><br />");

                    // Increment the counter
                    counter++;
                }

                // Trim the last line breaks and close the small tag
                message.delete(message.length() - 12, message.length());
                message.append("</small>");

                // Add the instructions to the dialog
                instrAlert.setMessage(Html.fromHtml(message.toString()));

                // Add an OK button
                instrAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                // Add a back button
                instrAlert.setNeutralButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Show the how to options again
                        showHowTos();
                    }
                });

                // Show the instructions
                instrAlert.show();
            }
        });

        // Show the dialog
        alert.show();
    }

    /**
     * Checks for and requests access to files
     */
    public void permissionRequiredFunction (int permissionRequestType) {
        // Only request runtime permissions on 23 or higher
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Request permissions
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        permissionRequestType);

            } else {
                // We already have permissions, run the function
                executePermReqFunction(permissionRequestType);
            }/**/
        } else {
            // Below SDK 23, don't need runtime permissions
            executePermReqFunction(permissionRequestType);
        }
    }

    /**
     * Executes the specified function now that we have permissions
     * @param permissionRequestType The switch for which function to execute
     */
    private void executePermReqFunction (int permissionRequestType) {
        switch (permissionRequestType) {
            case StaticVars.PERMISSIONS_BACKUP_IMPORT:
                selectImportFile(StaticVars.IMPORT_DB_ACTIVITY);
                break;
            case StaticVars.PERMISSIONS_BACKUP_EXPORT:
                selectExportFolder(StaticVars.EXPORT_DB_ACTIVITY);
                break;
            case StaticVars.PERMISSIONS_SONG_IMPORT:
                importSong();
                break;
            case StaticVars.PERMISSIONS_SET_EXPORT:
                selectExportFolder(StaticVars.EXPORT_SET_ACTIVITY);
                break;
            case StaticVars.PERMISSIONS_SET_IMPORT:
                selectImportFile(StaticVars.IMPORT_SET_ACTIVITY);
                break;
            default:
                break;
        }
    }

    /**
     * Opens the search dialog for songs
     * @param v
     */
    public void onSongSearchClick(View v) {
        findSongDialog();
    }

    /**
     * Opens the search dialog for sets
     * @param v
     */
    public void onSetSearchClick(View v) {
        findSetDialog();
    }
    //endregion


    //region Set Functions
    // *****************************************************************************
    // * 
    // * Set Functions
    // * 
    // *****************************************************************************
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
	    		String setDate = setDateDP.getYear() + "-" + String.format("%02d", (setDateDP.getMonth() + 1)) + "-" + String.format("%02d", setDateDP.getDayOfMonth());
	    		
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
    	Cursor c = dbAdapter.getSongs(SongsTab.ALL_SONGS_LABEL);
    	
    	// Clear the previous song lists
    	addSongsDialogList.clear();
    	addSongsDialogMap.clear();
    	
    	// Populate the songs lists
    	while(c.moveToNext()) {
    		addSongsDialogList.add(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME)));
    		addSongsDialogMap.put(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME)), false);
    	}
    	c.close();
    	Collections.sort(addSongsDialogList, new SortIgnoreCase());
    	
    	// Create the alert dialog and set the title
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Select Songs");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set_songs, (ViewGroup) findViewById(R.id.add_set_songs_root));
    	alert.setView(dialoglayout);
    	
    	// Get the views
    	final Spinner songGroupSP = (Spinner)dialoglayout.findViewById(R.id.add_set_songs_spinner);
    	final ListView songsLV = (ListView)dialoglayout.findViewById(R.id.add_set_songs_list);
    	final ArrayAdapter<String> songsAD;
    	
    	// Fill the list view
    	songsLV.setEmptyView(findViewById(R.id.empty_songs));
    	songsLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	songsLV.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	String song = addSongsDialogList.get(position);
            	addSongsDialogMap.put(song, !addSongsDialogMap.get(song));
            }
        });
    	songsAD = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, addSongsDialogList);
    	songsLV.setAdapter(songsAD);
    	
    	// Fill the group spinner
    	setSongGroupsList();
    	songGroupSP.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected group
            	String groupName = (String)songGroupSP.getSelectedItem();

            	// Fill the new songs list
            	Cursor c = dbAdapter.getSongs(groupName);
            	addSongsDialogList.clear();

            	// Populate the ArrayList
            	while (c.moveToNext()) {
            		// Get the strings from the cursor
                	String songName = c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME));
                	addSongsDialogList.add(songName);
            	}
            	c.close();
            	Collections.sort(addSongsDialogList, new SortIgnoreCase());

            	// Update list view
            	songsAD.notifyDataSetChanged();

            	// Set the list view checked properties
            	for(int i = 0; i < songsLV.getCount(); i++) {
            		songsLV.setItemChecked(i, addSongsDialogMap.get(songsLV.getItemAtPosition(i)));
            	}
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	songGroupSP.setAdapter(songGroupsAdapter);
    	
    	// Set positive button of the dialog
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Set all selected items to the songs for the set	    		
	    		ArrayList<String> setSongs = new ArrayList<String>();
	    		for(String s : addSongsDialogMap.keySet()) {
	    			if(addSongsDialogMap.get(s))
	    				setSongs.add(s);
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

    	// Set negative button of the dialog
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Set the current tab
	        	currentTab = 2;
	    	}
    	});

    	// Show the dialog
    	AlertDialog a = alert.create();
    	a.show();
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics); 
    	int height = metrics.heightPixels;
    	height = (int) (height / 1.5);
    	a.getWindow().setLayout(LayoutParams.WRAP_CONTENT, height);
    }
    
    /**
     * Adds the song to a group
     * @param setName The song to add
     */
    private void addSetToGroup(final String setName) {
    	// Remember the current scroll position
    	ListView lv = ((ListView)findViewById(R.id.sets_list));
    	setsCurrentScrollPosition = lv.getFirstVisiblePosition();
    	setsCurrentScrollOffset = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
    	
    	// Get the list of group names
    	Cursor c = dbAdapter.getSetGroupNames();
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount()];
    	final boolean[] checkedGroupNames = new boolean[c.getCount()];
    	int counter = 0;
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		String groupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME));
    		if (groupName.equals(SetsTab.ALL_SETS_LABEL))
    			groupName = "No Group";
    		checkedGroupNames[counter] = false;
    		groupNames[counter++] = groupName;
    	}
    	c.close();
    	
    	// Create the dialog to choose which group to add the song to
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Add Set to Group");
    	
    	alert.setMultiChoiceItems(groupNames, checkedGroupNames, new DialogInterface.OnMultiChoiceClickListener() {

            public void onClick(DialogInterface dialog, int which, boolean checked) {
                checkedGroupNames[which] = checked;
            }
        });
    	
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add the song to the selected groups
                for (int i = 0; i < groupNames.length; i++) {
                    if (!groupNames[i].equals("No Group") && checkedGroupNames[i])
                        dbAdapter.addSetToGroup(setName, groupNames[i].toString());
                }

                // Refresh song list
                fillSetsListView();
                fillSetGroupsSpinner();
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
    	// Remember the current scroll position
    	ListView lv = ((ListView)findViewById(R.id.sets_list));
    	setsCurrentScrollPosition = lv.getFirstVisiblePosition();
    	setsCurrentScrollOffset = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Remove Set From Group?!");
    	alert.setMessage("Are you sure you want to remove '" + setName + "' from '" + groupName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Remove song from the group
                dbAdapter.removeSetFromGroup(setName, groupName);

                // Update set list view
                fillSetsListView();

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
    	Cursor c = dbAdapter.getSongs(SongsTab.ALL_SONGS_LABEL);
    	
    	// Clear the previous song lists
    	addSongsDialogList.clear();
    	addSongsDialogMap.clear();
    	
    	// Populate the songs lists
    	while(c.moveToNext()) {
    		String songName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
    		addSongsDialogList.add(songName);
    		addSongsDialogMap.put(songName, dbAdapter.isSongInSet(songName, setName));
    	}
    	c.close();
    	Collections.sort(addSongsDialogList, new SortIgnoreCase());

    	// Create the alert dialog and set the title
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Select Songs");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set_songs, (ViewGroup) findViewById(R.id.add_set_songs_root));
    	alert.setView(dialoglayout);
    	
    	// Get the views
    	final Spinner songGroupSP = (Spinner)dialoglayout.findViewById(R.id.add_set_songs_spinner);
    	final ListView songsLV = (ListView)dialoglayout.findViewById(R.id.add_set_songs_list);
    	final ArrayAdapter<String> songsAD;
    	
    	// Fill the list view
    	songsLV.setEmptyView(findViewById(R.id.empty_songs));
    	songsLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	songsLV.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	String song = addSongsDialogList.get(position);
            	addSongsDialogMap.put(song, !addSongsDialogMap.get(song));
            }
        });
    	songsAD = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, addSongsDialogList);
    	songsLV.setAdapter(songsAD);
    	
    	// Fill the group spinner
    	setSongGroupsList();
    	songGroupSP.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected group
            	String groupName = (String)songGroupSP.getSelectedItem();
            	
            	// Fill the new songs list
            	Cursor c = dbAdapter.getSongs(groupName);
            	addSongsDialogList.clear();
            	
            	// Populate the ArrayList
            	while (c.moveToNext()) {
            		// Get the strings from the cursor
                	String songName = c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME));
                	addSongsDialogList.add(songName);
            	}
            	c.close();
            	Collections.sort(addSongsDialogList, new SortIgnoreCase());
            	
            	// Update list view
            	songsAD.notifyDataSetChanged();
            	
            	// Set the list view checked properties
            	for(int i = 0; i < songsLV.getCount(); i++) {
            		songsLV.setItemChecked(i, addSongsDialogMap.get(songsLV.getItemAtPosition(i)));
            	}
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	songGroupSP.setAdapter(songGroupsAdapter);
    	
    	// Set positive button of the dialog
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Set all selected items to the songs for the set	    		
	    		ArrayList<String> setSongs = new ArrayList<String>();
	    		for(String s : addSongsDialogMap.keySet()) {
	    			if(addSongsDialogMap.get(s))
	    				setSongs.add(s);
	    		}
	    		
	    		// Create the set and refresh the list
	    		if(!dbAdapter.updateSet(setName, setSongs.toArray(new String[setSongs.size()])))
	    			Toast.makeText(getApplicationContext(), "Failed to update set!", Toast.LENGTH_LONG).show();
	    		else {
	    			// Update current set list
	    			fillCurrentSetListView();
	    		}
	    		
	    		// Set the current tab
	        	currentTab = 2;
			}
    	});

    	// Set negative button of the dialog
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Set the current tab
	        	currentTab = 2;
	    	}
    	});

    	// Show the dialog
    	AlertDialog a = alert.create();
    	a.show();
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics); 
    	int height = metrics.heightPixels;
    	height = (int) (height / 1.5);
    	a.getWindow().setLayout(LayoutParams.WRAP_CONTENT, height);
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
	    		
	    		// Refresh song, set and current set lists
	    		fillCurrentSetListView();
	    		fillSetGroupsSpinner();
	    		fillSetsListView();
	        	
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
    	// Remember the current scroll position
    	ListView lv = ((ListView)findViewById(R.id.sets_list));
    	setsCurrentScrollPosition = lv.getFirstVisiblePosition();
    	setsCurrentScrollOffset = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete Song?!");
    	alert.setMessage("Are you sure you want to delete '" + setName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Delete set from database
                dbAdapter.deleteSet(setName);

                // Refresh set and current set list
                fillSetGroupsSpinner();
                fillSetsListView();
                fillCurrentSetListView();

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
    public int setSetsList(SetSearchCriteria setSearch) {
        int ret;
        Cursor c;

        // Determine if we are searching or using the song group
        if (setSearch == null)
            c = dbAdapter.getSets(currentSetGroup);
        else
            c = dbAdapter.getSetsSearch(setSearch);

        c.moveToFirst();
        ret = c.getCount();
    	
    	// Clear the ArrayList
    	setsList.clear();

        // Display error message for searching
        if (c.getCount() <= 0 && setSearch != null)
            Toast.makeText(getApplicationContext(), "No sets match that search", Toast.LENGTH_LONG).show();

    	// Populate the ArrayList
    	while (!c.isAfterLast()) {
    		// Get the strings from the cursor
        	String setName = c.getString(c.getColumnIndex(DBStrings.TBLSETS_NAME));
        	String setDate = c.getString(c.getColumnIndex(DBStrings.TBLSETS_DATE));
        	String[] datesplit = setDate.split("-");
        	setDate = datesplit[1] + "/" + datesplit[2] + "/" + datesplit[0];
    		
        	// Create a new set item
        	SetItem tmp = new SetItem(setName, setDate);
        	tmp.selfPopulateSongsList();
        	
        	// Add the set item
        	setsList.add(tmp);
        	
        	// Move to the next song
        	c.moveToNext();
    	}
    	c.close();
    	
    	// Sort the array list
    	switch(setsListSortByIndex) {
	    	case 0: // Date - Recent
	    		Collections.sort(setsList, new SetItemComparableDateReverse());
	    		break;
	    	case 1: // Date - Oldest
	    		Collections.sort(setsList, new SetItemComparableDate());
	    		break;
	    	case 2: // Title
	    		Collections.sort(setsList, new ItemComparableName());
	    		break;
    	}

        return ret;
    }

    /**
     * Fills the sets list
     * @param v The view for the list
     */
    public int fillSetsListView(SetSearchCriteria setSearch) {
        // Fill the sets array list
    	int ret = setSetsList(setSearch);
    	
    	// Set up the list view and adapter
        ListView lv = ((ListView)findViewById(R.id.sets_list));
        lv.setEmptyView(findViewById(R.id.empty_sets));
        setsAdapter = new ItemArrayAdapter(setsFragment.getActivity(), setsList);

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
        
        // Scroll to the previous scroll position
        lv.setSelectionFromTop(setsCurrentScrollPosition, setsCurrentScrollOffset);

        return ret;
    }

    /**
     * Fills the sets list view with no search results
     */
    public void fillSetsListView() {
        fillSetsListView(null);
    }
    
    /**
     * Edits the set name and date
     * @param setName The set to edit
     */
    private void editSetAtt(final String setName) {
    	// Remember the current scroll position
    	ListView lv = ((ListView)findViewById(R.id.sets_list));
    	setsCurrentScrollPosition = lv.getFirstVisiblePosition();
    	setsCurrentScrollOffset = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
    	
    	// Create the alert dialog
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Edit Set");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set, (ViewGroup) findViewById(R.id.add_set_root));
    	alert.setView(dialoglayout);
    	final EditText setNameET = (EditText)dialoglayout.findViewById(R.id.add_set_name);
    	final DatePicker setDateDP = (DatePicker)dialoglayout.findViewById(R.id.add_set_date);
    	
    	// Populate the set fields
    	setNameET.setText(setName);
    	String temp[] = dbAdapter.getSetDate(setName).split("-");
    	if (temp.length >= 2)
    		setDateDP.updateDate(Integer.parseInt(temp[0].trim()), Integer.parseInt(temp[1].trim()) - 1, Integer.parseInt(temp[2].trim()));

    	// Set the OK button
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Get the date and set name
                String newSetName = setNameET.getText().toString();
                String setDate = setDateDP.getYear() + "-" + String.format("%02d", (setDateDP.getMonth() + 1)) + "-" + String.format("%02d", setDateDP.getDayOfMonth());

                if (newSetName.length() > 0) {
                    dbAdapter.updateSetAttributes(setName, newSetName, setDate);

                    // Refresh set and current set list
                    fillSetsListView();
                    fillCurrentSetListView();
                } else
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
     * Emails the set
     * @param setItem The set to email
     * @param songFileType The type of sharing, (txt, pro, pdf)
     */
    private void emailSet(SetItem setItem, StaticVars.SongFileType songFileType) {
		String setDate = setItem.getDate();
		ArrayList<Uri> uris = new ArrayList<Uri>();
		
		// Start the output string
		StringBuilder sb = new StringBuilder();
		sb.append("<h2>" + setItem.getName() + "</h2>");
		sb.append("<i>" + setDate + "</i><br/><br/>");
		
		for (SongItem songItem : setItem.songs) {
			File att = null;
			
			// Add the attachment
			switch (songFileType) {
				case plainText:		
				case chordPro:
					// Create the attachment file
					att = saveSong(songItem, songFileType, songItem.getSetKey(), false);
					att.deleteOnExit();
					
					// Add the file as an attachment
					uris.add(Uri.fromFile(att));	
					
					break;				
				case PDF:
					// Save the song as a PDF
					att = saveSongAsPdf(songItem, songItem.getSetKey(), false);
					att.deleteOnExit();
					
					// Add the file as an attachment
					uris.add(Uri.fromFile(att));	
				default:
					break;
			}

            // Add song and key
			if (songItem.getSetKey() == "") 
				sb.append("<b>" + songItem.getName() + "</b> - " + songItem.getKey() + "<br/>");
			else
				sb.append("<b>" + songItem.getName() + "</b> - " + songItem.getSetKey() + "<br/>");

            // Add song link if it exists
            if (songItem.getSongLink() != null && !songItem.getSongLink().isEmpty()) {
                sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                sb.append("<a href=\"" + songItem.getSongLink() + "\">");
                sb.append(songItem.getSongLink());
                sb.append("</a><br/>");
            }

            // Add bpm and time signature if it exists
            if (songItem.getBpm() > 0) {
                sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                sb.append(songItem.getBpm() + " BPM <i>in</i> " + songItem.getTimeSignature() + "<br/>");
            }

            // Add a line break between songs
            sb.append("<br/>");
		}
		
		// Create the email intent
		Intent i = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
		i.setType("text/html");
		
		// Add the subject, body and attachments
		i.putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris);
		i.putExtra(android.content.Intent.EXTRA_SUBJECT, "SBGSoft Virtual SongBook - " + setItem.getName());
		i.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(sb.toString()));
		
		startActivity(Intent.createChooser(i, "Send song email via:"));  
    }
    
    /**
     * Saves the set
     * @param setItem The set to save
     * @param songFileType The type of sharing, (txt, pro, pdf)
     */
    private void saveSet(final SetItem setItem, final StaticVars.SongFileType songFileType) {
    	// For each song in the set
    	for (SongItem songItem : setItem.songs) {
			saveSong(songItem, songFileType, songItem.getSetKey(), false);
    	}
    	
    	Toast.makeText(getBaseContext(), "Saved set files to: " + Environment.getExternalStorageDirectory() + "!", Toast.LENGTH_LONG).show();
    }
     
    /**
     * Emails the set with the songs as attachments
     * @param setItem The set item object
     */
    private void shareSet(final SetItem setItem) {
    	// Create the options array
    	final CharSequence[] options;
    	
    	if (Build.VERSION.SDK_INT >= 19) {
	    	options = new CharSequence[] {getString(R.string.cmenu_sets_share_email), 
	    			getString(R.string.cmenu_sets_share_email_cp),
	    			getString(R.string.cmenu_sets_share_email_pdf),
	    			getString(R.string.cmenu_sets_share_save), 
	    			getString(R.string.cmenu_sets_share_save_cp),
	    			getString(R.string.cmenu_sets_share_save_pdf),
                    getString(R.string.cmenu_sets_share_export)};
    	} else {
    		options = new CharSequence[] {getString(R.string.cmenu_sets_share_email), 
	    			getString(R.string.cmenu_sets_share_email_cp),
	    			getString(R.string.cmenu_sets_share_save), 
	    			getString(R.string.cmenu_sets_share_save_cp),
                    getString(R.string.cmenu_sets_share_export)};
    	}
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Share How?");
    	alert.setItems(options, new OnClickListener() {
    		public void onClick (DialogInterface dialog, int whichItem) {	    		
	    		// Email, plain text
	    		if (options[whichItem] == getString(R.string.cmenu_sets_share_email)) {
	    			emailSet(setItem, SongFileType.plainText);
	    		}
	    		// Email, chordpro
	    		else if (options[whichItem] == getString(R.string.cmenu_sets_share_email_cp)) {
	    			emailSet(setItem, SongFileType.chordPro);
	    		}
	    		// Email, PDF
	    		else if (options[whichItem] == getString(R.string.cmenu_sets_share_email_pdf)) {
	    			emailSet(setItem, SongFileType.PDF);
	    		}
	    		// Save, plain text
	    		else if (options[whichItem] == getString(R.string.cmenu_sets_share_save)) {
	    			saveSet(setItem, SongFileType.plainText);
	    		}
	    		// Save, chordpro
	    		else if (options[whichItem] == getString(R.string.cmenu_sets_share_save_cp)) {
	    			saveSet(setItem, SongFileType.chordPro);
	    		}
	    		// Save, PDF
	    		else if (options[whichItem] == getString(R.string.cmenu_sets_share_save_pdf)) {
	    			saveSet(setItem, SongFileType.PDF);
	    		}
                // Export
                else if (options[whichItem] == getString(R.string.cmenu_sets_share_export)) {
                    setToExport = setItem;
                    permissionRequiredFunction(StaticVars.PERMISSIONS_SET_EXPORT);
                }
    		}
    	});
    	
    	alert.show();
    }

    /**
     * Calls the reorder activity for the specified set
     * @param setName The set to reorder
     */
    private void reorderSet(String setName) {
        // Get the set songs
        Cursor c = dbAdapter.getSetSongs(setName);
        String[] setSongs = new String[c.getCount()];
        c.moveToFirst();
        int songCounter = 0;

        // Loop through each song in the current set and add it to the array
        while(!c.isAfterLast()) {
            String song = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
            setSongs[songCounter++] = song;
            c.moveToNext();
        }
        c.close();

        // Edit the set
        Intent i = new Intent(getBaseContext(), DragNDropListActivity.class);
        i.putExtra(StaticVars.SET_SONGS_KEY, setSongs);
        i.putExtra(StaticVars.SET_NAME_KEY, setName);
        startActivityForResult(i, 1);
    }

    /**
     * Enables the user to find sets
     */
    private void findSetDialog() {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);

        // Set the dialog view to gather user input
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.search_dialog, (ViewGroup) findViewById(R.id.search_dialog_root));
        alert.setView(dialoglayout);
        final EditText setNameSearch = (EditText)dialoglayout.findViewById(R.id.search_dialog_text);

        // Add the dialog title
        alert.setTitle("Find Set");

        // Set the OK button
        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Make sure there is some text to search
                String searchText = setNameSearch.getText().toString();

                if (searchText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "You must enter text to search. Please try again.", Toast.LENGTH_LONG).show();
                } else {
                    // Create the song search object
                    SetSearchCriteria setSearch = new SetSearchCriteria();
                    setSearch.setNameSearchText = searchText;

                    // Fill the songs tab with the search data
                    int numResults = fillSetsListView(setSearch);
                    fillSetGroupsSpinner(true, numResults);

                    // Set the sets tab as the current
                    currentTab = 1;
                    mViewPager.setCurrentItem(currentTab);

                    // Close the dialog
                    dialog.dismiss();
                }
            }
        });

        alert.setNegativeButton("Cancel", null);
        alert.setCanceledOnTouchOutside(true);

        alert.show();
    }
    //endregion


    //region Song Functions
    // *****************************************************************************
    // * 
    // * Song Functions
    // * 
    // *****************************************************************************
    /**
     * Prompts the user for a name and creates the set
     */
    public void createSong() {
    	CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);
    	String[] pathSplit = importFilePath.split("/");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_song, (ViewGroup) findViewById(R.id.add_song_root));
    	alert.setView(dialoglayout);
    	final EditText songNameET = (EditText)dialoglayout.findViewById(R.id.add_song_name);
    	final EditText authorET = (EditText)dialoglayout.findViewById(R.id.add_song_author);
    	final EditText keyET = (EditText)dialoglayout.findViewById(R.id.add_song_key);
    	
    	// Add the dialog title
    	if (importFilePath != "") {
    		alert.setTitle("Add Song - " + pathSplit[pathSplit.length - 1]);
    		
    		// Populate the song name with the file name
        	songNameET.setText(pathSplit[pathSplit.length - 1].substring(0, pathSplit[pathSplit.length - 1].lastIndexOf(".")));
    	}
    	else
    		alert.setTitle("Add Song");

    	// Set the OK button
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Get the user inputs
                String songName = songNameET.getText().toString();
                String songAuthor = StaticVars.UNKNOWN;
                String songKey = "";
                if (authorET.getText().length() > 0)
                    songAuthor = authorET.getText().toString().trim();
                if (keyET.getText().length() > 1)
                    songKey = keyET.getText().toString().substring(0, 1).toUpperCase(Locale.US) + keyET.getText().toString().substring(1).trim();
                else if (keyET.getText().length() > 0)
                    songKey = keyET.getText().toString().toUpperCase(Locale.US).trim();

                // Check for a correct key
                if (!isValidKey(songKey)) {
                    Toast.makeText(getBaseContext(), "That is not a valid key!" +
                            StaticVars.EOL + "Please enter a valid key and try again.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Create the song
                if (songName.length() > 0) {
                    String songFile = songName + ".txt";
                    if (!dbAdapter.createSong(songName, songFile, songAuthor, songKey))
                        Toast.makeText(getApplicationContext(), "Failed to create song!", Toast.LENGTH_LONG).show();
                    else {
                        // If a file is waiting to be imported
                        if (importFilePath != "") {
                            // Copy the file into the tabapp songs directory
                            try {
                                if (importFilePath.substring(importFilePath.length() - 3).equals("txt")) {
                                    TextFileImporter.importTextFile(importFilePath, songFile, songAuthor, getApplicationContext());
                                } else {
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
                        } else {
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
                } else
                    Toast.makeText(getApplicationContext(), "Cannot create a song with no name!", Toast.LENGTH_LONG).show();

                // Close the dialog
                dialog.dismiss();
            }
        });

    	alert.setNegativeButton("Cancel", null);
    	alert.setCanceledOnTouchOutside(true);

    	alert.show();
    }
    
    /**
     * Adds the song to a group
     * @param songName The song to add
     */
    private void addSongToGroup(final String songName) {
    	// Remember the current scroll position
    	ListView lv = ((ListView)findViewById(R.id.songs_list));
    	songsCurrentScrollPosition = lv.getFirstVisiblePosition();
    	songsCurrentScrollOffset = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
    	
    	// Get the list of group names
    	Cursor c = dbAdapter.getSongGroupNames();
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount()];
    	final boolean[] checkedGroupNames = new boolean[c.getCount()];
    	int counter = 0;
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		String groupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME));
    		if (groupName.equals(SongsTab.ALL_SONGS_LABEL))
    			groupName = "No Group";
    		checkedGroupNames[counter] = false;
    		groupNames[counter++] = groupName;
    	}
    	
    	// Close the cursor
    	c.close();
    	
    	// Create the dialog to choose which group to add the song to
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Add Song to Group");
    	alert.setMultiChoiceItems(groupNames, checkedGroupNames, new DialogInterface.OnMultiChoiceClickListener() {

            public void onClick(DialogInterface dialog, int which, boolean checked) {
                checkedGroupNames[which] = checked;
            }
        });
    	
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add the song to the selected groups
                for (int i = 0; i < groupNames.length; i++) {
                    if (!groupNames[i].equals("No Group") && checkedGroupNames[i])
                        dbAdapter.addSongToGroup(songName, groupNames[i].toString());
                }

                // Refresh song list
                fillSongGroupsSpinner();
            }
        });

    	alert.show();
    }
    
    /**
     * Adds the song to a set
     * @param songName The song to add
     */
    private void addSongToSet(final String songName) {
    	// Remember the current scroll position
    	ListView lv = ((ListView)findViewById(R.id.songs_list));
    	songsCurrentScrollPosition = lv.getFirstVisiblePosition();
    	songsCurrentScrollOffset = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
    	
    	// Get the list of group names
    	Cursor c = dbAdapter.getSets(SetsTab.ALL_SETS_LABEL);
    	
    	// Clear the previous song lists
    	addSetsDialogList.clear();
    	addSetsDialogMap.clear();
    	
    	// Populate the songs lists
    	while(c.moveToNext()) {
    		String setName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_NAME));
    		addSetsDialogList.add(setName);
    		addSetsDialogMap.put(setName, dbAdapter.isSongInSet(songName, setName));
    	}
    	c.close();
    	Collections.sort(addSongsDialogList, new SortIgnoreCase());
    	
    	// Create the dialog to choose which group to add the song to
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Add Song to Which Set?");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set_songs, (ViewGroup) findViewById(R.id.add_set_songs_root));
    	alert.setView(dialoglayout);
    	
    	// Get the views
    	final Spinner setGroupSP = (Spinner)dialoglayout.findViewById(R.id.add_set_songs_spinner);
    	final ListView setsLV = (ListView)dialoglayout.findViewById(R.id.add_set_songs_list);
    	final ArrayAdapter<String> setsAD;
    	
    	// Fill the list view
    	setsLV.setEmptyView(findViewById(R.id.empty_sets));
    	setsLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	setsLV.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	String set = addSetsDialogList.get(position);
            	addSetsDialogMap.put(set, !addSetsDialogMap.get(set));
            }
        });
    	setsAD = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, addSetsDialogList);
    	setsLV.setAdapter(setsAD);
    	
    	// Fill the group spinner
    	setSetGroupsList();
    	setGroupSP.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected group
            	String groupName = (String)setGroupSP.getSelectedItem();
            	
            	// Fill the new songs list
            	Cursor c = dbAdapter.getSets(groupName);
            	addSetsDialogList.clear();
            	
            	// Populate the ArrayList
            	while (c.moveToNext()) {
            		// Get the strings from the cursor
                	String setName = c.getString(c.getColumnIndex(DBStrings.TBLSETS_NAME));
                	addSetsDialogList.add(setName);
            	}
            	c.close();
            	Collections.sort(addSetsDialogList, new SortIgnoreCase());
            	
            	// Update list view
            	setsAD.notifyDataSetChanged();
            	
            	// Set the list view checked properties
            	for(int i = 0; i < setsLV.getCount(); i++) {
            		setsLV.setItemChecked(i, addSetsDialogMap.get(setsLV.getItemAtPosition(i)));
            	}
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	setGroupSP.setAdapter(setGroupsAdapter);
    	
    	// Set positive button of the dialog
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		
				// Set all selected items to the set	    	
	    		for(String s : addSetsDialogMap.keySet()) {
	    			if(addSetsDialogMap.get(s))
	    				dbAdapter.addSongToSet(s, songName);
	    		}
	    		
	    		// Refresh the current set list
	    		fillCurrentSetListView();
			}
    	});

    	// Set negative button of the dialog
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {	}
    	});

    	// Show the dialog
    	AlertDialog a = alert.create();
    	a.show();
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics); 
    	int height = metrics.heightPixels;
    	height = (int) (height / 1.5);
    	a.getWindow().setLayout(LayoutParams.WRAP_CONTENT, height);
    }
    
    /**
     * Adds the song to a set
     * @param songName The song to add
     */
    private void addSongToCurrentSet(final String songName) {
    	// Remember the current scroll position
    	ListView lv = ((ListView)findViewById(R.id.songs_list));
    	songsCurrentScrollPosition = lv.getFirstVisiblePosition();
    	songsCurrentScrollOffset = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
    	
    	// Get the current set
    	final String currentSet = dbAdapter.getCurrentSetName();
   
		AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
		alert.setTitle("Add Song to Set?");
		alert.setMessage("Do you want to add '" + songName + "' to '" + currentSet + "'?");
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		dbAdapter.addSongToSet(currentSet, songName);
				
	    		// Refresh the current set list
	    		fillCurrentSetListView();
			}
    	});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) { 	}
    	});

		alert.show();
    }
    
    /**
     * Removes the song from the specified group
     * @param songName The song to remove
     * @param groupName The group to remove the song from
     */
    private void removeSongFromGroup(final String songName, final String groupName) {
    	// Remember the current scroll position
    	ListView lv = ((ListView)findViewById(R.id.songs_list));
    	songsCurrentScrollPosition = lv.getFirstVisiblePosition();
    	songsCurrentScrollOffset = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Remove Song From Group?!");
    	alert.setMessage("Are you sure you want to remove '" + songName + "' from '" + groupName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Remove song from the group
	    		dbAdapter.removeSongFromGroup(songName, groupName);
	    		
	    		// Refresh the song list
	    		//songsAdapter.notifyDataSetChanged();
	    		fillSongGroupsSpinner();
	    		fillSongsListView();
	        	
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
    	alert.setMessage("Are you sure you want to delete ALL songs???" + StaticVars.EOL +
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
	    		fillSongsListView();
	    		fillSongGroupsSpinner();
	    		fillCurrentSetListView();
	    		fillSetGroupsSpinner();
	    		fillSetsListView();
	        	
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
    	// Remember the current scroll position
    	ListView lv = ((ListView)findViewById(R.id.songs_list));
    	songsCurrentScrollPosition = lv.getFirstVisiblePosition();
    	songsCurrentScrollOffset = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
    	
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
                fillSongGroupsSpinner();
                fillCurrentSetListView();

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
     * @param songSearch The search criteria
     * @return the number of items in the song list
     */
    public int setSongsList(SongSearchCriteria songSearch) {
        int ret;

    	ArrayList<Item> temp = new ArrayList<Item>();
        Cursor c;

        // Determine if we are searching or using the song group
        if (songSearch == null)
    	    c = dbAdapter.getSongs(currentSongGroup);
        else
            c = dbAdapter.getSongsSearch(songSearch);

        // Move to the first and get the count
        c.moveToFirst();
        ret = c.getCount();

        // Display error message for searching
        if (c.getCount() <= 0 && songSearch != null)
            Toast.makeText(getApplicationContext(), "No songs match that search", Toast.LENGTH_LONG).show();
    	
    	// Populate the ArrayList
    	while (!c.isAfterLast()) {
            // Create the song item
            SongItem songItem = new SongItem();

    		// Set the song item values
        	songItem.setName(c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME)));
            songItem.setAuthor(c.getString(c.getColumnIndex(DBStrings.TBLSONG_AUTHOR)));
            songItem.setKey(c.getString(c.getColumnIndex(DBStrings.TBLSONG_KEY)));
            songItem.setFile(c.getString(c.getColumnIndex(DBStrings.TBLSONG_FILE)));
            songItem.setBpm(c.getInt(c.getColumnIndex(DBStrings.TBLSONG_BPM)));
            songItem.setTimeSignature(c.getString(c.getColumnIndex(DBStrings.TBLSONG_TIME)));
            songItem.setSongLink(c.getString(c.getColumnIndex(DBStrings.TBLSONG_LINK)));

        	// Add the song item
        	temp.add(songItem);
        	
        	// Move to the next song
        	c.moveToNext();
    	}
    	
    	c.close();
    	
    	// Sort the array list
    	Collections.sort(temp, new ItemComparableName());
    	
    	// Clear the current ArrayList
    	songsList.clear();
    	
    	// Add section headers
    	for (int i = 0; i < temp.size(); i++) {
    		if (i != 0) {
    			if (Character.toLowerCase(temp.get(i).getName().charAt(0)) != 
    					Character.toLowerCase(temp.get(i-1).getName().charAt(0))) {
    				// This is the first item with that letter, add the separator
    				songsList.add(new SectionItem(temp.get(i).getName().substring(0, 1).toUpperCase(Locale.US)));
    			}
    		}
    		else {
    			// First item, add section
    			songsList.add(new SectionItem(temp.get(i).getName().substring(0, 1).toUpperCase(Locale.US)));
    		}
    		
    		songsList.add(temp.get(i));
    	}

        return ret;
    }
    
    /**
     * Fills the songs list
     */
    public int fillSongsListView(SongSearchCriteria songSearch) {
        int ret;

    	// Fill the songs array list
    	ret = setSongsList(songSearch);

    	// Set up the list view and adapter
    	ListView lv = ((ListView)findViewById(R.id.songs_list));
        lv.setEmptyView(findViewById(R.id.empty_songs));
        songsAdapter = new ItemArrayAdapter(songsFragment.getActivity(), songsList);

        // Set the on click listener for each item
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
                // Get the song to show
                SongItem song = (SongItem) songsList.get(position);
                try {
                    FileInputStream fis = openFileInput(dbAdapter.getSongFile(song.getName()));
                    song.setText(ChordProParser.ParseSongFile(getApplicationContext(), song, song.getKey(), fis, true, false));

                    // Show the song activity
                    SongActivity songA = new SongActivity();
                    Intent showSong = new Intent(v.getContext(), songA.getClass());
                    showSong.putExtra(StaticVars.SONG_ITEM_KEY, (Parcelable) song);
                    startActivity(showSong);

                } catch (FileNotFoundException e) {
                    Toast.makeText(getBaseContext(), "Could not open song file!", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), "Could not open song file!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Register the context menu and add the adapter
        registerForContextMenu(lv);
        lv.setAdapter(songsAdapter);

        // Scroll to the previous scroll position
        lv.setSelectionFromTop(songsCurrentScrollPosition, songsCurrentScrollOffset);

        return ret;
    }

    /**
     * Fills the songs list with no search parameters
     */
    public void fillSongsListView() {
        fillSongsListView(null);
    }
        
    /**
     * Imports a song text file into the db
     */
    private void importSong() {
        // Create the open file intent
        Intent intent = new Intent(getBaseContext(), OpenFile.class);
        intent.putExtra(StaticVars.FILE_ACTIVITY_KEY, StaticVars.IMPORT_SONG_ACTIVITY);
        intent.putExtra(StaticVars.FILE_ACTIVITY_TYPE_KEY, StaticVars.FILE_ACTIVITY_FILE);
        
        // Start the activity
        startActivityForResult(intent, 1);
    }
    
    /**
     * Edits the song name, author and key
     * @param songName The song to edit
     */
    private void editSongAtt(final String songName) {
    	// Remember the current scroll position
    	ListView lv = ((ListView)findViewById(R.id.songs_list));
    	songsCurrentScrollPosition = lv.getFirstVisiblePosition();
    	songsCurrentScrollOffset = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
    	
    	CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);
    	alert.setTitle("Add Song");

    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_song, (ViewGroup) findViewById(R.id.add_song_root));
    	alert.setView(dialoglayout);
    	final EditText songNameET = (EditText)dialoglayout.findViewById(R.id.add_song_name);
    	final EditText authorET = (EditText)dialoglayout.findViewById(R.id.add_song_author);
    	final EditText keyET = (EditText)dialoglayout.findViewById(R.id.add_song_key);
        final EditText linkET = (EditText)dialoglayout.findViewById(R.id.add_song_link);
        final EditText bpmET = (EditText)dialoglayout.findViewById(R.id.add_song_bpm);
        final Spinner timeSpin = (Spinner)dialoglayout.findViewById(R.id.add_song_time);
    	
    	// Populate the text boxes
    	songNameET.setText(songName);
    	authorET.setText(dbAdapter.getSongAuthor(songName));
    	keyET.setText(dbAdapter.getSongKey(songName));
        linkET.setText(dbAdapter.getSongLink(songName));

        // Get the beats per minute and populate
        int bpm = dbAdapter.getSongBpm(songName);
        if (bpm > 0)
            bpmET.setText(Integer.toString(bpm));

        // Get the time signature and select the spinner
        TimeSignature ts = dbAdapter.getSongTimeSignature(songName);
        if (ts.noteOneBeat > 0 && ts.beatsPerBar > 0) {
            String[] timeSigs = getResources().getStringArray(R.array.time_signatures);
            int loc = Arrays.asList(timeSigs).indexOf(ts.toString());
            if (loc >= 0 && loc < timeSpin.getCount())
                timeSpin.setSelection(loc);
        } else {
            timeSpin.setSelection(3);
        }
    	
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String key = keyET.getText().toString();

                // Upper case the key
                if (key.length() > 1)
                    key = key.substring(0, 1).toUpperCase(Locale.US) + key.substring(1).trim();
                else if (key.length() > 0)
                    key = key.toUpperCase(Locale.US).trim();

                // Check for a correct key
                if (!isValidKey(key)) {
                    Toast.makeText(getBaseContext(), "That is not a valid key!" +
                            StaticVars.EOL + "Please enter a valid key and try again.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Check for bpm populated
                int bpm = 0;
                try {
                    bpm = Integer.parseInt(bpmET.getText().toString());
                } catch (NumberFormatException nfe) {
                }

                // Update the song in the database
                dbAdapter.updateSongAttributes(songName, songNameET.getText().toString(),
                        authorET.getText().toString(), key, String.valueOf(timeSpin.getSelectedItem()),
                        linkET.getText().toString(), bpm);

                // Refresh lists
                fillSongsListView();
                fillSetsListView();
                fillCurrentSetListView();

                // Close the dialog
                dialog.dismiss();
            }
        });
    	
    	alert.setNegativeButton("Cancel", null);
    	alert.setCanceledOnTouchOutside(true);
    	
    	alert.show();
    }

    /**
     * Emails the song
     * @param songName The song to email
     */
    private void emailSong(SongItem songItem, StaticVars.SongFileType songFileType, String newSongKey) {
		// Create the email intent
    	Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/Message");
		
		File att;
		
		// Add the attachment
		switch (songFileType) {
			case plainText:		
			case chordPro:
				// Create the attachment file
				att = saveSong(songItem, songFileType, newSongKey, false);
				att.deleteOnExit();
				
				// Add the file as an attachment
				i.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(att));	
				
				break;				
			case PDF:
				// Save the song as a PDF
				att = saveSongAsPdf(songItem, newSongKey, false);
				att.deleteOnExit();
				
				// Add the file as an attachment
				i.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(att));
			default:
				break;
		}
		
		// Set the songkey for the email
		String tmpkey;
		if (newSongKey == "")
			tmpkey = songItem.getKey();
		else
			tmpkey = newSongKey;

        // Build the email string
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>SBGSoft Virtual SongBook</h2>");
        sb.append("<b>Song Name:</b>&nbsp;&nbsp;" + songItem.getName() + "<br/>");
        sb.append("<b>Song Key:</b>&nbsp;&nbsp;" + tmpkey + "<br/>");

        if ((songItem.getSongLink() != null && !songItem.getSongLink().isEmpty() || songItem.getBpm() > 0))
            sb.append("<b>Song Details:</b><br/>");

        if (songItem.getSongLink() != null && !songItem.getSongLink().isEmpty()) {
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append("<a href=\"" + songItem.getSongLink() + "\">");
            sb.append(songItem.getSongLink());
            sb.append("</a><br/>");
        }

        if (songItem.getBpm() > 0) {
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append(songItem.getBpm() + " BPM <i>in</i> " + songItem.getTimeSignature() + "<br/>");
        }

        sb.append("<br/>");
        sb.append("The music for this song has been attached to this email as a file.");
        sb.append("<br/>");
		
		// Add the subject and body
		i.putExtra(android.content.Intent.EXTRA_SUBJECT, "SBGSoft Virtual SongBook - " + songItem.getName());
		//i.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml("<h2>" + songName + "</h2>" + getSongText(songI.getSongFile())));
		i.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(sb.toString()));

		startActivity(Intent.createChooser(i, "Send Song Email Via:"));
    }
    
    /**
     * Saves the song
     * @param songName The song to save
     */
    private File saveSong(final SongItem songItem, final StaticVars.SongFileType songFileType, String newSongKey, boolean showToast) {
		// Craft the file name
		String fileName = songItem.getName() + " - " + songItem.getAuthor();
		if (newSongKey == "" || songFileType == SongFileType.chordPro)
			fileName += " (" + songItem.getKey() + ")";
		else
			fileName += " (" + newSongKey + ")";
		
		File att = null;
		
		// Save the file
		switch (songFileType) {
			case plainText:
				// Add the file extension
				fileName += ".txt";
				
				try {
					// Open the file and translate it
    				FileInputStream fis = openFileInput(songItem.getFile());
    				String temp = ChordProParser.ParseSongFile(getApplicationContext(), songItem, newSongKey, fis, false, true);
    				
    				// Write the file
    				att = new File(Environment.getExternalStorageDirectory(), fileName);
    				FileOutputStream out = new FileOutputStream(att);
    		    	out.write(temp.getBytes());
    				
    		    	// Close the files
    		    	fis.close();
    		    	out.close();
    				
    			} catch (Exception e) {
    				if (showToast)
    					Toast.makeText(getBaseContext(), "Unable to save text file!", Toast.LENGTH_LONG).show();
    			}
				
				if (showToast)
					Toast.makeText(getBaseContext(), "Text file saved to: " + Environment.getExternalStorageDirectory() + "/" + fileName + "!", Toast.LENGTH_LONG).show();
				
				break;
			case chordPro:
				// Add the file extension
				fileName += ".pro";
				
				try {	        					
					// Open the input file
    				FileInputStream fis = openFileInput(songItem.getFile());
    				
    				// Open the output file
    				att = new File(Environment.getExternalStorageDirectory(), fileName);
    				FileOutputStream out = new FileOutputStream(att);
    		    	
    				// Copy the file
    				byte[] buffer = new byte[1024];
    				int read;
    				while ((read = fis.read(buffer)) != -1) {
    					out.write(buffer, 0, read);
    				}
    				
    				// Close the files
    		    	fis.close();
    		    	out.close();
    				
    			} catch (Exception e) {
    				if (showToast)
    					Toast.makeText(getBaseContext(), "Unable to save ChordPro file!", Toast.LENGTH_LONG).show();
    			}
				
				if (showToast)
					Toast.makeText(getBaseContext(), "Text file saved to: " + Environment.getExternalStorageDirectory() + "/" + fileName + "!", Toast.LENGTH_LONG).show();
				
				break;
			case PDF:
				// Add the file extension
				fileName += ".pdf";
				
				// Save the songs as a PDF
				att = saveSongAsPdf(songItem, newSongKey, showToast);
				
			default:
				break;
		}
		
		return att;
    }
        
    /**
     * Shares the song via email or saving it
     * @param songItem The SongItem
     * @param songName The song name
     */
    public void shareSong(final SongItem songItem) {    	
    	// Create the options array
    	final CharSequence[] options;
    	
    	if (Build.VERSION.SDK_INT >= 19) {
	    	options = new CharSequence[] {getString(R.string.cmenu_songs_share_email), 
	    			getString(R.string.cmenu_songs_share_email_cp),
	    			getString(R.string.cmenu_songs_share_email_pdf),
	    			getString(R.string.cmenu_songs_share_save), 
	    			getString(R.string.cmenu_songs_share_save_cp),
	    			getString(R.string.cmenu_songs_share_save_pdf)};
    	} else {
    		options = new CharSequence[] {getString(R.string.cmenu_songs_share_email), 
	    			getString(R.string.cmenu_songs_share_email_cp),
	    			getString(R.string.cmenu_songs_share_save), 
	    			getString(R.string.cmenu_songs_share_save_cp)};
    	}
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Share How?");
    	alert.setItems(options, new OnClickListener() {
    		public void onClick (DialogInterface dialog, int whichItem) {
    			// Dismiss the current dialog
    			dialog.dismiss();
    			
    			// Create the key array
	    		CharSequence[] keys = StaticVars.songKeys.toArray(new CharSequence[StaticVars.songKeys.size() + 1]);
	    		keys[StaticVars.songKeys.size()] = "Original Key";
	    	
	    		AlertDialog.Builder keysAlert;
	    		
	    		// Email, plain text
	    		if (options[whichItem] == getString(R.string.cmenu_songs_share_email)) {
	    			// Check for a special key
    		    	if (StaticVars.songKeyMap.containsKey(songItem.getKey())) {
    		    		// Set the song key to the associated key
    		    		songItem.setKey(StaticVars.songKeyMap.get(songItem.getKey()));
    		    	}
    		    	
    	    		keysAlert = new AlertDialog.Builder(MainActivity.this);

    	    		keysAlert.setTitle("Email Song in Which Key?");
    	    		keysAlert.setItems(keys, new OnClickListener() {
    	        		public void onClick (DialogInterface dialog, int whichItem) {
    	        			// Set the new song key
    	        			String newSongKey = "";
    	        			if (whichItem < StaticVars.songKeys.size()) {
    	        				newSongKey = StaticVars.songKeys.get(whichItem);
    	        			}
    	        			
    	        			// Check to make sure the song has a proper key
    	        	    	if (StaticVars.songKeys.contains(songItem.getKey()))
    	        	    		emailSong(songItem, StaticVars.SongFileType.plainText, newSongKey);
    	        		}
    	        	});
    	        	
    	    		keysAlert.show();
	    		}
	    		// Email, chordpro
	    		else if (options[whichItem] == getString(R.string.cmenu_songs_share_email_cp)) {
	    			emailSong(songItem, StaticVars.SongFileType.chordPro, "");
	    		}
	    		// Email, PDF
	    		else if (options[whichItem] == getString(R.string.cmenu_songs_share_email_pdf)) {
	    			// Check for a special key
    		    	if (StaticVars.songKeyMap.containsKey(songItem.getKey())) {
    		    		// Set the song key to the associated key
    		    		songItem.setKey(StaticVars.songKeyMap.get(songItem.getKey()));
    		    	}
    		    	
    	    		keysAlert = new AlertDialog.Builder(MainActivity.this);

    	    		keysAlert.setTitle("Email Song in Which Key?");
    	    		keysAlert.setItems(keys, new OnClickListener() {
    	        		public void onClick (DialogInterface dialog, int whichItem) {
    	        			// Set the new song key
    	        			String newSongKey = "";
    	        			if (whichItem < StaticVars.songKeys.size()) {
    	        				newSongKey = StaticVars.songKeys.get(whichItem);
    	        			}
    	        			
    	        			// Check to make sure the song has a proper key
    	        	    	if (StaticVars.songKeys.contains(songItem.getKey()))
    	        	    		emailSong(songItem, StaticVars.SongFileType.PDF, newSongKey);
    	        		}
    	        	});
    	        	
    	    		keysAlert.show();
	    		}
	    		// Save, plain text
	    		else if (options[whichItem] == getString(R.string.cmenu_songs_share_save)) {
	    			// Check for a special key
    		    	if (StaticVars.songKeyMap.containsKey(songItem.getKey())) {
    		    		// Set the song key to the associated key
    		    		songItem.setKey(StaticVars.songKeyMap.get(songItem.getKey()));
    		    	}
    	    		
    		    	keysAlert = new AlertDialog.Builder(MainActivity.this);

    		    	keysAlert.setTitle("Save Song in Which Key?");
    		    	keysAlert.setItems(keys, new OnClickListener() {
    	        		public void onClick (DialogInterface dialog, int whichItem) {
    	        			// Set the new song key
    	        			String newSongKey = "";
    	        			if (whichItem < StaticVars.songKeys.size()) {
    	        				newSongKey = StaticVars.songKeys.get(whichItem);
    	        			}
    	        			
    	        			// Check to make sure the song has a proper key
    	        	    	if (StaticVars.songKeys.contains(songItem.getKey()))
    	        	    		saveSong(songItem, StaticVars.SongFileType.plainText, newSongKey, true);
    	        		}
    	        	});
    	        	
    		    	keysAlert.show();
	    		}
	    		// Save, chordpro
	    		else if (options[whichItem] == getString(R.string.cmenu_songs_share_save_cp)) {
	    			saveSong(songItem, StaticVars.SongFileType.chordPro, "", true);
	    		}
	    		// Save, PDF
	    		else if (options[whichItem] == getString(R.string.cmenu_songs_share_save_pdf)) {
	    			// Check for a special key
    		    	if (StaticVars.songKeyMap.containsKey(songItem.getKey())) {
    		    		// Set the song key to the associated key
    		    		songItem.setKey(StaticVars.songKeyMap.get(songItem.getKey()));
    		    	}
    	    		
    		    	keysAlert = new AlertDialog.Builder(MainActivity.this);

    		    	keysAlert.setTitle("Save Song in Which Key?");
    		    	keysAlert.setItems(keys, new OnClickListener() {
    	        		public void onClick (DialogInterface dialog, int whichItem) {
    	        			// Set the new song key
    	        			String newSongKey = "";
    	        			if (whichItem < StaticVars.songKeys.size()) {
    	        				newSongKey = StaticVars.songKeys.get(whichItem);
    	        			}
    	        			
    	        			// Check to make sure the song has a proper key
    	        	    	if (StaticVars.songKeys.contains(songItem.getKey()))
    	        	    		saveSong(songItem, StaticVars.SongFileType.PDF, newSongKey, true);
    	        		}
    	        	});
    	        	
    		    	keysAlert.show();
	    		}
    		}
    	});
    	
    	alert.show();
    }
    
    /**
     * Saves the song as a PDF file
     * @param songItem The song to save
     * @return The created file
     */
    @TargetApi(19)
    public File saveSongAsPdf(SongItem songItem, String songKey, boolean showToast) {
    	int pageWidth = 450;
    	int pageHeight = 700;
    	int padding = 30;
    	final float densityMultiplier = getResources().getDisplayMetrics().density;
    	float defaultTextSize = 6.0f;
    	File att = null;
    	
    	// Craft the file name
		String fileName = songItem.getName() + " - " + songItem.getAuthor();
		if (songKey == "")
			fileName += " (" + songItem.getKey() + ").pdf";
		else
			fileName += " (" + songKey + ").pdf";
    	
    	// Create a new PDF document
    	PdfDocument document = new PdfDocument();
    	
    	try {
	    	// Create a page description
	    	PageInfo pageInfo = new PageInfo.Builder(pageWidth, pageHeight, 1).create();
	    	
	    	// Create a new page from the page info
	    	Page page = document.startPage(pageInfo);
	    	
	    	// Create the text view to add to the page
	    	AutoFitTextView tv = new AutoFitTextView(MainActivity.this);
	    	tv.setTypeface(Typeface.MONOSPACE);
	    	tv.setTextColor(Color.BLACK);
	    	tv.setPadding(padding, padding, padding, padding);
	    	tv.layout(0, 0, pageWidth, pageHeight);	 
	    	tv.setTextSize(defaultTextSize);
	    	tv.setTextDecrement(0.25f);
	    	tv.setMinimumTextSizePixels(2.0f * densityMultiplier);
	    	
	    	// Get the fitted text size
	    	FileInputStream fis = openFileInput(dbAdapter.getSongFile(songItem.getName()));
	    	String songText = ChordProParser.ParseSongFile(getApplicationContext(), songItem, songKey, fis, true, false);
	    	
	    	// Add the song text to the text view
            ChordDisplay disp = new ChordDisplay(this);
            tv.setText(disp.setChordClickableText(songText), TextView.BufferType.SPANNABLE);
	    	
	    	// Force the text to shrink
	    	tv.shrinkToFit();
	    	
	    	// Add the song to the page
	    	tv.draw(page.getCanvas());
	    	
	    	// Finish the page
	    	document.finishPage(page);
    	
	    	// Write the document
	    	att = new File(Environment.getExternalStorageDirectory(), fileName);
			FileOutputStream out = new FileOutputStream(att);
	    	document.writeTo(out);
    	} catch (Exception e) {
    		if (showToast)
    			Toast.makeText(getApplicationContext(), 
        			"Failed to save \"" + songItem.getName() + "\" to \"" + Environment.getExternalStorageDirectory() + "/" + fileName,
        			Toast.LENGTH_LONG).show();
    	}
    	
    	// Close the document
    	document.close();
    	
    	// Alert on success
    	if (showToast)
    		Toast.makeText(getApplicationContext(), 
    			"Saved \"" + songItem.getName() + "\" to \"" + Environment.getExternalStorageDirectory() + "/" + fileName,
    			Toast.LENGTH_LONG).show();
    	
    	return att;
    }
    
    /**
     * Sets the song key for the set
     */
    private void setSongKeyForSet(final String setName, final SongItem songI) {
    	// Create the key array
		CharSequence[] keys = StaticVars.songKeys.toArray(new CharSequence[StaticVars.songKeys.size() + 1]);
		keys[StaticVars.songKeys.size()] = "Original Key";
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Which Key?");
    	alert.setItems(keys, new OnClickListener() {
    		public void onClick (DialogInterface dialog, int whichItem) {
    			// Set the new song key for the set
    			if (whichItem < StaticVars.songKeys.size())
    				dbAdapter.setSongKeyForSet(setName, songI.getName(), StaticVars.songKeys.get(whichItem));
    			
    			// Refresh current set list
    			fillCurrentSetListView();

                // Refresh sets list
                fillSetsListView();
    		}
    	});
    	
    	alert.show();
    }
    
    /**
     * Shows the song statistics dialog
     * @param songName The song to give stats for
     */
    private void showSongStats(String songName) {
    	// Create the dialog
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("'" + songName + "' Statistics");
    	
    	// Build the message
    	StringBuilder message = new StringBuilder();
    	
    	// Show last 5 uses
    	message.append("Last 5 Uses: ");
    	message.append(StaticVars.EOL);
    	
    	Cursor c = dbAdapter.getSongLastFive(songName);
    	c.moveToFirst(); 
    	while(!c.isAfterLast()) {
    		// Get the set name and date
    		String setName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_NAME));
    		String setDate = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_DATE));
    		String[] datesplit = setDate.split("-");
    		setDate = datesplit[1] + "/" + datesplit[2] + "/" + datesplit[0];
    		
    		message.append("\t" + setName + ", " + setDate);
        	message.append(StaticVars.EOL);
        	c.moveToNext();
    	}
    	c.close();
    	message.append(StaticVars.EOL);
    	
    	// Show total usage
    	message.append("Total Usage in Sets: ");
    	float percent = dbAdapter.getSongTotalUsage(songName);
    	message.append(String.format("%.2f", percent) + "%");
    	message.append(StaticVars.EOL);
    	message.append(StaticVars.EOL);
    	
    	// Show group membership
    	message.append("Member of Song Groups: ");
    	
    	c = dbAdapter.getSongGroups(songName);
    	if(c.getCount() > 0) {
	    	c.moveToFirst();
	    	while(!c.isAfterLast()) {
	    		String songGroupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME));
	    		message.append(StaticVars.EOL);
	    		message.append("\t" + songGroupName);
	        	c.moveToNext();
	    	}
	    	c.close();
    	} else {
    		message.append(StaticVars.EOL);
    		message.append("\tNo Groups");
    	}
    	
    	// Display information
    	alert.setMessage(message);
    	
    	// Add an OK button
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});

    	alert.show();
	}

    /**
     * Determines if the specified key is valid
     * @param songKey The key
     * @return True if valid, false if invalid
     */
    private boolean isValidKey(String songKey) {
        boolean ret = true;

        // Check for a correct key
        if (songKey.isEmpty() || (!StaticVars.songKeyMap.containsKey(songKey) && !StaticVars.songKeys.contains(songKey))) {
            ret = false;
        }

        return ret;
    }

    /**
     * Enables the user to find songs
     */
    private void findSongDialog() {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);

        // Set the dialog view to gather user input
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.search_dialog, (ViewGroup) findViewById(R.id.search_dialog_root));
        alert.setView(dialoglayout);
        final EditText songNameSearch = (EditText)dialoglayout.findViewById(R.id.search_dialog_text);

        // Add the dialog title
        alert.setTitle("Find Song");

        // Set the OK button
        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Make sure there is some text to search
                String searchText = songNameSearch.getText().toString();

                if (searchText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "You must enter text to search. Please try again.", Toast.LENGTH_LONG).show();
                } else {
                    // Create the song search object
                    SongSearchCriteria songSearch = new SongSearchCriteria();
                    songSearch.songNameSearchText = searchText;

                    // Fill the songs tab with the search data
                    int numResults = fillSongsListView(songSearch);
                    fillSongGroupsSpinner(true, numResults);

                    // Set the songs tab as the current
                    currentTab = 3;
                    mViewPager.setCurrentItem(currentTab);

                    // Close the dialog
                    dialog.dismiss();
                }
            }
        });

        alert.setNegativeButton("Cancel", null);
        alert.setCanceledOnTouchOutside(true);

        alert.show();
    }
    //endregion


    //region Current Set Functions
    // *****************************************************************************
    // * 
    // * Current Set Functions
    // * 
    // *****************************************************************************
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
            // Create the song item
            SongItem songItem = new SongItem();

            // Set the song item values
            songItem.setName(c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME)));
            songItem.setAuthor(c.getString(c.getColumnIndex(DBStrings.TBLSONG_AUTHOR)));
            songItem.setSetKey(c.getString(c.getColumnIndex(DBStrings.TBLSLOOKUP_KEY)));
            songItem.setFile(c.getString(c.getColumnIndex(DBStrings.TBLSONG_FILE)));
            songItem.setBpm(c.getInt(c.getColumnIndex(DBStrings.TBLSONG_BPM)));
            songItem.setTimeSignature(c.getString(c.getColumnIndex(DBStrings.TBLSONG_TIME)));
            songItem.setKey(dbAdapter.getSongKey(c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME))));
            songItem.setSongLink(c.getString(c.getColumnIndex(DBStrings.TBLSONG_LINK)));

            // Add the song item
            currSetList.add(songItem);
        	
        	// Move to the next song
        	c.moveToNext();
    	}
    	c.close();
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
            	// Create a new SetItem to pass
            	SetItem setItem = new SetItem();
            	
            	// Loop through each song in the current set and add it to the array
            	for (Item i : currSetList) {
            		SongItem currSong = (SongItem)i;
            		
            		// Set song text
            		try {
	            		FileInputStream fis = openFileInput(dbAdapter.getSongFile(currSong.getName()));
	            		currSong.setKey(dbAdapter.getSongKey(currSong.getName()));
	            		currSong.setText(ChordProParser.ParseSongFile(getApplicationContext(), currSong, dbAdapter.getSongKeyForSet(dbAdapter.getCurrentSetName(), currSong.getName()), fis, true, false));
	            		
	            		setItem.songs.add(currSong);      
            		} catch (FileNotFoundException e) {
            			Toast.makeText(getBaseContext(), "Could not open one of the song files!", Toast.LENGTH_LONG).show();
    					return;
            		} catch (IOException e) {
    					Toast.makeText(getBaseContext(), "Could not open song file!", Toast.LENGTH_LONG).show();
    				}
            	}
            	
            	// Show the set activity
            	SetActivity set = new SetActivity();
            	Intent showSet = new Intent(v.getContext(), set.getClass());
            	showSet.putExtra(StaticVars.CURRENT_SONG_KEY, position);
            	showSet.putExtra(StaticVars.SET_SONGS_KEY, setItem);
                startActivity(showSet);
            }
    	});
      
        // Register the context menu and add the adapter
        registerForContextMenu(lv);
        lv.setAdapter(currSetAdapter);
        
        // Append the current set name to the title
        TextView title = ((TextView)findViewById(R.id.current_set_tab_title));
        if (dbAdapter.getCurrentSetName() != "") {
        	//title.setText(getResources().getString(R.string.title_current_set) + " - " + dbAdapter.getCurrentSetName());
        	title.setText(dbAdapter.getCurrentSetName());
        }
    }
    //endregion


    //region Song Group Functions
    // *****************************************************************************
    // * 
    // * Song Group Functions
    // * 
    // *****************************************************************************
    /**
     * Populates the song groups array list
     */
    public void setSongGroupsList(boolean showSearchResults) {
    	// Query the database
    	Cursor c = dbAdapter.getSongGroupNames();
    	
    	// Clear the existing groups list
    	songGroupsList.clear();
    	
    	// Populate the groups
    	c.moveToFirst();
    	while (!c.isAfterLast()) {
    		songGroupsList.add(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME)));
    		c.moveToNext();
    	}
    	c.close();
    	
    	// Sort the list alphabetically
    	Collections.sort(songGroupsList, new SortIgnoreCase());

        // Add search results field
        if (showSearchResults) {
            songGroupsList.add(0, StaticVars.searchResultsText);
        }
    }

    /**
     * Sets the song groups list with no search results display
     */
    public void setSongGroupsList() {
        setSongGroupsList(false);
    }
    
    /**
     * Fills the group list spinner
     * @param showSearchResults To show or not to show
     */
    public void fillSongGroupsSpinner(final boolean showSearchResults, final int numSearchResults) {
    	// Set the groups list
    	setSongGroupsList(showSearchResults);
        
    	// Create the spinner adapter
        if (showSearchResults)
    	    songGroupsAdapter = new SongGroupArrayAdapter(this, songGroupsList, numSearchResults);
        else
            songGroupsAdapter = new SongGroupArrayAdapter(this, songGroupsList);
    	final Spinner groupSpinner = (Spinner) findViewById(R.id.song_group_spinner);
    	
    	// Set the on click listener for each item
    	groupSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
                // Get the selected item and populate the songs list
                String groupName = songGroupsList.get(position);

                // If the selection has actually changed
                if (!currentSongGroup.equals(groupName)) {
                    // Reset the scroll positions
                    songsCurrentScrollPosition = 0;
                    songsCurrentScrollOffset = 0;

                    // Remove the search results option from the spinner
                    if (!groupName.equals(StaticVars.searchResultsText) &&
                            songGroupsList.get(0).equals(StaticVars.searchResultsText))
                        songGroupsList.remove(0);
                }

                // Refill song list (if not on search results)
                currentSongGroup = groupName;
                if (groupName != StaticVars.searchResultsText) {
                    fillSongsListView();
                }

                // Set the sort by spinner back to default
                ((Spinner) findViewById(R.id.song_sort_spinner)).setSelection(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // Nothing was clicked so ignore it
            }
        });
    	
    	// Set the adapter
    	groupSpinner.setAdapter(songGroupsAdapter);
    	
    	// Set the selected item to the current group
        if (showSearchResults)
            groupSpinner.setSelection(0);
        else
            groupSpinner.setSelection(songGroupsList.indexOf(currentSongGroup));
    }

    /**
     * Displays the song groups w/o the search results display
     */
    public void fillSongGroupsSpinner() {
        fillSongGroupsSpinner(false, -1);
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
	    		String groupName = input.getText().toString();
	    		if (groupName.length() > 0) {
	    			if(!dbAdapter.createSongGroup(groupName))
		    			Toast.makeText(getApplicationContext(), "Failed to create song group!", Toast.LENGTH_LONG).show();
	    			else
	    				addSongsToGroup(groupName);
	    		}
	    		else
	    			Toast.makeText(getApplicationContext(), "Cannot create a song group with no name!", Toast.LENGTH_LONG).show();
	    		
	    		// Refresh the song list and song group spinner
	    		fillSongGroupsSpinner();
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
     * Shows a dialog to select songs to add to the group
     */
    private void addSongsToGroup(final String groupName) {
    	Cursor c = dbAdapter.getSongs(SongsTab.ALL_SONGS_LABEL);
    	
    	// Clear the previous song lists
    	addSongsDialogList.clear();
    	addSongsDialogMap.clear();
    	
    	// Populate the songs lists
    	while(c.moveToNext()) {
    		addSongsDialogList.add(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME)));
    		addSongsDialogMap.put(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME)), false);
    	}
    	c.close();
    	Collections.sort(addSongsDialogList, new SortIgnoreCase());
    	
    	// Create the alert dialog and set the title
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Select Songs");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set_songs, (ViewGroup) findViewById(R.id.add_set_songs_root));
    	alert.setView(dialoglayout);
    	
    	// Get the views
    	final Spinner songGroupSP = (Spinner)dialoglayout.findViewById(R.id.add_set_songs_spinner);
    	final ListView songsLV = (ListView)dialoglayout.findViewById(R.id.add_set_songs_list);
    	final ArrayAdapter<String> songsAD;
    	
    	// Fill the list view
    	songsLV.setEmptyView(findViewById(R.id.empty_songs));
    	songsLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	songsLV.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	String song = addSongsDialogList.get(position);
            	addSongsDialogMap.put(song, !addSongsDialogMap.get(song));
            }
        });
    	songsAD = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, addSongsDialogList);
    	songsLV.setAdapter(songsAD);
    	
    	// Fill the group spinner
    	setSongGroupsList();
    	songGroupSP.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected group
            	String groupName = (String)songGroupSP.getSelectedItem();
            	
            	// Fill the new songs list
            	Cursor c = dbAdapter.getSongs(groupName);
            	addSongsDialogList.clear();
            	
            	// Populate the ArrayList
            	while (c.moveToNext()) {
            		// Get the strings from the cursor
                	String songName = c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME));
                	addSongsDialogList.add(songName);
            	}
            	c.close();
            	Collections.sort(addSongsDialogList, new SortIgnoreCase());
            	
            	// Update list view
            	songsAD.notifyDataSetChanged();
            	
            	// Set the list view checked properties
            	for(int i = 0; i < songsLV.getCount(); i++) {
            		songsLV.setItemChecked(i, addSongsDialogMap.get(songsLV.getItemAtPosition(i)));
            	}
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	songGroupSP.setAdapter(songGroupsAdapter);
    	
    	// Set positive button of the dialog
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Add all the selected songs to the group   
	    		for(String s : addSongsDialogMap.keySet()) {
	    			if(addSongsDialogMap.get(s)) {
	    				dbAdapter.addSongToGroup(s, groupName);
	    			}	
	    		}
			}
    	});

    	// Show the dialog
    	AlertDialog a = alert.create();
    	a.show();
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics); 
    	int height = metrics.heightPixels;
    	height = (int) (height / 1.5);
    	a.getWindow().setLayout(LayoutParams.WRAP_CONTENT, height);
    }
    
    /**
     * Deletes the specified group
     * @param groupName The group to delete
     */
    private void deleteSongGroup() {
    	// Get the list of group names
    	Cursor c = dbAdapter.getSongGroupNames();
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount() - 1];
    	int counter = 0;
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
            String groupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME));

            // Make sure we don't show the All Songs group to delete
            if (!groupName.equals("All Songs")) {
                groupNames[counter++] = groupName;
            }
    	}
    	c.close();
    	
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
							
				    		// Refresh the song list and song group spinner
				    		fillSongGroupsSpinner();
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
	    		
		    	// Refresh the song list and song group spinner
	    		fillSongGroupsSpinner();
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
    //endregion


    //region Set Group Functions
    // *****************************************************************************
    // * 
    // * Set Group Functions
    // * 
    // *****************************************************************************
    /**
     * Sets the set group array list
     */
    public void setSetGroupsList(boolean showSearchResults) {
    	// Query the database
    	Cursor c = dbAdapter.getSetGroupNames();
    	
    	// Clear the existing groups list
    	setGroupsList.clear();
    	
    	// Populate the groups
    	c.moveToFirst();
    	while (!c.isAfterLast()) {
    		setGroupsList.add(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME)));
    		c.moveToNext();
    	}
    	c.close();
    	
    	// Sort the list alphabetically
    	Collections.sort(setGroupsList, new SortIgnoreCase());

        // Add search results field
        if (showSearchResults) {
            setGroupsList.add(0, StaticVars.searchResultsText);
        }
    }

    /**
     * Fills the set groups list with no search results
     */
    public void setSetGroupsList() {
        setSetGroupsList(false);
    }
    
    /**
     * Fills the group list spinner
     * @param v
     */
    public void fillSetGroupsSpinner(final boolean showSearchResults, final int numSearchResults) {
    	// Set the groups list
    	setSetGroupsList(showSearchResults);

        // Create the spinner adapter
        if (showSearchResults)
            setGroupsAdapter = new SetGroupArrayAdapter(this, setGroupsList, numSearchResults);
        else
            setGroupsAdapter = new SetGroupArrayAdapter(this, setGroupsList);
        final Spinner groupSpinner = (Spinner) findViewById(R.id.set_group_spinner);
    	
    	// Set the on click listener for each item
    	groupSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
                // Get the selected item and populate the sets list
                String groupName = setGroupsList.get(position);

                // If the selection has actually changed
                if (!currentSetGroup.equals(groupName)) {
                    // Reset the scroll positions
                    setsCurrentScrollPosition = 0;
                    setsCurrentScrollOffset = 0;

                    // Remove the search results option from the spinner
                    if (!groupName.equals(StaticVars.searchResultsText) &&
                            setGroupsList.get(0).equals(StaticVars.searchResultsText))
                        setGroupsList.remove(0);
                }

                // Refill song list (if not on search results)
                currentSetGroup = groupName;
                if (groupName != StaticVars.searchResultsText) {
                    fillSetsListView();
                }
            	
            	// Set the sort by spinner back to default
            	((Spinner)findViewById(R.id.set_sort_spinner)).setSelection(0);
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	
    	groupSpinner.setAdapter(setGroupsAdapter);
    }

    /**
     * Fills the set groups with no search results
     */
    public void fillSetGroupsSpinner() {
        fillSetGroupsSpinner(false, -1);
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
	    		
	    		// Refresh the set group spinner and set list
	    		fillSetGroupsSpinner();
	    		setsAdapter.notifyDataSetChanged();
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
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount() - 1];
    	int counter = 0;
    	
    	// Don't show the all songs group
    	c.moveToFirst();
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		groupNames[counter++] = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME));
    	}
    	c.close();
    	
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
							
				    		// Refresh the set group spinner and set list
				    		fillSetGroupsSpinner();
				    		setsAdapter.notifyDataSetChanged();
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
	    		
		    	// Refresh the set group spinner and set list
	    		fillSetGroupsSpinner();
	    		setsAdapter.notifyDataSetChanged();
	        	
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
    //endregion


    //region Sorting Functions
    // *****************************************************************************
    // * 
    // * Sorting Functions
    // * 
    // *****************************************************************************
    /**
     * Fills the song sort spinner
     */
    public void fillSetSortSpinner() {
    	// Create the spinner adapter
    	setSortAdapter = new ArrayAdapter<String>(this, R.layout.group_spinner_item, StaticVars.setSortBy);
    	setSortAdapter.setDropDownViewResource( R.layout.group_spinner_dropdown_item );
    	final Spinner sortSpinner = (Spinner) findViewById(R.id.set_sort_spinner);
    	
    	// Set the on click listener for each item
    	sortSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	sortSets(position);
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	
    	
    	// Set the adapter
    	sortSpinner.setAdapter(setSortAdapter);
    }
    
    /**
     * Fills the song sort spinner
     */
    public void fillSongSortSpinner() {
    	// Create the spinner adapter
    	songSortAdapter = new ArrayAdapter<String>(this, R.layout.group_spinner_item, StaticVars.songSortBy);
    	songSortAdapter.setDropDownViewResource( R.layout.group_spinner_dropdown_item );
    	final Spinner sortSpinner = (Spinner) findViewById(R.id.song_sort_spinner);
    	
    	// Set the on click listener for each item
    	sortSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	sortSongs(position);
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	
    	// Set the adapter
    	sortSpinner.setAdapter(songSortAdapter);
    }
    
    /**
     * Sorts the song list by the selected item
     * @param sortByPosition The position in the song sort array list
     */
    private void sortSongs(int sortByPosition) {
    	ArrayList<Item> temp = new ArrayList<Item>();
    	
    	// Remove section items
    	for (Item i : songsList) {
    		if (!i.getClass().equals(SectionItem.class))
    			temp.add((SongItem)i);
    	}
    	
    	// Sort the array list
    	switch(sortByPosition) {
	    	case 0: //Title
	    		fillSongsListView();
	    		break;
	    	case 1: //Author
	    		// Sort the temp list
	    		Collections.sort(temp, new SongItemComparableAuthor());
	    		
	    		// Reset the songs list and add sections
	    		songsList.clear();
	        	for (int i = 0; i < temp.size(); i++) {
	        		if (i != 0) {
	        			if (Character.toLowerCase(((SongItem)temp.get(i)).getAuthor().charAt(0)) != 
	        					Character.toLowerCase(((SongItem)temp.get(i-1)).getAuthor().charAt(0))) {
	        				// This is the first item with that letter, add the separator
	        				songsList.add(new SectionItem(((SongItem)temp.get(i)).getAuthor().substring(0, 1).toUpperCase(Locale.US)));
	        			}
	        		}
	        		else {
	        			// First item, add section
	        			songsList.add(new SectionItem(((SongItem)temp.get(i)).getAuthor().substring(0, 1).toUpperCase(Locale.US)));
	        		}
	        		
	        		songsList.add(temp.get(i));
	        	}
	        	
	        	// Update the UI
	        	songsAdapter.notifyDataSetChanged();
	    		break;
	    	case 2: //Key
	    		Collections.sort(temp, new SongItemComparableKey());
	    		
	    		// Reset the songs list and add sections
	    		songsList.clear();
	        	for (int i = 0; i < temp.size(); i++) {
	        		if (i != 0) {
	        			if (!((SongItem)temp.get(i)).getKey().equals(((SongItem)temp.get(i-1)).getKey())) {
	        				// This is the first item with that key, add the separator
	        				songsList.add(new SectionItem(((SongItem)temp.get(i)).getKey()));
	        			}
	        		}
	        		else {
	        			// First item, add section
	        			songsList.add(new SectionItem(((SongItem)temp.get(i)).getKey()));
	        		}
	        		
	        		songsList.add(temp.get(i));
	        	}
	        	
	        	// Update the UI
	        	songsAdapter.notifyDataSetChanged();
	    		break;
    	}
    }
    
    /**
     * Sorts the set list by the selected item
     * @param sortByPosition The position in the song sort array list
     */
    private void sortSets(int sortByPosition) {
    	ArrayList<SetItem> temp = new ArrayList<SetItem>();
    	
    	// Remove section items
    	for (Item i : setsList) {
    		if (!i.getClass().equals(SectionItem.class))
    			temp.add((SetItem)i);
    	}
    	
    	// Set the sort index
		setsListSortByIndex = sortByPosition;
		
		// Refill the sets list
		fillSetsListView();
    }
    //endregion


    //region Import / Export functions
    // *****************************************************************************
    // * 
    // * Import / Export Functions
    // * 
    // *****************************************************************************
    /**
     * Selects the folder to export the backup file to
     */
    private void selectExportFolder(String activityKey) {
    	// Create the open file intent
        Intent intent = new Intent(getBaseContext(), OpenFile.class);
        intent.putExtra(StaticVars.FILE_ACTIVITY_KEY, activityKey);
        intent.putExtra(StaticVars.FILE_ACTIVITY_TYPE_KEY, StaticVars.FILE_ACTIVITY_FOLDER);
        
        // Start the activity
        startActivityForResult(intent, 1);
    }
    
    /**
     * Exports all songbook files and db
     */
    private void exportAll(String folder) {
    	final String exportZipLocation = folder + "/" + StaticVars.EXPORT_ZIP_FILE;
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Export");
    	alert.setMessage("Are you sure you want to export your data to '" + exportZipLocation + "'?");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Create the db backup sql script
                String exportSQLData = dbAdapter.exportDBData();

                try {
                    // Store the backup script in the app files folder
                    FileOutputStream out = openFileOutput(StaticVars.EXPORT_SQL_FILE, Context.MODE_PRIVATE);
                    out.write(exportSQLData.getBytes());
                    out.close();

                    // Get a list of all the files in the app files folder
                    String[] files = fileList();
                    for (int i = 0; i < files.length; i++) {
                        files[i] = getFilesDir() + "/" + files[i];
                    }

                    // Zip the files and save to the external storage
                    Compress newZip = new Compress(files, exportZipLocation);
                    if (newZip.zip())
                        Toast.makeText(getBaseContext(), "Your data has been successfully saved to: " + exportZipLocation, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getBaseContext(), "There was an error backing up your data. Please try again.", Toast.LENGTH_LONG).show();

                    // Delete the backup script
                    deleteFile(StaticVars.EXPORT_SQL_FILE);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Could not write db file!", Toast.LENGTH_LONG).show();
                }


            }
        });

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled, do not import
            }
        });

    	alert.show();
    }

    /**
     * Exports the set stored in setToExport
     * @param folder The folder to export the set to
     */
    private void exportSet(String folder) {
        // Make sure we have a valid set to export
        if (setToExport != null) {
            // Set the export location
            String filename = setToExport.getName() + ".bak";
            String exportZipLocation = folder + "/" + filename;

            // Create the db backup sql script
            String exportSQLData = dbAdapter.exportSetDBData(setToExport.getName());

            try {
                // Store the backup script in the app files folder
                FileOutputStream out = openFileOutput(StaticVars.EXPORT_SQL_FILE, Context.MODE_PRIVATE);
                out.write(exportSQLData.getBytes());
                out.close();

                // Get a list of all the files in the app files folder
                String[] allFiles = fileList();
                ArrayList<String> files = new ArrayList<>();
                for (int i = 0; i < allFiles.length; i++) {
                    // Only add the songs that are part of this set
                    for (SongItem s : setToExport.songs) {
                        String tmp = s.getName();
                        if (allFiles[i].contains(tmp)) {
                            // Song is in the set, add it to the list
                            files.add(getFilesDir() + "/" + allFiles[i]);
                        }
                    }
                }

                // Add the sql file to the list
                files.add(getFilesDir() + "/" + StaticVars.EXPORT_SQL_FILE);

                // Zip the files and save to the external storage
                String[] filesToZip = files.toArray(new String[0]);
                Compress newZip = new Compress(filesToZip, exportZipLocation);
                if (newZip.zip()) {
                    // Alert the user the set has been exported
                    Toast.makeText(this, "\"" + setToExport.getName() + "\" has been exported to " + exportZipLocation, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "There was an error exporting your set. Please try again.", Toast.LENGTH_LONG).show();
                }

                // Delete the backup script
                deleteFile(filename);
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Could not write export file!", Toast.LENGTH_LONG).show();
            }



            // Clear the set to export
            setToExport = null;
        }
    }
    
    /**
     * Opens the dialog to select the file to import
     */
    private void selectImportFile(String activityKey) {
    	// Create the open file intent
        Intent intent = new Intent(getBaseContext(), OpenFile.class);
        intent.putExtra(StaticVars.FILE_ACTIVITY_KEY, activityKey);
        intent.putExtra(StaticVars.FILE_ACTIVITY_TYPE_KEY, StaticVars.FILE_ACTIVITY_FILE);
        
        // Start the activity
        startActivityForResult(intent, 1);
    }
    
    /**
     * Imports songbook files and data from the specified file
     * @param filePath The compressed file to import
     */
    private void importFile(final String filePath, final boolean clearDB, String warningMessage) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Import");
    	alert.setMessage(warningMessage);
    	
    	final ImportDatabase importDBTask = new ImportDatabase();

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Configure progress dialog
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Importing Data. This may take a few minutes." + System.getProperty("line.separator") + "Please wait...");
                progressDialog.setTitle("Please Wait!");
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (DialogInterface.OnClickListener) null);

                progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button b = progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // Update the progress dialog text
                                progressDialog.setMessage("Please wait while we stop the import..." + System.getProperty("line.separator") + "This may take a few minutes.");

                                // Hide the cancel button
                                progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.INVISIBLE);

                                // Cancel the import task
                                importDBTask.cancel(true);
                            }
                        });
                    }
                });


                // Show progress dialog
                progressDialog.show();

                // Start the import task
                ImportDBParams params = new ImportDBParams(filePath, clearDB);
                importDBTask.execute(new ImportDBParams[]{params});
            }
        });

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled, do not import
            }
        });

    	alert.show();
    }
    //endregion


    //region Classes
    // *****************************************************************************
    // * 
    // * Classes
    // * 
    // *****************************************************************************
    public class ImportDBParams {

        public ImportDBParams() {
            filePath = "";
            clearDB = false;
            result = "";
        }

        public ImportDBParams(String _filepath, boolean _clearDB) {
            filePath = _filepath;
            clearDB = _clearDB;
            result = "";
        }

        public ImportDBParams(String _filepath, boolean _clearDB, String _result) {
            filePath = _filepath;
            clearDB = _clearDB;
            result = _result;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String filePath;

        public boolean getClearDB() {
            return clearDB;
        }

        public void setClearDB(boolean clearDB) {
            this.clearDB = clearDB;
        }

        public boolean clearDB;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String result;
    }

    public class ImportDatabase extends AsyncTask<ImportDBParams, Void, ImportDBParams> {
    	@Override
    	protected ImportDBParams doInBackground(ImportDBParams... params) {
            // Setup return
            ImportDBParams ret = new ImportDBParams();
    		ret.setResult("");

            // Get the parameters
            String filePath = params[0].getFilePath();
            boolean clearDB = params[0].getClearDB();
    		
    		// Decompress the backup file
    		String unzipFolder = "sbg_unzipped";
        	String unzipLocation = Environment.getExternalStorageDirectory() + "/" + unzipFolder + "/"; 
        	 
        	Decompress d = new Decompress(filePath, unzipLocation);
        	if (!d.unzip()) {
                ret.setResult("There was an error decompressing your backup file. Please try again.");
        	}
        	else {
            	// Run the sql script to import songs
            	try {
            		// Open and read the export file
        	    	InputStream fis = new FileInputStream(unzipLocation + "/" + StaticVars.EXPORT_SQL_FILE);
        	    	DataInputStream din = new DataInputStream(fis);
        	    	BufferedReader br = new BufferedReader(new InputStreamReader(din));
        	    	StringBuilder sb = new StringBuilder();
        	        String line = br.readLine();
        	        
        	        // Cycle through each line in the sql file and add it to the string builder
        	        while(line != null) {
        	        	sb.append(line + StaticVars.EOL);
        	        	line = br.readLine();
        	        }
        	        
        	        br.close();
        	        
        	        // Check for cancel
                	if (isCancelled()) {
                		return ret;
                	}
                	
                	// Clear the database and files
                    if (clearDB) {
                        dbAdapter.clearDB();
                        String[] files = fileList();
                        for (int i = 0; i < files.length; i++) {
                            deleteFile(files[i]);
                        }
                    }
                	
                	// Check for cancel
                	if (isCancelled()) {
                		return ret;
                	}
        	        
        	        // Execute the SQL file
        	        if (!dbAdapter.importDBData(sb.toString())) {
                        // Failed to import the database data properly
                        ret.setResult("Failed to import the database file. Import aborted.");
                        return ret;
                    }
        	        
        	        // Check for cancel
                	if (isCancelled()) {
                		return ret;
                	}
        	        
        	        // Add the song files to the files directory
                	File dir = new File(unzipLocation);
                	for (File child : dir.listFiles()) {
                		// Try to add the song file
                		try {
            	    		InputStream in = new FileInputStream(child);
            	    		OutputStream out = openFileOutput(child.getName(), Context.MODE_PRIVATE);
            	    		byte[] buf = new byte[1024];
            	    		int len;
            	    		while ((len = in.read(buf)) > 0) {
            	    		   out.write(buf, 0, len);
            	    		}
            	    		in.close();
            	    		out.close(); 
                		}
                		catch (Exception e) {
                			// If the song file failed, remove the song from the DB
                            if (clearDB) {
                                String songName = child.getName();
                                songName = songName.substring(0, songName.lastIndexOf("."));
                                dbAdapter.deleteSong(songName);

                                if (ret.equals("")) {
                                    ret.setResult("Import complete, some songs failed: " + songName);
                                } else {
                                    ret.setResult(ret.getResult() + ", " + songName );
                                }
                            }
                		}
                		
                		// Check for cancel
                    	if (isCancelled()) {
                    		return ret;
                    	}
                	}
            	}
            	catch (Exception e) {
            		// Add the default values back into the db
                    if (clearDB) {
                        dbAdapter.addDBDefaults();
                    }
                    ret.setResult("Could not import database file. Import aborted.");
            	}
            	
            	// Delete the unzipped temp files
            	File dir = new File(Environment.getExternalStorageDirectory(), unzipFolder);
            	if (dir.isDirectory()) {
            		String filesList[] = dir.list();
            		for (String f : filesList) {
            			new File(dir, f).delete();
            		}
            	}
        	}
        	
        	// Set return value
        	if (ret.getResult().equals("")) {
                ret.setResult("Successfully imported your data!");
        	}
        	
        	return ret;
    	}
    	
    	@Override
    	protected void onPostExecute(ImportDBParams result) {
    		// Refresh all the lists
    		fillSetGroupsSpinner();
    		fillSongGroupsSpinner();
        	fillSongGroupsSpinner();
        	fillSongsListView();
        	fillSetGroupsSpinner();
        	fillSetsListView();
        	fillCurrentSetListView();
        	
        	// Close the progress dialog
        	progressDialog.dismiss();
        	
        	// Show success message
        	Toast.makeText(getBaseContext(), result.getResult(), Toast.LENGTH_LONG).show();
    	}
    	
    	@Override
    	protected void onCancelled(ImportDBParams result) {
    		// Clear the database and files
            if (result.getClearDB()) {
                dbAdapter.clearDB();
                String[] files = fileList();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }

                // Add the default values back into the db
                dbAdapter.addDBDefaults();
            }
    		
    		// Refresh all the lists
    		fillSetGroupsSpinner();
    		fillSongGroupsSpinner();
        	fillSongGroupsSpinner();
        	fillSongsListView();
        	fillSetGroupsSpinner();
        	fillSetsListView();
        	fillCurrentSetListView();
        	
        	// Close the progress dialog
        	progressDialog.dismiss();
        	
        	// Show success message
        	Toast.makeText(getBaseContext(), "Your import was cancelled!", Toast.LENGTH_LONG).show();
    	}
    }
    
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
    
    /**
     * Comparator for case insensitive sorting
     * @author SamIAm
     *
     */
    @SuppressLint("DefaultLocale")
	public class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            int ret;

            String s1 = (String) o1;
            String s2 = (String) o2;

            // Check for special case
            if (s1.equals(SongsTab.ALL_SONGS_LABEL) || s1.equals(SetsTab.ALL_SETS_LABEL)) {
                ret = -10;
            } else if (s2.equals(SongsTab.ALL_SONGS_LABEL) || s2.equals(SetsTab.ALL_SETS_LABEL)) {
                ret = 10;
            } else {
                ret = s1.toLowerCase(Locale.ENGLISH).compareTo(s2.toLowerCase());
            }

            return ret;
        }
    }
    
    /**
     * Comparator for Song Items by author
     * @author SamIAm
     *
     */
    public static class SongItemComparableAuthor implements Comparator<Item>{
    	 
        public int compare(Item o1, Item o2) {
            return ((SongItem)o1).getAuthor().compareToIgnoreCase(((SongItem)o2).getAuthor());
        }
    }
    
    /**
     * Comparator for Song Items by author
     * @author SamIAm
     *
     */
    public static class ItemComparableName implements Comparator<Item>{
    	 
        public int compare(Item o1, Item o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
    
    /**
     * Comparator for Song Items by key
     * @author SamIAm
     *
     */
    public static class SongItemComparableKey implements Comparator<Item>{
    	 
        public int compare(Item o1, Item o2) {
        	// Get the song keys
        	String key1 = ((SongItem)o1).getKey();
        	String key2 = ((SongItem)o2).getKey();
        	
        	// Translate any special keys
        	if (StaticVars.songKeyMap.containsKey(key1))
        		key1 = StaticVars.songKeyMap.get(key1);
        	if (StaticVars.songKeyMap.containsKey(key2))
        		key2 = StaticVars.songKeyMap.get(key2);
        	
        	// Do a special compare for 'unknown'
        	if (key1.equals(StaticVars.UNKNOWN) && key2.equals(StaticVars.UNKNOWN))
        		return 0;
        	else if (key1.equals(StaticVars.UNKNOWN) && !key2.equals(StaticVars.UNKNOWN))
        		return 1;
        	else if (!key1.equals(StaticVars.UNKNOWN) && key2.equals(StaticVars.UNKNOWN))
        		return -1;
        	
        	// Compare the keys
        	if (StaticVars.songKeys.indexOf(key1) > StaticVars.songKeys.indexOf(key2))
        		return 1;
        	else if (StaticVars.songKeys.indexOf(key1) == StaticVars.songKeys.indexOf(key2))
        		return 0;
        	else
        		return -1;
        }
    }
    
    /**
     * Comparator for Set Items by date
     * Oldest date is first
     * @author SamIAm
     *
     */
    public static class SetItemComparableDate implements Comparator<Item>{
    	 
        public int compare(Item o1, Item o2) {
        	int ret = -1;
        	
        	String[] split1 = ((SetItem)o1).getDate().split("/");
        	String[] split2 = ((SetItem)o2).getDate().split("/");
        	
        	if (split1.length == 3 && split2.length == 3)
        	{
        		try
        		{
	        		// Parse date 1
	        		int month1 = Integer.parseInt(split1[0].trim());
	        		int day1 = Integer.parseInt(split1[1].trim());
	        		int year1 = Integer.parseInt(split1[2].trim());
	        		
	        		// Parse date 2
	        		int month2 = Integer.parseInt(split2[0].trim());
	        		int day2 = Integer.parseInt(split2[1].trim());
	        		int year2 = Integer.parseInt(split2[2].trim());
	        		
	        		// Compare years
	        		if (year1 < year2)
	        		{
	        			// o1 is earlier than o2
	        			ret = -1;
	        		}
	        		else if (year1 > year2)
	        		{
	        			// o1 is later than o2
	        			ret = 1;
	        		}
	        		else
	        		{
	        			// Years are the same, compare months
	        			if (month1 < month2)
	        			{
	        				// o1 is earlier than o2
	            			ret = -1;
	        			}
	            		else if (month1 > month2)
	            		{
	            			// o1 is later than o2
	            			ret = 1;
	            		}
	            		else 
	            		{
	            			// Years & Months are the same, compare days
	            			if (day1 < day2)
	            			{
	            				// o1 is less than o2
	            				ret = -1;
	            			}
	            			else if (day1 > day2)
	            			{
	            				// o1 is later than o2
	            				ret = 1;
	            			}
	            			else
	            			{
	            				// o1 and o2 are the same
	            				ret = 0;
	            			}
	            		}
	        		}
        		} 
        		catch (NumberFormatException e)
        		{
        			// Could not parse date correctly
            		ret = ((SetItem)o1).getDate().compareToIgnoreCase(((SetItem)o2).getDate());
        		}
        	}
        	else
        	{
        		// Could not parse date correctly
        		ret = ((SetItem)o1).getDate().compareToIgnoreCase(((SetItem)o2).getDate());
        	}
        	
        	return ret;
        }
    }
    
    /**
     * Comparator for Set Items by date
     * Most recent date is first
     * @author SamIAm
     *
     */
    public static class SetItemComparableDateReverse implements Comparator<Item>{
    	 
        public int compare(Item o1, Item o2) {
        	int ret = -1;
        	
        	String[] split1 = ((SetItem)o1).getDate().split("/");
        	String[] split2 = ((SetItem)o2).getDate().split("/");
        	
        	if (split1.length == 3 && split2.length == 3)
        	{
        		try
        		{
	        		// Parse date 1
	        		int month1 = Integer.parseInt(split1[0].trim());
	        		int day1 = Integer.parseInt(split1[1].trim());
	        		int year1 = Integer.parseInt(split1[2].trim());
	        		
	        		// Parse date 2
	        		int month2 = Integer.parseInt(split2[0].trim());
	        		int day2 = Integer.parseInt(split2[1].trim());
	        		int year2 = Integer.parseInt(split2[2].trim());
	        		
	        		// Compare years
	        		if (year1 < year2)
	        		{
	        			// o1 is earlier than o2
	        			ret = 1;
	        		}
	        		else if (year1 > year2)
	        		{
	        			// o1 is later than o2
	        			ret = -1;
	        		}
	        		else
	        		{
	        			// Years are the same, compare months
	        			if (month1 < month2)
	        			{
	        				// o1 is earlier than o2
	            			ret = 1;
	        			}
	            		else if (month1 > month2)
	            		{
	            			// o1 is later than o2
	            			ret = -1;
	            		}
	            		else 
	            		{
	            			// Years & Months are the same, compare days
	            			if (day1 < day2)
	            			{
	            				// o1 is less than o2
	            				ret = 1;
	            			}
	            			else if (day1 > day2)
	            			{
	            				// o1 is later than o2
	            				ret = -1;
	            			}
	            			else
	            			{
	            				// o1 and o2 are the same
	            				ret = 0;
	            			}
	            		}
	        		}
        		} 
        		catch (NumberFormatException e)
        		{
        			// Could not parse date correctly
            		ret = ((SetItem)o1).getDate().compareToIgnoreCase(((SetItem)o2).getDate());
        		}
        	}
        	else
        	{
        		// Could not parse date correctly
        		ret = ((SetItem)o1).getDate().compareToIgnoreCase(((SetItem)o2).getDate());
        	}
        	
        	return ret;
        }
    }
    //endregion
}

