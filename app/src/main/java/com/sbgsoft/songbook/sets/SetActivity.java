package com.sbgsoft.songbook.sets;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SetItem;
import com.sbgsoft.songbook.items.Settings;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.StaticVars;
import com.sbgsoft.songbook.songs.SetSongFragment;

public class SetActivity extends FragmentActivity {
	
	/*****************************************************************************
     * 
     * Class Variables
     * 
     *****************************************************************************/
	static ViewPager mViewPager;
	static PagerAdapter mPagerAdapter;
	private static int currentSong = 0;
    private boolean autoplayTrack = false;
	
	
	/*****************************************************************************
     * 
     * Class Functions
     * 
     *****************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Hide status bar of Android
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_set);
        
        // Create page adapter
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

        // Get the current settings
        Settings settings = MainActivity.dbAdapter.getCurrentSettings();
        autoplayTrack = settings.getAutoplayTrack();

        // Get songs and add them to the page adapter
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Get the extras
            int currSong = extras.getInt(StaticVars.CURRENT_SONG_KEY);
            SetItem setItem = extras.getParcelable(StaticVars.SET_SONGS_KEY);
            
            // Create each song fragment
            for (SongItem song : setItem.songs) {
            	// Create song fragment
            	Fragment songFrag = new SetSongFragment();
                ((SetSongFragment)songFrag).setActivity(this);
            	Bundle bSong = new Bundle();
            	bSong.putParcelable(StaticVars.SONG_ITEM_KEY, song);
            	songFrag.setArguments(bSong);
            	
            	// Add the fragment to the page adapter
            	mPagerAdapter.addFragment(songFrag);
            	
            	// Set the current page number
            	currentSong = currSong;
            }
        }
        
        mViewPager = (ViewPager) findViewById(R.id.set_pager);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(5);
		mViewPager.setCurrentItem(currentSong);
		
		mViewPager.addOnPageChangeListener(
	            new ViewPager.SimpleOnPageChangeListener() {
	                @Override
	                public void onPageSelected(int position) {
                        switchPages(position);
	                }
	            });

        // Start the current song track
        if (autoplayTrack) {
            Fragment f = mPagerAdapter.mFragments.get(currentSong);
            ((SetSongFragment) f).setAutostartWhenCreated(true);
        }
        
        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	// Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override 
    public void onPause() {
    	super.onPause();
    	
    	// Keep the screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
    	// Keep the screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_set, menu);
        return true;
    }
    
    
    /*****************************************************************************
     * 
     * Song Functions
     * 
     *****************************************************************************/
    /**
     * Transposes the currently viewed song
     * @param v
     */
    public void onTransposeButtonClick(View v) {
        // Get the current fragment
        Fragment f = mPagerAdapter.mFragments.get(currentSong);

        // Transpose the current song
        ((SetSongFragment)f).onTransposeButtonClick();
    }

    /**
     * Edits the currently viewed song
     * @param v
     */
    public void onEditButtonClick(View v) {
        // Get the current fragment
        Fragment f = mPagerAdapter.mFragments.get(currentSong);

        // Transpose the current song
        ((SetSongFragment)f).onEditButtonClick();
    }

    /**
     * Plays the current song's background track
     * @param v
     */
    public void onPlayButtonClick(View v) {
        // Get the current fragment
        Fragment f = mPagerAdapter.mFragments.get(currentSong);

        // Transpose the current song
        ((SetSongFragment)f).onPlayButtonClick();
    }

    /**
     * Handles the switching of pages
     * @param newPage the new page number
     */
    private void switchPages(int newPage) {
        // Get the previous fragment
        Fragment prev = mPagerAdapter.mFragments.get(currentSong);

        // Update the current song
        currentSong = newPage;

        // Get the current fragment
        Fragment curr = mPagerAdapter.mFragments.get(currentSong);

        // Stop the current track
        if (((SetSongFragment)prev).hasTrack()) {
            ((SetSongFragment) prev).stopPlayer(3);
        }

        // Start the next track
        if (((SetSongFragment)curr).hasTrack() && autoplayTrack) {
                ((SetSongFragment) curr).startPlayer(3);
        }
    }
    
    
    /*****************************************************************************
     * 
     * Classes
     * 
     *****************************************************************************/
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
