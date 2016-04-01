package com.sbgsoft.songbook.songs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.StaticVars;
import com.sbgsoft.songbook.views.AutoFitTextView;

public class SongActivity extends Activity {

    //region Class Variables
	/*****************************************************************************
     * 
     * Class Variables
     * 
     *****************************************************************************/
	AutoFitTextView song;
	private SongItem mSongItem;
    ScaleGestureDetector scaleGestureDetector;
    private Metronome mMetronome;
    ChordDisplay disp;
    //endregion


    //region Class Functions
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
        setContentView(R.layout.activity_song);
        
        // Get the song textview
        song = (AutoFitTextView)findViewById(R.id.song_text);

        // Instantiate the scale class
        scaleGestureDetector = new ScaleGestureDetector(this, new simpleOnScaleGestureListener());
        
        // Populate it with the song text
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	mSongItem = extras.getParcelable(StaticVars.SONG_ITEM_KEY);
        	
            if (mSongItem.getKey().length() > 1)
            	mSongItem.setKey(mSongItem.getKey().substring(0, 1).toUpperCase(Locale.ENGLISH) + mSongItem.getKey().substring(1).trim());
            else
            	mSongItem.setKey(mSongItem.getKey().toUpperCase(Locale.ENGLISH));
            
            // Set links to be clickable
            song.setMovementMethod(LinkMovementMethod.getInstance());

            // Set spans for the chords and add to the textview
            disp = new ChordDisplay(this);
            song.setText(disp.setChordClickableText(mSongItem.getText()), TextView.BufferType.SPANNABLE);
        }

        // Initialize the metronome
        initializeMetronome();
        
        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Add the touch listener
        song.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getPointerCount() == 1){
                    // Bypass single touches
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }else{ //when 2 pointers are present
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // Disallow ScrollView to intercept touch events.
                            view.getParent().requestDisallowInterceptTouchEvent(true);
                            scaleGestureDetector.onTouchEvent(motionEvent);
                            break;

                        case MotionEvent.ACTION_MOVE:
                            // Disallow ScrollView to intercept touch events.
                            view.getParent().requestDisallowInterceptTouchEvent(true);
                            scaleGestureDetector.onTouchEvent(motionEvent);
                            break;

                        case MotionEvent.ACTION_UP:
                            // Allow ScrollView to intercept touch events.
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Start the metronome with an initial 2 second delay
        if (mMetronome == null) {
            initializeMetronome();
        }
        mMetronome.start(2000);
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

        // Stop the metronome
        if (mMetronome != null)
            mMetronome.stop();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
    	// Keep the screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Stop the metronome
        if (mMetronome != null)
            mMetronome.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.view_song_menu, menu);
        return true;
    }
    //endregion


    //region Song Functions
    /*****************************************************************************
     * 
     * Song Functions
     * 
     *****************************************************************************/
    /**
     * Shows the transpose menu
     * @param v
     */
    public void onTransposeButtonClick(View v) {

    	// Check for a special key
    	if (StaticVars.songKeyMap.containsKey(mSongItem.getKey())) {
    		// Set the song key to the associated key
    		mSongItem.setKey(StaticVars.songKeyMap.get(mSongItem.getKey()));
    	}

    	// Check to make sure the song has a proper key
    	if (!StaticVars.songKeys.contains(mSongItem.getKey())) {
    		Toast.makeText(getBaseContext(),
    				"You cannot transpose a song without an legit assigned key. Please edit the song attributes, edit the key, and try again.", Toast.LENGTH_LONG).show();
    	}
    	else {
    		AlertDialog.Builder alert = new AlertDialog.Builder(this);

        	alert.setTitle("Transpose to Which Key?");
        	alert.setItems(StaticVars.songKeys.toArray(new CharSequence[StaticVars.songKeys.size()]), new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichItem) {
                    // Transpose the song
                    try {
                        FileInputStream fis = openFileInput(MainActivity.dbAdapter.getSongFile(mSongItem.getName()));
                        String transposedSongText = ChordProParser.ParseSongFile(getApplicationContext(), mSongItem, StaticVars.songKeys.get(whichItem), fis, true, false);

                        if (disp != null) {
                            song.setText(disp.setChordClickableText(transposedSongText), TextView.BufferType.SPANNABLE);
                        } else {
                            song.setText(Html.fromHtml(transposedSongText));
                        }
                    } catch (FileNotFoundException e) {
                        Toast.makeText(getBaseContext(), "Could not open song file!", Toast.LENGTH_LONG).show();
                        return;
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Could not open song file!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });

        	alert.show();
    	}
    }

    /**
     * Handles the edit song button click
     * @param v The song view
     */
    public void onEditButtonClick(View v) {
        // Get the song name
        final String editSongName = mSongItem.getName();
        final String editSongFile = mSongItem.getSongFile();

        // Create the edit activity intent
        Intent i = new Intent(getBaseContext(), EditSongRawActivity.class);
        i.putExtra(StaticVars.SONG_NAME_KEY, editSongName);
        i.putExtra(StaticVars.SONG_FILE_KEY, editSongFile);

        // Start the activity
        startActivity(i);
    }

    /**
     * Initializes the metronome object
     */
    public void initializeMetronome() {
        // Create the metronome object
        if (mSongItem != null) {
            // Create the metronome object
            mMetronome = new Metronome(this, mSongItem.getBpm(), new TimeSignature(mSongItem.getTimeSignature()), mSongItem.getName());
        }

        // Initialize the metronome
        LinearLayout metronomeBar = (LinearLayout)findViewById(R.id.metronome_bar);
        mMetronome.initialize(metronomeBar);
    }
    //endregion


    //region Classes
    /*****************************************************************************
     *
     * Classes
     *
     *****************************************************************************/
    public class simpleOnScaleGestureListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Disable auto-fit to allow user to manually change text size
            song.setFitTextToBox(false);

            // Get the current text size
            float size = song.getTextSize();

            // Get the scale factor from the touch event
            float factor = detector.getScaleFactor();

            // Calculate and set the new text size
            float product = size*factor;
            song.setTextSize(TypedValue.COMPLEX_UNIT_PX, product);

            return true;
        }
    }
    //endregion
}
