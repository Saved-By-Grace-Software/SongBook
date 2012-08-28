package com.sbgsoft.tabapp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.lamerman.FileDialog;
import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.db.DBAdapter;
import com.sbgsoft.tabapp.sets.SetsTab;
import com.sbgsoft.tabapp.songs.SongActivity;
import com.sbgsoft.tabapp.songs.SongsTab;

public class MainActivity extends FragmentActivity {
	public static final String SONG_NAME_KEY = "songName";
	public static final String SONG_TEXT_KEY = "songText";
	public static final String SONG_FILE_PATH = "/data/data/com.sbgsoft.tabapp/songs/";
	
	FragmentTransaction transaction;
	static ViewPager mViewPager;
	public static DBAdapter dbAdapter;
	private Cursor setsCursor;
	private Cursor songsCursor;
	private String importFilePath = "";

	
    /**
     *  Called when the activity is first created. 
     **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Fragment tabOneFragment = new SetsTab();
        Fragment tabTwoFragment = new SongsTab();
        
        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(tabOneFragment);
        mPagerAdapter.addFragment(tabTwoFragment);
        
        //transaction = getSupportFragmentManager().beginTransaction();
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
	    mViewPager.setCurrentItem(0);
		
		mViewPager.setOnPageChangeListener(
	            new ViewPager.SimpleOnPageChangeListener() {
	                @Override
	                public void onPageSelected(int position) {
	                    // When swiping between pages, select the
	                    // corresponding tab.
	                    getActionBar().setSelectedNavigationItem(position);
	                }
	            });
        
        ActionBar ab = getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ab.setDisplayShowTitleEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        Tab tab1 = ab.newTab().setText("Sets")
        		.setTabListener(new TabListener<SetsTab>(
                        this, "tabsets", SetsTab.class));

		Tab tab2 = ab.newTab().setText("Songs")
				.setTabListener(new TabListener<SongsTab>(
                        this, "tabsongs", SongsTab.class));
		
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
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
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
		    		if(!dbAdapter.createSet(value))
		    			Toast.makeText(getApplicationContext(), "Failed to create set!", Toast.LENGTH_LONG).show();
		    		else
		    			setsCursor.requery();
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
	    			String songFile = SONG_FILE_PATH + value + ".txt";
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
			    				OutputStream out = new FileOutputStream(songFile);
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
		    				File file = new File(songFile);
		    				try {
		    					file.createNewFile();
		    				} catch (IOException e) {
		    					// Delete the song since the file could not be imported
		    					dbAdapter.deleteSong(value);
		    					
		    					// Alert that the song failed
		    					Toast.makeText(getApplicationContext(), "Could not create song file, Song deleted.", Toast.LENGTH_LONG).show();
		    				}
		    				
		    			}
		    			
		    			// Refresh song list
		    			songsCursor.requery();
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
     * Prompts the user to confirm then deletes all sets
     */
    private void deleteAllSets() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete All?!");
    	alert.setMessage("Are you sure you want to delete ALL sets???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		dbAdapter.deleteAllSets();
	        	setsCursor.requery();
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
     * Prompts the user to confirm then deletes all songs
     */
    private void deleteAllSongs() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete All?!");
    	alert.setMessage("Are you sure you want to delete ALL songs???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		dbAdapter.deleteAllSongs();
	        	songsCursor.requery();
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
		    		File delFile = new File(fileToDelete);
		    		delFile.delete();
		    		
		    		// Delete song from database
		    		dbAdapter.deleteSong(songName);
	    		}
	    		
	    		// Refresh song list
	        	songsCursor.requery();
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
     * Fills the sets list
     * @param v The view for the list
     */
    public void fillSetsList(View v) {
    	setsCursor = dbAdapter.getSetNames();
    	startManagingCursor(setsCursor);
    	
    	String[] from = new String[] { "setName" };
        int[] to = new int[] { R.id.sets_row_text };
        
        SimpleCursorAdapter sets = new SimpleCursorAdapter(this, R.layout.sets_row, setsCursor, from, to);
        ListView lv = ((ListView)v.findViewById(R.id.sets_list));
        lv.setEmptyView(findViewById(R.id.empty_sets));
        lv.setAdapter(sets);
    }
    
    /**
     * Fills the songs list
     * @param v The view for the list
     */
    public void fillSongsList(View v) {
    	songsCursor = dbAdapter.getSongNames();
    	startManagingCursor(songsCursor);
    	
    	String[] from = new String[] { "songName" };
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
        
        // Set the long click listener for each item
        lv.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long row) {
            	// TODO: Show the delete menu option
            	            	
            	// Delete the song
            	songsCursor.moveToPosition(position);
            	String songName = songsCursor.getString(songsCursor.getColumnIndexOrThrow(DBAdapter.TBLSONG_NAME));
                deleteSong(songName);
            	
                return true;
            }
        });
        lv.setAdapter(songs);
    }
    
    /**
     * Gets the text from the specified file
     * @return The song text
     */
    private String getSongText(String fileName) {
    	String songText = "";
    	
    	
        try {
        	BufferedReader br = new BufferedReader(new FileReader(fileName));
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
    
    /**
     * Get the return from the file dialog activity
     */
    public synchronized void onActivityResult(final int requestCode,
        int resultCode, final Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            importFilePath = data.getStringExtra(FileDialog.RESULT_PATH);
            createSong();
        } 
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

