package com.sbgsoft.tabapp.songs;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.main.MainActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SetSongFragment extends Fragment {
	public TextView song;

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
            String songText = extras.getString(MainActivity.SONG_TEXT_KEY);
            
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

}
