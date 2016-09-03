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
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.Settings;
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
    ScrollView scrollView;
    ChordDisplay disp;
    ScaleGestureDetector scaleGestureDetector;
    private Metronome mMetronome;
    private SongItem mSongItem;
    private int EDIT_SONG_ACTIVITY = 1;
    private String mMetronomeState = StaticVars.SETTINGS_METRONOME_STATE_WITHBPM;
    private boolean mBrightMetronome = false;
    private String backgroundTrack = "";
    private FloatingActionButton playButton;
    private Drawable playImage;
    private Drawable stopImage;
    private boolean isPlaying = false;
    private MediaPlayer mPlayer;
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
        
        // Get the song and scroll view
        song = (AutoFitTextView)findViewById(R.id.song_text);
        scrollView = (ScrollView)findViewById(R.id.song_scrollview);

        // Instantiate the scale class
        scaleGestureDetector = new ScaleGestureDetector(this, new simpleOnScaleGestureListener());

        // Get the current settings
        Settings settings = MainActivity.dbAdapter.getCurrentSettings();
        mMetronomeState = settings.getMetronomeState();
        mBrightMetronome = settings.getUseBrightMetronome();

        // Set the play/pause button images
        playImage = ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp);
        stopImage = ContextCompat.getDrawable(this, R.drawable.ic_stop_black_24dp);
        
        // Populate it with the song text
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Get the song item
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

            song.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    // Check for key pressed
                    if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DPAD_UP:
                                upArrowPress();
                                return true;
                            case KeyEvent.KEYCODE_DPAD_DOWN:
                                downArrowPress();
                                return true;
                            case KeyEvent.KEYCODE_ENTER:
                            case KeyEvent.KEYCODE_SPACE:
                                enterSpacePress();
                                return true;
                            case KeyEvent.KEYCODE_PAGE_DOWN:
                                return true;
                            case KeyEvent.KEYCODE_PAGE_UP:
                                return true;
                            default:
                                return false;
                        }
                    } else {
                        return false;
                    }
                }
            });

            // Initialize the metronome
            initializeMetronome();

            // Enable the play button if a background track exists
            backgroundTrack = MainActivity.dbAdapter.getSongTrack(mSongItem.getName());
            if (backgroundTrack != null && !backgroundTrack.isEmpty())
            {
                // Enable the play button
                playButton = (FloatingActionButton)findViewById(R.id.song_play_button);
                playButton.setVisibility(View.VISIBLE);
            }
        }
        
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

        // Resume the media player
        if (mPlayer != null && isPlaying && !mPlayer.isPlaying()) {
            startPlayer(0);
        }
    }
    
    @Override 
    public void onPause() {
    	super.onPause();
    	
    	// Keep the screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Stop the metronome
        if (mMetronome != null)
            mMetronome.stop();

        // Stop the media player and reset
        if (mPlayer != null) {
            stopPlayer(0);
        }
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
    	// Keep the screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Stop the metronome
        if (mMetronome != null)
            mMetronome.stop();

        // Stop the media player
        if (mPlayer != null) {
            stopPlayer(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.view_song_menu, menu);
        return true;
    }

    /**
     * Get the return from the file dialog activity
     */
    @Override
    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {

        if (requestCode == EDIT_SONG_ACTIVITY && resultCode == Activity.RESULT_OK) {
            // Reset the song text
            try {
                FileInputStream fis = openFileInput(MainActivity.dbAdapter.getSongFile(mSongItem.getName()));
                mSongItem.setText(ChordProParser.ParseSongFile(getApplicationContext(), mSongItem, mSongItem.getKey(), fis, true, false));

                // Reload the song text
                if (disp != null) {
                    song.setText(disp.setChordClickableText(mSongItem.getText()), TextView.BufferType.SPANNABLE);
                }
            } catch (Exception e) { }
        }
    }
    //endregion


    //region Key Press Functions
    private void enterSpacePress() {
        // Call the button press action for the play button
        if (playButton != null) {
            playButton.performClick();
        }
    }

    private void downArrowPress() {
        // Scroll down
        scrollView.scrollTo(0, scrollView.getScrollY() + 50);
    }

    private void upArrowPress() {
        // Scroll up
        scrollView.scrollTo(0, scrollView.getScrollY() - 50);
    }
    //endregion


    //region Song Functions
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
        startActivityForResult(i, EDIT_SONG_ACTIVITY);
    }

    /**
     * Handles the play button click
     * @param v
     */
    public void onPlayButtonClick(View v) {
        if (playButton != null) {
            if (isPlaying) {
                // Stop playing the track
                stopPlayer(2);
            } else {
                // Start playing the track
                startPlayer(2);
            }
        }
    }

    /**
     * Initializes the metronome object
     */
    public void initializeMetronome() {
        // Create the metronome object
        if (mSongItem != null) {
            // Create the metronome object
            mMetronome = new Metronome(this, mSongItem.getBpm(), new TimeSignature(mSongItem.getTimeSignature()), mSongItem.getName());

            // Set the state
            mMetronome.setmMetronomeState(mMetronomeState);

            // Check for drummer mode
            if (mBrightMetronome) {
                // Do not show words behind metronome
                ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)scrollView.getLayoutParams();
                params.addRule(RelativeLayout.ABOVE, R.id.metronome_bar);
                scrollView.setLayoutParams(params);

                // Set bright images
                mMetronome.setImageOn(ContextCompat.getDrawable(this, R.drawable.bright_filled));
                mMetronome.setImageOff(ContextCompat.getDrawable(this, R.drawable.bright_open));
                mMetronome.setImageTempoMode(ContextCompat.getDrawable(this, R.drawable.bright_mid));
            } else {
                // Set normal images
                mMetronome.setImageOn(ContextCompat.getDrawable(this, R.drawable.filled));
                mMetronome.setImageOff(ContextCompat.getDrawable(this, R.drawable.open));
                mMetronome.setImageTempoMode(ContextCompat.getDrawable(this, R.drawable.mid));
            }

            // Initialize the metronome
            LinearLayout metronomeBar = (LinearLayout)findViewById(R.id.metronome_bar);
            mMetronome.initialize(metronomeBar);
        }
    }
    //endregion


    //region Media Player Functions
    /**
     * Stops the player after fading out the track
     * @param fadeOutTime
     */
    public void stopPlayer(final int fadeOutTime) {
        if (mPlayer != null) {
            // Disable the play button until its done
            setPlayButtonEnabled(false);

            // Create the thread
            Runnable runnable = new Runnable() {
                public void run() {
                    long endTime = System.currentTimeMillis() + (fadeOutTime * 1000);
                    float decrement = 1 / ((float)fadeOutTime * 4);
                    float vol = 1.0f;

                    while (System.currentTimeMillis() < endTime && vol >= 0.0f) {
                        vol -= decrement;
                        if (mPlayer != null)
                            mPlayer.setVolume(vol, vol);

                        try {
                            Thread.sleep(250);
                        } catch (Exception e) {}
                    }

                    // Stop the media player
                    endPlayer();
                }
            };

            // Start the thread
            Thread fadeOut = new Thread(runnable);
            fadeOut.start();
        }
    }

    /**
     * Fades the track in
     * @param fadeInTime Time in secods to fade in over
     */
    public void startPlayer(final int fadeInTime) {
        // Ensure stopped
        endPlayer();

        // Configure the player
        configurePlayer();

        // Change the button image
        showStopButton();
        isPlaying = true;

        // Create the thread
        Runnable runnable = new Runnable() {
            public void run() {
                long endTime = System.currentTimeMillis() + (fadeInTime * 1000);
                float increment = 1 / ((float)fadeInTime * 4);
                float vol = 0.0f;

                // Set initial volume and start player
                if (mPlayer != null)
                    mPlayer.setVolume(vol, vol);
                if (mPlayer != null)
                    mPlayer.start();

                while (System.currentTimeMillis() < endTime && vol <= 1.0f) {
                    vol += increment;
                    if (mPlayer != null)
                        mPlayer.setVolume(vol, vol);

                    try {
                        Thread.sleep(250);
                    } catch (Exception e) {}
                }

                // Make sure we are at max volume
                if (mPlayer != null)
                    mPlayer.setVolume(1.0f, 1.0f);
            }
        };

        // Start the thread
        Thread fadeOut = new Thread(runnable);
        fadeOut.start();
    }

    /**
     * Completely stops the media player
     */
    private void endPlayer() {
        if (mPlayer != null) {
            // Stop the media player
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        // Update the button
        isPlaying = false;
        showPlayButton();
    }

    /**
     * Configures the media player
     */
    private void configurePlayer() {
        // Configure the media player
        mPlayer = MediaPlayer.create(this, Uri.parse(backgroundTrack));
        mPlayer.setLooping(true);
    }

    /**
     * Disables the play or stop button
     */
    private void setPlayButtonEnabled(final boolean isEnabled) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                playButton.setEnabled(isEnabled);
            }
        });
    }

    /**
     * Shows the play button
     */
    private void showPlayButton() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                playButton.setImageDrawable(playImage);
                playButton.setEnabled(true);
            }
        });
    }

    /**
     * Shows the stop button
     */
    private void showStopButton() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                playButton.setImageDrawable(stopImage);
                playButton.setEnabled(true);
            }
        });
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
