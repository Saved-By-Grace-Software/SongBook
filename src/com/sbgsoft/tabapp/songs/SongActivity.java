package com.sbgsoft.tabapp.songs;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.main.MainActivity;

public class SongActivity extends Activity {
	TextView song;

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
            String songName = extras.getString(MainActivity.SONG_NAME_KEY);
            String songText = extras.getString(MainActivity.SONG_TEXT_KEY);
            song.setText(Html.fromHtml("<h2><i>" + songName + "</i></h2>" + songText));
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
        getMenuInflater().inflate(R.menu.view_song_menu, menu);
        return true;
    }
    
    /**
     * Increases the font size on the text view by 1
     * @param v
     */
    public void incFontSize(View v) {
    	song.setTextSize(song.getTextSize() + 1);
    }
    
    /**
     * Decreases the font size on the text view by 1
     * @param v
     */
    public void decFontSize(View v) {
    	song.setTextSize(song.getTextSize() - 1);
    }
    
    /**
     * Populates the text view with the song text
     * @param songText The song text to add to the view
     */
    public void populateSongText(String songText) {
    	song.setText(songText);
    }
    
    /**
     * Gets the text from the specified file
     * @return The song text
     */
    public String getSongText(String fileName) {
    	String songText = "";
    	String chordLine = "";
    	String lyricLine = "";
    	
        try {
        	FileInputStream fis = openFileInput(fileName);
        	DataInputStream in = new DataInputStream(fis);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            // Read each line of the file
            while (line != null) {
            	boolean inChord = false;
            	int skipCounter = 0;
            	String delimiter = "";
            	
            	// Check to see if this line is a delimiter
            	if (line.startsWith("{")) {
            		int i = line.indexOf(":");
            		delimiter = line.substring(1, i);
            		
            		// For comments just add the line with no formatting
            		if (delimiter.equals("comment")) {
            			sb.append(line.substring(i + 1, line.length() - 1) + "<br/>");
            		}
            		
            		// For intro add the line with chord formatting but all on the same line
            		if (delimiter.equals("intro")) {
            			for (char c : line.substring(i + 1, line.length() - 1).toCharArray()) {
            				if (c == '[') {
            					lyricLine += "<b><font color=\"#006B9F\">";
            					continue;
            				}
            				if (c == ']') {
            					lyricLine += "</font></b>";
            					continue;
            				}
            				lyricLine += c;
            			}
            			
            			if (!lyricLine.isEmpty()) {
    		                sb.append(lyricLine);
    		                sb.append("<br/>");
    	            	}
            		}
            		
            		// For title just add the line bolded
            		if (delimiter.equals("title")) {
            			sb.append("<b>" + line.substring(i + 1, line.length() - 1) + "</b><br/>");
            		}
            		
            	} else {
	            	// Step through each character in the line
	            	for (char c : line.toCharArray()) {
	            		// If the character is an open bracket set inChord true and continue
	            		if (c == '[') {
	            			chordLine += "<b><font color=\"#006B9F\">";
	            			inChord = true;
	            			continue;
	            		}
	            		
	            		// If the character is a closed bracket set inChord false and continue
	            		if (c == ']') {
	            			chordLine += "</font></b>";
	            			inChord = false;
	            			continue;
	            		}
	            		
	            		// If in a chord, add the chord to the chord line
	            		if (inChord) {
	            			chordLine += c;
	            			skipCounter++;
	            		} else {
	            			if (skipCounter > 0)
	            				skipCounter--;
	            			else
	            				chordLine += "&nbsp;";
	            			lyricLine += c;
	            		}
	            	}
	            	
	            	// Add the chord and lyric lines to the overall string builder
	            	if (!chordLine.isEmpty()) {
		                sb.append(chordLine);
		                sb.append("<br/>");
	            	}
	            	if (!lyricLine.isEmpty()) {
		                sb.append(lyricLine);
		                sb.append("<br/>");
	            	}
	            	if (chordLine.isEmpty() && lyricLine.isEmpty())
	            		sb.append("<br/>");
            	}
                
                // Clear the chord and lyric lines
                chordLine = "";
                lyricLine = "";
                
                // Read the next line
                line = br.readLine();
            }
            
            songText = sb.toString();
            br.close();
        } catch (Exception e) {
    		Toast.makeText(getApplicationContext(), "Could not open song file!", Toast.LENGTH_LONG).show();
        }     	
    	
    	return songText;
    }
}
