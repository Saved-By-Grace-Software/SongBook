package com.sbgsoft.tabapp.songs;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.main.MainActivity;
import com.sbgsoft.tabapp.main.MainStrings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SetSongFragment extends Fragment {
	public TextView song;
	private String songKey = "";
	private int capo = 0;

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
        	String songName = extras.getString(MainStrings.SONG_NAME_KEY);
            String songText = extras.getString(MainStrings.SONG_TEXT_KEY);
            
            // Set the song key
            songKey = MainActivity.dbAdapter.getSongKey(songName);
            
            // Populate the song text
            if (songText != "") {
                song.setText(Html.fromHtml(songText));
            }
        }
		
		return view;
	}
	
	/**
	 * Increases the size of the text in the text view
	 */
	public void incTextSize() {
		song.setTextSize(song.getTextSize() + 1);
	}
	
	/**
	 * Decreases the size of the text in the text view
	 */
	public void decTextSize() {
		song.setTextSize(song.getTextSize() - 1);
	}

	/**
	 * Transposes the song
	 */
	public void transposeSong() {
		// Check for a special key
    	if (MainStrings.keyMap.containsKey(songKey)) {
    		// Set the song key to the associated key
    		songKey = MainStrings.keyMap.get(songKey);
    	}
    	
    	// Check to make sure the song has a proper key
    	if (!MainStrings.songKeys.contains(songKey)) {
    		Toast.makeText(getActivity(), 
    				"You cannot transpose a song without an legit assigned key. Please edit the song attributes, edit the key, and try again.", Toast.LENGTH_LONG).show();
    	}
    	else {
    		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        	alert.setTitle("Transpose to Which Key?");
        	alert.setItems(MainStrings.songKeys.toArray(new CharSequence[MainStrings.songKeys.size()]), new OnClickListener() {
        		public void onClick (DialogInterface dialog, int whichItem) {
        			// Transpose the song
        			//transposeSong(MainStrings.songKeys.get(whichItem));
        			
        			// Print out the capo
        			if (capo != 0)
        				Toast.makeText(getActivity(), "Play in Capo " + capo, Toast.LENGTH_LONG).show();
        			capo = 0;
        		}
        	});
        	
        	alert.show();
    	}
	}
}
