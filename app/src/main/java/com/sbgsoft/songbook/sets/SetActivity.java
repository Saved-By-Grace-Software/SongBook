package com.sbgsoft.songbook.sets;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SetItem;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainStrings;
import com.sbgsoft.songbook.songs.SetSongFragment;

public class SetActivity extends FragmentActivity {
	
	/*****************************************************************************
     * 
     * Class Variables
     * 
     *****************************************************************************/
	TextView song;
	FragmentTransaction transaction;
	static ViewPager mViewPager;
	static PagerAdapter mPagerAdapter;
	private static int currentSong = 0;
    GestureDetector gestureDetector;
	
	
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

        // Instantiate the gesture listener
        gestureDetector = new GestureDetector(this, new GestureListener());
        
        // Create page adapter
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        
        // Get songs and add them to the page adapter
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	// Get the extras
            int currSong = extras.getInt(MainStrings.CURRENT_SONG_KEY);
            SetItem setItem = extras.getParcelable(MainStrings.SET_SONGS_KEY);
            
            // Create each song fragment
            for (SongItem song : setItem.songs) {
            	// Create song fragment
            	Fragment songFrag = new SetSongFragment();
            	Bundle bSong = new Bundle();
            	bSong.putParcelable(MainStrings.SONG_ITEM_KEY, song);
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
		
		mViewPager.setOnPageChangeListener(
	            new ViewPager.SimpleOnPageChangeListener() {
	                @Override
	                public void onPageSelected(int position) {
	                	currentSong = position;
	                }
	            });
        
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent e)
    {
        super.dispatchTouchEvent(e);
        return gestureDetector.onTouchEvent(e);
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

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {

            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE &&
                    Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //From Right to Left
                Log.d("SONGBOOK", "Right to left swipe");
                return true;
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE &&
                    Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //From Left to Right
                Log.d("SONGBOOK", "Left to right swipe");
                return true;
            }

//            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE &&
//                    Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                //From Bottom to Top
//                return true;
//            }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE &&
//                    Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                //From Top to Bottom
//                return true;
//            }
            return false;
        }
        @Override
        public boolean onDown(MotionEvent e) {
            //always return true since all gestures always begin with onDown and<br>
            //if this returns false, the framework won't try to pick up onFling for example.
            return true;
        }
    }
}
