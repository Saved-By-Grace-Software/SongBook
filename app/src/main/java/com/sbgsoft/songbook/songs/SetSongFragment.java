package com.sbgsoft.songbook.songs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.MainStrings;
import com.sbgsoft.songbook.views.AutoFitTextView;

public class SetSongFragment extends Fragment {
	public AutoFitTextView song;
	private SongItem mSongItem;
    ScaleGestureDetector scaleGestureDetector;
    GestureDetector gestureDetector;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_song, container, false);
		
		// Get the song textview
        song = (AutoFitTextView)view.findViewById(R.id.song_text);
        song.setMovementMethod(new ScrollingMovementMethod());

        // Instantiate the gesture listeners
        scaleGestureDetector = new ScaleGestureDetector(getActivity(), new simpleOnScaleGestureListener());
        gestureDetector = new GestureDetector(getActivity(), new GestureListener());
        
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
        
		return view;
	}

    @Override
    public boolean dispatchTouchEvent(MotionEvent e)
    {
        super.dispatchTouchEvent(e);
        return gestureDetector.onTouchEvent(e);
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
