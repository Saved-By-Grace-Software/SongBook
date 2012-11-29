package com.sbgsoft.songbook.songs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainStrings;

public class SongActivity extends Activity {
	
	/*****************************************************************************
     * 
     * Class Variables
     * 
     *****************************************************************************/
	TextView song;
	private String songName = "";
	private String songKey = "";
	private int capo = 0;

	
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
        song = (TextView)findViewById(R.id.song_text);
        song.setMovementMethod(new ScrollingMovementMethod());
        
        // Populate it with the song text
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            songName = extras.getString(MainStrings.SONG_NAME_KEY);
            if (extras.getString(MainStrings.SONG_KEY_KEY).length() > 1)
            	songKey = extras.getString(MainStrings.SONG_KEY_KEY).substring(0, 1).toUpperCase() + extras.getString(MainStrings.SONG_KEY_KEY).substring(1).trim();
            else
            	songKey = extras.getString(MainStrings.SONG_KEY_KEY).toUpperCase();
            String songText = extras.getString(MainStrings.SONG_TEXT_KEY);
            song.setText(Html.fromHtml("<h2>" + songName + "</h2>" + songText));
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
        //getMenuInflater().inflate(R.menu.view_song_menu, menu);
        return true;
    }
    
    
    /*****************************************************************************
     * 
     * Song Functions
     * 
     *****************************************************************************/
    /**
     * Increases the font size on the text view by 1
     * @param v
     */
    public void incFontSize(View v) {
    	song.setTextSize(TypedValue.COMPLEX_UNIT_DIP, song.getTextSize() + 1);
    }
    
    /**
     * Decreases the font size on the text view by 1
     * @param v
     */
    public void decFontSize(View v) {
    	song.setTextSize(TypedValue.COMPLEX_UNIT_DIP, song.getTextSize() - 1);
    }
    
    /**
     * Populates the text view with the song text
     * @param songText The song text to add to the view
     */
    public void populateSongText(String songText) {
    	song.setText(songText);
    }
    
    /**
     * Shows the transpose menu
     * @param v
     */
    public void onTransposeButtonClick(View v) {
    	
    	// Check for a special key
    	if (MainStrings.keyMap.containsKey(songKey)) {
    		// Set the song key to the associated key
    		songKey = MainStrings.keyMap.get(songKey);
    	}
    	
    	// Check to make sure the song has a proper key
    	if (!MainStrings.songKeys.contains(songKey)) {
    		Toast.makeText(getBaseContext(), 
    				"You cannot transpose a song without an legit assigned key. Please edit the song attributes, edit the key, and try again.", Toast.LENGTH_LONG).show();
    	}
    	else {
    		AlertDialog.Builder alert = new AlertDialog.Builder(this);

        	alert.setTitle("Transpose to Which Key?");
        	alert.setItems(MainStrings.songKeys.toArray(new CharSequence[MainStrings.songKeys.size()]), new OnClickListener() {
        		public void onClick (DialogInterface dialog, int whichItem) {
        			// Transpose the song
        			transposeSong(MainStrings.songKeys.get(whichItem));
        			
        			// Print out the capo
        			Toast.makeText(getBaseContext(), "Play in Capo " + capo, Toast.LENGTH_LONG).show();
        		}
        	});
        	
        	alert.show();
    	}
    }
    
    /**
     * Transposes the current song into the selected key
     * @param transposeKey The key to transpose the song into
     */
    private void transposeSong(String transposeKey) {
    	String currText = Html.toHtml((Spanned)song.getText());
    	String updatedText = "";
    	Pattern regex;
    	Matcher matcher;
    	int offset = 0;
    	
    	// Set the updated text to the current text
		updatedText = currText;
		
		// Set the capo and add the line to the song
		// Check for an existing capo line
		regex = Pattern.compile("([Cc][Aa][Pp][Oo])\\D*(\\d+)");
		matcher = regex.matcher(updatedText);
    	if (matcher.find()) {
    		capo = Integer.parseInt(matcher.group(2));
    		setCapo(transposeKey);
    		// If capo 0 remove the capo line
    		if (capo == 0)
    			updatedText = updatedText.substring(0, matcher.start()) + updatedText.substring(matcher.end());
    		else
    			updatedText = updatedText.substring(0, matcher.start()) + "Capo " + capo + "<br>" + updatedText.substring(matcher.end());
    	}
		// No existing capo, add one
    	else {
    		// Search for the author line
    		regex = Pattern.compile("(<i>.*</i>)");
    		matcher = regex.matcher(updatedText);
	    	if (matcher.find()) {
	    		setCapo(transposeKey);
	    		if (capo != 0)
	    			updatedText = updatedText.substring(0, matcher.end()) + "<br>Capo " + capo + "<br>" + updatedText.substring(matcher.end());
	    	}	
    	}
    	
    	// Compile the regex 
    	regex = Pattern.compile("(<b>|<font color =\"#006b9f\">)(?:&#160;)*([A-G][#bmad0-9su]*/*[A-G]*[#bmad0-9su]*)(?:&#160;)*(</font>|</b>)");
    	matcher = regex.matcher(updatedText);
    	try {    		
    		// Cycle through each match
    		while (matcher.find()) {
    			// Transpose the current chord
    			String newChord = transposeChord(matcher.group(2), transposeKey);
    			
    			// Update the chord in the updated text
    			int start = matcher.start(2) + offset;
    			int end = matcher.end(2) + offset;
    			updatedText = updatedText.substring(0, start) + newChord + updatedText.substring(end);
    			
    			// Reset the offset
    			offset += newChord.length() - matcher.group(2).length();
    		}
    		
    		// Reset the song key
    		songKey = transposeKey;
    	} catch (Exception e) {
    	    Toast.makeText(getBaseContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	
    	// Update song text
    	song.setText(Html.fromHtml(updatedText));
    }
    
    /**
     * Transposes the chord into the specified key
     * @param originalChord The chord to transpose
     * @param transposeKey The key to transpose it into
     * @return The transposed chord
     */
    private String transposeChord(final String originalChord, String transposeKey) {
    	String newChord = "", root = "", newRoot = "", bass = "", newBass = "";
    	int slashIndex = 0;
    	int diff = 0, rootIndex = 0, bassIndex = 0;    	
    	
    	// Get the root note of the chord
    	if (originalChord.length() > 1) {
	    	root = originalChord.substring(0, 1);
	    	if (originalChord.charAt(1) == 35 || originalChord.charAt(1) == 98) {
	    		root += originalChord.charAt(1);
	    	}
    	}
    	else
    		root = originalChord;
    	
    	// Get the root note index
    	rootIndex = MainStrings.songKeys.indexOf(root);
    	
    	// Get the index difference
    	diff = MainStrings.songKeys.indexOf(songKey) - MainStrings.songKeys.indexOf(transposeKey);
    	
    	// Set the new root note
    	if (diff < 0 && rootIndex - diff > MainStrings.songKeys.size() - 1) //wrap around right
    		newRoot = MainStrings.songKeys.get((rootIndex - diff) - MainStrings.songKeys.size());
    	else if (rootIndex - diff < 0) //wrap around left
			newRoot = MainStrings.songKeys.get(MainStrings.songKeys.size() - (diff - rootIndex));
		else 
			newRoot = MainStrings.songKeys.get(rootIndex - diff);
    	
    	// Check for a bass note
    	if (originalChord.contains("/")) {
    		slashIndex = originalChord.indexOf("/");
    		
    		// Get the bass note
    		bass = originalChord.substring(slashIndex + 1, slashIndex + 2);
    		if (originalChord.length() - (slashIndex + 1) > 1) {
    			if (originalChord.charAt(slashIndex + 2) == 35 || originalChord.charAt(slashIndex + 2) == 98) {
    				bass += originalChord.charAt(slashIndex + 2);
    	    	}
    		}
    		
    		// Get the bass note index
    		bassIndex = MainStrings.songKeys.indexOf(bass);
    		
    		// Set the new bass note
        	if (diff < 0 && bassIndex - diff > MainStrings.songKeys.size() - 1) //wrap around right
        		newBass = MainStrings.songKeys.get((bassIndex - diff) - MainStrings.songKeys.size());
        	else if (bassIndex - diff < 0) //wrap around left
        		newBass = MainStrings.songKeys.get(MainStrings.songKeys.size() - (diff - bassIndex));
    		else 
    			newBass = MainStrings.songKeys.get(bassIndex - diff);
    		
    		// Create the new chord
    		// Replace the root note
    		if (root.length() == 2)
        		newChord = newRoot + originalChord.substring(2, slashIndex);
        	else
        		newChord = newRoot + originalChord.substring(1, slashIndex);
    		
    		// Replace the bass note
    		newChord += "/";
    		if (bass.length() == 2)
    			newChord += newBass + originalChord.substring(slashIndex + 3);
    		else
    			newChord += newBass + originalChord.substring(slashIndex + 2);
    	}
    	else {
    		// Create the new chord
    		// No bass note so only replace the root note
        	if (root.length() == 2)
        		newChord = newRoot + originalChord.substring(2);
        	else
        		newChord = newRoot + originalChord.substring(1);
    	}
    	
    	// Add spaces to newChord
    	int dif = originalChord.length() - newChord.length();
    	if (dif < 0) {
    		for(int i = 0; i < dif; i++) {
    			newChord = "&nbsp;" + newChord;
        	}
    	}
    	else {
    		for(int i = 0; i < Math.abs(dif); i++) {
    			newChord = newChord + "&nbsp;";
        	}
    	}
    	
    	return newChord;
    }

    /**
     * Gets the capo to play in
     * @param transposeKey The key the song is being transposed into
     * @return The capo number
     */
    private void setCapo(String transposeKey) {
    	// Get the song key and transpose key locations
    	int songKeyLoc = MainStrings.songKeys.indexOf(songKey);
    	int tranKeyLoc = MainStrings.songKeys.indexOf(transposeKey);
    	
    	//If the current capo is not 0 then change the song key location
    	if (capo != 0) {
    		// Set the new song key location
    		if (songKeyLoc + capo > MainStrings.songKeys.size())
    			songKeyLoc = (songKeyLoc + capo) - MainStrings.songKeys.size();
    		else
    			songKeyLoc = songKeyLoc + capo;
    	}
    	
    	// Determine the capo number
    	if (songKeyLoc > tranKeyLoc)
    		capo = songKeyLoc - tranKeyLoc;
    	else
    		capo = songKeyLoc + (MainStrings.songKeys.size() - tranKeyLoc);
    	
    	// Check for capo 12
    	if (capo == 12)
    		capo = 0;
    }
}
