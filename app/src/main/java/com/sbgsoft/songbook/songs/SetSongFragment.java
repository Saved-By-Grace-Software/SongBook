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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
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

public class SetSongFragment extends Fragment {

    //region Class Members
	public AutoFitTextView song;
	private SongItem mSongItem;
    private View mView;
    ScaleGestureDetector scaleGestureDetector;
    private Metronome mMetronome;
    ChordDisplay disp;
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
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.activity_set_song, container, false);
		
		// Get the song textview
        song = (AutoFitTextView)mView.findViewById(R.id.song_text);
        song.setMovementMethod(new ScrollingMovementMethod());

        // Instantiate the scale class
        scaleGestureDetector = new ScaleGestureDetector(getActivity(), new simpleOnScaleGestureListener());

        // Get a reference to the edit/transpose buttons
        FloatingActionButton editButton = (FloatingActionButton)mView.findViewById(R.id.set_song_edit_button);
        FloatingActionButton transposeButton = (FloatingActionButton)mView.findViewById(R.id.set_song_transpose_button);

        // Get the current settings
        Settings settings = MainActivity.dbAdapter.getCurrentSettings();
        boolean showTranspose = settings.getShowTransposeInSet();
        boolean showEdit = settings.getShowEditInSet();
        mMetronomeState = settings.getMetronomeState();
        mBrightMetronome = settings.getUseBrightMetronome();

        // Set the play/pause button images
        playImage = ContextCompat.getDrawable(getActivity(), R.drawable.ic_play_arrow_black_24dp);
        stopImage = ContextCompat.getDrawable(getActivity(), R.drawable.ic_stop_black_24dp);

        // Enable/Disable the buttons according to the settings
        if (showEdit)
            editButton.setVisibility(View.VISIBLE);
        else
            editButton.setVisibility(View.GONE);
        if (showTranspose)
            transposeButton.setVisibility(View.VISIBLE);
        else
            transposeButton.setVisibility(View.GONE);

        // Populate it with the song text
        Bundle extras = getArguments();
        if (extras != null) {
        	mSongItem = extras.getParcelable(StaticVars.SONG_ITEM_KEY);

        	if (mSongItem.getKey().length() > 1)
            	mSongItem.setKey(mSongItem.getKey().substring(0, 1).toUpperCase(Locale.ENGLISH) + mSongItem.getKey().substring(1).trim());
            else
            	mSongItem.setKey(mSongItem.getKey().toUpperCase(Locale.ENGLISH));

            // Populate the song text
            if (mSongItem.getText() != "") {
                // Set spans for the chords and add to the textview
                disp = new ChordDisplay((Activity)mView.getContext());
                song.setText(disp.setChordClickableText(mSongItem.getText()), TextView.BufferType.SPANNABLE);
            }

            // Initialize the metronome
            initializeMetronome();

            // Enable the play button if a background track exists
            backgroundTrack = MainActivity.dbAdapter.getSongTrack(mSongItem.getName());
            if (backgroundTrack != null && backgroundTrack != "")
            {
                // Enable the play button
                playButton = (FloatingActionButton)mView.findViewById(R.id.set_song_play_button);
                playButton.setVisibility(View.VISIBLE);
            }
        }

        // Add the touch listener
        song.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getPointerCount() == 1) {
                    // Bypass single touches
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                } else { //when 2 pointers are present
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
        
		return mView;
	}

    @Override
    public void onStart() {
        super.onStart();

        // Start the metronome with an initial 2 second delay
        if (mMetronome == null) {
            initializeMetronome();
        }
        mMetronome.start(2000);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resume the media player
        if (mPlayer != null && isPlaying && !mPlayer.isPlaying()) {
            startPlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop the metronome
        if (mMetronome != null)
            mMetronome.stop();

        // Stop the media player and reset
        if (mPlayer != null) {
            stopPlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Stop the metronome
        if (mMetronome != null)
            mMetronome.stop();

        // Stop the media player
        if (mPlayer != null) {
            stopPlayer();
        }
    }

    /**
     * Get the return from the file dialog activity
     */
    @Override
    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {

        if (requestCode == EDIT_SONG_ACTIVITY && resultCode == Activity.RESULT_OK) {
            // Reset the song text
            try {
                FileInputStream fis = mView.getContext().openFileInput(MainActivity.dbAdapter.getSongFile(mSongItem.getName()));
                mSongItem.setText(ChordProParser.ParseSongFile(mView.getContext(), mSongItem, mSongItem.getKey(), fis, true, false));

                // Reload the song text
                if (disp != null) {
                    song.setText(disp.setChordClickableText(mSongItem.getText()), TextView.BufferType.SPANNABLE);
                }
            } catch (Exception e) { }
        }
    }
    //endregion

    //region Click Functions
	/**
	 * Transposes the song
	 */
	public void onTransposeButtonClick() {
		// Check for a special key
    	if (StaticVars.songKeyMap.containsKey(mSongItem.getKey())) {
    		// Set the song key to the associated key
    		mSongItem.setKey(StaticVars.songKeyMap.get(mSongItem.getKey()));
    	}
    	
    	// Check to make sure the song has a proper key
    	if (!StaticVars.songKeys.contains(mSongItem.getKey())) {
    		Toast.makeText(getActivity(), 
    				"You cannot transpose a song without an legit assigned key. Please edit the song attributes, edit the key, and try again.", Toast.LENGTH_LONG).show();
    	}
    	else {
    		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        	alert.setTitle("Transpose to Which Key?");
        	alert.setItems(StaticVars.songKeys.toArray(new CharSequence[StaticVars.songKeys.size()]), new OnClickListener() {
        		public void onClick (DialogInterface dialog, int whichItem) {
        			// Transpose the song
					try {
						FileInputStream fis = getActivity().openFileInput(MainActivity.dbAdapter.getSongFile(mSongItem.getName()));
						String transposedSongText = ChordProParser.ParseSongFile(getActivity().getApplicationContext(), mSongItem, StaticVars.songKeys.get(whichItem), fis, true, false);

                        if (disp != null) {
                            song.setText(disp.setChordClickableText(transposedSongText), TextView.BufferType.SPANNABLE);
                        } else {
                            song.setText(Html.fromHtml(transposedSongText));
                        }
					} catch (FileNotFoundException e) {
						Toast.makeText(getActivity(), "Could not open song file!", Toast.LENGTH_LONG).show();
						return;
					} catch (IOException e) {
						Toast.makeText(getActivity(), "Could not open song file!", Toast.LENGTH_LONG).show();
						return;
					}
        		}
        	});
        	
        	alert.show();
    	}
	}

    /**
     * Handles the edit song button click
     */
    public void onEditButtonClick() {
        // Get the song name
        final String editSongName = mSongItem.getName();
        final String editSongFile = mSongItem.getSongFile();

        // Create the edit activity intent
        Intent i = new Intent(mView.getContext(), EditSongRawActivity.class);
        i.putExtra(StaticVars.SONG_NAME_KEY, editSongName);
        i.putExtra(StaticVars.SONG_FILE_KEY, editSongFile);

        // Start the activity
        startActivityForResult(i, EDIT_SONG_ACTIVITY);
    }

    /**
     * Handles the play button click
     */
    public void onPlayButtonClick() {
        if (playButton != null) {
            if (isPlaying) {
                // Stop playing the track
                stopPlayer(2);
            } else {
                // Start playing the track
                startPlayer();
            }
        }
    }
    //endregion

    //region Other Functions
    /**
     * Initializes the metronome
     */
    public void initializeMetronome() {
        // Create the metronome object
        if (mSongItem != null) {
            // Create the metronome object
            mMetronome = new Metronome(getActivity(), mSongItem.getBpm(), new TimeSignature(mSongItem.getTimeSignature()), mSongItem.getName());

            // Set the state
            mMetronome.setmMetronomeState(mMetronomeState);

            // Check for drummer mode
            if (mBrightMetronome) {
                // Do not show words behind metronome
                ScrollView scrollView = (ScrollView)(mView.findViewById(R.id.set_scrollView));
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)scrollView.getLayoutParams();
                params.addRule(RelativeLayout.ABOVE, R.id.metronome_bar);
                scrollView.setLayoutParams(params);

                // Set bright images
                mMetronome.setImageOn(ContextCompat.getDrawable(mView.getContext(), R.drawable.bright_filled));
                mMetronome.setImageOff(ContextCompat.getDrawable(mView.getContext(), R.drawable.bright_open));
                mMetronome.setImageTempoMode(ContextCompat.getDrawable(mView.getContext(), R.drawable.bright_mid));
            } else {
                // Set normal images
                mMetronome.setImageOn(ContextCompat.getDrawable(mView.getContext(), R.drawable.filled));
                mMetronome.setImageOff(ContextCompat.getDrawable(mView.getContext(), R.drawable.open));
                mMetronome.setImageTempoMode(ContextCompat.getDrawable(mView.getContext(), R.drawable.mid));
            }

            // Initialize the metronome
            LinearLayout metronomeBar = (LinearLayout)mView.findViewById(R.id.metronome_bar);
            mMetronome.initialize(metronomeBar);
        }
    }

    /**
     * Stops and resets the media player
     */
    public void stopPlayer() {
        // Stop and release
        if (mPlayer != null) {
            // Stop the media player
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;

            // Update the button
            isPlaying = false;
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    playButton.setImageDrawable(playImage);
                }
            });

        }
    }

    /**
     * Stops the player after fading out the track
     * @param fadeOutTime
     */
    public void stopPlayer(final int fadeOutTime) {
        if (mPlayer != null) {
            // Create the thread
            Runnable runnable = new Runnable() {
                public void run() {
                    long endTime = System.currentTimeMillis() + (fadeOutTime * 1000);
                    float decrement = 1 / ((float)fadeOutTime * 4);
                    float vol = 1.0f;

                    while (System.currentTimeMillis() < endTime) {
                        vol -= decrement;
                        mPlayer.setVolume(vol, vol);

                        try {
                            Thread.sleep(250);
                        } catch (Exception e) {}
                    }

                    stopPlayer();
                }
            };

            // Start the thread
            Thread fadeOut = new Thread(runnable);
            fadeOut.start();
        }
    }

    /**
     * Starts the media player
     */
    public void startPlayer() {
        // Ensure stopped
        stopPlayer();

        // Configure the player
        configurePlayer();

        // Change the button image
        playButton.setImageDrawable(stopImage);
        isPlaying = true;

        // Start the player
        mPlayer.start();
    }

    /**
     * Configures the media player
     */
    private void configurePlayer() {
        // Configure the media player
        mPlayer = MediaPlayer.create(getActivity(), Uri.parse(backgroundTrack));
        mPlayer.setLooping(true);
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
