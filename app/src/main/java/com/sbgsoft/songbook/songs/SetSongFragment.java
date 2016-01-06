package com.sbgsoft.songbook.songs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.MainStrings;
import com.sbgsoft.songbook.views.AutoFitTextView;

public class SetSongFragment extends Fragment {
	public AutoFitTextView song;
	private SongItem mSongItem;
    private View mView;
    ScaleGestureDetector scaleGestureDetector;
    private Metronome mMetronome;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.activity_song, container, false);
		
		// Get the song textview
        song = (AutoFitTextView)mView.findViewById(R.id.song_text);
        song.setMovementMethod(new ScrollingMovementMethod());

        // Instantiate the scale class
        scaleGestureDetector = new ScaleGestureDetector(getActivity(), new simpleOnScaleGestureListener());
        
        // Populate it with the song text
        Bundle extras = getArguments();
        if (extras != null) {
        	mSongItem = extras.getParcelable(MainStrings.SONG_ITEM_KEY);
            
        	if (mSongItem.getKey().length() > 1)
            	mSongItem.setKey(mSongItem.getKey().substring(0, 1).toUpperCase(Locale.ENGLISH) + mSongItem.getKey().substring(1).trim());
            else
            	mSongItem.setKey(mSongItem.getKey().toUpperCase(Locale.ENGLISH));
            
            // Populate the song text
            if (mSongItem.getText() != "") {
                song.setText(Html.fromHtml(mSongItem.getText()));
            }
        }

        // Resize metronome bar icons
        initializeMetronome();

        // Add the touch listener
        song.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getPointerCount() == 1){
                    //stuff for 1 pointer
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
        
		return mView;
	}

    @Override
    public void onStart() {
        super.onStart();

        // Start the metronome
        if (mMetronome == null) {
            initializeMetronome();
        }
        mMetronome.start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop the metronome
        if (mMetronome != null)
            mMetronome.stop();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Stop the metronome
        if (mMetronome != null)
            mMetronome.stop();
    }

	/**
	 * Transposes the song
	 */
	public void onTransposeButtonClick() {
		// Check for a special key
    	if (MainStrings.keyMap.containsKey(mSongItem.getKey())) {
    		// Set the song key to the associated key
    		mSongItem.setKey(MainStrings.keyMap.get(mSongItem.getKey()));
    	}
    	
    	// Check to make sure the song has a proper key
    	if (!MainStrings.songKeys.contains(mSongItem.getKey())) {
    		Toast.makeText(getActivity(), 
    				"You cannot transpose a song without an legit assigned key. Please edit the song attributes, edit the key, and try again.", Toast.LENGTH_LONG).show();
    	}
    	else {
    		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        	alert.setTitle("Transpose to Which Key?");
        	alert.setItems(MainStrings.songKeys.toArray(new CharSequence[MainStrings.songKeys.size()]), new OnClickListener() {
        		public void onClick (DialogInterface dialog, int whichItem) {
        			// Transpose the song
					try {
						FileInputStream fis = getActivity().openFileInput(MainActivity.dbAdapter.getSongFile(mSongItem.getName()));
						String transposedSongText = ChordProParser.ParseSongFile(getActivity().getApplicationContext(), mSongItem, MainStrings.songKeys.get(whichItem), fis, true, false);
	        			song.setText(Html.fromHtml(transposedSongText));
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
     * Initializes the metronome
     */
    public void initializeMetronome() {
        // Create the metronome object
        mMetronome = new Metronome(getActivity());

        // Set the beats per minute
        if (mSongItem != null)
            mMetronome.setBeatsPerMinute(mSongItem.getBpm());

        // Determine if we should show the metronome
        boolean showMetronome = false;
        if (mMetronome.getBeatsPerMinute() > 0)
            showMetronome = true;

        // Get the height to fit it to
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int width = size.x;

        // Get the metronome bar layout
        LinearLayout metronomeBar = (LinearLayout)mView.findViewById(R.id.metronome_bar);
        int children = metronomeBar.getChildCount();

        // Resize all icons in the bar and add them to the metronome
        for (int i = 0; i < children; i++) {
            ImageView icon = (ImageView)metronomeBar.getChildAt(i);

            if (showMetronome) {
                // Adjust the metronome size
                icon.getLayoutParams().height = (int)(height * 0.1);
                icon.getLayoutParams().width = (int)(width * 0.12);
                icon.requestLayout();

                // Add to the metronome
                mMetronome.mDots.add(icon);
            } else {
                // Hide the icons if we are not showing the metronome
                icon.setVisibility(View.GONE);
            }
        }

        // Set the on and off images for the metronome
        mMetronome.setImageOn(R.drawable.filled);
        mMetronome.setImageOff(R.drawable.open);
    }


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
}
