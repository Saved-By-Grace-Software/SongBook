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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.MainStrings;

public class SetSongFragment extends Fragment {
	public TextView song;
	private SongItem mSongItem;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.set_song, container, false);
		
		// Get the song textview
        song = (TextView)view.findViewById(R.id.set_song_text);
        song.setMovementMethod(new ScrollingMovementMethod());
        
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
		
		return view;
	}
	
	/**
	 * Increases the size of the text in the text view
	 */
	public void incTextSize() {
		song.setTextSize(TypedValue.COMPLEX_UNIT_DIP, song.getTextSize() + 1);
	}
	
	/**
	 * Decreases the size of the text in the text view
	 */
	public void decTextSize() {
		song.setTextSize(TypedValue.COMPLEX_UNIT_DIP, song.getTextSize() - 1);
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
						String transposedSongText = ChordProParser.ParseSongFile(mSongItem, MainStrings.songKeys.get(whichItem), fis);
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
}
